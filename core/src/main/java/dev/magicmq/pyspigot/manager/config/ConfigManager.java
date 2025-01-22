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

package dev.magicmq.pyspigot.manager.config;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.util.ArgParser;
import org.python.core.PyObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Manager for scripts to interface with configuration files. Primarily used by scripts to load, write to, and save .yml files.
 */
public abstract class ConfigManager {

    private static ConfigManager instance;

    private final Path configFolder;

    protected ConfigManager() {
        instance = this;

        configFolder = PyCore.get().getDataFolderPath().resolve("configs");
        if (!Files.exists(configFolder)) {
            try {
                Files.createDirectories(configFolder);
            } catch (IOException exception) {
                PyCore.get().getLogger().log(Level.SEVERE, "Error when creating configs folder for script config files", exception);
            }
        }
    }

    protected abstract ScriptConfig loadConfigImpl(Path configFile, String defaults) throws IOException;

    /**
     * Load a configuration file with the given path/name, relative to the {@code configs} folder, with the specified defaults. If the configuration file exists, it will load the existing file. If the configuration file does not exist, a new file will be created with the given path/name.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code file_path} (Required): The path of the configuration file to load, can be either the file name alone or a path (containing subfolders)</li>
     * <li>{@code defaults} (Optional): A YAML-formatted string containing the desired default values for the configuration. If not specified, then no defaults will be used</li>
     * </ul>
     * @return A ScriptConfig representing the configuration file that was loaded
     * @throws IOException If there was an IOException when attempting to load the configuration
     */
    public ScriptConfig loadConfig(PyObject[] args, String[] keywords) throws IOException {
        ArgParser argParser = new ArgParser("loadConfig", args, keywords, new String[]{"file_path", "defaults"}, 1);
        String filePath = argParser.getString(0);
        String defaults = argParser.getString(1, null);

        return loadConfig(filePath, defaults);
    }

    /**
     * Load a configuration file with the given path/name, relative to the {@code configs} folder, with the specified defaults. If the configuration file exists, it will load the existing file. If the configuration file does not exist, a new file will be created with the given path/name.
     * @param filePath The path of the configuration file to load, can be either the file name alone or a path (containing subfolders)
     * @param defaults A YAML-formatted string containing the desired default values for the configuration
     * @return A ScriptConfig representing the configuration file that was loaded
     * @throws IOException If there was an IOException when attempting to load the configuration
     */
    public ScriptConfig loadConfig(String filePath, String defaults) throws IOException {
        Path configFile = createConfigIfNotExists(filePath);
        return loadConfigImpl(configFile, defaults);
    }

    /**
     * Check if a configuration file exists with the given path/name, relative to the {@code configs} folder.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code file_path} (Required): The path of the configuration file to check, can be either the file name alone or a path (containing subfolders)</li>
     * </ul>
     * @return True if the file exists, false if it does not
     */
    public boolean doesConfigExist(PyObject[] args, String[] keywords) {
        ArgParser argParser = new ArgParser("doesConfigExist", args, keywords, new String[]{"file_path"}, 1);
        String filePath = argParser.getString(0);

        return doesConfigExist(filePath);
    }

    /**
     * Check if a configuration file exists with the given path/name, relative to the {@code configs} folder.
     * @param filePath The path of the configuration file to check, can be either the file name alone or a path (containing subfolders)
     * @return True if the file exists, false if it does not
     */
    public boolean doesConfigExist(String filePath) {
        Path configFile = configFolder.resolve(filePath);
        return Files.exists(configFile);
    }

    /**
     * Creates a config file with the given path if it did not previously exist.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code file_path} (Required): The file path for the config file to create, relative to the {@code configs} folder. Can be either the file name alone or a path (containing subfolders)</li>
     * </ul>
     * @return The {@link Path} to the config file
     * @throws IOException If there was an IOException when attempting to create the config file
     */
    public Path createConfigFileIfNotExists(PyObject[] args, String[] keywords) throws IOException {
        ArgParser argParser = new ArgParser("createConfigFileIfNotExists", args, keywords, new String[]{"file_path"}, 1);
        String filePath = argParser.getString(0);

        return createConfigIfNotExists(filePath);
    }

    /**
     * Creates a config file with the given path if it did not previously exist.
     * @param filePath The file path for the config file to create, relative to the {@code configs} folder. Can be either the file name alone or a path (containing subfolders)
     * @return The {@link Path} to the config file
     * @throws IOException If there was an IOException when attempting to create the config file
     */
    public Path createConfigIfNotExists(String filePath) throws IOException {
        Path configFile = configFolder.resolve(Paths.get(filePath));

        if (!Files.exists(configFile)) {
            Files.createDirectories(configFile.getParent());
            Files.createFile(configFile);
        }

        return configFile;
    }

    /**
     * Delete a configuration file with the given path/name.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code file_path} (Required): The path of the configuration file to delete, relative to the {@code configs} folder. Can be either the file name alone or a path (containing subfolders)</li>
     * </ul>
     * @return True if the file was deleted, false if the file could not be deleted because it does not exist
     * @throws IOException If there was an IOException when attempting to delete the file
     */
    public boolean deleteConfig(PyObject[] args, String[] keywords) throws IOException {
        ArgParser argParser = new ArgParser("deleteConfig", args, keywords, new String[]{"file_path"}, 1);
        String filePath = argParser.getString(0);

        return deleteConfig(filePath);
    }

    /**
     * Delete a configuration file with the given path/name
     * @param filePath The path of the configuration file to delete, relative to the {@code configs} folder. Can be either the file name alone or a path (containing subfolders)
     * @return True if the file was deleted, false if the file could not be deleted because it does not exist
     * @throws IOException If there was an IOException when attempting to delete the file
     */
    public boolean deleteConfig(String filePath) throws IOException {
        Path configFile = configFolder.resolve(Paths.get(filePath));
        return Files.deleteIfExists(configFile);
    }

    /**
     * Get the path of the folder where script configuration files are stored.
     * @return The path of the folder where script configuration files are stored
     */
    public Path getConfigFolder() {
        return configFolder;
    }

    /**
     * Get the singleton instance of this ConfigManager.
     * @return The instance
     */
    public static ConfigManager get() {
        return instance;
    }

}
