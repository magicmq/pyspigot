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
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

/**
 * The BungeeCord-specific implementation of the {@link dev.magicmq.pyspigot.config.PluginConfig} class, for retrieving values from the plugin config.yml.
 */
public class BungeePluginConfig implements PluginConfig {

    private Configuration config;

    private DateTimeFormatter logTimestamp;

    @Override
    public void reload() {
        try {
            Configuration defaultConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(PyBungee.get().getResourceAsStream("config.yml"));
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(PyBungee.get().getDataFolder(), "config.yml"), defaultConfig);
        } catch (IOException e) {
            PyBungee.get().getLogger().log(Level.SEVERE, "There was an exception when loading the config file.", e);
        }

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
        //Plugin permissions are not implemented in BungeeCord
        return null;
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
}
