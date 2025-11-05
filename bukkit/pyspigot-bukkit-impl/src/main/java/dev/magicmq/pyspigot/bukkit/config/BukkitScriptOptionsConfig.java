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

package dev.magicmq.pyspigot.bukkit.config;


import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.util.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Bukkit-specific implementation of the {@link dev.magicmq.pyspigot.config.ScriptOptionsConfig} class, for retrieving values from the script_options.yml file.
 */
public class BukkitScriptOptionsConfig implements ScriptOptionsConfig {

    private FileConfiguration config;

    @Override
    public void reload() {
        File file = new File(PyCore.get().getDataFolder(), "script_options.yml");
        if (!file.exists()) {
            PyCore.get().saveResource("script_options.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public boolean contains(String key) {
        return config.contains(key) || config.contains(StringUtils.stripFileExtension(key));
    }

    @Override
    public boolean getEnabled(String scriptName, boolean defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getBoolean("enabled", defaultValue);
    }

    @Override
    public boolean getAutoLoad(String scriptName, boolean defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getBoolean("auto-load", defaultValue);
    }

    @Override
    public int getLoadPriority(String scriptName, int defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getInt("load-priority", defaultValue);
    }

    @Override
    public List<String> getPluginDepend(String scriptName, List<String> defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        if (scriptSection.contains("plugin-depend"))
            return scriptSection.getStringList("plugin-depend");
        else
            return defaultValue;
    }

    @Override
    public boolean getFileLoggingEnabled(String scriptName, boolean defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getBoolean("file-logging-enabled", defaultValue);
    }

    @Override
    public String getMinLoggingLevel(String scriptName, String defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getString("min-logging-level", defaultValue);
    }

    @Override
    public String getPermissionDefault(String scriptName, String defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getString("permission-default", defaultValue);
    }

    @Override
    public Map<String, Object> getPermissions(String scriptName, Map<String, Object> defaultValue) {
        ConfigurationSection scriptSection = config.getConfigurationSection(scriptName);
        if (scriptSection == null)
            scriptSection = config.getConfigurationSection(StringUtils.stripFileExtension(scriptName));
        if (scriptSection.contains("permissions"))
            return getNestedMap(scriptSection.getConfigurationSection("permissions"));
        else
            return defaultValue;
    }

    private Map<String, Object> getNestedMap(ConfigurationSection section) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection)
                result.put(key, getNestedMap((ConfigurationSection) value));
            else
                result.put(key, value);
        }
        return result;
    }
}
