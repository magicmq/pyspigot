package dev.magicmq.pyspigot.config;

import dev.magicmq.pyspigot.PySpigot;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.logging.Level;

public class PluginConfig {

    private static FileConfiguration config;

    private static DateTimeFormatter logTimestamp;

    static {
        reload();
    }

    public static void reload() {
        config = PySpigot.get().getConfig();

        logTimestamp = DateTimeFormatter.ofPattern(config.getString("log-timestamp-format", "MMM dd yyyy HH:mm:ss"));
    }

    public static long getLoadScriptDelay() {
        return config.getLong("script-load-delay", 20L);
    }

    public static boolean doAutoImportBukkit() {
        return config.getBoolean("auto-import-bukkit", false);
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
        return config.getBoolean("log-to-file", true);
    }

    public static Level getLogLevel() {
        return Level.parse(config.getString("min-log-level", "INFO"));
    }

    public static DateTimeFormatter getLogTimestamp() {
        return logTimestamp;
    }

    public static String getMessage(String key, boolean withPrefix) {
        return ChatColor.translateAlternateColorCodes('&', (withPrefix ? config.getString("messages.plugin-prefix") : "") + config.getString("messages." + key));
    }
}