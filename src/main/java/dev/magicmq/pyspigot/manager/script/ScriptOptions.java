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
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
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
    private final PermissionDefault permissionDefault;
    private final List<Permission> permissions;

    /**
     * Initialize a new ScriptOptions with the default values.
     */
    public ScriptOptions() {
        this.enabled = PluginConfig.scriptOptionEnabled();
        this.loadPriority = PluginConfig.scriptOptionLoadPriority();
        this.pluginDepend = PluginConfig.scriptOptionPluginDepend();
        this.fileLoggingEnabled = PluginConfig.scriptOptionFileLoggingEnabled();
        this.minLoggingLevel = Level.parse(PluginConfig.scriptOptionMinLoggingLevel());
        this.permissionDefault = PermissionDefault.getByName(PluginConfig.scriptOptionPermissionDefault());
        this.permissions = Permission.loadPermissions(PluginConfig.scriptOptionPermissions(), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
    }

    /**
     * Initialize a new ScriptOptions using the appropriate values in the script_options.yml file, using the script name to search for the values.
     * @param scriptName The name of the script whose script options should be initialized
     */
    public ScriptOptions(String scriptName) throws InvalidConfigurationException {
        if (ScriptOptionsConfig.contains(scriptName)) {
            this.enabled = ScriptOptionsConfig.getEnabled(scriptName, PluginConfig.scriptOptionEnabled());
            this.loadPriority = ScriptOptionsConfig.getLoadPriority(scriptName, PluginConfig.scriptOptionLoadPriority());
            this.pluginDepend = ScriptOptionsConfig.getPluginDepend(scriptName, PluginConfig.scriptOptionPluginDepend());
            this.fileLoggingEnabled = ScriptOptionsConfig.getFileLoggingEnabled(scriptName, PluginConfig.scriptOptionFileLoggingEnabled());
            this.minLoggingLevel = Level.parse(ScriptOptionsConfig.getMinLoggingLevel(scriptName, PluginConfig.scriptOptionMinLoggingLevel()));
            this.permissionDefault = PermissionDefault.getByName(ScriptOptionsConfig.getPermissionDefault(scriptName, PluginConfig.scriptOptionPermissionDefault()));
            this.permissions = Permission.loadPermissions(ScriptOptionsConfig.getPermissions(scriptName, PluginConfig.scriptOptionPermissions()), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
        } else {
            this.enabled = PluginConfig.scriptOptionEnabled();
            this.loadPriority = PluginConfig.scriptOptionLoadPriority();
            this.pluginDepend = PluginConfig.scriptOptionPluginDepend();
            this.fileLoggingEnabled = PluginConfig.scriptOptionFileLoggingEnabled();
            this.minLoggingLevel = Level.parse(PluginConfig.scriptOptionMinLoggingLevel());
            this.permissionDefault = PermissionDefault.getByName(PluginConfig.scriptOptionPermissionDefault());
            this.permissions = Permission.loadPermissions(PluginConfig.scriptOptionPermissions(), "Permission node '%s' in config.yml for default script permissions is invalid", permissionDefault);
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
        return String.format("ScriptOptions[Enabled: %b, Load Priority: %d, Plugin Dependencies: %s, File Logging Enabled: %b, Minimum Logging Level: %s, Permission Default: %s, Permissions: %s", enabled, loadPriority, pluginDepend, fileLoggingEnabled, minLoggingLevel, permissionDefault, printPermissions());
    }

    private List<String> printPermissions() {
        List<String> toReturn = new ArrayList<>();
        for (Permission permission : permissions) {
            toReturn.add(String.format("Permission[Name: %s, Description: %s, Default: %s, Children: %s]", permission.getName(), permission.getDescription(), permission.getDefault(), permission.getChildren()));
        }
        return toReturn;
    }
}
