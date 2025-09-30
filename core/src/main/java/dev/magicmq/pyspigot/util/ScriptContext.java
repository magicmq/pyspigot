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

package dev.magicmq.pyspigot.util;


import dev.magicmq.pyspigot.manager.script.Script;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.function.Supplier;

/**
 * Maintains a <strong>per-thread</strong>, <strong>re-entrant</strong> stack of {@link Script}
 * instances that identifies which script is currently executing when PySpigot calls into Python.
 *
 * <h2>Why this exists</h2>
 * Many Minecraft APIs (Bukkit/Bungee/Protocolize/PlaceholderAPI) invoke callbacks
 * <em>synchronously</em> and may re-enter PySpigot while user code is still running.
 * That produces nested flows such as A → B → A (e.g., Script A’s listener triggers a placeholder
 * owned by Script B before returning). A single ThreadLocal value can’t reliably restore the
 * previous script in these cases; a LIFO stack can.
 *
 * <h2>What this class guarantees</h2>
 * <ul>
 *   <li>Each Java thread has its own independent stack ({@link ThreadLocal}).</li>
 *   <li>Entering a context pushes the owning {@link Script}; leaving pops it (LIFO).</li>
 *   <li>{@link #current()} always returns the script at the <em>top</em> of the stack, or null if none.</li>
 *   <li>{@link #require()} throws if used outside a script context (useful in "scripts-only" manager entry points).</li>
 *   <li>{@link Scope#close()} throws on underflow or mismatch, catching push/pop mistakes early.</li>
 * </ul>
 *
 * <h2>How to use</h2>
 * <p>Always wrap every Java → Python boundary:</p>
 * <pre>{@code
 * try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
 *     // Call into Python here
 *     pyFunction.__call__(args); // or use a central PyInvoke utility
 * }
 * }</pre>
 *
 * <p>Or use the convenience wrappers:</p>
 * <pre>{@code
 * ScriptContext.runWith(script, () -> pyFunction.__call__(args));
 * String value = ScriptContext.callWith(script, () -> pyFunction.__call__(args).asString());
 * }</pre>
 *
 * <h2>Where this class is used</h2>
 * <ul>
 *   <li>Loader when importing a script/project and calling {@code start()}/{@code stop()}.</li>
 *   <li>Event/listener dispatch (Bukkit/Bungee/etc.).</li>
 *   <li>Command execute and tab-complete callbacks.</li>
 *   <li>Task execution (one-shot, repeating, both halves of sync-callback tasks).</li>
 *   <li>Placeholder resolution handlers.</li>
 *   <li>Protocolize receive/send packet listeners.</li>
 *   <li>Current script context is also fetched anytime the current script is required, I.E. when registering event
 *   listeners.</li>
 * </ul>
 *
 * <h2>Thread-safety</h2>
 * <p>Backed by a {@link ThreadLocal} of {@link ArrayDeque}. No synchronization is performed because each
 * thread owns its stack. Do not share {@link Script} execution across threads without entering a context
 * on the target thread. If you hand off work to another thread/executor, capture the script at submission
 * time and re-enter it in the worker.</p>
 *
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>Uses {@link ArrayDeque} as a stack via head-based {@code push/peek/pop}; this is the JDK-recommended
 *       replacement for legacy {@link java.util.Stack} (which is synchronized and {@code Vector}-based).</li>
 *   <li>Underflow and mismatch throw {@link IllegalStateException} to fail fast during development.</li>
 * </ul>
 */
public final class ScriptContext {

    private static final ThreadLocal<Deque<Script>> threadLocal = ThreadLocal.withInitial(ArrayDeque::new);

    private ScriptContext() {}

    /**
     * Returns the {@link Script} at the top of the current thread's stack, or null if no
     * script context is active.
     * <p>
     * Use this for logging or optional behavior; prefer {@link #require()} in code paths that must only run inside a
     * script context.
     * @return The current {@link Script}, or null if there is none
     */
    public static Script current() {
        Deque<Script> stack = threadLocal.get();
        return stack.peek();
    }

    /**
     * Returns the {@link Script} at the top of the current thread's stack, throwing if none.
     * <p>
     * Call this in "scripts-only" manager entry points (e.g., when a script registers a listener, schedules a task, or
     * creates a placeholder) to bind the created handle to the correct script.
     * @return The current {@link Script}
     * @throws IllegalStateException If no script context is active on this thread
     */
    public static Script require() {
        Script script = current();
        if (script == null)
            throw new IllegalStateException("No script context available on this thread. Was PySpigot called from a thread that it is unaware of?");
        return script;
    }

    /**
     * Enters a script context by pushing the given {@link Script} onto this thread's stack.
     * <p>
     * Always pair with try-with-resources so {@link Scope#close()} pops the same script, even if an
     * exception is thrown.
     * <p>
     * <pre>{@code
     * try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
     *     // invoke Python here
     * }
     * }</pre>
     * @param script The owning script for the ensuing Java → Python calls; must not be null
     * @return A {@link Scope} that must be closed to restore the previous context
     */
    public static Scope enter(Script script) {
        threadLocal.get().push(script);
        return new Scope();
    }

    /**
     * Runs the given {@link Runnable} with the provided script bound as the current context.
     * <p>
     * Convenience for small call sites; functionally equivalent to:
     * <p>
     * <pre>{@code
     * try (ScriptContext.Scope ignored = ScriptContext.enter(script)) {
     *     r.run();
     * }
     * }</pre>
     * @param script The script to bind for the duration of {@code r.run()}
     * @param runnable The work to run
     */
    public static void runWith(Script script, Runnable runnable) {
        try (Scope ignored = enter(script)) {
            runnable.run();
        }
    }

    /**
     * Evaluates the given {@link Supplier} with the provided script bound as the current context and returns its result.
     * @param <V> The return type
     * @param script The script to bind for the duration of the call
     * @param supplier The callable to execute
     * @return the value returned by {@code supplier.get()}
     */
    public static <V> V supplyWith(Script script, Supplier<V> supplier) {
        try (Scope ignored = enter(script)) {
            return supplier.get();
        }
    }

    /**
     * A RAII scope that pops the {@link Script} previously pushed by {@link #enter(Script)}.
     * <p>
     * This class is not intended for direct instantiation; obtain instances via {@link ScriptContext#enter(Script)}.
     */
    public static final class Scope implements AutoCloseable {

        private boolean closed;

        @Override
        public void close() {
            if (!closed) {
                Deque<Script> stack = threadLocal.get();
                stack.pop();
                closed = true;
            }
        }
    }
}