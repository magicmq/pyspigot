package dev.magicmq.pyspigot.managers.command;

import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.python.core.PyBoolean;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;

import java.util.List;

public class ScriptCommand extends BukkitCommand {

    private final Script script;
    private final PyFunction function;

    public ScriptCommand(Script script, PyFunction function, String name) {
        super(name);
        this.script = script;
        this.function = function;
    }

    public ScriptCommand(Script script, PyFunction function, String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
        this.script = script;
        this.function = function;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        try {
            PyObject result = function._jcall(new Object[]{sender, this, commandLabel, args});
            if (result instanceof PyBoolean) {
                return ((PyBoolean) result).getBooleanValue();
            }

            throw new CommandException("Error when executing command belonging to script " + script.getName() + ": Command function must return a boolean!");
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing command beloning to script");
        }
        return true;
    }

    public Script getScript() {
        return script;
    }
}
