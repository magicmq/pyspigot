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

package dev.magicmq.pyspigot.config;


import java.util.List;
import java.util.Map;

/**
 * Loads and provides the script_options.yml file for accessing script options.
 */
public interface ScriptOptionsConfig {

    /**
     * Reload the script_options.yml
     */
    void reload();

    /**
     * Check if the script_options.yml file contains a particular key. Used to check if a script has any script options defined.
     * @param key The key (typically a script name) to check
     * @return True if the key exists, false if it does not
     */
    boolean contains(String key);

    /**
     * Get if a script is enabled.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return True if the script is enabled, false if it does not, or the default value if not explicitly defined
     */
    boolean getEnabled(String scriptName, boolean defaultValue);

    /**
     * Get if a script should be automatically loaded on server start, plugin enable, or plugin reload.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return True if the script should be utomatically loaded, false if it should not
     */
    boolean getAutoLoad(String scriptName, boolean defaultValue);

    /**
     * Get the load priority for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return The load priority of the script, or the default value if not explicitly defined
     */
    int getLoadPriority(String scriptName, int defaultValue);

    /**
     * Get the plugin dependencies for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return A String list of plugin dependencies for the script, or the default value if not explicitly defined
     */
    List<String> getPluginDepend(String scriptName, List<String> defaultValue);

    /**
     * Get if file logging is enabled for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return True if the script has file logging enabled, flase if it does not, or the default value if not explicitly defined
     */
    boolean getFileLoggingEnabled(String scriptName, boolean defaultValue);

    /**
     * Get the minimum logging level for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return The minimum logging level for the script, or the default value if not explicitly defined
     */
    String getMinLoggingLevel(String scriptName, String defaultValue);

    /**
     * Get the default permission level for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return The default permission level for the script, or the default value if not explicitly defined
     */
    String getPermissionDefault(String scriptName, String defaultValue);

    /**
     * Get a section (formatted in the same way as a spigot plugin.yml) representing the permissions for a script.
     * @param scriptName The name of the script
     * @param defaultValue The default value if the script does not have this script option defined
     * @return A nested Map representing the script's permissions, or the default value if not explicitly defined
     */
    Map<String, Object> getPermissions(String scriptName, Map<String, Object> defaultValue);

}
