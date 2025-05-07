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

package dev.magicmq.pyspigot.velocity;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.magicmq.pyspigot.PlatformAdapter;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;

@Plugin(
        id = "pyvelocity",
        name = "PyVelocity",
        version = "0.9.1-SNAPSHOT",
        url = "https://pyspigot-docs.magicmq.dev",
        description = "Python scripting engine for Velocity proxy servers",
        authors = {"magicmq"}
)
public class PyVelocity implements PlatformAdapter {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataFolder;

    @Inject
    public PyVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataFolder) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }


    @Override
    public PluginConfig initConfig() {
        return null;
    }

    @Override
    public ScriptOptionsConfig initScriptOptionsConfig() {
        return null;
    }

    @Override
    public void initCommands() {

    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initPlatformManagers() {

    }

    @Override
    public void initVersionChecking() {

    }

    @Override
    public void setupMetrics() {

    }

    @Override
    public void shutdownMetrics() {

    }

    @Override
    public void shutdownVersionChecking() {

    }

    @Override
    public java.util.logging.Logger getLogger() {
        return null;
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }

    @Override
    public Path getDataFolderPath() {
        return dataFolder;
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public String getVersion() {
        return "0.9.1-SNAPSHOT";
    }

    @Override
    public String getPluginIdentifier() {
        return "PySpigot-Velocity";
    }
}
