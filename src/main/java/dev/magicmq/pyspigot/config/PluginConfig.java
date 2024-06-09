/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot.config;

import dev.magicmq.pyspigot.PySpigot;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/**
 * Helper class to retrieve configuration values from the plugin config.
 */
public class PluginConfig {

    private static FileConfiguration config;

    private static DateTimeFormatter logTimestamp;

    static {
        reload();
    }

    public static void reload() {
        config = PySpigot.get().getConfig();

        logTimestamp = DateTimeFormatter.ofPattern(config.getString("log-timestamp-format"));
    }

    public static boolean getMetricsEnabled() {
        return config.getBoolean("metrics-enabled");
    }

    public static long getScriptLoadDelay() {
        return config.getLong("script-load-delay");
    }

    public static HashMap<String, String> getLibraryRelocations() {
        HashMap<String, String> toReturn = new HashMap<>();
        for (String string : config.getStringList("library-relocations")) {
            String[] split = string.split("\\|");
            toReturn.put(split[0], split[1]);
        }
        return toReturn;
    }

    public static boolean doLogToFile() {
        return config.getBoolean("log-to-file");
    }

    public static String getLogLevel() {
        return config.getString("min-log-level");
    }

    public static DateTimeFormatter getLogTimestamp() {
        return logTimestamp;
    }

    public static boolean doScriptActionLogging() {
        return config.getBoolean("script-action-logging");
    }

    public static boolean doVerboseRedisLogging() {
        return config.getBoolean("verbose-redis-logging");
    }

    public static boolean shouldPrintStackTraces() {
        return config.getBoolean("debug-options.print-stack-traces");
    }

    public static boolean shouldSuppressUpdateMessages() {
        return config.getBoolean("debug-options.suppress-update-messages");
    }

    public static boolean shouldUpdatePySpigotLib() {
        return config.getBoolean("debug-options.auto-pyspigot-lib-update-enabled");
    }

    public static String getMessage(String key, boolean withPrefix) {
        return ChatColor.translateAlternateColorCodes('&', (withPrefix ? config.getString("messages.plugin-prefix") : "") + config.getString("messages." + key));
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("messages.plugin-prefix"));
    }
}