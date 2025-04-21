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

import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.bukkit.util.CommandAliasHelpTopic;
import dev.magicmq.pyspigot.manager.command.ScriptCommand;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpMap;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicComparator;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.plugin.Plugin;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.ThreadState;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Represents a registered Bukkit command belonging to a script.
 * @see org.bukkit.command.TabExecutor
 * @see org.bukkit.command.defaults.BukkitCommand
 */
public class BukkitScriptCommand implements TabExecutor, ScriptCommand {

    private final Script script;
    private final PyFunction commandFunction;
    private final PyFunction tabFunction;
    private final String name;
    private final PluginCommand bukkitCommand;

    private List<HelpTopic> helps;

    /**
     *
     * @param script The script to which this command belongs
     * @param commandFunction The command function that should be called when the command is executed
     * @param tabFunction The tab function that should be called for tab completion of the command. Can be null
     * @param name The name of the command to register
     * @param description The description of the command. Use an empty string for no description
     * @param usage The usage message for the command
     * @param aliases A List of String containing all the aliases for this command. Use an empty list for no aliases
     * @param permission The required permission node to use this command. Can be null
     */
    public BukkitScriptCommand(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases, String permission) {
        this.script = script;
        this.commandFunction = commandFunction;
        this.tabFunction = tabFunction;
        this.name = name;

        try {
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            final PluginCommand bukkitCommand = constructor.newInstance(name, PySpigot.get());
            bukkitCommand.setLabel(name.toLowerCase());
            bukkitCommand.setDescription(description);
            bukkitCommand.setUsage(usage);
            bukkitCommand.setAliases(aliases);
            bukkitCommand.setPermission(permission);
            bukkitCommand.setExecutor(this);
            this.bukkitCommand = bukkitCommand;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            //This should not happen, reflection checks done on plugin enable
            throw new RuntimeException("Unhandled exception when initializing command '" + name + "'", e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(sender, label, args);
            PyObject result = commandFunction.__call__(threadState, parameters[0], parameters[1], parameters[2]);
            if (result instanceof PyBoolean)
                return ((PyBoolean) result).getBooleanValue();
            else
                script.getLogger().log(Level.WARNING, "Script command function '" + commandFunction.__name__ + "' should return a boolean!");
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when executing command '" + label + "'");
            //Mimic Bukkit behavior
            sender.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (tabFunction != null) {
            try {
                Py.setSystemState(script.getInterpreter().getSystemState());
                ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                PyObject[] parameters = Py.javas2pys(sender, alias, args);
                PyObject result = tabFunction.__call__(threadState, parameters[0], parameters[1], parameters[2]);
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
                ScriptManager.get().handleScriptException(script, exception, "Unhandled exception when tab completing command '" + bukkitCommand.getLabel() + "'");
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get the script associated with this command.
     * @return The script associated with this command
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the name of this command.
     * @return The name of this command
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link org.bukkit.command.PluginCommand} that underlies this ScriptCommand
     * @return The underlying PluginCommand
     */
    public PluginCommand getBukkitCommand() {
        return bukkitCommand;
    }

    protected void initHelp() {
        helps = new ArrayList<>();
        HelpMap helpMap = Bukkit.getHelpMap();
        HelpTopic helpTopic = new GenericCommandHelpTopic(bukkitCommand);
        helpMap.addTopic(helpTopic);
        helps.add(helpTopic);

        HelpTopic aliases = helpMap.getHelpTopic("Aliases");
        if (aliases instanceof IndexHelpTopic) {
            aliases.getFullText(Bukkit.getConsoleSender());
            try {
                Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
                topics.setAccessible(true);
                List<HelpTopic> aliasTopics = new ArrayList<>((Collection<HelpTopic>) topics.get(aliases));
                for (String alias : bukkitCommand.getAliases()) {
                    HelpTopic toAdd = new CommandAliasHelpTopic("/" + alias, "/" + bukkitCommand.getLabel(), helpMap);
                    aliasTopics.add(toAdd);
                    helps.add(toAdd);
                }
                aliasTopics.sort(HelpTopicComparator.helpTopicComparatorInstance());
                topics.set(aliases, aliasTopics);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //This should not happen, reflection checks done on plugin enable
                throw new RuntimeException("Unhandled exception when initializing command '" + name + "'", e);
            }
        }
    }

    protected void removeHelp() {
        Bukkit.getHelpMap().getHelpTopics().removeAll(helps);

        HelpTopic aliases = Bukkit.getHelpMap().getHelpTopic("Aliases");
        if (aliases instanceof IndexHelpTopic) {
            try {
                Field topics = IndexHelpTopic.class.getDeclaredField("allTopics");
                topics.setAccessible(true);
                List<HelpTopic> aliasTopics = new ArrayList<>((Collection<HelpTopic>) topics.get(aliases));
                aliasTopics.removeAll(helps);
                topics.set(aliases, aliasTopics);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                //This should not happen, reflection checks done on plugin enable
                throw new RuntimeException("Unhandled exception when unregistering command '" + name + "'", e);
            }
        }
    }

    /**
     * Prints a representation of this ScriptCommand in string format, including all variables that pertain to the command (such as name, label, description, etc.)
     * @return A string representation of the ScriptCommand
     */
    @Override
    public String toString() {
        return String.format("ScriptCommand[Name: %s, Label: %s, Description: %s, Usage: %s, Aliases: %s, Permission: %s, Permission Message: %s]",
                name,
                bukkitCommand.getLabel(),
                bukkitCommand.getDescription(),
                bukkitCommand.getUsage(),
                bukkitCommand.getAliases(),
                bukkitCommand.getPermission(),
                bukkitCommand.getPermissionMessage());
    }
}
