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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * A simple log handler that captures any log messages submitted to Jython's logger (by Jython) and forwards them to PySpigot's plugin logger.
 */
public class JythonLogHandler extends Handler {

    private static final String LOG_MESSAGE_FORMAT = "[%s] %s";

    /**
     * Intercepts a log record, modifies the LogRecord, and forwards it to PySpigot's logger.
     * The following modifications are made to the LogRecord:
     * <ul>
     *     <li>The logger name of the LogRecord is changed to the name of PySpigot's logger.</li>
     *     <li>The original logger name (a Jython logger) is inserted at the beginning of the log message in square brackets.</li>
     * </ul>
     * @param record The LogRecord to forward
     */
    @Override
    public void publish(LogRecord record) {
        String jythonLoggerName = record.getLoggerName();
        String message = record.getMessage();

        record.setLoggerName(PyCore.get().getLogger().getName());
        record.setMessage(String.format(LOG_MESSAGE_FORMAT, jythonLoggerName, message));

        PyCore.get().getLogger().log(record);
    }

    /**
     * No-op implementation
     */
    @Override
    public void flush() {}

    /**
     * No-op implementation
     */
    @Override
    public void close() throws SecurityException {}
}
