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

import dev.magicmq.pyspigot.exception.ScriptInitializationException;
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.magicmq.pyspigot.util.logging.PrintStreamWrapper;
import dev.magicmq.pyspigot.util.logging.ScriptLogger;
import org.python.util.PythonInterpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An object that represents a loaded script or project. Because this object is instantiated some time before the script/project is actually executed (in order to fetch its options and order scripts to load according to dependencies), there may be a brief time when this object represents a loaded <i>but not running </i> script/project. To check if this script object represents a running script/project, call {@link ScriptManager#isScriptRunning(String)}.
 * <p>
 * Note that although most of the documentation of this class uses the word "script", this object encapsulates both single-file scripts and multi-file projects. "Script" and "Project" are interchangeable in this context.
 */
public class Script implements Comparable<Script> {

    private final Path path;
    private final Path mainScriptPath;
    private final String name;
    private final ScriptOptions options;
    private final boolean project;
    private final Set<Path> modules;

    private PythonInterpreter interpreter;
    private ScriptLogger logger;
    private long loadTime;

    /**
     *
     * @param path The path that corresponds to the file/folder where the script or project lives
     * @param name The name of this script/project. If this is a single-file script, the name should contain the extension (.py)
     * @param options The {@link ScriptOptions} for this script/project
     * @param project True if this script is a multi-file project, false if it is a single-file script
     */
    public Script(Path path, String name, ScriptOptions options, boolean project) {
        this.path = path;
        if (project)
            this.mainScriptPath = path.resolve(options.getMainScript());
        else
            this.mainScriptPath = path;
        this.name = name;
        this.options = options;
        this.project = project;
        this.modules = new HashSet<>();
    }

    protected void prepare() throws ScriptInitializationException {
        if (project)
            this.interpreter = new PythonInterpreter(null, ScriptUtils.initPySystemState(path));
        else
            this.interpreter = new PythonInterpreter(null, ScriptUtils.initPySystemState(null));

        this.interpreter.setOut(new PrintStreamWrapper(System.out, this, Level.INFO, "[STDOUT]"));
        this.interpreter.setErr(new PrintStreamWrapper(System.err, this, Level.SEVERE, "[STDERR]"));

        this.logger = new ScriptLogger(this);

        interpreter.set("logger", logger);

        if (project) {
            try (Stream<Path> walk = Files.walk(path)) {
                this.modules.addAll(walk
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".py"))
                        .collect(Collectors.toSet()));
            } catch (IOException e) {
                this.interpreter.close();
                this.logger.close();
                throw new ScriptInitializationException(this, "Error when fetching project modules", e);
            }
        } else
            this.modules.add(mainScriptPath);

        loadTime = System.currentTimeMillis();
    }

    /**
     * Closes this script's file logger and interpreter. Called when a script is unloaded/stopped.
     */
    public void close() {
        interpreter.close();
        logger.close();
    }

    /**
     * Get the path corresponding to the script file, or folder (if it is a multi-file project).
     * @return The path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Get the main script file path. For a single-file script, this will return the same value as {@link #getPath()}. For a multi-file project, this will return the Path resolved to the main script as defined in the project's project.yml file.
     * @return The main script file path for this script
     */
    public Path getMainScriptPath() {
        return mainScriptPath;
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
        if (project)
            return name;
        else
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
     * Get if this Script object represents a multi-file project or a single-file script.
     * @return True if this Script is a multi-file project, false if it is not
     */
    public boolean isProject() {
        return project;
    }

    /**
     * Get all modules belonging to this script/project.
     * @return An immutable set containing all modules belonging to this script/project. If this is a single-file script, the set will only contain the script itself.
     */
    public Set<Path> getModules() {
        return new HashSet<>(modules);
    }

    /**
     * Get the {@link org.python.util.PythonInterpreter} associated wtih this script.
     * @return The {@link org.python.util.PythonInterpreter} associated with this script
     */
    public PythonInterpreter getInterpreter() {
        return interpreter;
    }

    /**
     * Get this script's logger.
     * @return This script's logger
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
     * Get the millisecond duration that this script has been loaded.
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

    /**
     * Computes a hash code for this Script.
     * <p>
     * The hash code is based upon the name of the script, and satisfies the general contract of the {@link Object#hashCode} method.
     * @return The hash-code value for this Script
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
