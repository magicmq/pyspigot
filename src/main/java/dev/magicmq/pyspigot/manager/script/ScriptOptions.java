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

import dev.magicmq.pyspigot.config.PluginConfig;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A class representing various runtime options belonging to a certain script.
 */
public class ScriptOptions {

    private final boolean enabled;
    private final List<String> depend;
    private final boolean fileLoggingEnabled;
    private final Level minLoggingLevel;

    /**
     * Initialize a new ScriptOptions using values from the provided ConfigurationSection. If this constructor is passed a null value for the config parameter, then the default script options will be used.
     * @param config The configuration section from which script options should be read, or null if the default script options should be used
     */
    public ScriptOptions(ConfigurationSection config) {
        if (config != null) {
            this.enabled = config.getBoolean("enabled", true);
            this.depend = config.getStringList("depend");
            this.fileLoggingEnabled = config.getBoolean("file-logging-enabled", PluginConfig.doLogToFile());
            this.minLoggingLevel = Level.parse(config.getString("min-logging-level", PluginConfig.getLogLevel()));
        } else {
            this.enabled = true;
            this.depend = new ArrayList<>();
            this.fileLoggingEnabled = PluginConfig.doLogToFile();
            this.minLoggingLevel = Level.parse(PluginConfig.getLogLevel());
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
     * Get a list of dependencies for this script.
     * @return A list of dependencies for this script. Will return an empty list if this script has no dependencies
     */
    public List<String> getDependencies() {
        return depend;
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
        return String.format("ScriptOptions[Enabled: %b, Depend: %s, File Logging Enabled: %b, Minimum Logging Level: %s", enabled, depend, fileLoggingEnabled, minLoggingLevel);
    }
}
