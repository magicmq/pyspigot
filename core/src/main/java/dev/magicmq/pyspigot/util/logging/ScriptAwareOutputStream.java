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

package dev.magicmq.pyspigot.util.logging;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptContext;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * OutputStream installed via {@code JepConfig.redirectStdout}/{@code redirectStdErr}.
 * <p>
 * JEP routes every Python {@code sys.stdout}/{@code sys.stderr} write through this stream.
 * Bytes are accumulated into an internal buffer and emitted as a single log entry once writes
 * stop arriving for a short debounce window, or when {@link #flush()} is called. This means a
 * multi-write Python traceback (which calls {@code stderr.write()} once per line) becomes one
 * multi-line log entry, while a standalone {@code print()} still emits as its own entry after
 * the debounce window elapses.
 * <p>
 * Routing: when a buffer is emitted, the active {@link Script} captured at write-time
 * determines the destination logger; if no script context was active, output goes to the plugin
 * logger as a fallback.
 */
public final class ScriptAwareOutputStream extends OutputStream {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "PySpigot-LogFlush");
                t.setDaemon(true);
                return t;
            });
    private static final long DEBOUNCE_MS = 30L;

    private final boolean error;
    private final String prefix;
    private final StringBuilder buffer = new StringBuilder();
    private Script bufferedScript;
    private ScheduledFuture<?> pendingFlush;

    /**
     *
     * @param error True if this stream backs Python's {@code sys.stderr}, false if it backs
     *              {@code sys.stdout}. Determines log level and the bracketed prefix.
     */
    public ScriptAwareOutputStream(boolean error) {
        this.error = error;
        this.prefix = error ? "[STDERR] " : "[STDOUT] ";
    }

    /**
     * Appends the slice (decoded as UTF-8) to the internal buffer and (re)schedules a
     * deferred emit. If the script context changes mid-buffer, the previously buffered
     * content is emitted first so writes are not misattributed.
     */
    @Override
    public synchronized void write(byte[] buf, int off, int len) {
        Script current = ScriptContext.current();
        if (buffer.length() > 0 && current != bufferedScript)
            emitLocked();

        buffer.append(new String(buf, off, len, StandardCharsets.UTF_8));
        bufferedScript = current;

        if (pendingFlush != null)
            pendingFlush.cancel(false);
        pendingFlush = SCHEDULER.schedule(this::scheduledFlush, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] buf) {
        write(buf, 0, buf.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) {
        write(new byte[]{(byte) b}, 0, 1);
    }

    /**
     * Forces any buffered content to be emitted immediately. Called by JEP when Python code
     * does {@code sys.stdout.flush()} / {@code sys.stderr.flush()}.
     */
    @Override
    public synchronized void flush() {
        if (pendingFlush != null) {
            pendingFlush.cancel(false);
            pendingFlush = null;
        }
        emitLocked();
    }

    private synchronized void scheduledFlush() {
        pendingFlush = null;
        emitLocked();
    }

    private void emitLocked() {
        if (buffer.length() == 0)
            return;

        String text = buffer.toString().replaceAll("\\R$", "");
        Script script = bufferedScript;
        buffer.setLength(0);
        bufferedScript = null;

        if (text.isEmpty())
            return;

        if (script != null) {
            if (error)
                script.getLogger().error(prefix + text);
            else
                script.getLogger().info(prefix + text);
        } else {
            if (error)
                PyCore.get().getLogger().error("{}{}", prefix, text);
            else
                PyCore.get().getLogger().info("{}{}", prefix, text);
        }
    }
}
