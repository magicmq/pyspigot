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

package dev.magicmq.pyspigot;


import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.config.PluginConfig;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

/**
 * An adapter class that wraps platform-specific code for plugin initialization, shutdown, and other plugin-related activities.
 */
public interface PlatformAdapter {

    /**
     * Initialize the config file via a platform-specific implementation, saving the default config file in the process if it does not already exist.
     * @return A platform-specific implementation of the loaded plugin config.yml
     */
    PluginConfig initConfig();

    /**
     * Initializes the script options config file via a platform-specific implementation. A blank script_options.yml file is saved if no script_options.yml existed previously.
     * @return A platform-specific implementation of the loaded script_options.yml
     */
    ScriptOptionsConfig initScriptOptionsConfig();

    /**
     * Initializes adventure using the platform-specific adventure adapter.
     */
    void initAdventure();

    /**
     * Initialize plugin commands via a platform-specific implementation.
     */
    void initCommands();

    /**
     * Initialize plugin listeners via a platform-specific implementation.
     */
    void initListeners();

    /**
     * Initialize managers that require a platform-specific implementation.
     * <p>
     * Also initializes managers that are entirely specific to one platform (no core abstraction).
     */
    void initPlatformManagers();

    /**
     * Initialize the version-checking task via a platform-specific implementation.
     */
    void initVersionChecking();

    /**
     * Setup bStats metrics via a platform-specific implementation.
     */
    void setupMetrics();

    /**
     * Shutdown bStats metrics via a platform-specific implementation.
     */
    void shutdownMetrics();

    /**
     * Stop the version-checking task via a platform-specific implementation.
     */
    void shutdownVersionChecking();

    /**
     * Get the logger for the plugin via a platform-specific implementation.
     * @return The logger
     */
    Logger getPlatformLogger();

    /**
     * Get the data folder for the plugin via a platform-specific implementation.
     * @return The data folder
     */
    File getDataFolder();

    /**
     * Get the data folder path for the plugin via a platform-specific implementation.
     * @return The data folder path
     */
    Path getDataFolderPath();

    /**
     * Get the class loader for the plugin via a platform-specific implementation.
     * @return The class loader
     */
    ClassLoader getPluginClassLoader();

    /**
     * Get the plugin version via a platform-specific implementation.
     * @return The plugin version
     */
    String getVersion();

    /**
     * Get the appropriate plugin identifier, depending on the platform.
     * @return The plugin identifier
     */
    String getPluginIdentifier();

}
