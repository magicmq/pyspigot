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
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The Bukkit-specific implementation of the {@link dev.magicmq.pyspigot.config.PluginConfig} class, for retreiving values from the plugin config.yml.
 */
public class BukkitPluginConfig implements PluginConfig {

    private FileConfiguration config;

    private DateTimeFormatter logTimestamp;

    public void reload() {
        PySpigot.get().reloadConfig();
        config = PySpigot.get().getConfig();

        logTimestamp = DateTimeFormatter.ofPattern(config.getString("log-timestamp-format"));
    }

    public boolean getMetricsEnabled() {
        return config.getBoolean("metrics-enabled");
    }

    public long getScriptLoadDelay() {
        return config.getLong("script-load-delay");
    }

    public HashMap<String, String> getLibraryRelocations() {
        HashMap<String, String> toReturn = new HashMap<>();
        for (String string : config.getStringList("library-relocations")) {
            String[] split = string.split("\\|");
            toReturn.put(split[0], split[1]);
        }
        return toReturn;
    }

    public DateTimeFormatter getLogTimestamp() {
        return logTimestamp;
    }

    public boolean doScriptActionLogging() {
        return config.getBoolean("script-action-logging");
    }

    public boolean doVerboseRedisLogging() {
        return config.getBoolean("verbose-redis-logging");
    }

    public boolean doScriptUnloadOnPluginDisable() {
        return config.getBoolean("script-unload-on-plugin-disable");
    }

    public String scriptOptionMainScript() {
        return config.getString("script-option-defaults.main");
    }

    public boolean scriptOptionEnabled() {
        return config.getBoolean("script-option-defaults.enabled");
    }

    public int scriptOptionLoadPriority() {
        return config.getInt("script-option-defaults.load-priority");
    }

    public List<String> scriptOptionPluginDepend() {
        return config.getStringList("script-option-defaults.plugin-depend");
    }

    public boolean scriptOptionFileLoggingEnabled() {
        return config.getBoolean("script-option-defaults.file-logging-enabled");
    }

    public String scriptOptionMinLoggingLevel() {
        return config.getString("script-option-defaults.min-logging-level");
    }

    public String scriptOptionPermissionDefault() {
        return config.getString("script-option-defaults.permission-default");
    }

    public Map<?, ?> scriptOptionPermissions() {
        return new HashMap<>();
    }

    public boolean shouldPrintStackTraces() {
        return config.getBoolean("debug-options.print-stack-traces");
    }

    public boolean shouldShowUpdateMessages() {
        return config.getBoolean("debug-options.show-update-messages");
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
}