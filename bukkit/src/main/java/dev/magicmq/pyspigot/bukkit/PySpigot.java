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

import dev.magicmq.pyspigot.PlatformAdapter;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bukkit.command.BukkitPluginCommand;
import dev.magicmq.pyspigot.bukkit.config.BukkitPluginConfig;
import dev.magicmq.pyspigot.bukkit.manager.command.BukkitCommandManager;
import dev.magicmq.pyspigot.bukkit.manager.config.BukkitConfigManager;
import dev.magicmq.pyspigot.bukkit.manager.listener.BukkitListenerManager;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bukkit.manager.script.BukkitScriptManager;
import dev.magicmq.pyspigot.bukkit.manager.task.BukkitTaskManager;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 * Entry point of PySpigot for the Bukkit server software.
 */
public class PySpigot extends JavaPlugin implements PlatformAdapter {

    private static PySpigot instance;

    private Metrics metrics;
    private BukkitTask versionCheckTask;

    @Override
    public void onEnable() {
        instance = this;

        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            getLogger().log(Level.SEVERE, "Error when accessing CraftBukkit (Are you running a supported version?), PySpigot will not work correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        PyCore.newInstance(this);
        PyCore.get().init();
    }

    @Override
    public void onDisable() {
        PyCore.get().shutdown();
    }

    @Override
    public PluginConfig initConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        return new BukkitPluginConfig();
    }

    @Override
    public void initCommands() {
        getCommand("pyspigot").setExecutor(new BukkitPluginCommand());
    }

    @Override
    public void initListeners() {
        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);
    }

    @Override
    public void initPlatformManagers() {
        BukkitScriptManager.get();
        BukkitListenerManager.get();
        BukkitCommandManager.get();
        BukkitTaskManager.get();
        BukkitConfigManager.get();
        ProtocolManager.get();
        PlaceholderManager.get();
    }

    @Override
    public void initVersionChecking() {
        Bukkit.getScheduler().runTaskLater(this, PyCore.get()::compareVersions, 20L);
        versionCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, PyCore.get()::fetchSpigotVersion, 864000L, 864000L);
    }

    @Override
    public void setupMetrics() {
        metrics = new Metrics(this, 18991);

        metrics.addCustomChart(new SimplePie("all_scripts", () -> {
            int allScripts = ScriptManager.get().getAllScriptPaths().size();
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
    public Path getDataFolderPath() {
        return Paths.get(getDataFolder().getAbsolutePath());
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return getClassLoader();
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public String getAuthor() {
        return getDescription().getAuthors().get(0);
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