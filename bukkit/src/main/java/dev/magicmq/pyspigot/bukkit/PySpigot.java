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
import dev.magicmq.pyspigot.bukkit.command.SpigotPluginCommand;
import dev.magicmq.pyspigot.bukkit.config.SpigotPluginConfig;
import dev.magicmq.pyspigot.bukkit.manager.command.SpigotCommandManager;
import dev.magicmq.pyspigot.bukkit.manager.config.SpigotConfigManager;
import dev.magicmq.pyspigot.bukkit.manager.listener.SpigotListenerManager;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bukkit.manager.script.SpigotScriptInfo;
import dev.magicmq.pyspigot.bukkit.manager.script.SpigotScriptManager;
import dev.magicmq.pyspigot.bukkit.manager.task.SpigotTaskManager;
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
     * Can be used by scripts to access the {@link SpigotScriptManager}.
     */
    public static SpigotScriptManager script;
    /**
     * Can be used by scripts to access the {@link GlobalVariables}
     */
    public static GlobalVariables global_vars;
    /**
     * Can be used by scripts to access the {@link SpigotListenerManager}.
     */
    public static SpigotListenerManager listener;
    /**
     * Can be used by scripts to access the {@link SpigotCommandManager}.
     */
    public static SpigotCommandManager command;
    /**
     * Can be used by scripts to access the {@link SpigotTaskManager}.
     */
    public static SpigotTaskManager scheduler;
    /**
     * Can be used by scripts to access the {@link SpigotConfigManager}.
     */
    public static SpigotConfigManager config;
    /**
     * Can be used by scripts to access the {@link ProtocolManager}.
     */
    public static ProtocolManager protocol;
    /**
     * Can be used by scripts to access the {@link PlaceholderManager}.
     */
    public static PlaceholderManager placeholder;
    /**
     * Can be used by scripts to access the {@link DatabaseManager}
     */
    public static DatabaseManager database;
    /**
     * Can be used by scripts to access the {@link RedisManager}
     */
    public static RedisManager redis;

    private Metrics metrics;
    private BukkitTask versionCheckTask;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        SpigotPluginConfig pluginConfig = new SpigotPluginConfig();
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

        getCommand("pyspigot").setExecutor(new SpigotPluginCommand());

        Bukkit.getPluginManager().registerEvents(new SpigotListener(), this);

        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            getLogger().log(Level.SEVERE, "Error when accessing CraftBukkit (Are you on a supported MC version?), PySpigot will not work correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        script = SpigotScriptManager.get();
        global_vars = GlobalVariables.get();
        listener = SpigotListenerManager.get();
        command = SpigotCommandManager.get();
        scheduler = SpigotTaskManager.get();
        config = SpigotConfigManager.get();
        database = DatabaseManager.get();
        redis = RedisManager.get();

        if (isProtocolLibAvailable())
            protocol = ProtocolManager.get();

        if (isPlaceholderApiAvailable())
            placeholder = PlaceholderManager.get();

        if (pluginConfig.getMetricsEnabled())
            setupMetrics();

        SpigotScriptInfo.get();

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