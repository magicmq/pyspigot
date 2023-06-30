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

package dev.magicmq.pyspigot;

import dev.magicmq.pyspigot.command.PySpigotCommand;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.config.ConfigManager;
import dev.magicmq.pyspigot.manager.listener.ListenerManager;
import dev.magicmq.pyspigot.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the plugin.
 */
public class PySpigot extends JavaPlugin {

    private static PySpigot instance;

    //Define static variables for ease of access by scripts
    public static ListenerManager listener;
    public static CommandManager command;
    public static TaskManager scheduler;
    public static ConfigManager config;
    public static ProtocolManager protocol;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        getCommand("pyspigot").setExecutor(new PySpigotCommand());

        ScriptManager.get();
        listener = ListenerManager.get();
        command = CommandManager.get();
        scheduler = TaskManager.get();
        config = ConfigManager.get();

        if (isProtocolLibAvailable())
            protocol = ProtocolManager.get();
    }

    @Override
    public void onDisable() {
        ScriptManager.get().shutdown();
    }

    /**
     * Reload the plugin configuration.
     */
    public void reload() {
        reloadConfig();
        PluginConfig.reload();
    }

    /**
     * Check if ProtocolLib is available on the server.
     * @return True if ProtocolLib is loaded and enabled, false if otherwise.
     */
    public boolean isProtocolLibAvailable() {
        return Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
    }

    /**
     * Get the instance of this plugin.
     * @return The instance
     */
    public static PySpigot get() {
        return instance;
    }
}
