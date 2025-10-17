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

package dev.magicmq.pyspigot.dependency;


import dev.magicmq.pyspigot.classpath.ClassPathAppender;
import dev.magicmq.pyspigot.exception.DependencyDownloadException;
import me.lucko.jarrelocator.JarRelocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A manager class, which handles dynamically downloading and adding PySpigot's dependencies to the class path at runtime.
 */
public class DependencyManager {

    private final ExecutorService downloader;
    private final ClassPathAppender classPathAppender;
    private final Path depsFolderPath;

    /**
     *
     * @param classPathAppender The platform-specific class path appender
     * @param pluginDataFolder The plugin data folder
     */
    public DependencyManager(ClassPathAppender classPathAppender, Path pluginDataFolder) {
        this.downloader = Executors.newFixedThreadPool(4);
        this.classPathAppender = classPathAppender;
        this.depsFolderPath = pluginDataFolder.resolve("java-libs").resolve("internal");
        try {
            Files.createDirectories(this.depsFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create dependencies directory", e);
        }
    }

    /**
     * Shut down this DependencyManager. This method also shuts down the attached ClassPathAppender.
     */
    public void shutdown() {
        downloader.shutdownNow();

        classPathAppender.close();
    }

    /**
     * Get the ClassPathAppender attached to this DependencyManager.
     * @return The ClassPathAppender
     */
    public ClassPathAppender getClassPathAppender() {
        return classPathAppender;
    }

    /**
     * Load all dependencies. This method loads and adds to the class path any dependencies registered in the {@link DependencyRegistry}.
     */
    public void loadDependencies() {
        List<Dependency> dependencies = DependencyRegistry.getDependencies();

        CountDownLatch latch = new CountDownLatch(dependencies.size());

        for (Dependency dependency : dependencies) {
            downloader.execute(() -> {
                try {
                    loadDependency(dependency);
                } catch (Throwable e) {
                    throw new RuntimeException("Unable to load depdendency '" + dependency + "'", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Load a dependency.
     * @param dependency The dependency to load
     * @throws Exception If an error occured while loading the dependency
     */
    public void loadDependency(Dependency dependency) throws Exception {
        Path dependencyPath = remapDependency(dependency, downloadDependency(dependency));
        classPathAppender.addJarToClassPath(dependencyPath);
    }

    private Path downloadDependency(Dependency dependency) throws DependencyDownloadException {
        Path dependencyPath = depsFolderPath.resolve(dependency.getFilePath());

        if (Files.exists(dependencyPath)) {
            return dependencyPath;
        }

        DependencyDownloadException lastException = null;

        for (DependencyRepository repo : DependencyRepository.values()) {
            try {
                repo.download(dependency, dependencyPath);
                return dependencyPath;
            } catch (DependencyDownloadException e) {
                lastException = e;
            }
        }

        throw Objects.requireNonNull(lastException);
    }

    private Path remapDependency(Dependency dependency, Path original) throws IOException {
        Path remapped = depsFolderPath.resolve(dependency.getRelocatedFilePath());
        if (Files.exists(remapped)) {
            return remapped;
        }

        JarRelocator relocator = new JarRelocator(original.toFile(), remapped.toFile(), DependencyRegistry.getRelocations());
        relocator.run();

        return remapped;
    }

}
