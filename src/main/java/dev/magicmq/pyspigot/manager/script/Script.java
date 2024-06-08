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

package dev.magicmq.pyspigot.manager.script;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.ScriptOptions;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.util.logging.PrintStreamWrapper;
import dev.magicmq.pyspigot.util.logging.ScriptLogger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * An object that represents a loaded script. Because this object is instantiated some time before the script is actually executed (in order to fetch its options and order scripts to load according to dependencies), there may be a brief time when this object represents a loaded <i>but not running </i> script. To check if this script object represents a running script, call {@link ScriptManager#isScriptRunning(String)}.
 */
public class Script {

    private final File file;
    private final String name;
    private final ScriptOptions options;

    private Context context;
    private ScriptLogger logger;
    private long loadTime;

    /**
     *
     * @param name The name of this script. Should contain its extension (.py)
     * @param file The file where this script lives
     * @param options The {@link ScriptOptions} for this script
     */
    public Script(String name, File file, ScriptOptions options) {
        this.name = name;
        this.file = file;
        this.options = options;
    }

    /**
     * Prepares this script for execution by initializing its interpreter and logger. Called just prior to executing the script's code.
     * @param engine The engine that should be used to build the context that runs the script.
     */
    public void prepare(Engine engine) {
        Context.Builder builder = options.getContextBuilder();

        builder.hostClassLoader(LibraryManager.get().getClassLoader());

        builder.out(new PrintStreamWrapper(System.out, this, Level.INFO, "[STDOUT]"));
        builder.err(new PrintStreamWrapper(System.err, this, Level.SEVERE, ""));

        builder.option("python.PythonPath", "./plugins/PySpigot/python-libs/");

        builder.engine(engine);

        this.context = builder.build();

        this.logger = new ScriptLogger(this);
        this.logger.setLevel(options.getMinLoggingLevel());
        if (options.isFileLoggingEnabled()) {
            try {
                this.logger.initFileHandler();
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing log file for script " + name, e);
            }
        }
        context.getBindings("python").putMember("logger", this.logger);

        loadTime = System.currentTimeMillis();
    }

    /**
     * Closes this script's file logger and interpreter. Called when a script is unloaded/stopped.
     */
    public void close() {
        context.close(true);

        if (options.isFileLoggingEnabled())
            logger.closeFileHandler();
    }

    /**
     * Get the File associated with this script.
     * @return The File associated with this script
     */
    public File getFile() {
        return file;
    }

    /**
     * Get the name associated with this script.
     * @return The name associated with this script. Will contain its extension (.py)
     */
    public String getName() {
        return name;
    }

    /**
     * Get the simple name (without the file extension, .py) associated with this script.
     * @return The simple name associated with this script. Will contain only the file name, without the extension (.py)
     */
    public String getSimpleName() {
        return name.substring(0, name.length() - 3);
    }

    /**
     * Get the {@link ScriptOptions} for this script, which contains various runtime options associated with this script.
     * @return The {@link ScriptOptions} for this script
     */
    public ScriptOptions getOptions() {
        return options;
    }

    /**
     * Get the {@link org.graalvm.polyglot.Context} associated wtih this script.
     * @return The {@link org.graalvm.polyglot.Context} associated with this script
     */
    public Context getContext() {
        return context;
    }

    /**
     * Get this scripts logger.
     * @return This script's logger
     * @see ScriptLogger
     */
    public ScriptLogger getLogger() {
        return logger;
    }

    /**
     * Get the log file name for this script.
     * @return The log file name for this script. Will contain its extension (.log)
     */
    public String getLogFileName() {
        return getSimpleName() + ".log";
    }

    /**
     * Get the millisecond duration that this script has been loaded
     * @return The duration that the script has been loaded
     */
    public long getUptime() {
        return System.currentTimeMillis() - loadTime;
    }

    /**
     * Check if this script is the same as another script. Will check the names of both scripts to see if they match.
     * @param other The other script to check against this script
     * @return True if the scripts are equal, false if otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Script))
            return false;

        return name.equals(((Script) other).name);
    }
}
