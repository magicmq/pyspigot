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

package dev.magicmq.pyspigot.bungee.manager.command;

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.ScriptContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.ThreadState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a registered BungeeCord command belonging to a script.
 * @see net.md_5.bungee.api.plugin.Command
 * @see net.md_5.bungee.api.plugin.TabExecutor
 */
public class BungeeScriptCommand extends Command implements TabExecutor, ScriptCommand {

    private final Script script;
    private final PyFunction commandFunction;
    private final String name;
    private final List<String> aliases;
    private final String permission;

    private PyFunction tabFunction;

    /**
     *
     * @param script The script to which this command belongs
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command to register
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     */
    public BungeeScriptCommand(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, List<String> aliases, String permission) {
        super(name, permission, aliases.toArray(new String[0]));
        this.script = script;
        this.commandFunction = commandFunction;
        this.tabFunction = tabFunction;
        this.name = name;
        this.aliases = aliases;
        this.permission = permission;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public PyFunction getCommandFunction() {
        return commandFunction;
    }

    @Override
    public void setTabFunction(PyFunction tabFunction) {
        this.tabFunction = tabFunction;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(sender, getName(), args);
            ScriptContext.runWith(script, () -> commandFunction.__call__(threadState, parameters[0], parameters[1], parameters[2]));
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when executing command '" + getName() + "'");
            //Mimic BungeeCord behavior
            PyBungee.get().getAdventure().sender(sender).sendMessage(Component.text("An internal error occurred whilst executing this command, please check the console log for details.", NamedTextColor.RED));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (tabFunction != null) {
            try {
                Py.setSystemState(script.getInterpreter().getSystemState());
                ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                PyObject[] parameters = Py.javas2pys(sender, getName(), args);
                PyObject result = ScriptContext.supplyWith(script, () -> tabFunction.__call__(threadState, parameters[0], parameters[1], parameters[2]));
                if (result instanceof PyList pyList) {
                    ArrayList<String> toReturn = new ArrayList<>();
                    for (Object object : pyList) {
                        if (object instanceof String)
                            toReturn.add((String) object);
                        else {
                            script.getLogger().warn("Script tab complete function '{}' should return a list of str", tabFunction.__name__);
                            return Collections.emptyList();
                        }
                    }
                    return toReturn;
                } else
                    script.getLogger().warn("Script tab complete function '{}' should return a list of str", tabFunction.__name__);
            } catch (PyException exception) {
                ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when tab completing command '" + getName() + "'");
            }
        }
        return Collections.emptyList();
    }

    /**
     * Prints a representation of this BungeeScriptCommand in string format, including all variables that pertain to the command (such as name, label, description, etc.)
     * @return A string representation of the BungeeScriptCommand
     */
    @Override
    public String toString() {
        return String.format("BungeeScriptCommand[Name: %s, Aliases: %s, Permission: %s]", name, aliases, permission);
    }
}
