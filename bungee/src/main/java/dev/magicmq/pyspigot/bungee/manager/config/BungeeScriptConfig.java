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

package dev.magicmq.pyspigot.bungee.manager.config;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * A class representing a script configuration file, for the BungeeCord implementation.
 * @see net.md_5.bungee.config.Configuration
 */
public class BungeeScriptConfig {

    private final File configFile;
    private final String defaults;

    private Configuration config;

    /**
     *
     * @param configFile The configuration file
     * @param defaults A YAML-formatted string containing the desired default values for the configuration
     */
    public BungeeScriptConfig(File configFile, String defaults) {
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
     * @see net.md_5.bungee.config.Configuration#set(String, Object)
     * @param path Path of the object to set
     * @param value Value to set the path to
     * @return True if the path was set to the value (in other words the path was not previously set), false if the path was not set to the value (in other words the path was already previously set)
     */
    public boolean setIfNotExists(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
            return true;
        }
        return false;
    }

    /**
     * Loads the config from the configuration file. Will also set defaults for the configuration, if they were specified.
     * @throws IOException If there was an exception when loading the file
     */
    public void load() throws IOException {
        if (defaults != null) {
            Configuration defaultConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(defaults);
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile, defaultConfig);
        } else {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        }
    }

    /**
     * Reload the configuration. Will read all changes made to the configuration file since the configuration was last loaded/reloaded.
     * @throws IOException If there was an exception when loading the file
     */
    public void reload() throws IOException {
        load();
    }

    /**
     * Save the configuration to its associated file. For continuity purposes, the configuration is also reloaded from the file after saving.
     * @throws IOException If there is an IOException when saving the file
     */
    public void save() throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        reload();
    }

    /*------------------------------- Passthrough methods for consistency with Bukkit implementation -------------------------------*/

    public <T> T get(String path, T def) {
        return config.get(path, def);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public Object getDefault(String path) {
        return config.getDefault(path);
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }

    public Configuration getSection(String path) {
        return config.getSection(path);
    }

    public Collection<String> getKeys() {
        return config.getKeys();
    }

    public byte getByte(String path) {
        return config.getByte(path);
    }

    public byte getByte(String path, byte def) {
        return config.getByte(path, def);
    }

    public List<Byte> getByteList(String path) {
        return config.getByteList(path);
    }

    public short getShort(String path) {
        return config.getShort(path);
    }

    public short getShort(String path, short def) {
        return config.getShort(path, def);
    }

    public List<Short> getShortList(String path) {
        return config.getShortList(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public List<Integer> getIntList(String path) {
        return config.getIntList(path);
    }

    public long getLong(String path) {
        return config.getLong(path);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public List<Long> getLongList(String path) {
        return config.getLongList(path);
    }

    public float getFloat(String path) {
        return config.getFloat(path);
    }

    public float getFloat(String path, float def) {
        return config.getFloat(path, def);
    }

    public List<Float> getFloatList(String path) {
        return config.getFloatList(path);
    }

    public double getDouble(String path) {
        return config.getDouble(path);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public List<Double> getDoubleList(String path) {
        return config.getDoubleList(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public List<Boolean> getBooleanList(String path) {
        return config.getBooleanList(path);
    }

    public char getChar(String path) {
        return config.getChar(path);
    }

    public char getChar(String path, char def) {
        return config.getChar(path, def);
    }

    public List<Character> getCharList(String path) {
        return config.getCharList(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public List<?> getList(String path) {
        return config.getList(path);
    }

    public List<?> getList(String path, List<?> def) {
        return config.getList(path, def);
    }
}
