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
import dev.magicmq.pyspigot.util.ScriptLogger;
import org.python.core.PyCode;
import org.python.core.PyFunction;
import org.python.util.PythonInterpreter;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * An object that represents a loaded script. Because loading and running a script is done in phases, there may be a brief time when this object represents a loaded <i>but not running </i> script, as this object is instantiated prior to running the script. To check if this script object represents a running script, call {@link ScriptManager#isScriptRunning(String)}.
 */
public class Script {

    private final String name;
    private final ScriptOptions options;
    private final PythonInterpreter interpreter;
    private final PyCode code;
    private final File file;
    private final ScriptLogger logger;

    private PyFunction startFunction;
    private PyFunction stopFunction;

    /**
     *
     * @param name The name of this script. Should contain its extension (.py)
     * @param options The {@link ScriptOptions} for this script
     * @param interpreter The {@link org.python.util.PythonInterpreter} for this script
     * @param code The {@link org.python.core.PyCode} for this script
     * @param file The file associated with this script
     */
    public Script(String name, ScriptOptions options, PythonInterpreter interpreter, PyCode code, File file) {
        this.name = name;
        this.options = options;
        this.interpreter = interpreter;
        this.code = code;
        this.file = file;

        this.logger = new ScriptLogger(this);
        this.logger.setLevel(options.getLoggingLevel());
        if (options.isLoggingEnabled()) {
            try {
                this.logger.initFileHandler();
            } catch (IOException e) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing log file for script " + name, e);
            }
        }
        interpreter.set("logger", logger);
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
     * Get the log file name for this script.
     * @return The log file name for this script. Will contain its extension (.log)
     */
    public String getLogFileName() {
        return getSimpleName() + ".log";
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
     * Get the {@link org.python.core.PyCode} associated wtih this script.
     * @return The {@link org.python.core.PyCode} associated with this script
     */
    public PyCode getCode() {
        return code;
    }

    /**
     * Get the File associated with this script.
     * @return The File associated with this script
     */
    public File getFile() {
        return file;
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
     * Get the start function defined in this script, if there is one defined.
     * @return The start function for this script, or null if there is no start function defined in the script
     */
    public PyFunction getStartFunction() {
        return startFunction;
    }

    /**
     * Set the start function for this script.
     * @param startFunction The start function defined in the script. Can be null
     */
    public void setStartFunction(PyFunction startFunction) {
        this.startFunction = startFunction;
    }

    /**
     * Get the stop function defined in this script, if there is one defined.
     * @return The stop function for this script, or null if there is no start function defined in the script
     */
    public PyFunction getStopFunction() {
        return stopFunction;
    }

    /**
     * Set the stop function for this script.
     * @param stopFunction The stop function defined in the script. Can be null
     */
    public void setStopFunction(PyFunction stopFunction) {
        this.stopFunction = stopFunction;
    }

    /**
     * Closes this script's  file logger.
     */
    public void close() {
        if (options.isLoggingEnabled())
            logger.closeFileHandler();
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
