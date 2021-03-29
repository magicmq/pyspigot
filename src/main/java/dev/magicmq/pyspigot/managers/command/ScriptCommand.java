package dev.magicmq.pyspigot.managers.command;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.python.core.*;

import java.util.List;
import java.util.logging.Level;

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
            if (e.getCause() != null && !(e.getCause() instanceof PyException))
                e.getCause().printStackTrace();
            else {
                if (e.traceback != null)
                    PySpigot.get().getLogger().log(Level.SEVERE, "Error when executing command belonging to script " + script.getName() + ": " + e.getMessage() + "\n\n" + e.traceback.dumpStack());
                else
                    PySpigot.get().getLogger().log(Level.SEVERE, "Error when executing command belonging to script " + script.getName() + ": " + e.getMessage());
            }
        }
        return true;
    }

    public Script getScript() {
        return script;
    }
}
