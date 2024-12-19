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

package dev.magicmq.pyspigot.bungee.manager.config;


import dev.magicmq.pyspigot.manager.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Manager for scripts to interface with configuration files. Primarily used by scripts to load, write to, and save .yml files.
 */
public class BungeeConfigManager extends ConfigManager<BungeeScriptConfig> {

    private static BungeeConfigManager manager;

    private BungeeConfigManager() {
        super();
    }

    @Override
    public BungeeScriptConfig loadConfig(String filePath) throws IOException {
        return loadConfig(filePath, null);
    }

    @Override
    public BungeeScriptConfig loadConfig(String filePath, String defaults) throws IOException {
        Path configFile = createConfigIfNotExists(filePath);

        BungeeScriptConfig config = new BungeeScriptConfig(configFile.toFile(), defaults);
        config.load();
        return config;
    }

    /**
     * Get the singleton instance of this BungeeConfigManager.
     * @return The instance
     */
    public static BungeeConfigManager get() {
        if (manager == null)
            manager = new BungeeConfigManager();
        return manager;
    }

}
