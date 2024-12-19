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
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class TaskManager {

    private static TaskManager manager;

    private final HashMap<Script, List<Task>> activeTasks;

    protected TaskManager() {
        manager = this;

        activeTasks = new HashMap<>();
    }

    /**
     * Schedule a new synchronous task via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public abstract int runTask(PyFunction function, Object... functionArgs);

    /**
     * Schedule a new asynchronous task via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public abstract int runTaskAsync(PyFunction function, Object... functionArgs);

    /**
     * Schedule a new synchronous task to run at a later point in time via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the synchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public abstract int runTaskLater(PyFunction function, long delay, Object... functionArgs);

    /**
     * Schedule a new asynchronous task to run at a later point in time via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public abstract int runTaskLaterAsync(PyFunction function, long delay, Object... functionArgs);

    /**
     * Schedule a new synchronous repeating task via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the synchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this synchronous repeating task
     * @param interval The interval, in ticks, that the synchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public abstract int scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs);

    /**
     * Schedule a new asynchronous repeating task via a platform-specific implementation.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the asynchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this asynchronous repeating task
     * @param interval The interval, in ticks, that the asynchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public abstract int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs);

    /**
     * Schedule a new asynchronous task with a synchronous callback via a platform-specific implementation. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public abstract int runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs);

    /**
     * Schedule a new asynchronous task with a synchronous callback to run at a later point in time via a platform-specific implementation. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public abstract int runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs);

    /**
     * Terminate a task with the given task ID via a platform-specific implementation.
     * @param taskId The ID of the task to terminate
     */
    public abstract void stopTask(int taskId);

    /**
     * Terminate all scheduled tasks belonging to a script via a platform-specific implementation.
     * @param script The script whose scheduled tasks should be terminated
     */
    public abstract void stopTasks(Script script);

    /**
     * Run the synchronous callback portion of a task via a platform-specific implementation.
     * @param runnable The synchronous callback task to run
     * @return The ID of the scheduled task
     */
    public abstract int runSyncCallback(Runnable runnable);

    /**
     * Get a scheduled task from its ID.
     * @param taskId The task ID
     * @return The scheduled task associated with the task ID, null if no task was found with the given ID
     */
    public synchronized Task getTask(int taskId) {
        for (List<Task> scriptTasks : activeTasks.values()) {
            for (Task task : scriptTasks) {
                if (task.getTaskId() == taskId)
                    return task;
            }
        }
        return null;
    }

    /**
     * Get all scheduled tasks associated with a script.
     * @param script The script whose scheduled tasks should be gotten
     * @return An immutable list containing all scheduled tasks associated with the script. Returns null if the script has no scheduled tasks
     */
    public synchronized List<Task> getTasks(Script script) {
        List<Task> scriptTasks = activeTasks.get(script);
        if (scriptTasks != null)
            return new ArrayList<>(scriptTasks);
        else
            return null;
    }

    protected synchronized void taskFinished(Task task) {
        removeTask(task);
    }

    protected synchronized void addTask(Task task) {
        Script script = task.getScript();
        if (activeTasks.containsKey(script))
            activeTasks.get(script).add(task);
        else {
            List<Task> scriptTasks = new ArrayList<>();
            scriptTasks.add(task);
            activeTasks.put(script, scriptTasks);
        }
    }

    protected synchronized void removeTask(Task task) {
        Script script = task.getScript();
        List<Task> scriptTasks = activeTasks.get(script);
        scriptTasks.remove(task);
        if (scriptTasks.isEmpty())
            activeTasks.remove(script);
    }

    protected synchronized void removeTasks(Script script) {
        activeTasks.remove(script);
    }

    public static TaskManager get() {
        return manager;
    }
}
