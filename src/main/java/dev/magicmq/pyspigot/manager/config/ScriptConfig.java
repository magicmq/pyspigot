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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class representing a script configuration file.
 * @see org.bukkit.configuration.file.YamlConfiguration
 */
public class ScriptConfig extends YamlConfiguration {

    private final File configFile;

    private final String defaults;

    /**
     *
     * @param configFile The configuration file
     * @param defaults A YAML-formatted string containing the desired default values for the configuration
     */
    public ScriptConfig(File configFile, String defaults) {
        this.configFile = configFile;
        this.defaults = defaults;
    }

    /**
     * Get the file associated with this configuration.
     * @return The file associated with this configuration
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Get the absolute path of the file associated with this configuration.
     * @return The path of the file
     */
    public Path getConfigPath() {
        return Paths.get(configFile.getAbsolutePath());
    }

    /**
     * Sets the specified path to the given value only if the path is not already set in the config file. Any specified default values are ignored when checking if the path is set.
     * @see org.bukkit.configuration.ConfigurationSection#set(String, Object)
     * @param path Path of the object to set
     * @param value Value to set the path to
     * @return True if the path was set to the value (in other words the path was not previously set), false if the path was not set to the value (in other words the path was already previously set)
     */
    public boolean setIfNotExists(String path, Object value) {
        if (!isSet(path)) {
            super.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Loads the config from the configuration file. Will also set defaults for the configuration, if they were specified.
     * @throws IOException If there was an exception when loading the file
     * @throws org.bukkit.configuration.InvalidConfigurationException If there was an error when parsing the loaded file (invalid configuration)
     */
    public void load() throws IOException, InvalidConfigurationException {
        super.load(configFile);
        if (defaults != null) {
            YamlConfiguration defaultConfig = new YamlConfiguration();
            defaultConfig.loadFromString(defaults);
            this.setDefaults(defaultConfig);
        }
    }

    /**
     * Reload the configuration. Will read all changes made to the configuration file since the configuration was last loaded/reloaded.
     * @throws IOException If there was an exception when loading the file
     * @throws org.bukkit.configuration.InvalidConfigurationException If there was an error when parsing the loaded file (invalid configuration)
     */
    public void reload() throws IOException, InvalidConfigurationException {
        load();
    }

    /**
     * Save the configuration to its associated file. For continuity purposes, the configuration is also reloaded from the file after saving.
     * @throws IOException If there is an IOException when saving the file
     * @throws org.bukkit.configuration.InvalidConfigurationException If there was an error when parsing the file when reloading (invalid configuration)
     */
    public void save() throws IOException, InvalidConfigurationException {
        this.save(configFile);
        reload();
    }
}
