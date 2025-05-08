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


import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class VelocityPluginConfig implements PluginConfig {

    private ConfigurationNode config;

    private DateTimeFormatter logTimestamp;

    @Override
    public void reload() {
        try {
            URL defaultConfigUrl = PyVelocity.get().getPluginClassLoader().getResource("config.yml");
            YamlConfigurationLoader defaultLoader = YamlConfigurationLoader.builder().url(defaultConfigUrl).build();
            ConfigurationNode defaultConfig = defaultLoader.load();

            try {
                Path configPath = PyVelocity.get().getDataFolderPath().resolve("config.yml");
                YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
                config = loader.load();

                config.mergeFrom(defaultConfig);

                logTimestamp = DateTimeFormatter.ofPattern(config.node("log-timestamp-format").getString());
            } catch (ConfigurateException e) {
                PyVelocity.get().getPlatformLogger().error("There was an exception when loading the config.yml", e);
            }
        } catch (ConfigurateException e) {
            PyVelocity.get().getPlatformLogger().error("There was an exception when loading the default config.yml", e);
        }
    }

    @Override
    public boolean getMetricsEnabled() {
        return config.node("metrics-enabled").getBoolean();
    }

    @Override
    public long getScriptLoadDelay() {
        return config.node("script-load-delay").getLong();
    }

    @Override
    public HashMap<String, String> getLibraryRelocations() {
        HashMap<String, String> toReturn = new HashMap<>();
        try {
            for (String string : config.node("library-relocations").getList(String.class)) {
                String[] split = string.split("\\|");
                toReturn.put(split[0], split[1]);
            }
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching library relocations from config.yml", e);
        }
        return toReturn;
    }

    @Override
    public DateTimeFormatter getLogTimestamp() {
        return logTimestamp;
    }

    @Override
    public boolean doScriptActionLogging() {
        return config.node("script-action-logging").getBoolean();
    }

    @Override
    public boolean doVerboseRedisLogging() {
        return config.node("verbose-redis-logging").getBoolean();
    }

    @Override
    public boolean doScriptUnloadOnPluginDisable() {
        return config.node("script-unload-on-plugin-disable").getBoolean();
    }

    @Override
    public String scriptOptionMainScript() {
        return config.node("script-option-defaults").node("main").getString();
    }

    @Override
    public boolean scriptOptionEnabled() {
        return config.node("script-option-defaults").node("enabled").getBoolean();
    }

    @Override
    public int scriptOptionLoadPriority() {
        return config.node("script-option-defaults").node("load-priority").getInt();
    }

    @Override
    public List<String> scriptOptionPluginDepend() {
        try {
            return config.node("script-option-defaults").node("plugin-depend").getList(String.class);
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching plugin dependencies from config.yml", e);
            return List.of();
        }
    }

    @Override
    public boolean scriptOptionFileLoggingEnabled() {
        return config.node("script-option-defaults").node("file-logging-enabled").getBoolean();
    }

    @Override
    public String scriptOptionMinLoggingLevel() {
        return config.node("script-option-defaults").node("min-logging-level").getString();
    }

    @Override
    public String scriptOptionPermissionDefault() {
        return config.node("script-option-defaults").node("permission-default").getString();
    }

    /**
     * No-op implementation
     */
    @Override
    public Map<String, Object> scriptOptionPermissions() {
        //Plugin permissions are not implemented in Velocity
        return null;
    }

    @Override
    public boolean shouldShowUpdateMessages() {
        return config.node("debug-options").node("show-update-messages").getBoolean();
    }

    @Override
    public String jythonLoggingLevel() {
        return config.node("debug-options").node("jython-logging-level").getString();
    }

    @Override
    public boolean patchThreading() {
        return config.node("debug-options").node("patch-threading").getBoolean();
    }

    @Override
    public boolean loadJythonOnStartup() {
        return config.node("debug-options").node("init-on-startup").getBoolean();
    }

    @Override
    public Properties getJythonProperties() {
        try {
            List<String> properties = config.node("jython-options").node("properties").getList(String.class);
            Properties toReturn = new Properties();
            for (String property : properties) {
                String[] split = property.split("=", 2);
                String key = split[0].trim();
                String value = split[1].trim();
                toReturn.setProperty(key, value);
            }
            return toReturn;
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching Jython properties from config.yml", e);
            return new Properties();
        }
    }

    @Override
    public String[] getJythonArgs() {
        try {
            return config.node("jython-options").node("args").getList(String.class).toArray(new String[0]);
        } catch (SerializationException e) {
            PyVelocity.get().getPlatformLogger().error("Error when fetching Jython args from config.yml", e);
            return new String[0];
        }
    }
}
