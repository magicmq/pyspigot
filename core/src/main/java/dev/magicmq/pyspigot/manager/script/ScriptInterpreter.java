/*
 *    Copyright 2025 magicmq
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.magicmq.pyspigot.manager.script;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.jep.BukkitClassEnquirer;
import dev.magicmq.pyspigot.util.ScriptContext;
import dev.magicmq.pyspigot.util.logging.ScriptAwareOutputStream;
import jep.Jep;
import jep.JepConfig;
import jep.JepException;
import jep.SharedInterpreter;
import jep.python.PyCallable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Owns and manages the JEP runtime for PySpigot.
 * <p>
 * Two kinds of interpreters are kept: the main-thread {@link SharedInterpreter}, used directly
 * by code running on the server's primary thread, and a configurable-size fixed pool of
 * attached {@link Jep} instances bound one-per-worker to a dedicated executor that runs all
 * Python work originating from non-main contexts (async event listeners, async tasks, packet
 * listeners, Redis pub/sub, etc.). Async callers submit to that executor and block on the
 * result, which gives:
 * <ul>
 *     <li>Deterministic interpreter ownership and shutdown — every {@link Jep} is closed on the
 *         thread that created it.</li>
 *     <li>Memory-visibility guarantees for any Java state mutated by Python: the calling
 *         thread's {@code Future.get()} happens-before the return.</li>
 *     <li>A single chokepoint where script context, exception unwrapping, and re-entry can be
 *         centralized.</li>
 * </ul>
 * <p>
 * The pool size determines how many async listeners can be in flight simultaneously. The GIL
 * still serializes Python execution within a given Python build, so a larger pool does not
 * give Python parallelism; it prevents one slow listener from holding up unrelated listeners.
 * <p>
 * <h2>Lifecycle</h2>
 * Construction is two-phase so the loader module's call back into
 * {@link #initFunctions(PyCallable, PyCallable, PyCallable, PyCallable, PyCallable)} can reach
 * this instance through the owning {@link ScriptManager} before construction completes:
 * <ol>
 *     <li>{@code new ScriptInterpreter(int)} — does no JEP work.</li>
 *     <li>{@link #start()} — builds the main {@link SharedInterpreter} on the calling thread,
 *         spins up the async pool, attaches a {@link Jep} on each worker, and exec's the
 *         loader module on the main interpreter.</li>
 * </ol>
 * <p>
 * <h2>Threading rules for callers</h2>
 * <ul>
 *     <li>Code running on the main server thread may call {@link #callLoad}, {@link #callStop},
 *         {@link #callUnload}, {@link #callIsLoaded}, {@link #callLoadedScripts}, or invoke
 *         {@link PyCallable}s directly. No bouncing.</li>
 *     <li>Code running on any other thread <em>must</em> route Python work through
 *         {@link #runAsync(Script, Supplier)} or {@link #runAsync(Script, Runnable)}.</li>
 *     <li>Re-entrant calls from any pool worker back into {@link #runAsync} are detected and
 *         executed inline; they do not deadlock.</li>
 * </ul>
 *
 * @see ScriptManager
 */
public class ScriptInterpreter {

    private static final long ASYNC_SHUTDOWN_TIMEOUT_SECONDS = 5L;

    private final int asyncPoolSize;
    private final Map<Thread, Jep> asyncInterpreters = new ConcurrentHashMap<>();

    private SharedInterpreter mainInterpreter;
    private Thread mainThread;

    private ExecutorService asyncExecutor;

    private PyCallable defLoad;
    private PyCallable defStop;
    private PyCallable defUnload;
    private PyCallable defIsLoaded;
    private PyCallable defLoadedScripts;

    /**
     *
     * @param asyncPoolSize Number of worker threads (and attached {@link Jep} instances) for
     *                      the async executor; must be at least 1
     */
    public ScriptInterpreter(int asyncPoolSize) {
        this.asyncPoolSize = asyncPoolSize;
    }

