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

import dev.magicmq.pyspigot.config.ScriptOptionsConfig;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.database.DatabaseManager;
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.manager.packetevents.PacketEventsManager;
import dev.magicmq.pyspigot.manager.redis.RedisManager;
import dev.magicmq.pyspigot.manager.script.GlobalVariables;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Core class of PySpigot for all platform-specific implementations. Platform-specific code is implemented via the PlatformAdapter.
 * @see PlatformAdapter
 */
public class PyCore {

    private static PyCore instance;

    private final PlatformAdapter adapter;

    private Logger logger;
    private PluginConfig config;
    private ScriptOptionsConfig scriptOptionsConfig;
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
        logger = adapter.getPlatformLogger();

        if (adapter.getVersion().contains("SNAPSHOT")) {
            logger.warn("It looks like you're running a SNAPSHOT (development) build of PySpigot.");
            logger.warn("SNAPSHOT builds are untested and may contain bugs. Use at your own risk.");
            logger.warn("Download the latest stable release here: https://www.spigotmc.org/resources/pyspigot.111006/");
        }

        adapter.initAdventure();

        initFolders();

        saveDefaultConfig();
        config = adapter.initConfig();
        config.reload();

        scriptOptionsConfig = adapter.initScriptOptionsConfig();
        scriptOptionsConfig.reload();

        adapter.initCommands();
        adapter.initListeners();

        LibraryManager.get();
        initCommonManagers();
        adapter.initPlatformManagers();

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
        scriptOptionsConfig.reload();
    }

    /**
     * Get the logger for PySpigot, furnished by the server.
     * @return The logger
     */
    public Logger getLogger() {
        return adapter.getPlatformLogger();
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
     * Get if PacketEvents is loaded and present on the platform.
     * @return True if PacketEvents is loaded and present, false if it is not
     */
    public boolean isPacketEventsAvailable() {
        return adapter.isPacketEventsAvailable();
    }

    /**
     * Get the plugin configuration for PySpigot.
     * @return The PySpigot plugin config
     */
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Get the script options configuration for PySpigot.
     * @return The script options config
     */
    public ScriptOptionsConfig getScriptOptionsConfig() {
        return scriptOptionsConfig;
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
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spigotmc.org/legacy/update.php?resource=111006"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isEmpty()) {
                spigotVersion = response.body().trim();
            }
        } catch (Exception e) {
            logger.warn("Error when attempting to get latest plugin version from Spigot.", e);
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
                logger.warn("You're running an outdated version of PySpigot. You are running {}, but the latest version is {}.", adapter.getVersion(), spigotVersion);
                logger.warn("Download the latest version here: https://www.spigotmc.org/resources/pyspigot.111006/");
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
        try (InputStream in = getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("The resource '" + resourcePath + "' could not be found");
            }

            File outFile = new File(adapter.getDataFolder(), resourcePath);
            int lastIndex = resourcePath.lastIndexOf('/');
            File outDir = new File(adapter.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

            if (!outDir.exists() && !outDir.mkdirs()) {
                throw new IOException("Failed to create directory " + outDir);
            }

            if (!outFile.exists() || replace) {
                try (OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            } else {
                logger.warn("Could not save {} to {} because {} already exists.", outFile.getName(), outFile, outFile.getName());
            }
        } catch (IOException ex) {
            logger.error("Could not save {} to {}", resourcePath, adapter.getDataFolder(), ex);
        }
    }

    private InputStream getResourceAsStream(String name) {
        return adapter.getPluginClassLoader().getResourceAsStream(name);
    }

    private void initCommonManagers() {
        GlobalVariables.get();
        DatabaseManager.get();
        RedisManager.get();

        if (adapter.isPacketEventsAvailable())
            PacketEventsManager.get();
    }

    private void initFolders() {
        String[] folders = new String[]{"java-libs", "python-libs", "scripts", "projects", "logs"};
        for (String folder : folders) {
            File file = new File(adapter.getDataFolder(), folder);
            if (!file.exists() && !file.mkdirs()) {
                logger.warn("Failed to create directory: {}", file.getAbsolutePath());
            }
        }
    }

    private void saveDefaultConfig() {
        Path configPath = adapter.getDataFolderPath().resolve("config.yml");
        if (!Files.exists(configPath))
            saveResource("config.yml", false);
    }

    public static PyCore get() {
        if (instance == null) {
            throw new IllegalStateException("PyCore has not been initialized");
        }
        return instance;
    }
}
