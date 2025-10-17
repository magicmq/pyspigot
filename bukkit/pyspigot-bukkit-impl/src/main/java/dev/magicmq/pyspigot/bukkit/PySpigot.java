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

package dev.magicmq.pyspigot.bukkit;

import dev.magicmq.pyspigot.MetricsAdapter;
import dev.magicmq.pyspigot.PlatformAdapter;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bukkit.command.BukkitPluginCommand;
import dev.magicmq.pyspigot.bukkit.config.BukkitPluginConfig;
import dev.magicmq.pyspigot.bukkit.config.BukkitScriptOptionsConfig;
import dev.magicmq.pyspigot.bukkit.manager.command.BukkitCommandManager;
import dev.magicmq.pyspigot.bukkit.manager.config.BukkitConfigManager;
import dev.magicmq.pyspigot.bukkit.manager.listener.BukkitListenerManager;
import dev.magicmq.pyspigot.bukkit.manager.messaging.PluginMessageManager;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bukkit.manager.script.BukkitScriptManager;
import dev.magicmq.pyspigot.bukkit.manager.task.BukkitTaskManager;
import dev.magicmq.pyspigot.classpath.ClassPathAppender;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.classpath.JarInJarClassPathAppender;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.loader.LoaderBootstrap;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point of PySpigot for Bukkit servers.
 * <p>
 * Note that in order to facilitate an isolated classloader for runtime dependency management, this class is instantiated
 * from a loader class, which serves as the actual plugin class.
 */
public class PySpigot implements LoaderBootstrap, PlatformAdapter {

    private static PySpigot instance;

    private final JavaPlugin plugin;

    private boolean paper;
    private BukkitAudiences adventure;
    private BukkitTask versionCheckTask;

    public PySpigot(JavaPlugin plugin) {
        instance = this;

        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            throw new PluginInitializationException("Error when accessing Bukkit via reflection, PySpigot will not be initialized.", e);
        }

        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ignored) {
            paper = false;
        }

        BukkitDependencies.addToRegistry();

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
        plugin.getConfig().options().copyDefaults(true);
        return new BukkitPluginConfig();
    }

    @Override
    public ScriptOptionsConfig initScriptOptionsConfig() {
        return new BukkitScriptOptionsConfig();
    }

    @Override
    public void initCommands() {
        plugin.getCommand("pyspigot").setExecutor(new BukkitPluginCommand());
    }

    @Override
    public void initListeners() {
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), plugin);
    }

    @Override
    public void initPlatformManagers() {
        BukkitListenerManager.get();
        BukkitCommandManager.get();
        BukkitTaskManager.get();
        BukkitConfigManager.get();

        if (isProtocolLibAvailable())
            ProtocolManager.get();
        if (isPlaceholderApiAvailable())
            PlaceholderManager.get();

        PluginMessageManager.get();

        BukkitScriptManager.get();
    }

    @Override
    public void initAdventure() {
        adventure = BukkitAudiences.create(plugin);
    }

    @Override
    public void initVersionChecking() {
        Bukkit.getScheduler().runTaskLater(plugin, PyCore.get()::compareVersions, 20L);
        versionCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, PyCore.get()::fetchSpigotVersion, 864000L, 864000L);
    }

    @Override
    public ClassPathAppender initClassPathAppender() {
        return new JarInJarClassPathAppender(getClass().getClassLoader());
    }

    @Override
    public MetricsAdapter initMetrics() {
        BukkitMetrics metrics = new BukkitMetrics();
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
        if (paper)
            return plugin.getSLF4JLogger();
        else
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
        return "PySpigot";
    }

    @Override
    public boolean isPacketEventsAvailable() {
        return Bukkit.getPluginManager().getPlugin("PacketEvents") != null;
    }

    /**
     * Get the underlying Bukkit plugin object.
     * @return The underlying Bukkit plugin.
     */
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Check if ProtocolLib is available on the server.
     * @return True if ProtocolLib is loaded and enabled, false if otherwise
     */
    public boolean isProtocolLibAvailable() {
        return Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
    }

    /**
     * Check if PlacehodlerAPI is available on the server.
     * @return True if PlaceholderAPI is loaded and enabled, false if otherwise
     */
    public boolean isPlaceholderApiAvailable() {
        return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    /**
     * Get the adventure API for the Bukkit platform.
     * @return The adventure API
     */
    public BukkitAudiences getAdventure() {
        return adventure;
    }

    private void checkReflection() throws NoSuchMethodException, NoSuchFieldException {
        //Check reflection for commands
        PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        Bukkit.getServer().getClass().getDeclaredField("commandMap");
        SimpleCommandMap.class.getDeclaredField("knownCommands");
        IndexHelpTopic.class.getDeclaredField("allTopics");
    }

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PySpigot get() {
        return instance;
    }
}