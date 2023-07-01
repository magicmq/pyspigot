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

package dev.magicmq.pyspigot.manager.config;

import dev.magicmq.pyspigot.PySpigot;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

/**
 * Manager for scripts to interface with configuration files. Primarily used by scripts to load, write to, and save .yml files.
 */
public class ConfigManager {

    private static ConfigManager manager;

    private final File configFolder;

    private ConfigManager() {
        configFolder = new File(PySpigot.get().getDataFolder(), "configs");
        if (!configFolder.exists())
            configFolder.mkdir();
    }

    /**
     * Load a config file with the given name.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param fileName The name of the config file to load
     * @return A {@link ScriptConfig} representing the config file that was loaded
     * @throws IOException If there was an IOException when loading the config
     * @throws InvalidConfigurationException If there was an InvalidConfigurationException when loading the config
     */
    public ScriptConfig loadConfig(String fileName) throws IOException, InvalidConfigurationException {
        File configFile = new File(configFolder, fileName);

        configFile.createNewFile();

        return ScriptConfig.loadConfig(configFile);
    }

    /**
     * Reload an already loaded ScriptConfig.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param config The {@link ScriptConfig} to reload
     * @return A new {@link ScriptConfig} representing the reloaded config file
     * @throws IOException If there was an IOException when reloading the config
     * @throws InvalidConfigurationException If there was an InvalidConfigurationException when reloading the config
     */
    public ScriptConfig reloadConfig(ScriptConfig config) throws IOException, InvalidConfigurationException {
        File configFile = new File(configFolder, config.getConfigFile().getName());

        return ScriptConfig.loadConfig(configFile);
    }

    /**
     * Get the folder where script config files are stored.
     * @return The folder where script config files are stored
     */
    public File getConfigFolder() {
        return configFolder;
    }

    /**
     * Get the singleton instance of this ConfigManager
     * @return The instance
     */
    public static ConfigManager get() {
        if (manager == null)
            manager = new ConfigManager();
        return manager;
    }

}
