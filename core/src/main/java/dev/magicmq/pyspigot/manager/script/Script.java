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
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.magicmq.pyspigot.util.logging.PrintStreamWrapper;
import dev.magicmq.pyspigot.util.logging.ScriptLogger;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * An object that represents a loaded script. Because this object is instantiated some time before the script is actually executed (in order to fetch its options and order scripts to load according to dependencies), there may be a brief time when this object represents a loaded <i>but not running </i> script. To check if this script object represents a running script, call {@link ScriptManager#isScriptRunning(String)}.
 */
public class Script implements Comparable<Script> {

    private final Path path;
    private final String name;
    private final ScriptOptions options;

    private PythonInterpreter interpreter;
    private ScriptLogger logger;
    private long loadTime;

    /**
     *
     * @param path The path that corresponds to the file where the script lives
     * @param name The name of this script. Should contain its extension (.py)
     * @param options The {@link ScriptOptions} for this script
     */
    public Script(Path path, String name, ScriptOptions options) {
        this.path = path;
        this.name = name;
        this.options = options;
    }

    protected void prepare() {
        this.interpreter = new PythonInterpreter(null, ScriptUtils.initPySystemState());
        this.interpreter.setOut(new PrintStreamWrapper(System.out, this, Level.INFO, "[STDOUT]"));
        this.interpreter.setErr(new PrintStreamWrapper(System.err, this, Level.SEVERE, "[STDERR]"));

        this.logger = new ScriptLogger(this);
        this.logger.setLevel(options.getMinLoggingLevel());
        if (options.isFileLoggingEnabled()) {
            try {
                this.logger.initFileHandler();
            } catch (IOException e) {
                PyCore.get().getLogger().log(Level.SEVERE, "Error when initializing log file for script " + name, e);
            }
        }
        interpreter.set("logger", logger);

        loadTime = System.currentTimeMillis();
    }

    /**
     * Closes this script's file logger and interpreter. Called when a script is unloaded/stopped.
     */
    public void close() {
        interpreter.close();

        if (options.isFileLoggingEnabled())
            logger.closeFileHandler();
    }

    /**
     * Get the File associated with this script.
     * @return The File associated with this script
     */
    public File getFile() {
        return path.toFile();
    }

    /**
     * Get the path corresponding to the script file.
     * @return The path
     */
    public Path getPath() {
        return path;
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
     * Get the {@link org.python.util.PythonInterpreter} associated wtih this script.
     * @return The {@link org.python.util.PythonInterpreter} associated with this script
     */
    public PythonInterpreter getInterpreter() {
        return interpreter;
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
     * Compares this script to another script, using load order as the primary comparison. If the load order of this script is higher than other, then this script will be considered "less" than other (I.E. sorted earlier in a set than the other script). If the load order of this script is lower than other, then this script will be considered "greater" than the other script (I.E. sorted later in a set than other).
     * <p>
     * If the load priority of this script is equal to other, then a comparison is performed based on the name of the two scripts (alphabetical).
     * @param other The other script to be compared
     * @return 1 if this script is greater than other, -1 if this script is less than other, and 0 if the two scripts are equal
     */
    @Override
    public int compareTo(Script other) {
        //Normally, would compare this to other, however we need descending order (higher numbers first), so reverse the comparison
        int compare = Integer.compare(other.options.getLoadPriority(), this.options.getLoadPriority());
        if (compare == 0) {
            return this.name.compareTo(other.name);
        } else
            return compare;
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
