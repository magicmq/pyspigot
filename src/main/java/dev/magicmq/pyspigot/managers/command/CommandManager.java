package dev.magicmq.pyspigot.managers.command;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import dev.magicmq.pyspigot.utils.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CommandManager {

    private static CommandManager manager;

    private CommandMap commandMap;
    private HashMap<String, Command> knownCommands;
    private final List<ScriptCommand> registeredCommands;

    private CommandManager() {
        try {
            commandMap = getCommandMap();
            knownCommands = getKnownCommands(commandMap);
        } catch (NoSuchFieldException |IllegalAccessException e) {
            e.printStackTrace();
        }

        registeredCommands = new ArrayList<>();
    }

    public void shutdown() {
        for (ScriptCommand command : registeredCommands) {
            command.unregister(commandMap);
            knownCommands.remove(command.getName());
        }
    }

    public void registerCommand(PyFunction function, String name) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.getFuncCode()).co_filename);
        ScriptCommand command = new ScriptCommand(script, function, name);
        boolean registered = commandMap.register(script.getName(), command);
        if (registered)
            registeredCommands.add(command);
        else
            PySpigot.get().getLogger().log(Level.WARNING, "Used fallback prefix (script name) when registering command " + name + " for script " + script.getName());
    }

    public void registerCommand(PyFunction function, String name, String description, String usageMessage, List<String> aliases) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.getFuncCode()).co_filename);
        ScriptCommand command = new ScriptCommand(script, function, name, description, usageMessage, aliases);
        boolean registered = commandMap.register(script.getName(), command);
        if (registered)
            registeredCommands.add(command);
        else
            throw new UnsupportedOperationException("Used fallback prefix (script name) when registering command " + name);
    }

    public void unregisterCommand(String name) {
        ScriptCommand command = getCommand(name);
        if (command != null) {
            deregisterCommand(command);
        } else
            throw new NullPointerException("Command " + name + " not found");
    }

    public void stopScript(Script script) {
        for (ScriptCommand command : getCommands(script)) {
            deregisterCommand(command);
        }
    }

    public ScriptCommand getCommand(String name) {
        for (ScriptCommand command : registeredCommands) {
            if (command.getName().equals(name))
                return command;
        }
        return null;
    }

    public List<ScriptCommand> getCommands(Script script) {
        List<ScriptCommand> toReturn = new ArrayList<>();
        for (ScriptCommand command : registeredCommands) {
            if (command.getScript().equals(script))
                toReturn.add(command);
        }
        return toReturn;
    }

    private CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        field.setAccessible(true);
        return (CommandMap) field.get(Bukkit.getServer());
    }

    private HashMap<String, Command> getKnownCommands(CommandMap commandMap) throws NoSuchFieldException, IllegalAccessException {
        Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
        field.setAccessible(true);
        return (HashMap<String, Command>) field.get(commandMap);
    }

    private void deregisterCommand(ScriptCommand command) {
        command.unregister(commandMap);
        knownCommands.remove(command.getName());
        registeredCommands.remove(command);
    }

    public static CommandManager get() {
        if (manager == null)
            manager = new CommandManager();
        return manager;
    }
}
