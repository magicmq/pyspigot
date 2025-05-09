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

package dev.magicmq.pyspigot.velocity.manager.command;


import com.velocitypowered.api.command.CommandMeta;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.command.CommandManager;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

public class VelocityCommandManager extends CommandManager {

    private static VelocityCommandManager instance;

    private VelocityCommandManager() {
        super();
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name) {
        return registerCommand(commandFunction, tabFunction, asyncTabComplete, name, "", "", new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String permission) {
        return registerCommand(commandFunction, tabFunction, asyncTabComplete, name, "", "", new ArrayList<>(), permission);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, List<String> aliases, String permission) {
        return registerCommand(commandFunction, tabFunction, asyncTabComplete, name, "", "", aliases, null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String description, String usage) {
        return registerCommand(commandFunction, tabFunction, asyncTabComplete, name, description, usage, new ArrayList<>(), null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, tabFunction, asyncTabComplete, name, description, usage, aliases, null);
    }

    /**
     * Register a new command.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param asyncTabComplete Pass true if the tab complete function should be run asynchronously; pass false if it should not.
     *                         Setting this to true can be useful in cases where more extensive logic is used to provide
     *                         tab complete suggestions.
     * @param name The name of the command to register
     * @param description The description of the command. Can be null (or an empty string)
     * @param usage The usage message for the command. Can be null (or an empty string)
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     * @return A ScriptCommand representing the command that was registered
     */
    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String description, String usage, List<String> aliases, String permission) {
        Script script = ScriptUtils.getScriptFromCallStack();
        ScriptCommand command = getCommand(script, name);
        if (command == null) {
            ScriptCommand newCommand = registerCommandImpl(script, commandFunction, tabFunction, asyncTabComplete, name, description, usage, aliases, permission);
            addCommand(script, newCommand);
            return newCommand;
        } else
            throw new ScriptRuntimeException(script, "Command '" + name + "' is already registered");
    }

    @Override
    protected ScriptCommand registerCommandImpl(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        return registerCommandImpl(script, commandFunction, tabFunction, true, name, description, usage, aliases, permission);
    }

    /**
     * Although the description and usage parameters are no-op on Velocity (I.E. Velocity commands don't
     * support specifying these, they are kept here and in other methods of this class for consistency
     * with the core {@link CommandManager}
     */
    protected ScriptCommand registerCommandImpl(Script script, PyFunction commandFunction, PyFunction tabFunction, boolean asyncTabComplete, String name, String description, String usage, List<String> aliases, String permission) {
        com.velocitypowered.api.command.CommandManager commandManager = PyVelocity.get().getProxy().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(name)
                .aliases(aliases.toArray(new String[0]))
                .plugin(PyVelocity.get())
                .build();
        VelocityScriptCommand command = new VelocityScriptCommand(commandMeta, script, commandFunction, tabFunction, asyncTabComplete, name, permission);
        commandManager.register(commandMeta, command);

        return command;
    }

    @Override
    protected void unregisterCommandImpl(ScriptCommand command) {
        PyVelocity.get().getProxy().getCommandManager().unregister(((VelocityScriptCommand) command).getCommandMeta());
    }

    @Override
    protected void unregisterCommandsImpl(List<ScriptCommand> commands) {
        commands.forEach(this::unregisterCommandImpl);
    }

    /**
     * Get the singleton instance of this VelocityCommandManager.
     * @return The instance
     */
    public static VelocityCommandManager get() {
        if (instance == null)
            instance = new VelocityCommandManager();
        return instance;
    }
}
