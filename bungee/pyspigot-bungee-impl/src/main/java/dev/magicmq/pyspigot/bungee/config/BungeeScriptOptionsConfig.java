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


import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.util.StringUtils;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The BungeeCord-specific implementation of the {@link dev.magicmq.pyspigot.config.ScriptOptionsConfig} class, for retrieving values from the script_options.yml file.
 */
public class BungeeScriptOptionsConfig implements ScriptOptionsConfig {

    private Configuration config;

    @Override
    public void reload() {
        File file = new File(PyCore.get().getDataFolder(), "script_options.yml");
        if (!file.exists()) {
            PyCore.get().saveResource("script_options.yml", false);
        }

        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(PyBungee.get().getDataFolder(), "script_options.yml"));
        } catch (IOException e) {
            PyBungee.get().getPlatformLogger().error("An error occurred when attempting to load the script_options.yml file", e);
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load("");
        }
    }

    @Override
    public boolean contains(String key) {
        return config.contains(StringUtils.stripFileExtension(key));
    }

    @Override
    public boolean getEnabled(String scriptName, boolean defaultValue) {
        Configuration scriptSection = config.getSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getBoolean("enabled", defaultValue);
    }

    @Override
    public int getLoadPriority(String scriptName, int defaultValue) {
        Configuration scriptSection = config.getSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getInt("load-priority", defaultValue);
    }

    @Override
    public List<String> getPluginDepend(String scriptName, List<String> defaultValue) {
        Configuration scriptSection = config.getSection(StringUtils.stripFileExtension(scriptName));
        if (scriptSection.contains("plugin-depend"))
            return scriptSection.getStringList("plugin-depend");
        else
            return defaultValue;
    }

    @Override
    public boolean getFileLoggingEnabled(String scriptName, boolean defaultValue) {
        Configuration scriptSection = config.getSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getBoolean("file-logging-enabled", defaultValue);
    }

    @Override
    public String getMinLoggingLevel(String scriptName, String defaultValue) {
        Configuration scriptSection = config.getSection(StringUtils.stripFileExtension(scriptName));
        return scriptSection.getString("min-logging-level", defaultValue);
    }

    /**
     * No-op implementation
     */
    @Override
    public String getPermissionDefault(String scriptName, String defaultValue) {
        //Plugin permissions are not implemented in BungeeCord
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    public Map<String, Object> getPermissions(String scriptName, Map<String, Object> defaultValue) {
        //Plugin permissions are not implemented in BungeeCord
        return null;
    }
}
