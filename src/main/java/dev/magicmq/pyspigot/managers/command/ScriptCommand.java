package dev.magicmq.pyspigot.managers.command;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.python.core.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ScriptCommand implements TabExecutor {

    private final Script script;
    private final PyFunction commandFunction;
    private final PyFunction tabFunction;
    private String name;
    private String label;
    private String description;
    private String prefix;
    private String usage;
    private List<String> aliases;
    private String permission;
    private String permissionMessage;

    private PluginCommand bukkitCommand;

    public ScriptCommand(Script script, PyFunction commandFunction, PyFunction tabFunction, String name, String description, String prefix, String usage, List<String> aliases, String permission, String permissionMessage) {
        this.script = script;
        this.commandFunction = commandFunction;
        this.tabFunction = tabFunction;
        this.name = name;
        this.label = name.toLowerCase();
        this.description = description;
        this.prefix = prefix;
        this.usage = usage;
        this.aliases = aliases.stream().map(String::toLowerCase).collect(Collectors.toList());
        aliases.removeIf(label::equalsIgnoreCase);
        this.permission = permission;
        this.permissionMessage = permissionMessage;

        this.bukkitCommand = initBukkitCommand();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            PyObject result = commandFunction._jcall(new Object[]{sender, label, args});
            if (result instanceof PyBoolean)
                return ((PyBoolean) result).getBooleanValue();
            else
                script.getLogger().log(Level.SEVERE, "Script command function '" + commandFunction.__name__ + "' should return a boolean!");
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (tabFunction != null) {
            try {
                PyObject result = tabFunction._jcall(new Object[]{sender, alias, args});
                if (result instanceof PyList) {
                    PyList pyList = (PyList) result;
                    ArrayList<String> toReturn = new ArrayList<>();
                    for (Object object : pyList) {
                        if (object instanceof String)
                            toReturn.add((String) object);
                        else {
                            script.getLogger().log(Level.SEVERE, "Script tab complete function '" + tabFunction.__name__ + "' should return a list of str!");
                            return null;
                        }
                    }
                    return toReturn;
                }
            } catch (PyException e) {
                ScriptManager.get().handleScriptException(script, e, "Error when tab completing command");
            }
        }
        return null;
    }

    public Script getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void register(SimpleCommandMap map) {
        map.register(prefix, bukkitCommand);
        bukkitCommand.register(map);
    }

    public void unregister(SimpleCommandMap map, Map<String, Command> knownCommands) {
        bukkitCommand.unregister(map);
        knownCommands.remove(label);
        knownCommands.remove(prefix + ":" + label);
        for (String alias : aliases) {
            knownCommands.remove(alias);
            knownCommands.remove(prefix + ":" + alias);
        }
    }

    private PluginCommand initBukkitCommand() {
        try {
            final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            final PluginCommand bukkitCommand = constructor.newInstance(name, PySpigot.get());
            bukkitCommand.setLabel(this.label);
            bukkitCommand.setDescription(this.description);
            bukkitCommand.setUsage(this.usage);
            bukkitCommand.setAliases(this.aliases);
            bukkitCommand.setPermission(this.permission);
            bukkitCommand.setPermissionMessage(this.permissionMessage);
            bukkitCommand.setExecutor(this);
            return bukkitCommand;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


}
