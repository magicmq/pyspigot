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

package dev.magicmq.pyspigot.manager.libraries;

import dev.magicmq.pyspigot.PyCore;
import me.lucko.jarrelocator.JarRelocator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A manager class to help with dynamically loading Jar files into the classpath at runtime. Most commonly, scripts will not use this directly and PySpigot will be the primary user of this manager.
 * <p>
 * Internally, this utilizes the jar-relocator library from lucko to dynamically load Jar files at runtime.
 * @see me.lucko.jarrelocator.JarRelocator
 */
public class LibraryManager {

    private static LibraryManager instance;

    private final JarClassLoader classLoader;

    private final File libsFolder;
    private final HashMap<String, String> relocations;
    private final ExecutorService initializer;

    private LibraryManager() {
        classLoader = new JarClassLoader(PyCore.get().getPluginClassLoader());

        libsFolder = new File(PyCore.get().getDataFolder(), "java-libs");

        relocations = PyCore.get().getConfig().getLibraryRelocations();
        initializer = Executors.newSingleThreadExecutor();
        initLibraries();
    }

    /**
     * Closes the class loader for scripts.
     */
    public void shutdown() {
        try {
            classLoader.close();
        } catch (IOException e) {
            PyCore.get().getLogger().error("Exception when closing JarClassLoader", e);
        }
    }

    /**
     * Attempts to load all libraries that are not currently loaded. Libraries that are already loaded will be unaffected.
     */
    public void reload() {
        initLibraries();
    }

    /**
     * Load a library into the classpath.
     * @param libName The name of the Jar file to load into the classpath, including the extension (.jar)
     * @return A {@link LoadResult} describing the outcome of the load attempt
     */
    public LoadResult loadLibrary(String libName) {
        try {
            File file = new File(libsFolder, libName);
            if (file.exists())
                return loadLibrary(file);
            else
                return LoadResult.FAILED_FILE;
        } catch (Throwable throwable) {
            PyCore.get().getLogger().error("Unable to load library '{}'", libName, throwable);
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
        PyCore.get().getLogger().info("Loading external libraries...");

        SortedSet<File> toLoad = new TreeSet<>();
        if (libsFolder.isDirectory()) {
            toLoad.addAll(Arrays.asList(libsFolder.listFiles()));
        }

        for (File library : toLoad) {
            if (library.isDirectory())
                continue;

            String libName = library.getName();
            if (libName.endsWith(".jar")) {
                if (!libName.endsWith("-relocated.jar")) {
                    try {
                        long start = System.nanoTime();
                        LoadResult result = loadLibrary(library);
                        double duration = (System.nanoTime() - start) / 1000000.0;
                        if (result == LoadResult.SUCCESS)
                            PyCore.get().getLogger().info("Loaded library '{}' in {} ms", libName, Math.round(duration * 10.0) / 10.0);
                    } catch (Throwable throwable) {
                        PyCore.get().getLogger().error("Unable to load library '{}'!", libName, throwable);
                    }
                }
            }
        }
    }

    private LoadResult loadLibrary(File lib) throws Exception {
        String libNameNoExt = lib.getName().replaceFirst("[.][^.]+$", "");
        File relocated = new File(libsFolder, libNameNoExt + "-relocated.jar");
        if (!relocated.exists()) {
            JarRelocator relocator = new JarRelocator(lib, relocated, relocations);
            relocator.run();
        }
        Callable<LoadResult> work = () -> {
            if (classLoader.isJarInClassPath(relocated.toPath()))
                return LoadResult.FAILED_LOADED;

            classLoader.addJarToClasspath(relocated.toPath());
            return LoadResult.SUCCESS;
        };
        return initializer.submit(work).get();
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
