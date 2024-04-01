/*
 *    Copyright 2023 magicmq
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
import org.bukkit.scheduler.BukkitRunnable;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;

/**
 * Represents a task defined by a script.
 */
public class Task extends BukkitRunnable {

    protected final Script script;
    protected final PyFunction function;
    protected final Object[] functionArgs;

    /**
     *
     * @param script The script associated with this task
     * @param function The script function that should be called when the task executes
     * @param functionArgs Any arguments that should be passed to the function
     */
    public Task(Script script, PyFunction function, Object[] functionArgs) {
        this.script = script;
        this.function = function;
        this.functionArgs = functionArgs;
    }

    /**
     * Called internally when the task executes.
     */
    @Override
    public void run() {
        try {
            if (functionArgs != null) {
                PyObject[] pyObjects = Py.javas2pys(functionArgs);
                function.__call__(pyObjects);
            } else {
                function.__call__();
            }
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when executing task #" + getTaskId());
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
}
