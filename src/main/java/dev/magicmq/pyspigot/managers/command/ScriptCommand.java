package dev.magicmq.pyspigot.managers.command;

import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.python.core.PyBaseCode;
import org.python.core.PyBoolean;
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
        PyObject result = function._jcall(new Object[]{sender, this, commandLabel, args});
        if (result instanceof PyBoolean) {
            return ((PyBoolean) result).getBooleanValue();
        }
        throw new UnsupportedOperationException("Command function must return a boolean!");
    }

    public Script getScript() {
        return script;
    }
}
