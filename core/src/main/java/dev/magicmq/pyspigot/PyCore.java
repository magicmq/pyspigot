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

public class PyCore {

    private static PyCore instance;

    private final PlatformAdapter adapter;

    private boolean paper;
    private PluginConfig config;
    private ScriptOptionsConfig scriptOptionsConfig;
    private volatile String spigotVersion;

    private PyCore(PlatformAdapter adapter) {
        this.adapter = adapter;
    }

    public static void newInstance(PlatformAdapter adapter) {
        if (instance != null) {
            throw new UnsupportedOperationException("PyCore has already been initialized");
        }
        instance = new PyCore(adapter);
    }

    public void init() {
        try {
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        } catch (ClassNotFoundException ignored) {
            paper = false;
        }

        config = adapter.initConfig();
        config.reload();

        scriptOptionsConfig = adapter.initScriptOptionsConfig();
        scriptOptionsConfig.reload();

        initFolders();

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

    public void shutdown() {
        if (ScriptManager.get() != null)
            ScriptManager.get().shutdown();
        if (LibraryManager.get() != null)
            LibraryManager.get().shutdown();

        adapter.shutdownMetrics();
        adapter.shutdownVersionChecking();
    }

    public void reloadConfigs() {
        config.reload();
        scriptOptionsConfig.reload();
    }

    public Logger getLogger() {
        return adapter.getLogger();
    }

    public File getDataFolder() {
        return adapter.getDataFolder();
    }

    public Path getDataFolderPath() {
        return adapter.getDataFolderPath();
    }

    public ClassLoader getPluginClassLoader() {
        return adapter.getPluginClassLoader();
    }

    public String getVersion() {
        return adapter.getVersion();
    }

    public String getPluginIdentifier() {
        return adapter.getPluginIdentifier();
    }

    public boolean isPaper() {
        return paper;
    }

    public PluginConfig getConfig() {
        return config;
    }

    public ScriptOptionsConfig getScriptOptionsConfig() {
        return scriptOptionsConfig;
    }

    public String getSpigotVersion() {
        return spigotVersion;
    }

    public void fetchSpigotVersion() {
        try {
            URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=111006/");
            try (InputStream is = url.openStream(); Scanner scanner = new Scanner(is)) {
                if (scanner.hasNext()) {
                    spigotVersion = scanner.next();
                }
            }
        } catch (IOException e) {
            adapter.getLogger().log(Level.WARNING, "Error when attempting to get latest plugin version from Spigot.", e);
        }
    }

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
                adapter.getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            adapter.getLogger().log(Level.SEVERE, "Could not save " + resourcePath + " to " + adapter.getDataFolder(), ex);
        }
    }

    private void initCommonManagers() {
        GlobalVariables.get();
        DatabaseManager.get();
        RedisManager.get();
    }

    private void initFolders() {
        String[] folders = new String[]{"java-libs", "python-libs", "scripts", "projects", "logs"};
        for (String folder : folders) {
            File file = new File(adapter.getDataFolder(), folder);
            if (!file.exists() && !file.mkdirs()) {
                adapter.getLogger().log(Level.WARNING, "Failed to create directory: " + file.getAbsolutePath());
            }
        }
    }

    private InputStream getResourceAsStream(String name) {
        return adapter.getPluginClassLoader().getResourceAsStream(name);
    }

    public static PyCore get() {
        if (instance == null) {
            throw new IllegalStateException("PyCore has not been initialized");
        }
        return instance;
    }
}
