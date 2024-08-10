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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Manager for scripts to interface with configuration files. Primarily used by scripts to load, write to, and save .yml files.
 */
public class ConfigManager {

    private static ConfigManager manager;

    private final Path configFolder;

    private ConfigManager() {
        configFolder = PySpigot.get().getDataFolderPath().resolve("configs");
        if (!Files.exists(configFolder)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException exception) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Error when creating configs folder for script config files", exception);
            }
        }
    }

    /**
     * Check if a config file exists with the given path/name, relative to the {@code configs} folder.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param filePath The path of the config file to check, can be either the file name alone or a path (containing subfolders)
     * @return True if the file exists, false if it does not
     */
    public boolean doesConfigExist(String filePath) {
        Path configFile = configFolder.resolve(filePath);
        return Files.exists(configFile);
    }

    /**
     * Load a config file with the given path/name, relative to the {@code configs} folder. If the config file exists, it will load the existing file. If the config file does not exist, a new file will be created with the given path/name.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param filePath The path of the config file to load, can be either the file name alone or a path (containing subfolders)
     * @return A {@link ScriptConfig} representing the config file that was loaded
     * @throws IOException If there was an IOException when attempting to load the config
     * @throws org.bukkit.configuration.InvalidConfigurationException If there was an error when parsing the loaded file (invalid configuration)
     */
    public ScriptConfig loadConfig(String filePath) throws IOException, InvalidConfigurationException {
        Path configFile = configFolder.resolve(Paths.get(filePath));

        if (!Files.exists(configFile)) {
            Files.createDirectories(configFile.getParent());
            Files.createFile(configFile);
        }

        return ScriptConfig.loadConfig(configFile.toFile());
    }

    /**
     * Reload an already loaded ScriptConfig.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param config The {@link ScriptConfig} to reload
     * @return A new {@link ScriptConfig} representing the reloaded config file
     * @throws IOException If there was an IOException when attempting to reload the config
     * @throws org.bukkit.configuration.InvalidConfigurationException If there was an error when parsing the loaded file (invalid configuration)
     */
    public ScriptConfig reloadConfig(ScriptConfig config) throws IOException, InvalidConfigurationException {
        Path configFile = config.getConfigPath();

        return ScriptConfig.loadConfig(configFile.toFile());
    }

    /**
     * Delete a config file with the given path/name.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param filePath The path of the config file to delete, relative to the {@code configs} folder. Can be either the file name alone or a path (containing subfolders)
     * @return True if the file was deleted, false if the file could not be deleted because it does not exist
     * @throws IOException If there was an IOException when attempting to delete the file
     */
    public boolean deleteConfig(String filePath) throws IOException {
        Path configFile = configFolder.resolve(Paths.get(filePath));
        return Files.deleteIfExists(configFile);
    }

    /**
     * Get the path of the folder where script config files are stored.
     * @return The path of the folder where script config files are stored
     */
    public Path getConfigFolder() {
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
