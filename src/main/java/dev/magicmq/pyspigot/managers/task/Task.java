package dev.magicmq.pyspigot.managers.task;

import dev.magicmq.pyspigot.managers.script.Script;
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
        function._jcall(new Object[]{});
    }

    public Script getScript() {
        return script;
    }
}
