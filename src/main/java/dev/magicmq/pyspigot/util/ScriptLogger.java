/*
 *    Copyright 2023 magicmq
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

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.script.Script;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.*;

public class ScriptLogger extends Logger {

    private String logFilePath;
    private String logIdentifier;
    private FileHandler handler;

    public ScriptLogger(Script script) {
        super("PySpigot/" + script.getName(), null);
        this.logIdentifier = "[" + "PySpigot/" + script.getName() + "] ";
        this.setParent(PySpigot.get().getLogger());
        this.setLevel(Level.ALL);

        File file = new File("");
        this.logFilePath = file.getAbsolutePath().replace("\\", "/") + "/plugins/PySpigot/logs/" + script.getLogFileName();
    }

    public void initFileHandler() throws IOException {
        this.handler = new FileHandler(logFilePath, true);
        handler.setFormatter(new ScriptLogFormatter());
        this.addHandler(handler);
    }

    public void closeFileHandler() {
        handler.close();
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(logIdentifier + record.getMessage());
        super.log(record);
    }

    //Convenience method added for scipts to print debug information to console
    public void print(String logText) {
        super.log(Level.INFO, logText);
    }

    //Convenience method added for scipts to print debug information to console
    public void debug(String logText) {
        super.log(Level.INFO, logText);
    }

    private static class ScriptLogFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder();

            ZonedDateTime zdt = ZonedDateTime.ofInstant(record.getInstant(), ZoneId.systemDefault());
            builder.append("[" + zdt.format(PluginConfig.getLogTimestamp()) + "] ");

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
