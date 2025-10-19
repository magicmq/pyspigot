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

package dev.magicmq.pyspigot.classpath;


import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.loader.JarInJarClassLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * The class path appender for platforms that load via a JAR-in-JAR fasion (I.E. with a plugin bootstrap).
 * <p>
 * Currently, Bukkit and BungeeCord require a loader bootstrap, because their plugin class loaders do not support adding
 * additional JARs at runtime.
 */
public class JarInJarClassPathAppender implements ClassPathAppender {

    private final JarInJarClassLoader classLoader;

    public JarInJarClassPathAppender(ClassLoader classLoader) {
        this.classLoader = (JarInJarClassLoader) classLoader;
    }

    @Override
    public void addJarToClassPath(Path file) {
        try {
            this.classLoader.addJarToClassPath(file.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.classLoader.deleteJarResource();
        try {
            this.classLoader.close();
        } catch (IOException e) {
            PyCore.get().getLogger().error("An error occurred when closing the class loader", e);
        }
    }

}
