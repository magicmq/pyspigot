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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A manager class, which handles dynamically downloading and adding PySpigot's dependencies to the class path at runtime.
 */
public class DependencyManager {

    private final Path depsFolderPath;
    private final ClassPathAppender classPathAppender;
    private final DependencyRegistry registry;

    /**
     *
     * @param classPathAppender The platform-specific class path appender
     * @param pluginDataFolder The plugin data folder
     */
    public DependencyManager(ClassPathAppender classPathAppender, Path pluginDataFolder) {
        this.depsFolderPath = pluginDataFolder.resolve("java-libs").resolve("internal");
        try {
            Files.createDirectories(this.depsFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create dependencies directory", e);
        }
        this.classPathAppender = classPathAppender;

        initSelfDependencies();

        this.registry = new DependencyRegistry();
        this.registry.loadDependencies();
    }

    /**
     * Shut down this DependencyManager. This method also shuts down the attached ClassPathAppender.
     */
    public void shutdown() {
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
        List<Dependency> dependencies = registry.getDependencies();

        CountDownLatch latch = new CountDownLatch(dependencies.size());
        ExecutorService downloader = Executors.newFixedThreadPool(4);

        for (Dependency dependency : dependencies) {
            downloader.execute(() -> {
                try {
                    loadDependency(dependency);
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to load depdendency '" + dependency + "'", t);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            downloader.shutdownNow();
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

        me.lucko.jarrelocator.JarRelocator relocator = new me.lucko.jarrelocator.JarRelocator(original.toFile(), remapped.toFile(), registry.getRelocations());
        relocator.run();

        return remapped;
    }

    private void initSelfDependencies() {
        List<Dependency> selfDeps = new ArrayList<>();

        //GSON (for loading dependency JSON files)
        selfDeps.add(Dependency.builder()
                .groupId("com.google.code.gson")
                .artifactId("gson")
                .version("2.13.2")
                .checksum("3QzhtVo+0ggMtw+cZVhQzahsIGhiMQAJ3LXlyVJlpeA=")
                .build());
        selfDeps.add(Dependency.builder()
                .groupId("com.google.errorprone")
                .artifactId("error_prone_annotations")
                .version("2.41.0")
                .checksum("pW54K1tQgRrCBAc6NVoh2RWiEH/OE+xxEzGtA29mD8w=")
                .build());

        //jar-relocator (for remapping dependencies)
        selfDeps.add(Dependency.builder()
                .groupId("me.lucko")
                .artifactId("jar-relocator")
                .version("1.7")
                .checksum("b30RhOF6kHiHl+O5suNLh/+eAr1iOFEFLXhwkHHDu4I=")
                .build());
        selfDeps.add(Dependency.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm")
                .version("9.2")
                .checksum("udT+TXGTjfOIOfDspCqqpkz4sxPWeNoDbwyzyhmbR/U=")
                .build());
        selfDeps.add(Dependency.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm-commons")
                .version("9.2")
                .checksum("vkzlMTiiOLtSLNeBz5Hzulzi9sqT7GLUahYqEnIl4KY=")
                .build());
        selfDeps.add(Dependency.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm-tree")
                .version("9.2")
                .checksum("qr+b0jCRpOv8EJwfPufPPkuJ9rotP1HFJD8Ws8/64BE=")
                .build());
        selfDeps.add(Dependency.builder()
                .groupId("org.ow2.asm")
                .artifactId("asm-analysis")
                .version("9.2")
                .checksum("h4++UhcxwHLRTS1luYOxvq5q0G/aAAe2qLroH3P0M8Q=")
                .build());

        ExecutorService service = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(selfDeps.size());

        for (Dependency dependency : selfDeps) {
            service.execute(() -> {
                try {
                    Path path = downloadDependency(dependency);
                    classPathAppender.addJarToClassPath(path);
                } catch (Throwable t) {
                    throw new RuntimeException("Unable to load depdendency '" + dependency + "'", t);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            service.shutdownNow();
        }
    }
}
