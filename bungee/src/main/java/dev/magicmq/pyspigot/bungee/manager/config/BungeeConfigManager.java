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
import dev.magicmq.pyspigot.manager.config.ScriptConfig;

import java.io.IOException;
import java.nio.file.Path;

/**
 * The BungeeCord-specific implementation of the config manager.
 */
public class BungeeConfigManager extends ConfigManager {

    private static BungeeConfigManager instance;

    private BungeeConfigManager() {
        super();
    }

    @Override
    protected ScriptConfig loadConfigImpl(Path configFile, String defaults) throws IOException {
        BungeeScriptConfig config = new BungeeScriptConfig(configFile.toFile(), defaults);
        config.load();
        return config;
    }

    /**
     * Get the singleton instance of this BungeeConfigManager.
     * @return The instance
     */
    public static BungeeConfigManager get() {
        if (instance == null)
            instance = new BungeeConfigManager();
        return instance;
    }

}
