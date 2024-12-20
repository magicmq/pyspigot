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

package dev.magicmq.pyspigot.bukkit.manager.config;

import dev.magicmq.pyspigot.exception.InvalidConfigurationException;
import dev.magicmq.pyspigot.manager.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Path;

public class SpigotConfigManager extends ConfigManager<SpigotScriptConfig> {

    private static SpigotConfigManager manager;

    private SpigotConfigManager() {
        super();
    }

    @Override
    public SpigotScriptConfig loadConfig(String filePath) throws IOException, InvalidConfigurationException {
        return loadConfig(filePath, null);
    }

    @Override
    public SpigotScriptConfig loadConfig(String filePath, String defaults) throws IOException, InvalidConfigurationException {
        Path configFile = createConfigIfNotExists(filePath);

        SpigotScriptConfig config = new SpigotScriptConfig(configFile.toFile(), defaults);
        config.load();
        return config;
    }

    /**
     * Get the singleton instance of this SpigotConfigManager.
     * @return The instance
     */
    public static SpigotConfigManager get() {
        if (manager == null)
            manager = new SpigotConfigManager();
        return manager;
    }

}
