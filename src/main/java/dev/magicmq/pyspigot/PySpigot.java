package dev.magicmq.pyspigot;

import dev.magicmq.pyspigot.commands.PySpigotCommand;
import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.managers.command.CommandManager;
import dev.magicmq.pyspigot.managers.config.ConfigManager;
import dev.magicmq.pyspigot.managers.listener.ListenerManager;
import dev.magicmq.pyspigot.managers.protocol.ProtocolManager;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import dev.magicmq.pyspigot.managers.task.TaskManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author magicmq
 */
public class PySpigot extends JavaPlugin {

    private static PySpigot instance;

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        getCommand("pyspigot").setExecutor(new PySpigotCommand());

        ScriptManager.get();
        ListenerManager.get();
        CommandManager.get();
        TaskManager.get();
        ConfigManager.get();
    }

    @Override
    public void onDisable() {
        TaskManager.get().shutdown();
        CommandManager.get().shutdown();
        ScriptManager.get().shutdown();
    }

    public void reload() {
        reloadConfig();
        PluginConfig.reload();
    }

    public static PySpigot get() {
        return instance;
    }
}
