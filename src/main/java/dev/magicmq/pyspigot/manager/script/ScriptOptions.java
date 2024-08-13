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
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A class representing various runtime options belonging to a certain script.
 */
public class ScriptOptions {

    private final boolean enabled;
    private final List<String> scriptDepend;
    private final List<String> pluginDepend;
    private final boolean fileLoggingEnabled;
    private final Level minLoggingLevel;
    private final PermissionDefault permissionDefault;
    private final List<Permission> permissions;

    /**
     * Initialize a new ScriptOptions with the default values.
     */
    public ScriptOptions() {
        this(null);
    }

    /**
     * Initialize a new ScriptOptions using values from the provided ConfigurationSection. If this constructor is passed a null value for the config parameter, then the default script options will be used.
     * @param config The configuration section from which script options should be read, or null if the default script options should be used
     */
    public ScriptOptions(ConfigurationSection config) {
        if (config != null) {
            this.enabled = config.getBoolean("enabled", true);
            this.scriptDepend = config.getStringList("depend");
            this.pluginDepend = config.getStringList("plugin-depend");
            this.fileLoggingEnabled = config.getBoolean("file-logging-enabled", PluginConfig.doLogToFile());
            this.minLoggingLevel = Level.parse(config.getString("min-logging-level", PluginConfig.getLogLevel()));
            this.permissionDefault = PermissionDefault.getByName(config.getString("permission-default", "op"));
            this.permissions = Permission.loadPermissions((Map<?, ?>) config.get("permissions", new HashMap<>()), "Permission node '%s' in script_options.yml for '" + config.getName() + "' is invalid", permissionDefault);
        } else {
            this.enabled = true;
            this.scriptDepend = new ArrayList<>();
            this.pluginDepend = new ArrayList<>();
            this.fileLoggingEnabled = PluginConfig.doLogToFile();
            this.minLoggingLevel = Level.parse(PluginConfig.getLogLevel());
            this.permissionDefault = PermissionDefault.OP;
            this.permissions = new ArrayList<>();
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
     * Get a list of script dependencies for this script.
     * @return A list of script dependencies for this script. Will return an empty list if this script has no script dependencies
     */
    public List<String> getScriptDependencies() {
        return scriptDepend;
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
     * Get the default permissions for permissions defined for this script.
     * @return The default permission level
     */
    public PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    /**
     * Get a list of permissions defined for this script.
     * @return A list of permissions. Will return an empty list if this script has no permissions defined
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Prints a representation of this ScriptOptions in string format, including all options as defined in script_options.yml
     * @return A string representation of the ScriptOptions
     */
    @Override
    public String toString() {
        return String.format("ScriptOptions[Enabled: %b, Depend: %s, File Logging Enabled: %b, Minimum Logging Level: %s, Permission Default: %s, Permissions: %s", enabled, scriptDepend, fileLoggingEnabled, minLoggingLevel, permissionDefault, permissions);
    }
}