    /**
     * Build the main {@link SharedInterpreter} on the calling thread, start the async pool and
     * attach a {@link Jep} on each worker, install stdout/stderr redirection into script-aware
     * streams, and execute the {@code pyspigot_loader} Python module so it can register its
     * callable handles.
     * <p>
     * Per JEP's threading rules, the main interpreter is bound to whichever thread invokes this
     * method. The async interpreters are each bound to a worker thread owned by this class.
     */
    public void start() {
        PyCore.get().getLogger().info("Creating JEP shared interpreter...");

        mainThread = Thread.currentThread();

        try (InputStream is = PyCore.get().getResourceAsStream("loader_module.py")) {
            //TODO Additional configurable options
            JepConfig jepConfig = new JepConfig();
            jepConfig.setClassEnquirer(new BukkitClassEnquirer());
            jepConfig.redirectStdout(new ScriptAwareOutputStream(false));
            jepConfig.redirectStdErr(new ScriptAwareOutputStream(true));
            SharedInterpreter.setConfig(jepConfig);

            mainInterpreter = new SharedInterpreter();

            startAsyncPool();

            String loaderCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            mainInterpreter.exec(loaderCode);

            this.defLoad = mainInterpreter.getValue("load", PyCallable.class);
            this.defStop = mainInterpreter.getValue("stop", PyCallable.class);
            this.defUnload = mainInterpreter.getValue("unload", PyCallable.class);
            this.defIsLoaded = mainInterpreter.getValue("is_loaded", PyCallable.class);
            this.defLoadedScripts = mainInterpreter.getValue("loaded_scripts", PyCallable.class);
        } catch (JepException | IOException e) {
            throw new RuntimeException("Failed to initialize JEP shared interpreter", e);
        }
    }

    /**
     * Get the {@link SharedInterpreter} bound to the main server thread.
     * <p>
     * Use only from the thread that called {@link #start()}.
     * @return The main-thread shared interpreter
     */
    public SharedInterpreter getMainInterpreter() {
        return mainInterpreter;
    }

    /**
     * @return True if the calling thread is the main server thread (the one that called {@link #start()})
     */
    public boolean isOnMainThread() {
        return Thread.currentThread() == mainThread;
    }

    /**
     * @return True if the calling thread is one of the async pool's worker threads
     */
    public boolean isOnAsyncThread() {
        return asyncInterpreters.containsKey(Thread.currentThread());
    }

    /**
     * Dispatch Python work to the right place: run inline (with a {@link ScriptContext} push)
     * if the caller is already on the main thread, otherwise bounce to the async pool.
     * @param script The script the work belongs to; may be null
     * @param work   The work to execute
     */
    public void call(Script script, Runnable work) {
        if (isOnMainThread())
            ScriptContext.runWith(script, work);
        else
            runAsync(script, work);
    }

    /**
     * Result-returning variant of {@link #call(Script, Runnable)}.
     * @param script The script the work belongs to; may be null
     * @param work   The work to execute
     * @param <T>    Result type
     * @return The value supplied by {@code work}
     */
    public <T> T callWithResult(Script script, Supplier<T> work) {
        if (isOnMainThread())
            return ScriptContext.supplyWith(script, work);
        else
            return runAsync(script, work);
    }

    /**
     * Invoke the loader's {@code load(script_id, java_script, java_logger, main_path, source, project_path)} entry.
     * Must be called on the main thread.
     * @param args Positional arguments forwarded to the Python function
     * @throws JepException Propagated from the underlying call
     */
    protected synchronized void callLoad(Object... args) throws JepException {
        defLoad.call(args);
    }

    /**
     * Invoke the loader's {@code stop(script_id)} entry. Must be called on the main thread.
     * @param scriptId The script identifier
     * @return The Python return value (a list of {@code (hook_name, traceback)} tuples for hooks that raised)
     * @throws JepException Propagated from the underlying call
     */
    protected Object callStop(String scriptId) throws JepException {
        return defStop.call(scriptId);
    }

    /**
     * Invoke the loader's {@code unload(script_id)} entry. Must be called on the main thread.
     * @param scriptId The script identifier
     * @return The Python return value (boolean — true if the script was loaded)
     * @throws JepException Propagated from the underlying call
     */
    protected Object callUnload(String scriptId) throws JepException {
        return defUnload.call(scriptId);
    }

