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


import dev.magicmq.pyspigot.config.ProjectOptionsConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Bukkit-specific implementation of the {@link dev.magicmq.pyspigot.config.ProjectOptionsConfig} class, for retrieving values from a project's project.yml file.
 */
public class BukkitProjectOptionsConfig implements ProjectOptionsConfig {

    private final FileConfiguration config;

    public BukkitProjectOptionsConfig(Path configPath) {
        this.config = YamlConfiguration.loadConfiguration(configPath.toFile());
    }

    @Override
    public boolean contains(String key) {
        return config.contains(key);
    }

    @Override
    public String getMainScript(String defaultValue) {
        return config.getString("main", defaultValue);
    }

    @Override
    public boolean getEnabled(boolean defaultValue) {
        return config.getBoolean("enabled", defaultValue);
    }

    @Override
    public int getLoadPriority(int defaultValue) {
        return config.getInt("load-priority", defaultValue);
    }

    @Override
    public List<String> getPluginDepend(List<String> defaultValue) {
        if (config.contains("plugin-depend"))
            return config.getStringList("plugin-depend");
        else
            return defaultValue;
    }

    @Override
    public boolean getFileLoggingEnabled(boolean defaultValue) {
        return config.getBoolean("file-logging-enabled", defaultValue);
    }

    @Override
    public String getMinLoggingLevel(String defaultValue) {
        return config.getString("min-logging-level", defaultValue);
    }

    @Override
    public String getPermissionDefault(String defaultValue) {
        return config.getString("permission-default", defaultValue);
    }

    @Override
    public Map<String, Object> getPermissions(Map<String, Object> defaultValue) {
        if (config.contains("permissions"))
            return getNestedMap(config.getConfigurationSection("permissions"));
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
