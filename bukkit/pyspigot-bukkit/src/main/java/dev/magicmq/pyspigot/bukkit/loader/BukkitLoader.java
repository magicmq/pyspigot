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

package dev.magicmq.pyspigot.bukkit.loader;


import dev.magicmq.pyspigot.loader.JarInJarClassLoader;
import dev.magicmq.pyspigot.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The loader plugin for the Bukkit platform. This is the true entry point for PySpigot on Bukkit.
 */
public class BukkitLoader extends JavaPlugin {

    private static final String JAR_NAME = "pyspigot-bukkit-impl.jar";
    private static final String MAIN_CLASS = "dev.magicmq.pyspigot.bukkit.PySpigot";

    private final LoaderBootstrap plugin;

    /**
     * Creates a new {@link JarInJarClassLoader} and instantiates the bootstrap plugin via the class loader.
     */
    public BukkitLoader() {
        JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
        this.plugin = loader.instantiatePlugin(MAIN_CLASS, JavaPlugin.class, this);
    }

    @Override
    public void onEnable() {
        plugin.onEnable();
    }

    @Override
    public void onLoad() {
        plugin.onLoad();
    }

    @Override
    public void onDisable() {
        plugin.onDisable();
    }

}
