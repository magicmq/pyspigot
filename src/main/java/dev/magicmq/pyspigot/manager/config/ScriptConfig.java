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

package dev.magicmq.pyspigot.manager.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * A class representing a script configuration file.
 * @see org.bukkit.configuration.file.YamlConfiguration
 */
public class ScriptConfig extends YamlConfiguration {

    private final File configFile;

    /**
     *
     * @param configFile The config file
     */
    private ScriptConfig(File configFile) {
        this.configFile = configFile;
    }

    /**
     * Get the file associated with this configuration.
     * @return The file associated with this configuration
     */
    public File getConfigFile() {
        return configFile;
    }

    /**
     * Save the config to its associated file.
     * @throws IOException If there is an IOException when saving the file
     */
    public void save() throws IOException {
        this.save(configFile);
    }

    protected static ScriptConfig loadConfig(File configFile) throws IOException, InvalidConfigurationException {
        ScriptConfig config = new ScriptConfig(configFile);
        config.load(configFile);
        return config;
    }
}
