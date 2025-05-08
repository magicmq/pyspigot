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

package dev.magicmq.pyspigot.bungee.config;

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.config.ProjectOptionsConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The Bungee-specific implementation of the {@link dev.magicmq.pyspigot.config.ProjectOptionsConfig} class, for retrieving values from a project's project.yml file.
 */
public class BungeeProjectOptionsConfig implements ProjectOptionsConfig {

    private Configuration config;

    public BungeeProjectOptionsConfig(Path configPath) {
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configPath.toFile());
        } catch (IOException e) {
            PyBungee.get().getPlatformLogger().error("Error when loading the project.yml file", e);
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load("");
        }
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

    /**
     * No-op implementation
     */
    @Override
    public String getPermissionDefault(String defaultValue) {
        //Plugin permissions are not implemented in BungeeCord
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    public Map<String, Object> getPermissions(Map<String, Object> defaultValue) {
        //Plugin permissions are not implemented in BungeeCord
        return null;
    }
}

