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

