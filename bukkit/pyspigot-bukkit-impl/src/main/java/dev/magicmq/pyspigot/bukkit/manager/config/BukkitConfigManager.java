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

import dev.magicmq.pyspigot.manager.config.ConfigManager;
import dev.magicmq.pyspigot.manager.config.ScriptConfig;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The Bukkit-specific implementation of the config manager.
 */
public class BukkitConfigManager extends ConfigManager {

    private static BukkitConfigManager instance;

    private BukkitConfigManager() {
        super();
    }

    @Override
    protected ScriptConfig loadConfigImpl(Path configFile, String defaults) throws IOException {
        BukkitScriptConfig config = new BukkitScriptConfig(configFile.toFile(), defaults);
        config.load();
        return config;
    }

    /**
     * Get the singleton instance of this BukkitConfigManager.
     * @return The instance
     */
    public static BukkitConfigManager get() {
        if (instance == null)
            instance = new BukkitConfigManager();
        return instance;
    }

}
