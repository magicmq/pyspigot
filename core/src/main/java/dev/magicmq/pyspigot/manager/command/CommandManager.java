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

package dev.magicmq.pyspigot.manager.command;

import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptContext;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract manager to interface with a server-specific command framework. Primarily used by scripts to register and unregister commands.
 */
public abstract class CommandManager {

    private static CommandManager instance;

    private final HashMap<Script, List<ScriptCommand>> registeredCommands;

    protected CommandManager() {
        instance = this;

        registeredCommands = new HashMap<>();
    }

    /**
     * Register a command with a platform's API.
     * @param script The script registering the command
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    protected abstract ScriptCommand registerCommandImpl(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission);

    /**
     * Unregister a command from a platform's API.
     * @param command The command to unregister
     */
    protected abstract void unregisterCommandImpl(ScriptCommand command);

    /**
     * Unregister a list of commands from a platform's API.
     * @param commands The list of commands to unregister
     */
    protected abstract void unregisterCommandsImpl(List<ScriptCommand> commands);

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name) {
        return registerCommand(commandFunction, null, name, "", "", new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name) {
        return registerCommand(commandFunction, tabFunction, name, "", "", new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String permission) {
        return registerCommand(commandFunction, null, name, "", "", new ArrayList<>(), permission);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String permission) {
        return registerCommand(commandFunction, tabFunction, name, "", "", new ArrayList<>(), permission);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, List<String> aliases, String permission) {
        return registerCommand(commandFunction, null, name, "", "", aliases, permission);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, List<String> aliases, String permission) {
        return registerCommand(commandFunction, tabFunction, name, "", "", aliases, permission);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, null, name, description, usage, new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, tabFunction, name, description, usage, new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, null, name, description, usage, aliases, null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, tabFunction, name, description, usage, aliases, null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        Script script = ScriptContext.require();
        ScriptCommand command = getCommand(script, name);
        if (command == null) {
            ScriptCommand newCommand = registerCommandImpl(script, commandFunction, tabFunction, name, description, usage, aliases, permission);
            addCommand(script, newCommand);
            return newCommand;
        } else
            throw new ScriptRuntimeException(script, "Command '" + name + "' is already registered");
    }

    /**
     * Set the tab completion function for a command that was registered previously.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command that was previously registered
     */
    public void registerTabFunction(PyFunction tabFunction, String name) {
        Script script = ScriptContext.require();
        ScriptCommand command = getCommand(script, name);
        if (command != null)
            command.setTabFunction(tabFunction);
        else
            throw new ScriptRuntimeException(script, "Could not find a registered command with the name '" + name + "'");
    }

    /**
     * Unregister a script's command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param command The command to be unregistered
     */
    public void unregisterCommand(ScriptCommand command) {
        unregisterCommandImpl(command);
        removeCommand(command.getScript(), command);
    }

    /**
     * Unregister a script's command. Note that multiple listeners may be unregistered, if multiple listeners are registered to the same function.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function associated with the command to be unregistered
     */
    public void unregisterCommand(PyFunction commandFunction) {
        Script script = ScriptContext.require();
        List<ScriptCommand> commands = getCommands(script);
        for (ScriptCommand command : commands) {
            if (command.getCommandFunction().equals(commandFunction)) {
                unregisterCommandImpl(command);
                removeCommand(command.getScript(), command);
            }
        }
    }

    /**
     * Unregister all commands belonging to a particular script.
     * @param script The script from which all commands should be unregistered
     */
    public void unregisterCommands(Script script) {
        List<ScriptCommand> associatedCommands = getCommands(script);
        if (!associatedCommands.isEmpty()) {
            unregisterCommandsImpl(associatedCommands);
            removeCommands(script);
        }
    }

    /**
     * Get a command associated with a particular script by the command name
     * @param script The script
     * @param name The name of the command
     * @return The command with this name and associated with the script, or null if none was found
     */
    public ScriptCommand getCommand(Script script, String name) {
        List<ScriptCommand> scriptCommands = getCommands(script);
        for (ScriptCommand command : scriptCommands) {
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
        List<ScriptCommand> scriptCommands = registeredCommands.get(script);
        return scriptCommands != null ? List.copyOf(scriptCommands) : List.of();
    }

    protected void addCommand(Script script, ScriptCommand command) {
        if (registeredCommands.containsKey(script))
            registeredCommands.get(script).add(command);
        else {
            List<ScriptCommand> scriptCommands = new ArrayList<>();
            scriptCommands.add(command);
            registeredCommands.put(script, scriptCommands);
        }
    }

    private void removeCommand(Script script, ScriptCommand command) {
        List<ScriptCommand> scriptCommands = registeredCommands.get(script);
        scriptCommands.remove(command);
        if (scriptCommands.isEmpty())
            registeredCommands.remove(script);
    }

    private void removeCommands(Script script) {
        registeredCommands.remove(script);
    }

    /**
     * Get the singleton instance of this CommandManager.
     * @return The instance
     */
    public static CommandManager get() {
        return instance;
    }
}
