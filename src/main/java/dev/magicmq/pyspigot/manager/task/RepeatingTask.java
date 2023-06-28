package dev.magicmq.pyspigot.manager.task;

import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

public class RepeatingTask extends Task {

    private int taskId;

    public RepeatingTask(Script script, PyFunction function) {
        super(script, function);
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getTaskId() {
        return taskId;
    }

}
