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
 * Loads and provides the project.yml file for accessing a project's options.
 */
public interface ProjectOptionsConfig {

    /**
     * Check if the project.yml file contains a particular key.
     * @param key The key to check
     * @return True if the key exists, false if it does not
     */
    boolean contains(String key);

    String getMainScript(String defaultValue);

    /**
     * Get if the project is enabled.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return True if the project is enabled, false if it is not, or the default value if not explicitly defined
     */
    boolean getEnabled(boolean defaultValue);

    /**
     * Get the load priority for the project.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return The load priority of the project, or the default value if not explicitly defined
     */
    int getLoadPriority(int defaultValue);

    /**
     * Get the plugin dependencies for the project.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return A String list of plugin dependencies for the project, or the default value if not explicitly defined
     */
    List<String> getPluginDepend(List<String> defaultValue);

    /**
     * Get if file logging is enabled for the project.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return True if the project has file logging enabled, flase if it does not, or the default value if not explicitly defined
     */
    boolean getFileLoggingEnabled(boolean defaultValue);

    /**
     * Get the minimum logging level for the project.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return The minimum logging level for the project, or the default value if not explicitly defined
     */
    String getMinLoggingLevel(String defaultValue);

    /**
     * Get the default permission level for the project.
     * @param defaultValue The default value if the project.yml does not have this script option defined
     * @return The default permission level for the project, or the default value if not explicitly defined
     */
    String getPermissionDefault(String defaultValue);

    /**
     * Get a section (formatted in the same way as a spigot plugin.yml) representing the permissions for the project.
     * @param defaultValue The default value if the project.yml does not have this option defined
     * @return A nested Map representing the project's permissions, or the default value if not explicitly defined
     */
    Map<String, Object> getPermissions(Map<String, Object> defaultValue);

}
