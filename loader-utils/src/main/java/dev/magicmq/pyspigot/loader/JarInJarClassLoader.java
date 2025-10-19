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

package dev.magicmq.pyspigot.loader;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Class loader which loads a JAR file contained within another JAR file.
 * <p>
 * The "loader" JAR contains the loading code and public API classes, and is class-loaded by the platform.
 * <p>
 * The inner "plugin" JAR contains the plugin itself, and is class-loaded by the loading code and this class loader.
 */
public class JarInJarClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     *
     * @param parent the loader plugin's class loader (setup and created by the platform)
     * @param jarResourcePath the path to the inner JAR file within the loader JAR
     */
    public JarInJarClassLoader(ClassLoader parent, String jarResourcePath) {
        super(new URL[]{extractJar(parent, jarResourcePath)}, parent);
    }

    /**
     * Add a JAR to the class path.
     * @param url The URL pointing to the JAR file to add to the class path
     */
    public void addJarToClassPath(URL url) {
        addURL(url);
    }

    /**
     * Delete the inner JAR for this class loader.
     */
    public void deleteJarResource() {
        URL[] urls = getURLs();
        if (urls.length == 0) {
            return;
        }

        try {
            Path path = Paths.get(urls[0].toURI());
            Files.deleteIfExists(path);
        } catch (Exception e) {}
    }

    /**
     * Create a new plugin instance.
     * @param bootstrapClass The name of the boostrap plugin class
     * @param loaderPluginType The type of the loader plugin, should also be the only parameter of the boostrap plugin's constructor
     * @param loaderPlugin The loader plugin instance
     * @return The instantiated bootstrap plugin
     * @param <T> The type of the loader plugin
     */
    public <T> LoaderBootstrap instantiatePlugin(String bootstrapClass, Class<T> loaderPluginType, T loaderPlugin) {
        Class<? extends LoaderBootstrap> plugin;
        try {
            plugin = loadClass(bootstrapClass).asSubclass(LoaderBootstrap.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to load bootstrap class", e);
        }

        Constructor<? extends LoaderBootstrap> constructor;
        try {
            constructor = plugin.getConstructor(loaderPluginType);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to get bootstrap constructor", e);
        }

        try {
            return constructor.newInstance(loaderPlugin);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Unable to create bootstrap plugin instance", e);
        }
    }

    private static URL extractJar(ClassLoader loaderClassLoader, String jarResourcePath) {
        URL jarInJar = loaderClassLoader.getResource(jarResourcePath);
        if (jarInJar == null) {
            throw new RuntimeException("Could not locate jar-in-jar");
        }

        Path path;
        try {
            path = Files.createTempFile("pyspigot", ".jar.tmp");
        } catch (IOException e) {
            throw new RuntimeException("Unable to create a temporary file", e);
        }

        path.toFile().deleteOnExit();

        try (InputStream in = jarInJar.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to copy jar-in-jar to temporary path", e);
        }

        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unable to get URL from path", e);
        }
    }
}
