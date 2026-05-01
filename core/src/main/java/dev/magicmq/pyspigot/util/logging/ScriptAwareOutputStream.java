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

/**
 * OutputStream installed via {@code JepConfig.redirectStdout}/{@code redirectStderr}.
 * <p>
 * JEP routes every Python {@code sys.stdout}/{@code sys.stderr} write through this stream.
 * On each write, the current script is resolved from {@link ScriptContext} and the line is
 * forwarded to that script's logger. When no script context is active on this thread
 * (e.g., interpreter initialization, top-level module imports), output is forwarded to the
 * plugin logger as a fallback so it is still visible without being misattributed.
 */
public final class ScriptAwareOutputStream extends OutputStream {

    private final boolean error;
    private final String prefix;

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
     * Decodes the slice as UTF-8, strips a single trailing newline (the script logger adds
     * its own line break), and routes the resulting text to the current script's logger or,
     * if no script context is active on this thread, to the plugin logger.
     */
    @Override
    public void write(byte[] buf, int off, int len) {
        String text = new String(buf, off, len, StandardCharsets.UTF_8).replaceAll("\\R$", "");
        if (text.isEmpty())
            return;

        Script script = ScriptContext.current();
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
}
