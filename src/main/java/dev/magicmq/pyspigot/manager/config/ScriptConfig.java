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

    private String defaults;

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
     * Set the defaults for this configuration.
     * @param defaults A YAML-formatted string containing the desired default values for the configuration
     */
    public void setDefaults(String defaults) {
        this.defaults = defaults;
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
