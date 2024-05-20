/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot;

import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.config.ConfigManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.script.GlobalVariables;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.StringUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * Main class of the plugin.
 */
public class PySpigot extends JavaPlugin {

    private static PySpigot instance;

    /**
     * Can be used by scripts to access the {@link ScriptManager}.
     */
    public static ScriptManager script;
    /**
     * Can be used by scripts to access the {@link GlobalVariables}
     */
    public static GlobalVariables global_vars;
    /**
     * Can be used by scripts to access the {@link ListenerManager}.
     */
    public static ListenerManager listener;
    /**
     * Can be used by scripts to access the {@link CommandManager}.
     */
    public static CommandManager command;
    /**
     * Can be used by scripts to access the {@link TaskManager}.
     */
    public static TaskManager scheduler;
    /**
     * Can be used by scripts to access the {@link ConfigManager}.
     */
    public static ConfigManager config;
    /**
     * Can be used by scripts to access the {@link ProtocolManager}.
     */
    public static ProtocolManager protocol;
    /**
     * Can be used by scripts to access the {@link PlaceholderManager}.
     */
    public static PlaceholderManager placeholder;

    private FileConfiguration scriptOptionsConfig;
    private Metrics metrics;
    private BukkitTask versionCheckTask;
    private volatile String spigotVersion;

    @Override
    public void onEnable() {
        instance = this;

        initFolders();
        initHelperLib();

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        reloadScriptOptionsConfig();

        getCommand("pyspigot").setExecutor(new PySpigotCommand());

        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);

        try {
            checkReflection();
        } catch (NoSuchMethodException | NoSuchFieldException e) {
            getLogger().log(Level.SEVERE, "Error when accessing CraftBukkit (Are you on a supported MC version?), PySpigot will not work correctly.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        LibraryManager.get();
        script = ScriptManager.get();
        global_vars = GlobalVariables.get();
        listener = ListenerManager.get();
        command = CommandManager.get();
        scheduler = TaskManager.get();
        config = ConfigManager.get();

        if (isProtocolLibAvailable())
            protocol = ProtocolManager.get();

        if (isPlaceholderApiAvailable())
            placeholder = PlaceholderManager.get();

        if (PluginConfig.getMetricsEnabled())
            setupMetrics();

        fetchSpigotVersion();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (spigotVersion != null && !PluginConfig.shouldSuppressUpdateMessages()) {
                StringUtils.Version currentVersion = new StringUtils.Version(getDescription().getVersion());
                StringUtils.Version latestVersion = new StringUtils.Version(spigotVersion);
                if (currentVersion.compareTo(latestVersion) < 0) {
                    getLogger().log(Level.WARNING, "You're running an outdated version of PySpigot. The latest version is " + spigotVersion + ".");
                    getLogger().log(Level.WARNING, "Download it here: https://www.spigotmc.org/resources/pyspigot.111006/");
                }
            }
        }, 20L);
        versionCheckTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::fetchSpigotVersion, 864000L, 864000L);
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
     * Reload the plugin configuration.
     */
    public void reload() {
        reloadConfig();
        PluginConfig.reload();
        reloadScriptOptionsConfig();
    }

    /**
     * Get the {@link ClassLoader} for PySpigot.
     * @return The ClassLoader
     */
    public ClassLoader getPluginClassLoader() {
        return this.getClassLoader();
    }

    /**
     * Get the script_options.yml configuration file.
     * @return The script_options.yml configuration file
     */
    public FileConfiguration getScriptOptionsConfig() {
        return scriptOptionsConfig;
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

    protected String getSpigotVersion() {
        return spigotVersion;
    }

    private void fetchSpigotVersion() {
        try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=111006/~").openStream(); Scanner scanner = new Scanner(is)) {
            if (scanner.hasNext())
                spigotVersion = scanner.next();
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Error when attempting to get latest plugin version from Spigot:");
            e.printStackTrace();
        }
    }

    private void initFolders() {
        String[] folders = new String[]{"java-libs", "python-libs", "scripts", "logs"};
        for (String folder : folders) {
            File file = new File(getDataFolder(), folder);
            if (!file.exists())
                file.mkdirs();
        }
    }

    private void initHelperLib() {
        if (!PluginConfig.shouldUpdatePySpigotLib()) {
            return;
        }

        File pythonLibs = new File(getDataFolder(), "python-libs");
        if (pythonLibs.exists()) {
            File libFile = new File(pythonLibs, "pyspigot.py");
            if (!libFile.exists()) {
                saveResource("python-libs/pyspigot.py", true);
            } else {
                try {
                    FileInputStream savedFile = new FileInputStream(libFile);

                    URL url = getClassLoader().getResource("python-libs/pyspigot.py");
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    InputStream jarFile = connection.getInputStream();

                    if (!checkFilesEqual(savedFile, jarFile)) {
                        saveResource("python-libs/pyspigot.py", true);
                    }
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Error when initializing library files: ", e);
                }
            }
        }
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
            int allScripts = ScriptManager.get().getAllScriptNames().size();
            return "" + allScripts;
        }));

        metrics.addCustomChart(new SimplePie("loaded_scripts", () -> {
            int loadedScripts = ScriptManager.get().getLoadedScripts().size();
            return "" + loadedScripts;
        }));
    }

    private void reloadScriptOptionsConfig() {
        File file = new File(PySpigot.get().getDataFolder(), "script_options.yml");
        if (!file.exists()) {
            saveResource("script_options.yml", false);
        }
        scriptOptionsConfig = YamlConfiguration.loadConfiguration(file);
    }

    private boolean checkFilesEqual(InputStream is1, InputStream is2) {
        try (BufferedInputStream bis1 = new BufferedInputStream(is1); BufferedInputStream bis2 = new BufferedInputStream(is2)) {
            int ch;
            while ((ch = bis1.read()) != -1) {
                if (ch != bis2.read())
                    return false;
            }
            return bis2.read() == -1;
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error when initializing library files: ", e);
            return false;
        }
    }

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PySpigot get() {
        return instance;
    }
}
