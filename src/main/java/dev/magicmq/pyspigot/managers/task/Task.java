package dev.magicmq.pyspigot.managers.task;

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import org.python.core.PyException;
import org.python.core.PyFunction;

import java.util.logging.Level;

public class Task implements Runnable {

    private final Script script;
    private final PyFunction function;

    public Task(Script script, PyFunction function) {
        this.script = script;
        this.function = function;
    }

    @Override
    public void run() {
        try {
            function._jcall(new Object[]{});
        } catch (PyException e) {
            if (e.getCause() != null && !(e.getCause() instanceof PyException))
                e.getCause().printStackTrace();
            else {
                if (e.traceback != null)
                    PySpigot.get().getLogger().log(Level.SEVERE, "Error when running task belonging to script " + script.getName() + ": " + e.getMessage() + "\n\n" + e.traceback.dumpStack());
                else
                    PySpigot.get().getLogger().log(Level.SEVERE, "Error when running task belonging to script " + script.getName() + ": " + e.getMessage());
            }
        }
    }

    public Script getScript() {
        return script;
    }
}
