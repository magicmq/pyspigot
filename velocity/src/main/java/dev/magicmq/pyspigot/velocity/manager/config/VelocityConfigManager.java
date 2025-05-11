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

package dev.magicmq.pyspigot.velocity.manager.config;


import dev.magicmq.pyspigot.manager.config.ConfigManager;
import dev.magicmq.pyspigot.manager.config.ScriptConfig;

import java.io.IOException;
import java.nio.file.Path;

public class VelocityConfigManager extends ConfigManager {

    private static VelocityConfigManager instance;

    private VelocityConfigManager() {
        super();
    }

    @Override
    protected ScriptConfig loadConfigImpl(Path configFile, String defaults) throws IOException {
        VelocityScriptConfig config = new VelocityScriptConfig(configFile, defaults);
        config.load();
        return config;
    }


    public static VelocityConfigManager get() {
        if (instance == null)
            instance = new VelocityConfigManager();
        return instance;
    }
}
