/*
 *    Copyright 2025 magicmq
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

package dev.magicmq.pyspigot.manager.task;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.Py;
import org.python.core.PyBaseCode;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

import java.util.Arrays;

/**
 * Represents a task defined by a script.
 */
public class Task implements Runnable {

    protected final Script script;
    protected final PyFunction function;
    protected final Object[] functionArgs;
    protected final boolean async;
    protected final long delay;

    protected int taskId;

    /**
     *
     * @param script The script associated with this task
     * @param function The script function that should be called when the task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @param async True if the task is asynchronous, false if otherwise
     * @param delay The delay, in ticks, to wait until running the task
     */
    public Task(Script script, PyFunction function, Object[] functionArgs, boolean async, long delay) {
        this.script = script;
        this.function = function;

        if (functionArgs != null) {
            int numOfFunctionArgs = ((PyBaseCode) function.__code__).co_argcount;
            if (numOfFunctionArgs < functionArgs.length)
                functionArgs = Arrays.copyOf(functionArgs, numOfFunctionArgs);
            this.functionArgs = functionArgs;
        } else
            this.functionArgs = null;

        this.async = async;
        this.delay = delay;
    }

    /**
     * Called internally when the task executes.
     */
    @Override
    public void run() {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());

            if (functionArgs != null) {
                PyObject[] pyObjects = Py.javas2pys(functionArgs);
                function.__call__(threadState, pyObjects);
            } else {
                function.__call__(threadState);
            }
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing task #" + taskId);
        } finally {
            TaskManager.get().taskFinished(this);
        }
    }

    /**
     * Get the script associated with this task.
     * @return The script associated with this task
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the ID for this task.
     * @return The task ID for this task
     */
    public int getTaskId() {
        return taskId;
    }

    /**
     * Set the task ID for this task.
     * @param taskId The task ID to set
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    /**
     * Prints a representation of this Task in string format, including the task ID, if it is async, and delay (if applicable)
     * @return A string representation of the Task
     */
    @Override
    public String toString() {
        return String.format("Task[Task ID: %d, Async: %b, Delay: %d]", taskId, async, (int) delay);
    }
}
