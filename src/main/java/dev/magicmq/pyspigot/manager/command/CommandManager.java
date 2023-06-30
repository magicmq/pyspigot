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

package dev.magicmq.pyspigot.manager.command;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Manager to interface with Bukkit's command framework. Primarily used by scripts to register and unregister commands.
 */
public class CommandManager {

    private static CommandManager manager;

    private Method bSyncCommands;
    private SimpleCommandMap bCommandMap;
    private HashMap<String, Command> bKnownCommands;

    private final List<ScriptCommand> registeredCommands;

    private CommandManager() {
        bSyncCommands = ReflectionUtils.getMethod(Bukkit.getServer().getClass(), "syncCommands");
        if (bSyncCommands != null)
            bSyncCommands.setAccessible(true);
        try {
            bCommandMap = getCommandMap();
            bKnownCommands = getKnownCommands(bCommandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            PySpigot.get().getLogger().log(Level.SEVERE, "Error when initializing command manager:", e);
        }

        registeredCommands = new ArrayList<>();
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name) {
        return registerCommand(commandFunction, null, name, "/" + name, "", new ArrayList<>(), null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name) {
        return registerCommand(commandFunction, tabFunction, name, "/" + name, "", new ArrayList<>(), null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param description The description of the command
     * @param usage The usage message for the command
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, null, name, usage, description, new ArrayList<>(), null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param description The description of the command
     * @param usage The usage message for the command
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, tabFunction, name, usage, description, new ArrayList<>(), null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param description The description of the command
     * @param usage The usage message for the command
     * @param aliases A List of String containing all the aliases for this command
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, null, name, usage, description, aliases, null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param description The description of the command
     * @param usage The usage message for the command
     * @param aliases A List of String containing all the aliases for this command
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, tabFunction, name, usage, description, aliases, null, null);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new command.
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command to register
     * @param description The description of the command. Use an empty string for no description
     * @param usage The usage message for the command
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @param permissionMessage The message do display if there is insufficient permission to run the command. Can be null
     * @return A {@link ScriptCommand} representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission, String permissionMessage) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) commandFunction.__code__).co_filename);
        ScriptCommand command = getCommand(name);
        if (command == null) {
            ScriptCommand newCommand = new ScriptCommand(script, commandFunction, tabFunction, name, description, script.getName(), usage, aliases, permission, permissionMessage);
            newCommand.register(bCommandMap);
            registeredCommands.add(newCommand);
            syncCommands();
            return newCommand;
        } else
            throw new UnsupportedOperationException("Command with the name " + name + " is already registered!");
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Unregister a script's command.
     * @param name The name of the command to unregister.
     */
    public void unregisterCommand(String name) {
        ScriptCommand command = getCommand(name);
        unregisterCommand(command);
    }

    /**
     * Unregister a script's command.
     * @param command The command to be unregistered
     */
    public void unregisterCommand(ScriptCommand command) {
        if (registeredCommands.contains(command)) {
            command.unregister(bCommandMap, bKnownCommands);
            registeredCommands.remove(command);
            syncCommands();
        }
    }

    /**
     * Unregister all commands belonging to a particular script.
     * @param script The script from which all commands should be unregistered
     */
    public void unregisterCommands(Script script) {
        List<ScriptCommand> toRemove = new ArrayList<>();
        for (ScriptCommand command : registeredCommands) {
            if (command.getScript().equals(script))
                toRemove.add(command);
        }
        toRemove.forEach(this::unregisterCommand);
    }

    /**
     * Get a script command from its name.
     * @param name The name of the command to get
     * @return The command with this name, null if no command was found by the specified name
     */
    public ScriptCommand getCommand(String name) {
        for (ScriptCommand command : registeredCommands) {
            if (command.getName().equalsIgnoreCase(name))
                return command;
        }
        return null;
    }

    /**
     * Get an immutable list containing all commands belonging to a particular script.
     * @param script The script to get commands from
     * @return An immutable list containing all commands belonging to the script. Will return an empty list if no commands belong to the script
     */
    public List<ScriptCommand> getCommands(Script script) {
        List<ScriptCommand> commands = new ArrayList<>();
        for (ScriptCommand command : registeredCommands) {
            if (command.getScript().equals(script))
                commands.add(command);
        }
        return commands;
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

    private void syncCommands() {
        if (bSyncCommands != null) {
            try {
                bSyncCommands.invoke(Bukkit.getServer());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Get the singleton instance of this CommandManager
     * @return The instance
     */
    public static CommandManager get() {
        if (manager == null)
            manager = new CommandManager();
        return manager;
    }
}
