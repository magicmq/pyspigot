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

package dev.magicmq.pyspigot;


import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.script.GlobalVariables;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core class of PySpigot for all platform-specific implementations. Platform-specific code is implemented via the PlatformAdapter.
 * @see PlatformAdapter
 */
public class PyCore {

    private static PyCore instance;

    private final PlatformAdapter adapter;

    private boolean paper;
    private PluginConfig config;
    private volatile String spigotVersion;

    private PyCore(PlatformAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Initialize the PyCore instance.
     * <p>
     * Called from the {@code onEnable} method of the platform-specific plugin class (PySpigot for Bukkit, for example).
     * @param adapter The platform-specific adapter.
     */
    public static void newInstance(PlatformAdapter adapter) {
        if (instance != null) {
            throw new UnsupportedOperationException("PyCore has already been initialized");
        }

        instance = new PyCore(adapter);
    }

    /**
     * Initialize the plugin.
     */
    public void init() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ignored) {
            paper = false;
        }

        config = adapter.initConfig();
        config.reload();

        initFolders();

        adapter.initCommands();
        adapter.initListeners();

        LibraryManager.get();
        adapter.initPlatformManagers();
        initCommonManagers();

        if (config.getMetricsEnabled())
            adapter.setupMetrics();

        fetchSpigotVersion();
        adapter.initVersionChecking();
    }

    /**
     * Shutdown the plugin.
     */
    public void shutdown() {
        if (ScriptManager.get() != null)
            ScriptManager.get().shutdown();
        if (LibraryManager.get() != null)
            LibraryManager.get().shutdown();

        adapter.shutdownMetrics();

        adapter.shutdownVersionChecking();
    }

    /**
     * Reload the plugin config and the script options config.
     */
    public void reloadConfigs() {
        config.reload();
        ScriptOptionsConfig.reload();
    }

    /**
     * Get the logger for PySpigot.
     * @return The logger
     */
    public Logger getLogger() {
        return adapter.getLogger();
    }

    /**
     * Get the data folder for PySpigot.
     * @return The data folder
     */
    public File getDataFolder() {
        return adapter.getDataFolder();
    }

    /**
     * Get the path of the data folder for PySpigot.
     * @return A path representing the data folder
     */
    public Path getDataFolderPath() {
        return adapter.getDataFolderPath();
    }

    /**
     * Get the {@link ClassLoader} for PySpigot.
     * @return The ClassLoader
     */
    public ClassLoader getPluginClassLoader() {
        return adapter.getPluginClassLoader();
    }

    /**
     * Get the version of the plugin.
     * @return The version
     */
    public String getVersion() {
        return adapter.getVersion();
    }

    /**
     * Get the identifier of the plugin.
     * @return The plugin identifier
     */
    public String getPluginIdentifier() {
        return adapter.getPluginIdentifier();
    }

    /**
     * Get if the server is running paper.
     * @return True if the server is running paper, false if otherwise
     */
    public boolean isPaper() {
        return paper;
    }

    /**
     * Get the plugin configuration for PySpigot.
     * @return The PySpigot plugin config
     */
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Get the latest available plugin version on Spigot.
     * @return The latest available version on Spigot
     */
    public String getSpigotVersion() {
        return spigotVersion;
    }

    /**
     * Fetch the latest available plugin version from SpigotMC.
     */
    public void fetchSpigotVersion() {
        try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=111006/").openStream(); Scanner scanner = new Scanner(is)) {
            if (scanner.hasNext())
                spigotVersion = scanner.next();
        } catch (IOException e) {
            adapter.getLogger().log(Level.WARNING, "Error when attempting to get latest plugin version from Spigot.", e);
        }
    }

    /**
     * Compare the current loaded plugin version with the cached latest SpigotMC plugin version, and log a message to console if the current version is detected as outdated.
     */
    public void compareVersions() {
        if (spigotVersion != null && config.shouldShowUpdateMessages()) {
            StringUtils.Version currentVersion = new StringUtils.Version(adapter.getVersion());
            StringUtils.Version latestVersion = new StringUtils.Version(spigotVersion);
            if (currentVersion.compareTo(latestVersion) < 0) {
                getLogger().log(Level.WARNING, "You're running an outdated version of PySpigot. The latest version is " + spigotVersion + ".");
                getLogger().log(Level.WARNING, "Download it here: https://www.spigotmc.org/resources/pyspigot.111006/");
            }
        }
    }

    /**
     * Save a resource from the plugin JAR file to the plugin data folder.
     * @param resourcePath The path of the resource to save
     * @param replace True if the file should be replaced (if it already exists in the data folder), false if it should not
     */
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResourceAsStream(resourcePath);

        File outFile = new File(adapter.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(adapter.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                adapter.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            adapter.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    private void initCommonManagers() {
        GlobalVariables.get();
        DatabaseManager.get();
        RedisManager.get();
    }

    private void initFolders() {
        String[] folders = new String[]{"java-libs", "python-libs", "scripts", "logs"};
        for (String folder : folders) {
            File file = new File(adapter.getDataFolder(), folder);
            if (!file.exists())
                file.mkdirs();
        }
    }

    private InputStream getResourceAsStream(String name) {
        return adapter.getPluginClassLoader().getResourceAsStream(name);
    }

    public static PyCore get() {
        return instance;
    }
}
