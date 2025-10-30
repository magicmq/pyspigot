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

package dev.magicmq.pyspigot.velocity.config;


import dev.magicmq.pyspigot.config.ProjectOptionsConfig;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class VelocityProjectOptionsConfig implements ProjectOptionsConfig {

    private ConfigurationNode config;

    public VelocityProjectOptionsConfig(Path configPath) {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
        try {
            this.config = loader.load();
        } catch (ConfigurateException e) {
            PyVelocity.get().getPlatformLogger().error("An error occurred when attempting to load the project.yml file", e);
            try {
                this.config = YamlConfigurationLoader.builder().buildAndLoadString("");
            } catch (ConfigurateException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public boolean contains(String key) {
        return config.hasChild(key);
    }

    @Override
    public String getMainScript(String defaultValue) {
        return config.node("main").getString(defaultValue);
    }

    @Override
    public boolean getEnabled(boolean defaultValue) {
        return config.node("enabled").getBoolean(defaultValue);
    }

    @Override
    public boolean getAutoLoad(boolean defaultValue) {
        return config.node("auto-load").getBoolean(defaultValue);
    }

    @Override
    public int getLoadPriority(int defaultValue) {
        return config.node("load-priority").getInt(defaultValue);
    }

    @Override
    public List<String> getPluginDepend(List<String> defaultValue) {
        try {
            return config.node("plugin-depend").getList(String.class, defaultValue);
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching plugin dependencies from project.yml", e);
            return defaultValue;
        }
    }

    @Override
    public boolean getFileLoggingEnabled(boolean defaultValue) {
        return config.node("file-logging-enabled").getBoolean();
    }

    @Override
    public String getMinLoggingLevel(String defaultValue) {
        return config.node("min-logging-level").getString(defaultValue);
    }

    /**
     * No-op implementation
     */
    @Override
    public String getPermissionDefault(String defaultValue) {
        //Plugin permissions are not implemented in Velocity
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    public Map<String, Object> getPermissions(Map<String, Object> defaultValue) {
        //Plugin permissions are not implemented in Velocity
        return null;
    }
}
