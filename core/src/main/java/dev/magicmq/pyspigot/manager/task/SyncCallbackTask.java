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

import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyNone;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * Represents an async task with a synchronous callback defined by a script.
 * @param <T> The platform-specific scheduled task type. For example, {@code BukkitTask} for Bukkit, and {@code ScheduledTask} for BungeeCord
 */
public class SyncCallbackTask<T> extends Task<T> {

    private final PyFunction callbackFunction;

    private volatile boolean cancelled;
    private Callback<T> callback;

    /**
     *
     * @param script The script associated with this task
     * @param function The script function that should be called when the async task executes
     * @param callbackFunction The script function that should be called for the synchronous callback
     * @param functionArgs Any arguments that should be passed to the function
     */
    public SyncCallbackTask(Script script, PyFunction function, PyFunction callbackFunction, Object[] functionArgs, long delay) {
        super(script, function, functionArgs, true, delay);
        this.callbackFunction = callbackFunction;

        this.cancelled = false;
    }

    /**
     * Called internally when the task executes.
     */
    @Override
    public void run() {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());

            PyObject outcome;
            if (functionArgs != null) {
                PyObject[] pyObjects = Py.javas2pys(functionArgs);
                outcome = function.__call__(threadState, pyObjects);
            } else {
                outcome = function.__call__(threadState);
            }

            if (!cancelled) {
                callback = new Callback<>(this, outcome);
                callback.setPlatformTask(TaskManager.<T>getTyped().runSyncCallbackImpl(callback));
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        throw new ScriptRuntimeException(script, "Async thread was interrupted while executing callback task", e);
                    }
                }
            }
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error while executing callback task");
        } finally {
            if (!cancelled)
                TaskManager.<T>getTyped().taskFinished(this);
        }
    }

    @Override
    public void cancel() {
        cancelled = true;
        super.cancel();
    }

    /**
     * Prints a representation of this SyncCallbackTask in string format, including the task ID, if it is async, and delay (if applicable)
     * @return A string representation of the SyncCallbackTask
     */
    @Override
    public String toString() {
        if (callback == null)
            return String.format("SyncCallbackTask[Platform Task: %s, Async: %b, Delay: %d]", TaskManager.<T>getTyped().describeTask(platformTask), async, (int) delay);
        else
            return String.format("SyncCallbackTask[Platform Task: %s, Async: %b, Delay: %d, Callback: %s]", TaskManager.<T>getTyped().describeTask(platformTask), async, (int) delay, callback);
    }

    /**
     * The synchronous callback, which runs after the asynchronous task finishes
     * @param <T> The platform-specific task object
     */
    private static class Callback<T> implements Runnable {

        private final SyncCallbackTask<T> task;
        private final PyObject outcome;

        private T platformTask;

        /**
         *
         * @param task The asynchronous portion of the task
         * @param outcome The value(s) returned from the function called during the asynchronous portion of the task
         */
        private Callback(SyncCallbackTask<T> task, PyObject outcome) {
            this.task = task;
            this.outcome = outcome;
        }

        /**
         * Set the platform-specific task object for this task.
         * @param platformTask The platform-specific task object to set
         */
        public void setPlatformTask(T platformTask) {
            this.platformTask = platformTask;
        }

        /**
         * Called internally when the task executes.
         */
        @Override
        public void run() {
            Py.setSystemState(task.script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(task.script.getInterpreter().getSystemState());

            try {
                if (outcome instanceof PyNone)
                    task.callbackFunction.__call__(threadState);
                else
                    task.callbackFunction.__call__(threadState, outcome);
            } catch (PyException e) {
                ScriptManager.get().handleScriptException(task.script, e, "Error while executing callback task");
            } finally {
                synchronized (task) {
                    task.notify();
                }
            }
        }

        /**
         * Prints a representation of this Callback in string format, including the task ID
         * @return A string representation of the Callback
         */
        @Override
        public String toString() {
            return String.format("Callback[%s]", TaskManager.<T>getTyped().describeTask(platformTask));
        }
    }
}
