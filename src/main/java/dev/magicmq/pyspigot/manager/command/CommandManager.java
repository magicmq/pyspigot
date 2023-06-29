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

    public ScriptCommand registerCommand(PyFunction commandFunction, String name) {
        return registerCommand(commandFunction, null, name, "/" + name, "", new ArrayList<>(), null, null);
    }

    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name) {
        return registerCommand(commandFunction, tabFunction, name, "/" + name, "", new ArrayList<>(), null, null);
    }

    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, null, name, usage, description, new ArrayList<>(), null, null);
    }

    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage) {
        return registerCommand(commandFunction, tabFunction, name, usage, description, new ArrayList<>(), null, null);
    }

    public ScriptCommand registerCommand(PyFunction commandFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, null, name, usage, description, aliases, null, null);
    }

    public ScriptCommand registerCommand(PyFunction commandFunction, PyFunction tabFunction, String name, String description, String usage, List<String> aliases) {
        return registerCommand(commandFunction, tabFunction, name, usage, description, aliases, null, null);
    }

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

    public void unregisterCommand(String name) {
        ScriptCommand command = getCommand(name);
        unregisterCommand(command);
    }

    public void unregisterCommand(ScriptCommand command) {
        if (registeredCommands.contains(command)) {
            command.unregister(bCommandMap, bKnownCommands);
            registeredCommands.remove(command);
            syncCommands();
        }
    }

    public void unregisterCommands(Script script) {
        List<ScriptCommand> toRemove = new ArrayList<>();
        for (ScriptCommand command : registeredCommands) {
            if (command.getScript().equals(script))
                toRemove.add(command);
        }
        toRemove.forEach(this::unregisterCommand);
    }

    public ScriptCommand getCommand(String name) {
        for (ScriptCommand command : registeredCommands) {
            if (command.getName().equalsIgnoreCase(name))
                return command;
        }
        return null;
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

    public static CommandManager get() {
        if (manager == null)
            manager = new CommandManager();
        return manager;
    }
}
