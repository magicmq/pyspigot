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

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bungee.command.BungeePluginCommand;
import dev.magicmq.pyspigot.bungee.config.BungeePluginConfig;
import dev.magicmq.pyspigot.bungee.manager.command.BungeeCommandManager;
import dev.magicmq.pyspigot.bungee.manager.config.BungeeConfigManager;
import dev.magicmq.pyspigot.bungee.manager.listener.BungeeListenerManager;
import dev.magicmq.pyspigot.bungee.manager.script.BungeeScriptInfo;
import dev.magicmq.pyspigot.bungee.manager.script.BungeeScriptManager;
import dev.magicmq.pyspigot.bungee.manager.task.BungeeTaskManager;
import dev.magicmq.pyspigot.manager.config.ConfigManager;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.script.GlobalVariables;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Main class of the Spigot plugin.
 */
public class PyBungee extends Plugin {

    private static PyBungee instance;
    private static PyCore core;

    /**
     * Can be used by scripts to access the {@link BungeeScriptManager}.
     */
    public static BungeeScriptManager script;
    /**
     * Can be used by scripts to access the {@link GlobalVariables}
     */
    public static GlobalVariables global_vars;
    /**
     * Can be used by scripts to access the {@link BungeeListenerManager}.
     */
    public static BungeeListenerManager listener;
    /**
     * Can be used by scripts to access the {@link BungeeCommandManager}.
     */
    public static BungeeCommandManager command;
    /**
     * Can be used by scripts to access the {@link BungeeTaskManager}.
     */
    public static BungeeTaskManager scheduler;
    /**
     * Can be used by scripts to access the {@link ConfigManager}.
     */
    public static BungeeConfigManager config;
    /**
     * Can be used by scripts to access the {@link DatabaseManager}
     */
    public static DatabaseManager database;
    /**
     * Can be used by scripts to access the {@link RedisManager}
     */
    public static RedisManager redis;

    private Metrics metrics;
    private ScheduledTask versionCheckTask;

    @Override
    public void onEnable() {
        instance = this;

        try {
            saveDefaultConfig();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error when saving the default config file.", e);
        }
        BungeePluginConfig pluginConfig = new BungeePluginConfig();
        pluginConfig.reload();

        core = new PyCore(
                getLogger(),
                getDataFolder(),
                getClass().getClassLoader(),
                pluginConfig,
                getDescription().getVersion(),
                getDescription().getAuthor(),
                false
        );
        core.init();

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeePluginCommand());

        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener());

        script = BungeeScriptManager.get();
        global_vars = GlobalVariables.get();
        listener = BungeeListenerManager.get();
        command = BungeeCommandManager.get();
        scheduler = BungeeTaskManager.get();
        config = BungeeConfigManager.get();
        database = DatabaseManager.get();
        redis = RedisManager.get();

        if (pluginConfig.getMetricsEnabled())
            setupMetrics();

        BungeeScriptInfo.get();

        core.fetchSpigotVersion();
        ProxyServer.getInstance().getScheduler().schedule(this, core::compareVersions, 1L, TimeUnit.SECONDS);
        versionCheckTask = ProxyServer.getInstance().getScheduler().schedule(this, core::fetchSpigotVersion, 12L, 12L, TimeUnit.HOURS);
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

    private void saveDefaultConfig() throws IOException {
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            FileOutputStream outputStream = new FileOutputStream(configFile);
            InputStream in = getResourceAsStream("config.yml");
            in.transferTo(outputStream);
        }
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
    public static PyBungee get() {
        return instance;
    }
}
