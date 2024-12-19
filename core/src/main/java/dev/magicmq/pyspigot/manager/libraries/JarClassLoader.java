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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * Utility class for assisting with loading Jar files into the classpath.
 */
public class JarClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * Initialize a new JarClassLoader using a parent class loader.
     * @param parentClassLoader The parent class loader to use
     */
    public JarClassLoader(ClassLoader parentClassLoader) {
        super(new URL[]{}, parentClassLoader);
    }

    /**
     * Add a new Jar to the classpath.
     * @param file The Jar file to add to the classpath
     * @throws MalformedURLException If the file has an invalid URL
     */
    public void addJarToClasspath(Path file) throws MalformedURLException {
        addURL(file.toUri().toURL());
    }

    /**
     * Check if a Jar file is in the classpath.
     * @param file The Jar file to check
     * @return True if the Jar file is already in the classpath, false if otherwise
     */
    public boolean isJarInClassPath(Path file) {
        try {
            URL jarURL = file.toUri().toURL();
            URL[] loaded = getURLs();
            for (URL url : loaded) {
                if (url.equals(jarURL))
                    return true;
            }
            return false;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

