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
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventBus;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * Entry point of PySpigot for the BungeeCord servers.
 */
public class PyBungee extends Plugin implements PlatformAdapter {

    private static PyBungee instance;

    private BungeeAudiences adventure;
    private Metrics metrics;
    private ScheduledTask versionCheckTask;

    @Override
    public void onEnable() {
        instance = this;

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
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeePluginCommand());
    }

    @Override
    public void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener());
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
        adventure = BungeeAudiences.create(this);
    }

    @Override
    public void initVersionChecking() {
        ProxyServer.getInstance().getScheduler().schedule(this, PyCore.get()::compareVersions, 1L, TimeUnit.SECONDS);
        versionCheckTask = ProxyServer.getInstance().getScheduler().schedule(this, PyCore.get()::fetchSpigotVersion, 12L, 12L, TimeUnit.HOURS);
    }

    @Override
    public void setupMetrics() {
        metrics = new Metrics(this, 18991);

        metrics.addCustomChart(new SimplePie("all_scripts", () -> {
            int allScripts = ScriptManager.get().getAllScriptPaths().size() + ScriptManager.get().getAllProjectPaths().size();
            return "" + allScripts;
        }));

        metrics.addCustomChart(new SimplePie("loaded_scripts", () -> {
            int loadedScripts = ScriptManager.get().getLoadedScripts().size();
            return "" + loadedScripts;
        }));
    }

    @Override
    public void shutdownMetrics() {
        if (metrics != null)
            metrics.shutdown();
    }

    @Override
    public void shutdownVersionChecking() {
        if (versionCheckTask != null)
            versionCheckTask.cancel();
    }

    @Override
    public Logger getPlatformLogger() {
        return LoggerFactory.getLogger(getLogger().getName());
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
        return getDescription().getVersion();
    }

    @Override
    public String getPluginIdentifier() {
        return "PySpigot-Bungee";
    }

    @Override
    public boolean isPacketEventsAvailable() {
        return ProxyServer.getInstance().getPluginManager().getPlugin("PacketEvents") != null;
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
