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
import dev.magicmq.pyspigot.manager.libraries.LibraryManager;
import dev.magicmq.pyspigot.util.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core class of PySpigot for both Spigot and BungeeCord.
 */
public class PyCore {

    private static PyCore instance;

    private final Logger logger;
    private final File dataFolder;
    private final Path dataFolderPath;
    private final ClassLoader pluginClassLoader;
    private final String version;
    private final String author;
    private final boolean paper;
    private final PluginConfig config;

    private volatile String spigotVersion;

    public PyCore(Logger logger, File dataFolder, ClassLoader pluginClassLoader, PluginConfig config, String version, String author, boolean paper) {
        instance = this;

        this.logger = logger;
        this.dataFolder = dataFolder;
        this.dataFolderPath = Paths.get(dataFolder.getAbsolutePath());
        this.pluginClassLoader = pluginClassLoader;
        this.version = version;
        this.author = author;
        this.paper = paper;

        this.config = config;
    }

    public void init() {
        LibraryManager.get();

        initFolders();
        initHelperLib();
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
        return logger;
    }

    /**
     * Get the data folder for PySpigot.
     * @return The data folder
     */
    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * Get the path of the data folder for PySpigot.
     * @return A path representing the data folder
     */
    public Path getDataFolderPath() {
        return dataFolderPath;
    }

    /**
     * Get the {@link ClassLoader} for PySpigot.
     * @return The ClassLoader
     */
    public ClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    /**
     * Get the version of the plugin.
     * @return The version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the author of the plugin.
     * @return The author
     */
    public String getAuthor() {
        return author;
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

    public void fetchSpigotVersion() {
        try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=111006/").openStream(); Scanner scanner = new Scanner(is)) {
            if (scanner.hasNext())
                spigotVersion = scanner.next();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error when attempting to get latest plugin version from Spigot.", e);
        }
    }

    public void compareVersions() {
        if (spigotVersion != null && config.shouldShowUpdateMessages()) {
            StringUtils.Version currentVersion = new StringUtils.Version(version);
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
        InputStream in = getResourceAsStream(resourcePath);

        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

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
                logger.log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    private void initFolders() {
        String[] folders = new String[]{"java-libs", "python-libs", "scripts", "logs"};
        for (String folder : folders) {
            File file = new File(dataFolder, folder);
            if (!file.exists())
                file.mkdirs();
        }
    }

    private void initHelperLib() {
        if (!config.shouldUpdatePySpigotLib()) {
            return;
        }

        File pythonLibs = new File(dataFolder, "python-libs");
        if (pythonLibs.exists()) {
            File libFile = new File(pythonLibs, "pyspigot.py");
            if (!libFile.exists()) {
                saveResource("python-libs/pyspigot.py", true);
            } else {
                try {
                    FileInputStream savedFile = new FileInputStream(libFile);

                    URL url = pluginClassLoader.getResource("python-libs/pyspigot.py");
                    URLConnection connection = url.openConnection();
                    connection.setUseCaches(false);
                    InputStream jarFile = connection.getInputStream();

                    if (!checkFilesEqual(savedFile, jarFile)) {
                        saveResource("python-libs/pyspigot.py", true);
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error when initializing library files: ", e);
                }
            }
        }
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
            logger.log(Level.SEVERE, "Error when initializing library files: ", e);
            return false;
        }
    }

    private InputStream getResourceAsStream(String name) {
        return pluginClassLoader.getResourceAsStream(name);
    }

    public static PyCore get() {
        return instance;
    }
}
