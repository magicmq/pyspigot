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

package dev.magicmq.pyspigot.bukkit.manager.config;

import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.config.ScriptConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A class representing a script configuration file, for the Bukkit implementation.
 * @see org.bukkit.configuration.file.YamlConfiguration
 */
public class BukkitScriptConfig extends YamlConfiguration implements ScriptConfig {

    private final File configFile;
    private final String defaults;

    /**
     *
     * @param configFile The configuration file
     * @param defaults A YAML-formatted string containing the desired default values for the configuration
     */
    public BukkitScriptConfig(File configFile, String defaults) {
        this.configFile = configFile;
        this.defaults = defaults;
    }

    @Override
    public File getConfigFile() {
        return configFile;
    }

    @Override
    public Path getConfigPath() {
        return Paths.get(configFile.getAbsolutePath());
    }

    @Override
    public void load() throws IOException {
        try {
            super.load(configFile);
            if (defaults != null) {
                YamlConfiguration defaultConfig = new YamlConfiguration();
                defaultConfig.loadFromString(defaults);
                this.setDefaults(defaultConfig);
            }
        } catch (InvalidConfigurationException e) {
            throw new ScriptRuntimeException("Unhandled exception when loading configuration '" + configFile.getName() + "'", e);
        }
    }

    @Override
    public void reload() throws IOException {
        load();
    }

    @Override
    public void save() throws IOException {
        this.save(configFile);
        reload();
    }

    /**
     * Sets the specified path to the given value only if the path is not already set in the config file. Any specified default values are ignored when checking if the path is set.
     * @see org.bukkit.configuration.ConfigurationSection#set(String, Object)
     * @param path Path of the object to set
     * @param value Value to set the path to
     * @return True if the path was set to the value (in other words the path was not previously set), false if the path was not set to the value (in other words the path was already previously set)
     */
    public boolean setIfNotExists(String path, Object value) {
        if (!super.isSet(path)) {
            super.set(path, value);
            return true;
        }
        return false;
    }
}
