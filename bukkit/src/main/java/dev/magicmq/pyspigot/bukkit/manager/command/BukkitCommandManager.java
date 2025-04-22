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

package dev.magicmq.pyspigot.bukkit.manager.command;

import dev.magicmq.pyspigot.bukkit.util.ReflectionUtils;
import dev.magicmq.pyspigot.exception.PluginInitializationException;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.python.core.PyFunction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * The Bukkit-specific implementation of the command manager.
 */
public class BukkitCommandManager extends CommandManager {

    private static BukkitCommandManager instance;

    private final Method bSyncCommands;

    private SimpleCommandMap bCommandMap;
    private HashMap<String, Command> bKnownCommands;

    private BukkitCommandManager() {
        super();

        bSyncCommands = ReflectionUtils.getMethod(Bukkit.getServer().getClass(), "syncCommands");
        if (bSyncCommands != null)
            bSyncCommands.setAccessible(true);
        try {
            bCommandMap = getCommandMap();
            bKnownCommands = getKnownCommands(bCommandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            //This should not happen, reflection checks done on plugin enable
            throw new PluginInitializationException("Error when initializing command manager, commands will not work correctly.", e);
        }
    }

    @Override
    protected ScriptCommand registerCommandImpl(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        BukkitScriptCommand newCommand = new BukkitScriptCommand(script, commandFunction, tabFunction, name, description, usage, aliases, permission);
        if (!addCommandToBukkit(newCommand))
            script.getLogger().log(Level.WARNING, "Used fallback prefix (script name) when registering command '" + name + "'");
        try {
            syncBukkitCommands();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ScriptRuntimeException(script, "Unhandled exception when syncing commands via Bukkit", e);
        }
        newCommand.initHelp();
        return newCommand;
    }

    @Override
    protected void unregisterCommandImpl(ScriptCommand command) {
        removeCommandFromBukkit((BukkitScriptCommand) command);
        ((BukkitScriptCommand) command).removeHelp();
        try {
            syncBukkitCommands();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ScriptRuntimeException(command.getScript(), "Unhandled exception when syncing commands via Bukkit", e);
        }
    }

    @Override
    protected void unregisterCommandsImpl(List<ScriptCommand> commands) {
        for (ScriptCommand command : commands) {
            removeCommandFromBukkit((BukkitScriptCommand) command);
            ((BukkitScriptCommand) command).removeHelp();
        }
        try {
            syncBukkitCommands();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error when syncing commands via Bukkit", e);
        }
    }

    private boolean addCommandToBukkit(BukkitScriptCommand command) {
        return bCommandMap.register(command.getScript().getName(), command.getBukkitCommand());
    }

    private void removeCommandFromBukkit(BukkitScriptCommand command) {
        command.getBukkitCommand().unregister(bCommandMap);
        bKnownCommands.remove(command.getBukkitCommand().getLabel());
        for (String alias : command.getBukkitCommand().getAliases())
            bKnownCommands.remove(alias);
    }

    private void syncBukkitCommands() throws IllegalAccessException, InvocationTargetException {
        if (bSyncCommands != null) {
            bSyncCommands.invoke(Bukkit.getServer());
        }
    }

    private SimpleCommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (SimpleCommandMap) field.get(Bukkit.getServer());
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Command> getKnownCommands(SimpleCommandMap commandMap) throws NoSuchFieldException, IllegalAccessException {
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        return (HashMap<String, Command>) field.get(commandMap);
    }

    /**
     * Get the singleton instance of this BukkitCommandManager.
     * @return The instance
     */
    public static BukkitCommandManager get() {
        if (instance == null)
            instance = new BukkitCommandManager();
        return instance;
    }
}

