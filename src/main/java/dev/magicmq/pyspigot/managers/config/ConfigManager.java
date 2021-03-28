package dev.magicmq.pyspigot.managers.config;

import dev.magicmq.pyspigot.PySpigot;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private static ConfigManager manager;

    private final File configFolder;

    private ConfigManager() {
        configFolder = new File(PySpigot.get().getDataFolder(), "configs");
        if (!configFolder.exists())
            configFolder.mkdir();
    }

    public ScriptConfig loadConfig(String fileName) {
        File configFile = new File(configFolder, fileName);
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            return ScriptConfig.loadConfig(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ScriptConfig reloadConfig(ScriptConfig config) {
        File configFile = new File(configFolder, config.getConfigFile().getName());

        try {
            return ScriptConfig.loadConfig(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getConfigFolder() {
        return configFolder;
    }

    public static ConfigManager get() {
        if (manager == null)
            manager = new ConfigManager();
        return manager;
    }

}
