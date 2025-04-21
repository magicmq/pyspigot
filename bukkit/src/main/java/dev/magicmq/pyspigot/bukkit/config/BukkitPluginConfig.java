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

import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Bukkit-specific implementation of the {@link dev.magicmq.pyspigot.config.PluginConfig} class, for retrieving values from the plugin config.yml.
 */
public class BukkitPluginConfig implements PluginConfig {

    private FileConfiguration config;

    private DateTimeFormatter logTimestamp;

    @Override
    public void reload() {
        PySpigot.get().reloadConfig();
        config = PySpigot.get().getConfig();

        logTimestamp = DateTimeFormatter.ofPattern(config.getString("log-timestamp-format"));
    }

    @Override
    public boolean getMetricsEnabled() {
        return config.getBoolean("metrics-enabled");
    }

    @Override
    public long getScriptLoadDelay() {
        return config.getLong("script-load-delay");
    }

    @Override
    public HashMap<String, String> getLibraryRelocations() {
        HashMap<String, String> toReturn = new HashMap<>();
        for (String string : config.getStringList("library-relocations")) {
            String[] split = string.split("\\|");
            toReturn.put(split[0], split[1]);
        }
        return toReturn;
    }

    @Override
    public DateTimeFormatter getLogTimestamp() {
        return logTimestamp;
    }

    @Override
    public boolean doScriptActionLogging() {
        return config.getBoolean("script-action-logging");
    }

    @Override
    public boolean doVerboseRedisLogging() {
        return config.getBoolean("verbose-redis-logging");
    }

    @Override
    public boolean doScriptUnloadOnPluginDisable() {
        return config.getBoolean("script-unload-on-plugin-disable");
    }

    @Override
    public boolean scriptOptionEnabled() {
        return config.getBoolean("script-option-defaults.enabled");
    }

    @Override
    public int scriptOptionLoadPriority() {
        return config.getInt("script-option-defaults.load-priority");
    }

    @Override
    public List<String> scriptOptionPluginDepend() {
        return config.getStringList("script-option-defaults.plugin-depend");
    }

    @Override
    public boolean scriptOptionFileLoggingEnabled() {
        return config.getBoolean("script-option-defaults.file-logging-enabled");
    }

    @Override
    public String scriptOptionMinLoggingLevel() {
        return config.getString("script-option-defaults.min-logging-level");
    }

    @Override
    public String scriptOptionPermissionDefault() {
        return config.getString("script-option-defaults.permission-default");
    }

    @Override
    public Map<String, Object> scriptOptionPermissions() {
        if (config.contains("script-option-defaults.permissions"))
            return getNestedMap(config.getConfigurationSection("script-option-defaults.permissions"));
        else
            return new HashMap<>();
    }

    @Override
    public boolean shouldPrintStackTraces() {
        return config.getBoolean("debug-options.print-stack-traces");
    }

    @Override
    public boolean shouldShowUpdateMessages() {
        return config.getBoolean("debug-options.show-update-messages");
    }

    @Override
    public String jythonLoggingLevel() {
        return config.getString("debug-options.jython-logging-level");
    }

    @Override
    public boolean loadJythonOnStartup() {
        return config.getBoolean("jython-options.init-on-startup");
    }

    @Override
    public Properties getJythonProperties() {
        List<String> properties = config.getStringList("jython-options.properties");
        Properties toReturn = new Properties();
        for (String property : properties) {
            String[] split = property.split("=", 2);
            String key = split[0].trim();
            String value = split[1].trim();
            toReturn.setProperty(key, value);
        }
        return toReturn;
    }

    @Override
    public String[] getJythonArgs() {
        return config.getStringList("jython-options.args").toArray(new String[0]);
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