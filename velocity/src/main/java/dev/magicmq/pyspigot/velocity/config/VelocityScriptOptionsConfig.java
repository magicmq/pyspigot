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


import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.util.StringUtils;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VelocityScriptOptionsConfig implements ScriptOptionsConfig {

    private ConfigurationNode config;

    @Override
    public void reload() {
        File file = new File(PyCore.get().getDataFolder(), "script_options.yml");
        if (!file.exists()) {
            PyCore.get().saveResource("script_options.yml", false);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().file(file).build();
        try {
            this.config = loader.load();
        } catch (ConfigurateException e) {
            PyVelocity.get().getPlatformLogger().error("An error occurred when attempting to load the script_options.yml file", e);
            try {
                this.config = YamlConfigurationLoader.builder().buildAndLoadString("");
            } catch (ConfigurateException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public boolean contains(String key) {
        return config.hasChild(key) || config.hasChild(StringUtils.stripFileExtension(key));
    }

    @Override
    public boolean getEnabled(String scriptName, boolean defaultValue) {
        if (config.hasChild(scriptName))
            return config.node(scriptName).node("enabled").getBoolean(defaultValue);
        else
            return config.node(StringUtils.stripFileExtension(scriptName)).node("enabled").getBoolean(defaultValue);
    }

    @Override
    public int getLoadPriority(String scriptName, int defaultValue) {
        if (config.hasChild(scriptName))
            return config.node(scriptName).node("load-priority").getInt(defaultValue);
        else
            return config.node(StringUtils.stripFileExtension(scriptName)).node("load-priority").getInt(defaultValue);
    }

    @Override
    public List<String> getPluginDepend(String scriptName, List<String> defaultValue) {
        try {
            if (config.hasChild(scriptName))
                return config.node(scriptName).node("plugin-depend").getList(String.class, defaultValue);
            else
                return config.node(StringUtils.stripFileExtension(scriptName)).node("plugin-depend").getList(String.class, defaultValue);
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching plugin dependencies from script_options.yml", e);
            return List.of();
        }
    }

    @Override
    public boolean getFileLoggingEnabled(String scriptName, boolean defaultValue) {
        if (config.hasChild(scriptName))
            return config.node(scriptName).node("file-logging-enabled").getBoolean(defaultValue);
        else
            return config.node(StringUtils.stripFileExtension(scriptName)).node("file-logging-enabled").getBoolean(defaultValue);
    }

    @Override
    public String getMinLoggingLevel(String scriptName, String defaultValue) {
        if (config.hasChild(scriptName))
            return config.node(scriptName).node("min-logging-level").getString(defaultValue);
        else
            return config.node(StringUtils.stripFileExtension(scriptName)).node("min-logging-level").getString(defaultValue);
    }

    /**
     * No-op implementation
     */
    @Override
    public String getPermissionDefault(String scriptName, String defaultValue) {
        //Plugin permissions are not implemented in Velocity
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    public Map<String, Object> getPermissions(String scriptName, Map<String, Object> defaultValue) {
        //Plugin permissions are not implemented in Velocity
        return null;
    }
}
