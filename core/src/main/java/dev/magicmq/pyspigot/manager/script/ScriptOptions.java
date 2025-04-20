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

import java.util.List;
import java.util.logging.Level;

/**
 * A class representing various runtime options belonging to a certain script.
 */
public class ScriptOptions {

    private final boolean enabled;
    private final int loadPriority;
    private final List<String> pluginDepend;
    private final boolean fileLoggingEnabled;
    private final Level minLoggingLevel;

    /**
     * Initialize a new ScriptOptions with the default values.
     */
    public ScriptOptions() {
        this.enabled = PyCore.get().getConfig().scriptOptionEnabled();
        this.loadPriority = PyCore.get().getConfig().scriptOptionLoadPriority();
        this.pluginDepend = PyCore.get().getConfig().scriptOptionPluginDepend();
        this.fileLoggingEnabled = PyCore.get().getConfig().scriptOptionFileLoggingEnabled();
        this.minLoggingLevel = Level.parse(PyCore.get().getConfig().scriptOptionMinLoggingLevel());
    }

    /**
     * Initialize a new ScriptOptions using the appropriate values in the script_options.yml file, using the script name to search for the values.
     * @param scriptName The name of the script whose script options should be initialized
     */
    public ScriptOptions(String scriptName) {
        if (PyCore.get().getScriptOptionsConfig().contains(scriptName)) {
            this.enabled = PyCore.get().getScriptOptionsConfig().getEnabled(scriptName, PyCore.get().getConfig().scriptOptionEnabled());
            this.loadPriority = PyCore.get().getScriptOptionsConfig().getLoadPriority(scriptName, PyCore.get().getConfig().scriptOptionLoadPriority());
            this.pluginDepend = PyCore.get().getScriptOptionsConfig().getPluginDepend(scriptName, PyCore.get().getConfig().scriptOptionPluginDepend());
            this.fileLoggingEnabled = PyCore.get().getScriptOptionsConfig().getFileLoggingEnabled(scriptName, PyCore.get().getConfig().scriptOptionFileLoggingEnabled());
            this.minLoggingLevel = Level.parse(PyCore.get().getScriptOptionsConfig().getMinLoggingLevel(scriptName, PyCore.get().getConfig().scriptOptionMinLoggingLevel()));
        } else {
            this.enabled = PyCore.get().getConfig().scriptOptionEnabled();
            this.loadPriority = PyCore.get().getConfig().scriptOptionLoadPriority();
            this.pluginDepend = PyCore.get().getConfig().scriptOptionPluginDepend();
            this.fileLoggingEnabled = PyCore.get().getConfig().scriptOptionFileLoggingEnabled();
            this.minLoggingLevel = Level.parse(PyCore.get().getConfig().scriptOptionMinLoggingLevel());
        }
    }

    /**
     * Get if this script is enabled.
     * @return True if the script is enabled, false if otherwise
     */
    public boolean isEnabled() {
        return enabled;
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
        return String.format("ScriptOptions[Enabled: %b, Load Priority: %d, Plugin Dependencies: %s, File Logging Enabled: %b, Minimum Logging Level: %s]", enabled, loadPriority, pluginDepend, fileLoggingEnabled, minLoggingLevel);
    }
}
