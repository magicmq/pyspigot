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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A Logger that logs messages to a script's log file.
 */
public class ScriptFileLogger extends Logger {

    private final FileHandler handler;

    /**
     *
     * @param script The script associated with this ScriptFileLogger
     */
    public ScriptFileLogger(Script script) throws IOException {
        super(script.getName(), null);

        Path scriptLogsFolder = PyCore.get().getDataFolderPath().resolve("logs");
        Path logFilePath = scriptLogsFolder.resolve(script.getLogFileName());

        this.handler = new FileHandler(logFilePath.toString(), true);
        handler.setFormatter(new ScriptFileLogger.ScriptLogFormatter());
        handler.setEncoding("UTF-8");
        this.addHandler(handler);
    }

    /**
     * Closes the FileHandler for this logger. Should only be called if script file logging is enabled.
     */
    public void closeFileHandler() {
        if (handler != null)
            handler.close();
    }

    /**
     * A {@link Formatter} to log script messages to their respective log file.
     */
    private static class ScriptLogFormatter extends Formatter {

        /**
         * Formats a LogRecord into an appropriate format for the script's log file.
         * @param record The log record to be formatted.
         * @return A String of formatted text that will be logged to the script's log file
         */
        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
            builder.append("[" + zdt.format(PyCore.get().getConfig().getLogTimestamp()) + "] ");

            builder.append("[" + record.getLevel().getLocalizedName() + "] ");

            builder.append(super.formatMessage(record));

            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            } else {
                throwable += "\n";
            }
            builder.append(throwable);

            return builder.toString();
        }
    }
}
