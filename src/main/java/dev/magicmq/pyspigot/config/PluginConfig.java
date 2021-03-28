package dev.magicmq.pyspigot.config;

import dev.magicmq.pyspigot.PySpigot;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class PluginConfig {

    private static FileConfiguration config;

    static {
        reload();
    }

    public static void reload() {
        config = PySpigot.get().getConfig();
    }

    public static boolean preloadScripts() {
        return config.getBoolean("preload-scripts", true);
    }

    public static boolean autorunScripts() {
        return config.getBoolean("autorun-scripts", true);
    }

    public static long getLoadScriptDelay() {
        return config.getLong("script-load-delay", 20L);
    }

    public static String getMessage(String key, boolean withPrefix) {
        return ChatColor.translateAlternateColorCodes('&', (withPrefix ? config.getString("messages.plugin-prefix") : "") + config.getString("messages." + key));
    }
}