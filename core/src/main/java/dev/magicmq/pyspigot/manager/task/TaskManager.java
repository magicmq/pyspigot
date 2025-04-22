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
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with a server platform's scheduler. Primarily used by scripts to register and unregister tasks.
 */
public abstract class TaskManager {

    private static TaskManager instance;

    private final HashMap<Script, List<Task>> activeTasks;

    protected TaskManager() {
        instance = this;

        activeTasks = new HashMap<>();
    }

    /**
     * Schedule a new synchronous task using the platform-specific scheduler.
     * @param task The task to schedule
     */
    protected abstract void runTaskImpl(Task task);

    /**
     * Schedule a new asynchronous task using the platform-specific scheduler.
     * @param task The task to schedule
     */
    protected abstract void runTaskAsyncImpl(Task task);

    /**
     * Schedule a new synchronous task to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     */
    protected abstract void runTaskLaterImpl(Task task, long delay);

    /**
     * Schedule a new asynchronous task to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     */
    protected abstract void runTaskLaterAsyncImpl(Task task, long delay);

    /**
     * Schedule a new synchronous repeating task using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @param interval The interval, in ticks, that the task should be executed
     */
    protected abstract void scheduleRepeatingTaskImpl(RepeatingTask task, long delay, long interval);

    /**
     * Schedule a new asynchronous repeating task using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @param interval The interval, in ticks, that the task should be executed
     */
    protected abstract void scheduleAsyncRepeatingTaskImpl(RepeatingTask task, long delay, long interval);

    /**
     * Schedule a new asynchronous task with a synchronous callback using the platform-specific scheduler.
     * @param task The task to schedule
     */
    protected abstract void runSyncCallbackTaskImpl(SyncCallbackTask task);

    /**
     * Schedule a new asynchronous task with a synchronous callback to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     */
    protected abstract void runSyncCallbackTaskLaterImpl(SyncCallbackTask task, long delay);

    /**
     * Run the synchronous callback portionof a SyncCallbackTask using the platform-specific scheduler.
     * @param runnable The synchronous callback
     * @return The ID of the task that was scheduled
     */
    protected abstract int runSyncCallbackImpl(Runnable runnable);

    /**
     * Stop a task using the platform-specific scheduler.
     * @param taskId The ID of the task to stop
     */
    protected abstract void stopTaskImpl(int taskId);

    /**
     * Schedule a new synchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    public synchronized int runTask(PyFunction function, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, false, 0);
        addTask(task);
        runTaskImpl(task);
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
    public synchronized int runTaskAsync(PyFunction function, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, true, 0);
        addTask(task);
        runTaskAsyncImpl(task);
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
    public synchronized int runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, false, delay);
        addTask(task);
        runTaskLaterImpl(task, delay);
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
    public synchronized int runTaskLaterAsync(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, true, delay);
        addTask(task);
        runTaskLaterAsyncImpl(task, delay);
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
    public synchronized int scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        RepeatingTask task = new RepeatingTask(script, function, functionArgs, false, delay, interval);
        addTask(task);
        scheduleRepeatingTaskImpl(task, delay, interval);
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
    public synchronized int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        RepeatingTask task = new RepeatingTask(script, function, functionArgs, true, delay, interval);
        addTask(task);
        scheduleAsyncRepeatingTaskImpl(task, delay, interval);
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
    public synchronized int runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        SyncCallbackTask task = new SyncCallbackTask(script, function, callback, functionArgs, 0);
        addTask(task);
        runSyncCallbackTaskImpl(task);
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
    public synchronized int runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        SyncCallbackTask task = new SyncCallbackTask(script, function, callback, functionArgs, delay);
        addTask(task);
        runSyncCallbackTaskLaterImpl(task, delay);
        return task.getTaskId();
    }

    /**
     * Terminate a task with the given task ID.
     * @param taskId The ID of the task to terminate
     */
    public synchronized void stopTask(int taskId) {
        Task task = getTask(taskId);
        stopTaskImpl(taskId);
        removeTask(task);
    }

    /**
     * Terminate all scheduled tasks belonging to a script.
     * @param script The script whose scheduled tasks should be terminated
     */
    public synchronized void stopTasks(Script script) {
        List<Task> associatedTasks = getTasks(script);
        if (associatedTasks != null) {
            for (Task task : associatedTasks) {
                stopTaskImpl(task.getTaskId());
            }
            removeTasks(script);
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
        return instance;
    }
}
