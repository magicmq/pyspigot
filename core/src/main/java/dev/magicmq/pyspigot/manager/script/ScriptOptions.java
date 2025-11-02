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
import dev.magicmq.pyspigot.config.ProjectOptionsConfig;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

/**
 * A class representing various runtime options belonging to a certain script.
 */
public class ScriptOptions {

    private final boolean project;

    private final String mainScript;
    private final boolean enabled;
    private final boolean autoLoad;
    private final int loadPriority;
    private final List<String> pluginDepend;
    private final boolean fileLoggingEnabled;
    private final Level minLoggingLevel;

    /**
     * Initialize a new ScriptOptions for a single-file script, using the appropriate values in the script_options.yml file.
     * @param scriptPath The path of the script file whose script options should be initialized
     */
    public ScriptOptions(Path scriptPath) {
        this.project = false;

        String scriptName = scriptPath.getFileName().toString();
        if (PyCore.get().getScriptOptionsConfig().contains(scriptName)) {
            this.mainScript = null;
            this.enabled = PyCore.get().getScriptOptionsConfig().getEnabled(scriptName, PyCore.get().getConfig().scriptOptionEnabled());
            this.autoLoad = PyCore.get().getScriptOptionsConfig().getAutoLoad(scriptName, PyCore.get().getConfig().scriptOptionAutoLoad());
            this.loadPriority = PyCore.get().getScriptOptionsConfig().getLoadPriority(scriptName, PyCore.get().getConfig().scriptOptionLoadPriority());
            this.pluginDepend = PyCore.get().getScriptOptionsConfig().getPluginDepend(scriptName, PyCore.get().getConfig().scriptOptionPluginDepend());
            this.fileLoggingEnabled = PyCore.get().getScriptOptionsConfig().getFileLoggingEnabled(scriptName, PyCore.get().getConfig().scriptOptionFileLoggingEnabled());
            this.minLoggingLevel = Level.parse(PyCore.get().getScriptOptionsConfig().getMinLoggingLevel(scriptName, PyCore.get().getConfig().scriptOptionMinLoggingLevel()));
        } else {
            this.mainScript = null;
            this.enabled = PyCore.get().getConfig().scriptOptionEnabled();
            this.autoLoad = PyCore.get().getConfig().scriptOptionAutoLoad();
            this.loadPriority = PyCore.get().getConfig().scriptOptionLoadPriority();
            this.pluginDepend = PyCore.get().getConfig().scriptOptionPluginDepend();
            this.fileLoggingEnabled = PyCore.get().getConfig().scriptOptionFileLoggingEnabled();
            this.minLoggingLevel = Level.parse(PyCore.get().getConfig().scriptOptionMinLoggingLevel());
        }
    }

    /**
     * Initialize a new ScriptOptions for a multi-file project, using the appropriate values in the project's project.yml file.
     * @param config The project.yml file to parse that belongs to the project.
     *               If the project does not have a project.yml file, pass null, and the default values will be used
     */
    public ScriptOptions(ProjectOptionsConfig config) {
        this.project = true;

        if (config != null) {
            this.mainScript = config.getMainScript(PyCore.get().getConfig().scriptOptionMainScript());
            this.enabled = config.getEnabled(PyCore.get().getConfig().scriptOptionEnabled());
            this.autoLoad = config.getAutoLoad(PyCore.get().getConfig().scriptOptionAutoLoad());
            this.loadPriority = config.getLoadPriority(PyCore.get().getConfig().scriptOptionLoadPriority());
            this.pluginDepend = config.getPluginDepend(PyCore.get().getConfig().scriptOptionPluginDepend());
            this.fileLoggingEnabled = config.getFileLoggingEnabled(PyCore.get().getConfig().scriptOptionFileLoggingEnabled());
            this.minLoggingLevel = Level.parse(config.getMinLoggingLevel(PyCore.get().getConfig().scriptOptionMinLoggingLevel()));
        } else {
            this.mainScript = PyCore.get().getConfig().scriptOptionMainScript();
            this.enabled = PyCore.get().getConfig().scriptOptionEnabled();
            this.autoLoad = PyCore.get().getConfig().scriptOptionAutoLoad();
            this.loadPriority = PyCore.get().getConfig().scriptOptionLoadPriority();
            this.pluginDepend = PyCore.get().getConfig().scriptOptionPluginDepend();
            this.fileLoggingEnabled = PyCore.get().getConfig().scriptOptionFileLoggingEnabled();
            this.minLoggingLevel = Level.parse(PyCore.get().getConfig().scriptOptionMinLoggingLevel());
        }
    }

    /**
     * Get the main script file for this project.
     * @return The main script file for this project
     */
    public String getMainScript() {
        return mainScript;
    }

    /**
     * Get if this script is enabled.
     * @return True if the script is enabled, false if otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get if this script should be automatically loaded on server start, plugin enable, or plugin reload.
     * @return True if the script should be automatically loaded, false if it should not
     */
    public boolean isAutoLoad() {
        return autoLoad;
    }

    /**
     * Get the load priority for this script. Scripts with greater load priority will load before scripts with lower load priority.
     * @return The script's load priority
     */
    public int getLoadPriority() {
        return loadPriority;
    }

    /**
     * Get a list of plugin dependencies for this script.
     * @return A list of plugin dependencies for this script. Will return an empty list if this script has no plugin dependencies
     */
    public List<String> getPluginDependencies() {
        return pluginDepend;
    }

    /**
     * Get if file logging is enabled for this script.
     * @return True if file logging is enabled, false if otherwise
     */
    public boolean isFileLoggingEnabled() {
        return fileLoggingEnabled;
    }

    /**
     * Get the minimum logging level for this script, represented as a {@link java.util.logging.Level}
     * @return The minimum logging level at which messages should be logged
     */
    public Level getMinLoggingLevel() {
        return minLoggingLevel;
    }

    /**
     * Prints a representation of this ScriptOptions in string format, including all options as defined in script_options.yml
     * @return A string representation of the ScriptOptions
     */
    @Override
    public String toString() {
        if (project)
            return String.format("ProjectOptions[Main: %s, Enabled: %b, Auto-Load: %b, Load Priority: %d, Plugin Dependencies: %s, File Logging Enabled: %b, Minimum Logging Level: %s]", mainScript, enabled, autoLoad, loadPriority, pluginDepend, fileLoggingEnabled, minLoggingLevel);
        else
            return String.format("ScriptOptions[Enabled: %b, Auto-Load: %b, Load Priority: %d, Plugin Dependencies: %s, File Logging Enabled: %b, Minimum Logging Level: %s]", enabled, autoLoad, loadPriority, pluginDepend, fileLoggingEnabled, minLoggingLevel);
    }
}
