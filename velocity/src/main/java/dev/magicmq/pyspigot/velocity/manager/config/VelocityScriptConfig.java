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

package dev.magicmq.pyspigot.velocity.manager.config;


import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.config.ScriptConfig;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class VelocityScriptConfig implements ScriptConfig {

    private final Path configPath;
    private final YamlConfigurationLoader loader;
    private final String defaults;

    private ConfigurationNode config;
    private ConfigurationNode defaultConfig;

    public VelocityScriptConfig(Path configPath, String defaults) {
        this.configPath = configPath;
        this.loader = YamlConfigurationLoader.builder().path(configPath).build();
        this.defaults = defaults != null ? defaults : "";
    }


    @Override
    public File getConfigFile() {
        return configPath.toFile();
    }

    @Override
    public Path getConfigPath() {
        return configPath;
    }

    @Override
    public void load() throws ConfigurateException {
        defaultConfig = YamlConfigurationLoader.builder().buildAndLoadString(defaults);
        config = loader.load();
        config.mergeFrom(defaultConfig);
    }

    @Override
    public void reload() throws ConfigurateException {
        load();
    }

    @Override
    public void save() throws ConfigurateException {
        loader.save(config);
    }

    @Override
    public boolean setIfNotExists(String path, Object value) {
        if (!contains(path)) {
            try {
                set(path, value);
                return true;
            } catch (SerializationException e) {
                throw new ScriptRuntimeException("Error when setting value '" + value + "' to path '" + path + "'");
            }
        }
        return false;
    }

    /**
     * Gets the underlying BungeeCord {@link org.spongepowered.configurate.ConfigurationNode} object.
     * @return The underlying config object
     */
    public ConfigurationNode getUnderlyingConfig() {
        return config;
    }

    public ConfigurationNode getDefaultConfig() {
        return defaultConfig;
    }

    /*------------------------------- Passthrough methods for consistency with Bukkit implementation -------------------------------*/

    public <T> T get(String path, T def) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get((Class<T>) def.getClass());
        else
            return getNodeFromPath(defaultConfig, path).get((Class<T>) def.getClass(), def);
    }

    public boolean contains(String path) {
        return !getNodeFromPath(config, path).virtual();
    }

    public Object get(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Object.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Object.class);
    }

    public void set(String path, Object value) throws SerializationException {
        getNodeFromPath(config, path).set(value);
    }

    public ConfigurationNode getSection(String path) {
        if (contains(path))
            return getNodeFromPath(config, path);
        else
            return getNodeFromPath(defaultConfig, path);
    }

    public List<String> getKeys() {
        return config.childrenList().stream().map(node -> (String) node.key()).toList();
    }

    public byte getByte(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Byte.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Byte.class);
    }

    public byte getByte(String path, byte def) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Byte.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Byte.class, def);
    }

    public List<Byte> getByteList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Byte.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Byte.class);
    }

    public short getShort(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Short.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Short.class);
    }

    public short getShort(String path, short def) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Short.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Short.class, def);
    }

    public List<Short> getShortList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Short.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Short.class);
    }

    public int getInt(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getInt();
        else
            return getNodeFromPath(defaultConfig, path).getInt();
    }

    public int getInt(String path, int def) {
        if (contains(path))
            return getNodeFromPath(config, path).getInt();
        else
            return getNodeFromPath(defaultConfig, path).getInt(def);
    }

    public List<Integer> getIntList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Integer.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Integer.class);
    }

    public long getLong(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getLong();
        else
            return getNodeFromPath(defaultConfig, path).getLong();
    }

    public long getLong(String path, long def) {
        if (contains(path))
            return getNodeFromPath(config, path).getLong();
        else
            return getNodeFromPath(defaultConfig, path).getLong(def);
    }

    public List<Long> getLongList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Long.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Long.class);
    }

    public float getFloat(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getFloat();
        else
            return getNodeFromPath(defaultConfig, path).getFloat();
    }

    public float getFloat(String path, float def) {
        if (contains(path))
            return getNodeFromPath(config, path).getFloat();
        else
            return getNodeFromPath(defaultConfig, path).getFloat(def);
    }

    public List<Float> getFloatList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Float.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Float.class);
    }

    public double getDouble(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getDouble();
        else
            return getNodeFromPath(defaultConfig, path).getDouble();
    }

    public double getDouble(String path, double def) {
        if (contains(path))
            return getNodeFromPath(config, path).getDouble();
        else
            return getNodeFromPath(defaultConfig, path).getDouble(def);
    }

    public List<Double> getDoubleList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Double.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Double.class);
    }

    public boolean getBoolean(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getBoolean();
        else
            return getNodeFromPath(defaultConfig, path).getBoolean();
    }

    public boolean getBoolean(String path, boolean def) {
        if (contains(path))
            return getNodeFromPath(config, path).getBoolean();
        else
            return getNodeFromPath(defaultConfig, path).getBoolean(def);
    }

    public List<Boolean> getBooleanList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Boolean.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Boolean.class);
    }

    public char getChar(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Character.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Character.class);
    }

    public char getChar(String path, char def) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).get(Character.class);
        else
            return getNodeFromPath(defaultConfig, path).get(Character.class, def);
    }

    public List<Character> getCharList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Character.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Character.class);
    }

    public String getString(String path) {
        if (contains(path))
            return getNodeFromPath(config, path).getString();
        else
            return getNodeFromPath(defaultConfig, path).getString();
    }

    public String getString(String path, String def) {
        if (contains(path))
            return getNodeFromPath(config, path).getString();
        else
            return getNodeFromPath(defaultConfig, path).getString(def);
    }

    public List<String> getStringList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(String.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(String.class);
    }

    public List<Object> getList(String path) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Object.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Object.class);
    }

    public List<Object> getList(String path, List<Object> def) throws SerializationException {
        if (contains(path))
            return getNodeFromPath(config, path).getList(Object.class);
        else
            return getNodeFromPath(defaultConfig, path).getList(Object.class, def);
    }

    private ConfigurationNode getNodeFromPath(ConfigurationNode config ,String path) {
        String[] nodes = path.split("\\.");
        if (nodes.length == 1)
            return config.node(nodes[0]);
        return config.node((Object[]) nodes);
    }
}
