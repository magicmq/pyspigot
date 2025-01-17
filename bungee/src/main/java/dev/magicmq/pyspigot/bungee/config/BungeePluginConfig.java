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
import dev.magicmq.pyspigot.config.PluginConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The BungeeCord-specific implementation of the {@link dev.magicmq.pyspigot.config.PluginConfig} class, for retreiving values from the plugin config.yml.
 */
public class BungeePluginConfig implements PluginConfig {

    private Configuration config;

    private DateTimeFormatter logTimestamp;

    public void reload() {
        try {
            Configuration defaultConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(PyBungee.get().getResourceAsStream("config.yml"));
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(PyBungee.get().getDataFolder(), "config.yml"), defaultConfig);
        } catch (IOException e) {
            PyBungee.get().getLogger().log(Level.SEVERE, "There was an exception when loading the config file.", e);
        }

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

    public boolean shouldUpdatePySpigotLib() {
        return config.getBoolean("debug-options.auto-pyspigot-lib-update-enabled");
    }
}
