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

package dev.magicmq.pyspigot.manager.libraries;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import me.lucko.jarrelocator.JarRelocator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * A manager class to help with dynamically loading Jar files into the classpath at runtime. Most commonly, scripts will not use this directly and PySpigot will be the primary user of this manager.
 * <p>
 * Internally, this utilizes the jar-relocator library from lucko.
 * @see me.lucko.jarrelocator.JarRelocator
 */
public class LibraryManager {

    private static LibraryManager instance;

    private File libsFolder;
    private HashMap<String, String> relocations;
    private JarClassLoader classLoader;
    private ExecutorService initializer;

    private LibraryManager() {
        libsFolder = new File(PySpigot.get().getDataFolder(), "libs");
        if (!libsFolder.exists())
            libsFolder.mkdir();

        relocations = PluginConfig.getLibraryRelocations();

        classLoader = new JarClassLoader(this.getClass().getClassLoader());
        initializer = Executors.newSingleThreadExecutor();
        initLibraries();
    }

    public void shutdown() {
        try {
            classLoader.close();
        } catch (IOException e) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Exception when closing JarClassLoader", e);
        }
    }

    /**
     * Load a library into the classpath.
     * @param fileName The name of the Jar file to load into the classpath
     * @return A {@link LoadResult} describing the outcome of the load attempt
     */
    public LoadResult loadLibrary(String fileName) {
        try {
            Path file = Paths.get(PySpigot.get().getDataFolder().getAbsolutePath(), "libs", fileName);
            if (file.toFile().exists())
                return loadLibrary(file);
            else
                return LoadResult.FAILED_FILE;
        } catch (Throwable throwable) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Unable to load library " + fileName + "!", throwable);
            return LoadResult.FAILED_ERROR;
        }
    }

    /**
     * Get the {@link JarClassLoader} for loading Jar files into the classpath.
     * @return The JarClassLoader
     */
    public JarClassLoader getClassLoader() {
        return classLoader;
    }

    private void initLibraries() {
        SortedSet<File> toLoad = new TreeSet<>();
        if (libsFolder.isDirectory()) {
            toLoad.addAll(Arrays.asList(libsFolder.listFiles()));
        }

        for (File library : toLoad) {
            String libraryName = library.getName();
            try {
                long start = System.nanoTime();
                loadLibrary(library.toPath());
                long duration = System.nanoTime() - start;
                PySpigot.get().getLogger().log(Level.INFO, "Loaded library " + libraryName + " in " + (duration / 1000000) + " ms");
            } catch (Throwable throwable) {
                PySpigot.get().getLogger().log(Level.SEVERE, "Unable to load library " + libraryName + "!", throwable);
            }
        }
    }

    private LoadResult loadLibrary(Path file) throws Exception {
        Callable<LoadResult> work = () -> {
            Path relocated = relocateJar(file);

            if (classLoader.isJarInClassPath(relocated))
                return LoadResult.FAILED_LOADED;

            classLoader.addJarToClasspath(relocated);
            return LoadResult.SUCCESS;
        };
        return initializer.submit(work).get();
    }

    private Path relocateJar(Path file) throws Exception {
        File output = new File(file.getFileName().toString());
        JarRelocator relocator = new JarRelocator(file.toFile(), output, relocations);
        relocator.run();
        return output.toPath();
    }

    /**
     * Get the singleton instance of this LibraryManager.
     * @return The instance
     */
    public static LibraryManager get() {
        if (instance == null)
            instance = new LibraryManager();
        return instance;
    }

    /**
     * An enum representing the outcome of an attempt to load a library.
     */
    public enum LoadResult {

        /**
         * The library failed to load because the libs folder does not exist.
         */
        FAILED_FILE,

        /**
         * The library failed to load because it is already loaded.
         */
        FAILED_LOADED,

        /**
         * The library failed to load because of some unrecoverable error.
         */
        FAILED_ERROR,

        /**
         * The library loaded successfully.
         */
        SUCCESS

    }
}
