package dev.magicmq.pyspigot.manager.libraries;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.config.PluginConfig;
import me.lucko.jarrelocator.JarRelocator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

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

    public void initLibraries() {
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

    public JarClassLoader getClassLoader() {
        return classLoader;
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

    public static LibraryManager get() {
        if (instance == null)
            instance = new LibraryManager();
        return instance;
    }

    public enum LoadResult {

        FAILED_FILE,
        FAILED_LOADED,
        FAILED_ERROR,
        SUCCESS

    }
}
