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

import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a registered BungeeCord command belonging to a script.
 * @see net.md_5.bungee.api.plugin.Command
 * @see net.md_5.bungee.api.plugin.TabExecutor
 */
public class BungeeScriptCommand extends Command implements TabExecutor, ScriptCommand {

    private final Script script;
    private final PyFunction commandFunction;
    private final PyFunction tabFunction;

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
    }

    /**
     * Get the script associated with this command.
     * @return The script associated with this command
     */
    public Script getScript() {
        return script;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            PyObject[] parameters = Py.javas2pys(sender, getName(), args);
            commandFunction.__call__(parameters[0], parameters[1], parameters[2]);
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when executing command '" + getName() + "'");
            //Mimic BungeeCord behavior
            sender.sendMessage(ChatColor.RED + "An internal error occurred whilst executing this command, please check the console log for details.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (tabFunction != null) {
            try {
                PyObject[] parameters = Py.javas2pys(sender, getName(), args);
                PyObject result = tabFunction.__call__(parameters[0], parameters[1], parameters[2]);
                if (result instanceof PyList pyList) {
                    ArrayList<String> toReturn = new ArrayList<>();
                    for (Object object : pyList) {
                        if (object instanceof String)
                            toReturn.add((String) object);
                        else {
                            script.getLogger().log(Level.WARNING, "Script tab complete function '" + tabFunction.__name__ + "' should return a list of str!");
                            return Collections.emptyList();
                        }
                    }
                    return toReturn;
                }
            } catch (PyException exception) {
                ScriptManager.get().handleScriptException(script, exception,  "Unhandled exception when tab completing command '" + getName() + "'");
            }
        }
        return Collections.emptyList();
    }
}
