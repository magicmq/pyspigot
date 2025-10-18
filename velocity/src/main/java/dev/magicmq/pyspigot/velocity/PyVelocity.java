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

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.magicmq.pyspigot.MetricsAdapter;
import dev.magicmq.pyspigot.PlatformAdapter;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.classpath.ClassPathAppender;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.velocity.classpath.VelocityClassPathAppender;
import dev.magicmq.pyspigot.velocity.command.VelocityPluginCommand;
import dev.magicmq.pyspigot.velocity.config.VelocityPluginConfig;
import dev.magicmq.pyspigot.velocity.config.VelocityScriptOptionsConfig;
import dev.magicmq.pyspigot.velocity.manager.command.VelocityCommandManager;
import dev.magicmq.pyspigot.velocity.manager.config.VelocityConfigManager;
import dev.magicmq.pyspigot.velocity.manager.listener.VelocityListenerManager;
import dev.magicmq.pyspigot.velocity.manager.script.VelocityScriptManager;
import dev.magicmq.pyspigot.velocity.manager.task.VelocityTaskManager;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PyVelocity implements PlatformAdapter {

    private static PyVelocity instance;

    private final ProxyServer proxy;
    private final Logger logger;
    private final PluginDescription pluginDescription;
    private final Path dataFolder;

    private ScheduledTask versionCheckTask;

    @Inject
    public PyVelocity(ProxyServer proxy, Logger logger, PluginDescription pluginDescription, @DataDirectory Path dataFolder) {
        instance = this;

        this.proxy = proxy;
        this.logger = logger;
        this.pluginDescription = pluginDescription;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PyCore.newInstance(this);
        PyCore.get().init();
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    @Override
    public PluginConfig initConfig() {
        return new VelocityPluginConfig();
    }

    @Override
    public ScriptOptionsConfig initScriptOptionsConfig() {
        return new VelocityScriptOptionsConfig();
    }

    /**
     * No-op implementation (Velocity natively supports Adventure)
     */
    @Override
    public void initAdventure() {}

    @Override
    public void initCommands() {
        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("pyvelocity")
                .aliases("pv")
                .plugin(this)
                .build();
        VelocityPluginCommand command = new VelocityPluginCommand();

        commandManager.register(commandMeta, command);
    }

    @Override
    public void initListeners() {
        proxy.getEventManager().register(this, new VelocityListener());
    }

    @Override
    public void initPlatformManagers() {
        VelocityListenerManager.get();
        VelocityCommandManager.get();
        VelocityTaskManager.get();
        VelocityConfigManager.get();

        VelocityScriptManager.get();
    }

    @Override
    public void initVersionChecking() {
        proxy.getScheduler().buildTask(this, PyCore.get()::compareVersions).delay(1L, TimeUnit.SECONDS).schedule();
        versionCheckTask = proxy.getScheduler().buildTask(this, PyCore.get()::fetchSpigotVersion).delay(12L, TimeUnit.HOURS).repeat(12L, TimeUnit.HOURS).schedule();
    }

    @Override
    public ClassPathAppender initClassPathAppender() {
        return new VelocityClassPathAppender();
    }

    @Override
    public MetricsAdapter initMetrics() {
        VelocityMetrics metrics = new VelocityMetrics();
        metrics.setup();
        return metrics;
    }

    @Override
    public void shutdownVersionChecking() {
        if (versionCheckTask != null)
            versionCheckTask.cancel();
    }

    @Override
    public Logger getPlatformLogger() {
        return logger;
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
        return pluginDescription.getVersion().get();
    }

    @Override
    public String getPluginIdentifier() {
        return "PySpigot-Velocity";
    }

    @Override
    public String getDependenciesFileName() {
        return "velocity-dependencies.json";
    }

    @Override
    public boolean isPacketEventsAvailable() {
        return proxy.getPluginManager().getPlugin("packetevents").isPresent();
    }

    /**
     * Add a JAR file at the specified URL to the plugin class path.
     * @param path The path of the JAR file to add to the class path
     */
    public void addToClassPath(Path path) {
        proxy.getPluginManager().addToClasspath(this, path);
    }

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PyVelocity get() {
        return instance;
    }
}
