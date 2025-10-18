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

package dev.magicmq.pyspigot.bungee;

import dev.magicmq.pyspigot.MetricsAdapter;
import dev.magicmq.pyspigot.PlatformAdapter;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bungee.command.BungeePluginCommand;
import dev.magicmq.pyspigot.bungee.config.BungeePluginConfig;
import dev.magicmq.pyspigot.bungee.config.BungeeScriptOptionsConfig;
import dev.magicmq.pyspigot.bungee.manager.command.BungeeCommandManager;
import dev.magicmq.pyspigot.bungee.manager.config.BungeeConfigManager;
import dev.magicmq.pyspigot.bungee.manager.listener.BungeeListenerManager;
import dev.magicmq.pyspigot.bungee.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bungee.manager.script.BungeeScriptManager;
import dev.magicmq.pyspigot.bungee.manager.task.BungeeTaskManager;
import dev.magicmq.pyspigot.classpath.ClassPathAppender;
import dev.magicmq.pyspigot.classpath.JarInJarClassPathAppender;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.loader.LoaderBootstrap;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Entry point of PySpigot for the BungeeCord servers.
 * <p>
 * Note that in order to facilitate an isolated classloader for runtime dependency management, this class is instantiated
 * from a loader class, which serves as the actual plugin class.
 */
public class PyBungee implements LoaderBootstrap, PlatformAdapter {

    private static PyBungee instance;

    private final Plugin plugin;

    private BungeeAudiences adventure;
    private ScheduledTask versionCheckTask;

    public PyBungee(Plugin plugin) {
        instance = this;

        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new PluginInitializationException("Error when accessing BungeeCord via reflection, PyBungee will not be initialized.", e);
        }

        PyCore.newInstance(this);
        PyCore.get().init();
    }

    @Override
    public void onDisable() {
        if (PyCore.get() != null)
            PyCore.get().shutdown();
    }

    @Override
    public PluginConfig initConfig() {
        return new BungeePluginConfig();
    }

    @Override
    public ScriptOptionsConfig initScriptOptionsConfig() {
        return new BungeeScriptOptionsConfig();
    }

    @Override
    public void initCommands() {
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new BungeePluginCommand());
    }

    @Override
    public void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new BungeeListener());
    }

    @Override
    public void initPlatformManagers() {
        BungeeListenerManager.get();
        BungeeCommandManager.get();
        BungeeTaskManager.get();
        BungeeConfigManager.get();

        if (isProtocolizeAvailable())
            ProtocolManager.get();

        BungeeScriptManager.get();
    }

    @Override
    public void initAdventure() {
        adventure = BungeeAudiences.create(plugin);
    }

    @Override
    public void initVersionChecking() {
        ProxyServer.getInstance().getScheduler().schedule(plugin, PyCore.get()::compareVersions, 1L, TimeUnit.SECONDS);
        versionCheckTask = ProxyServer.getInstance().getScheduler().schedule(plugin, PyCore.get()::fetchSpigotVersion, 12L, 12L, TimeUnit.HOURS);
    }

    @Override
    public ClassPathAppender initClassPathAppender() {
        return new JarInJarClassPathAppender(getClass().getClassLoader());
    }

    @Override
    public MetricsAdapter initMetrics() {
        BungeeMetrics metrics = new BungeeMetrics();
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
        return LoggerFactory.getLogger(plugin.getLogger().getName());
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public Path getDataFolderPath() {
        return Paths.get(getDataFolder().getAbsolutePath());
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getPluginIdentifier() {
        return "PySpigot-Bungee";
    }

    @Override
    public String getDependenciesFileName() {
        return "bungee-dependencies.json";
    }

    @Override
    public boolean isPacketEventsAvailable() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("PacketEvents") != null;
    }

    /**
     * Get the underlying BungeeCord plugin object.
     * @return The underlying BungeeCord plugin.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Check if Protocolize is available on the server.
     * @return True if Protocolize is loaded and enabled, false if otherwise
     */
    public boolean isProtocolizeAvailable() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("Protocolize") != null;
    }

    /**
     * Get the adventure API for the BungeeCord platform.
     * @return The adventure API
     */
    public BungeeAudiences getAdventure() {
        return adventure;
    }

    private void checkReflection() throws NoSuchMethodException, NoSuchFieldException {
        //Check reflection for registering listeners
        PluginManager.class.getDeclaredField("listenersByPlugin");
        EventBus.class.getDeclaredField("byListenerAndPriority");
        EventBus.class.getDeclaredField("lock");
        EventBus.class.getDeclaredMethod("bakeHandlers", Class.class);
    }

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PyBungee get() {
        return instance;
    }
}
