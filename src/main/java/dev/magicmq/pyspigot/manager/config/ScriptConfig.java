package dev.magicmq.pyspigot.manager.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ScriptConfig extends YamlConfiguration {

    private File configFile;

    private ScriptConfig(File configFile) {
        this.configFile = configFile;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void save() throws IOException {
        this.save(configFile);
    }

    protected static ScriptConfig loadConfig(File configFile) throws IOException, InvalidConfigurationException {
        ScriptConfig config = new ScriptConfig(configFile);
        config.load(configFile);
        return config;
    }
}
