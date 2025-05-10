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
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * Represents a repeating task defined by a script.
 * @param <T> The platform-specific scheduled task type. For example, {@code BukkitTask} for Bukkit, and {@code ScheduledTask} for BungeeCord
 */
public class RepeatingTask<T> extends Task<T> {

    private final long interval;

    /**
     *
     * @param script The script associated with this repeating task
     * @param function The script function that should be called every time the repeating task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @param async True if the task is asynchronous, false if otherwise
     * @param delay The delay, in ticks, to wait until running the task
     * @param interval The interval, in ticks, between each repeat of the task
     */
    public RepeatingTask(Script script, PyFunction function, Object[] functionArgs, boolean async, long delay, long interval) {
        super(script, function, functionArgs, async, delay);
        this.interval = interval;
    }

    /**
     * {@inheritDoc}
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
            ScriptManager.get().handleScriptException(script, e, "Error while executing repeating task");
        }
    }

    /**
     * Prints a representation of this RepeatingTask in string format, including the task ID, if it is async, delay (if applicable), and interval (if applicable)
     * @return A string representation of the RepeatingTask
     */
    @Override
    public String toString() {
        return String.format("RepeatingTask[Platform Task: %s, Async: %b, Delay: %d, Interval: %d]", TaskManager.<T>getTyped().describeTask(platformTask), async, (int) delay, (int) interval);
    }
}
