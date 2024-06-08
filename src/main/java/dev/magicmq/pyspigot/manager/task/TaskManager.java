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

import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with Bukkit's scheduler. Primarily used by scripts to register and unregister tasks.
 */
public class TaskManager {

    private static TaskManager manager;

    private final HashMap<Script, List<Task>> activeTasks;

    private TaskManager() {
        activeTasks = new HashMap<>();
    }

    /**
     * Schedule a new synchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public synchronized int runTask(Value function, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new Task(script, function, functionArgs, false, 0);
        addTask(task);
        task.runTask(PySpigot.get());
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public synchronized int runTaskAsync(Value function, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new Task(script, function, functionArgs, true, 0);
        addTask(task);
        task.runTaskAsynchronously(PySpigot.get());
        return task.getTaskId();
    }

    /**
     * Schedule a new synchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the synchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public synchronized int runTaskLater(Value function, long delay, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new Task(script, function, functionArgs, false, delay);
        addTask(task);
        task.runTaskLater(PySpigot.get(), delay);
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public synchronized int runTaskLaterAsync(Value function, long delay, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new Task(script, function, functionArgs, true, delay);
        addTask(task);
        task.runTaskLaterAsynchronously(PySpigot.get(), delay);
        return task.getTaskId();
    }

    /**
     * Schedule a new synchronous repeating task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the synchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this synchronous repeating task
     * @param interval The interval, in ticks, that the synchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public synchronized int scheduleRepeatingTask(Value function, long delay, long interval, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new RepeatingTask(script, function, functionArgs, false, delay, interval);
        addTask(task);
        task.runTaskTimer(PySpigot.get(), delay, interval);
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous repeating task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the asynchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this asynchronous repeating task
     * @param interval The interval, in ticks, that the asynchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public synchronized int scheduleAsyncRepeatingTask(Value function, long delay, long interval, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new RepeatingTask(script, function, functionArgs, true, delay, interval);
        addTask(task);
        task.runTaskTimerAsynchronously(PySpigot.get(), delay, interval);
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task with a synchronous callback. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public synchronized int runSyncCallbackTask(Value function, Value callback, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new SyncCallbackTask(script, function, callback, functionArgs, 0);
        addTask(task);
        task.runTaskAsynchronously(PySpigot.get());
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task with a synchronous callback to run at a later point in time. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the asynchronous task that was scheduled
     */
    public synchronized int runSyncCallbackTaskLater(Value function, Value callback, long delay, Object... functionArgs) {
        if (!function.canExecute())
            throw new RuntimeException("Task function must be a function (callable)");

        if (!callback.canExecute())
            throw new RuntimeException("Callback function must be a function (callable)");

        Script script = ScriptManager.get().getScript(Context.getCurrent());
        Task task = new SyncCallbackTask(script, function, callback, functionArgs, delay);
        addTask(task);
        task.runTaskLaterAsynchronously(PySpigot.get(), delay);
        return task.getTaskId();
    }

    /**
     * Terminate a task with the given task ID.
     * @param taskId The ID of the task to terminate
     */
    public synchronized void stopTask(int taskId) {
        Task task = getTask(taskId);
        stopTask(task);
    }

    /**
     * Terminate a scheduled task.
     * @param task The scheduled task to terminate
     */
    public synchronized void stopTask(Task task) {
        task.cancel();
        removeTask(task);
    }

    /**
     * Terminate all scheduled tasks belonging to a script.
     * @param script The script whose scheduled tasks should be terminated
     */
    public synchronized void stopTasks(Script script) {
        List<Task> associatedTasks = activeTasks.get(script);
        if (associatedTasks != null) {
            for (Task task : associatedTasks) {
                task.cancel();
            }
            activeTasks.remove(script);
        }
    }

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

    private synchronized void addTask(Task task) {
        Script script = task.getScript();
        if (activeTasks.containsKey(script))
            activeTasks.get(script).add(task);
        else {
            List<Task> scriptTasks = new ArrayList<>();
            scriptTasks.add(task);
            activeTasks.put(script, scriptTasks);
        }
    }

    private synchronized void removeTask(Task task) {
        Script script = task.getScript();
        List<Task> scriptTasks = activeTasks.get(script);
        scriptTasks.remove(task);
        if (scriptTasks.isEmpty())
            activeTasks.remove(script);
    }

    /**
     * Get the singleton instance of this TaskManager.
     * @return The instance
     */
    public static TaskManager get() {
        if (manager == null)
            manager = new TaskManager();
        return manager;
    }
}
