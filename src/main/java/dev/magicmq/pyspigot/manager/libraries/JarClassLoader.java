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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class JarClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public JarClassLoader(ClassLoader parentClassLoader) {
        super(new URL[]{}, parentClassLoader);
    }

    public void addJarToClasspath(Path file) throws MalformedURLException {
        addURL(file.toUri().toURL());
    }

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

