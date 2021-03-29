package dev.magicmq.pyspigot.managers.task;

import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.python.core.PyException;
import org.python.core.PyFunction;

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
            ScriptManager.get().handleScriptException(script, e, "Error when running task belonging to script");
        }
    }

    public Script getScript() {
        return script;
    }
}
