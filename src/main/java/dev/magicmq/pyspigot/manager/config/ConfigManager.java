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
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Load a config file with the given name.
     * @param fileName The name of the config file to load
     * @return A {@link ScriptConfig} representing the config file that was loaded
     */
    public ScriptConfig loadConfig(String fileName) {
        File configFile = new File(configFolder, fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return ScriptConfig.loadConfig(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Reload an already loaded ScriptConfig.
     * @param config The {@link ScriptConfig} to reload
     * @return A new {@link ScriptConfig} representing the reloaded config file
     */
    public ScriptConfig reloadConfig(ScriptConfig config) {
        File configFile = new File(configFolder, config.getConfigFile().getName());

        try {
            return ScriptConfig.loadConfig(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the folder where script config files are stored.
     * @return The folder where script config files are stored
     */
    public File getConfigFolder() {
        return configFolder;
    }

    /**
     * Get the instance of this ConfigManager
     * @return The instance
     */
    public static ConfigManager get() {
        if (manager == null)
            manager = new ConfigManager();
        return manager;
    }

}
