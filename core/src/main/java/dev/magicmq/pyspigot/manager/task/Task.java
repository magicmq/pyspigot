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
 * @param <T> The platform-specific scheduled task type. For example, {@code BukkitTask} for Bukkit, and {@code ScheduledTask} for BungeeCord
 */
public class Task<T> implements Runnable {

    protected final Script script;
    protected final PyFunction function;
    protected final Object[] functionArgs;
    protected final boolean async;
    protected final long delay;

    protected T platformTask;

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
            callTaskFunction();
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error while executing task");
        } finally {
            TaskManager.<T>getTyped().taskFinished(this);
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
     * Get the platform-specific task object for this task.
     * @return The platform-specific task object
     */
    public T getPlatformTask() {
        return platformTask;
    }

    /**
     * Set the platform-specific task object for this task.
     * @param platformTask The platform-specific task object to set
     * @throws UnsupportedOperationException If the platform-specific task object was already set for this task
     */
    public void setPlatformTask(T platformTask) {
        if (this.platformTask != null)
            throw new UnsupportedOperationException("The platformTask has already been set for this task");

        this.platformTask = platformTask;
    }

    /**
     * Cancel this task. Any current execution will continue, but future executions will not occur.
     */
    public void cancel() {
        TaskManager.<T>getTyped().stopTask(this);
    }

    /**
     * Prints a representation of this Task in string format, including the task ID, if it is async, and delay (if applicable)
     * @return A string representation of the Task
     */
    @Override
    public String toString() {
        return String.format("Task[Platform Task: %s, Async: %b, Delay: %d]", TaskManager.<T>getTyped().describeTask(platformTask), async, (int) delay);
    }

    protected PyObject callTaskFunction() {
        Py.setSystemState(script.getInterpreter().getSystemState());
        ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());

        if (functionArgs != null) {
            PyObject[] pyObjects = Py.javas2pys(functionArgs);
            return function.__call__(threadState, pyObjects);
        } else {
            return function.__call__(threadState);
        }
    }
}
