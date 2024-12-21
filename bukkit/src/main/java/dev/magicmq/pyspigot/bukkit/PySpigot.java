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

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bukkit.command.BukkitPluginCommand;
import dev.magicmq.pyspigot.bukkit.config.BukkitPluginConfig;
import dev.magicmq.pyspigot.bukkit.manager.command.BukkitCommandManager;
import dev.magicmq.pyspigot.bukkit.manager.config.BukkitConfigManager;
import dev.magicmq.pyspigot.bukkit.manager.listener.BukkitListenerManager;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bukkit.manager.script.BukkitScriptInfo;
import dev.magicmq.pyspigot.bukkit.manager.script.BukkitScriptManager;
import dev.magicmq.pyspigot.bukkit.manager.task.BukkitTaskManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.script.GlobalVariables;
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

import java.util.logging.Level;

/**
 * Entry point of PySpigot for Bukkit server software.
 */
public class PySpigot extends JavaPlugin {

    private static PySpigot instance;
    private static PyCore core;

    /**
     * Can be used by scripts to access the {@link BukkitScriptManager}.
     */
    public static BukkitScriptManager script;
    /**
     * Can be used by scripts to access the {@link GlobalVariables}
     */
    public static GlobalVariables global_vars;
    /**
     * Can be used by scripts to access the {@link BukkitListenerManager}.
     */
    public static BukkitListenerManager listener;
    /**
     * Can be used by scripts to access the {@link BukkitCommandManager}.
     */
    public static BukkitCommandManager command;
    /**
     * Can be used by scripts to access the {@link BukkitTaskManager}.
     */
    public static BukkitTaskManager scheduler;
    /**
     * Can be used by scripts to access the {@link BukkitConfigManager}.
     */
    public static BukkitConfigManager config;
    /**
     * Can be used by scripts to access the {@link DatabaseManager}
     */
    public static DatabaseManager database;
    /**
     * Can be used by scripts to access the {@link RedisManager}
     */
    public static RedisManager redis;
    /**
     * Can be used by scripts to access the {@link ProtocolManager}.
     */
    public static ProtocolManager protocol;
    /**
     * Can be used by scripts to access the {@link PlaceholderManager}.
     */
    public static PlaceholderManager placeholder;

    private Metrics metrics;
    private BukkitTask versionCheckTask;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        BukkitPluginConfig pluginConfig = new BukkitPluginConfig();
        pluginConfig.reload();

        boolean paper;
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ignored) {
            paper = false;
        }

        core = new PyCore(
                getLogger(),
                getDataFolder(),
                getClassLoader(),
                pluginConfig,
                getDescription().getVersion(),
                getDescription().getAuthors().get(0),
                paper
        );
        core.init();

        getCommand("pyspigot").setExecutor(new BukkitPluginCommand());

        Bukkit.getPluginManager().registerEvents(new BukkitListener(), this);

        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            getLogger().log(Level.SEVERE, "Error when accessing CraftBukkit (Are you on a supported MC version?), PySpigot will not work correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        script = BukkitScriptManager.get();
        global_vars = GlobalVariables.get();
        listener = BukkitListenerManager.get();
        command = BukkitCommandManager.get();
        scheduler = BukkitTaskManager.get();
        config = BukkitConfigManager.get();
        database = DatabaseManager.get();
        redis = RedisManager.get();

        if (isProtocolLibAvailable())
            protocol = ProtocolManager.get();

        if (isPlaceholderApiAvailable())
            placeholder = PlaceholderManager.get();

        if (pluginConfig.getMetricsEnabled())
            setupMetrics();

        BukkitScriptInfo.get();

        core.fetchSpigotVersion();
        Bukkit.getScheduler().runTaskLater(this, core::compareVersions, 20L);
        versionCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, core::fetchSpigotVersion, 864000L, 864000L);
    }

    @Override
    public void onDisable() {
        ScriptManager.get().shutdown();

        LibraryManager.get().shutdown();

        if (metrics != null)
            metrics.shutdown();

        if (versionCheckTask != null)
            versionCheckTask.cancel();
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

    private void setupMetrics() {
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

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PySpigot get() {
        return instance;
    }
}