    /**
     * Invoke the loader's {@code is_loaded(script_id)} entry. Must be called on the main thread.
     * @param scriptId The script identifier
     * @return The Python return value (boolean)
     * @throws JepException Propagated from the underlying call
     */
    protected Object callIsLoaded(String scriptId) throws JepException {
        return defIsLoaded.call(scriptId);
    }

    /**
     * Invoke the loader's {@code loaded_scripts()} entry. Must be called on the main thread.
     * @return The Python return value (list of script identifiers)
     * @throws JepException Propagated from the underlying call
     */
    protected Object callLoadedScripts() throws JepException {
        return defLoadedScripts.call();
    }

    /**
     * Get the
     */

    /**
     * Close every interpreter cleanly, each on its owning thread.
     * <p>
     * For each pool worker, a close task is submitted; the workers synchronize at a barrier so
     * each one picks up exactly one close task and closes its own attached {@link Jep}. The
     * executor is then shut down. Finally, the main {@link SharedInterpreter} is closed on the
     * calling thread, which must be the one that originally called {@link #start()}.
     */
    protected void shutdown() {
        if (asyncExecutor != null) {
            CyclicBarrier barrier = new CyclicBarrier(asyncPoolSize);
            List<Future<?>> futures = new ArrayList<>(asyncPoolSize);
            for (int i = 0; i < asyncPoolSize; i++) {
                futures.add(asyncExecutor.submit(() -> {
                    try {
                        barrier.await();
                    } catch (BrokenBarrierException e) {
                        // fall through to close anyway
                    }
                    Jep myJep = asyncInterpreters.remove(Thread.currentThread());
                    if (myJep != null) {
                        try {
                            myJep.close();
                        } catch (JepException e) {
                            PyCore.get().getLogger().warn("Error closing async JEP interpreter on shutdown", e);
                        }
                    }
                    return null;
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get(ASYNC_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    PyCore.get().getLogger().warn("Interrupted while closing async JEP interpreters on shutdown");
                } catch (ExecutionException | TimeoutException e) {
                    PyCore.get().getLogger().warn("Failed to close async JEP interpreter on shutdown", e);
                }
            }

            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(ASYNC_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                    asyncExecutor.shutdownNow();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                asyncExecutor.shutdownNow();
            }

            asyncExecutor = null;
            asyncInterpreters.clear();
        }

        if (mainInterpreter != null) {
            try {
                mainInterpreter.close();
            } catch (JepException e) {
                PyCore.get().getLogger().warn("Error closing main JEP interpreter on shutdown", e);
            }
            mainInterpreter = null;
        }
    }

    private void startAsyncPool() {
        AtomicInteger workerCounter = new AtomicInteger();
        asyncExecutor = Executors.newFixedThreadPool(asyncPoolSize, r -> {
            Thread t = new Thread(r, "PySpigot-Python-Async-" + workerCounter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });

        CyclicBarrier barrier = new CyclicBarrier(asyncPoolSize);
        List<Future<?>> futures = new ArrayList<>(asyncPoolSize);
        for (int i = 0; i < asyncPoolSize; i++) {
            futures.add(asyncExecutor.submit(() -> {
                Jep attached = (Jep) mainInterpreter.attach(true);
                asyncInterpreters.put(Thread.currentThread(), attached);
                try {
                    barrier.await();
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException("Async interpreter barrier broken during attach", e);
                }
                return null;
            }));
        }
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while attaching async JEP interpreters", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Failed to attach async JEP interpreter", e.getCause());
            }
        }
    }

    private <T> T runAsync(Script script, Supplier<T> work) {
        Supplier<T> wrapped = (script == null) ? work : () -> ScriptContext.supplyWith(script, work);

        if (isOnAsyncThread())
            return wrapped.get();

        try {
            return asyncExecutor.submit(wrapped::get).get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re)
                throw re;
            if (cause instanceof Error err)
                throw err;
            throw new RuntimeException(cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for async Python work", e);
        }
    }

    private void runAsync(Script script, Runnable work) {
        runAsync(script, () -> {
            work.run();
            return null;
        });
    }
}
