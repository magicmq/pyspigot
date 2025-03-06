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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A subclass of Logger that represents a script's logger.
 * @see Logger
 */
public class ScriptLogger extends Logger {

    private final String prefix;
    private final Path logFilePath;
    private FileHandler handler;

    /**
     *
     * @param script The script associated with this logger
     */
    public ScriptLogger(Script script) {
        super("PySpigot/" + script.getName(), null);
        this.setParent(PyCore.get().getLogger());

        this.prefix = "[PySpigot/" + script.getName() + "] ";

        Path scriptLogsFolder = PyCore.get().getDataFolderPath().resolve("logs");
        this.logFilePath = scriptLogsFolder.resolve(script.getLogFileName());
    }

    /**
     * Initializes the FileHandler to log script log messages to its respective log file.
     * @throws IOException If there was an IOException when initializing the FileHandler for this logger
     */
    public void initFileHandler() throws IOException {
        this.handler = new FileHandler(logFilePath.toString(), true);
        handler.setFormatter(new ScriptLogFormatter());
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
     * A convenience method added for a script to print debug information to console and its log file.
     * @param logText The message to print
     */
    public void print(String logText) {
        super.log(Level.INFO, logText);
    }

    /**
     * A convenience method added for a script to print debug information to console and its log file.
     * @param logText The message to print
     */
    public void debug(String logText) {
        super.log(Level.INFO, logText);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(LogRecord logRecord) {
        if (!PyCore.get().isPaper()) {
            logRecord.setMessage(prefix + logRecord.getMessage());
        }
        super.log(logRecord);
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
