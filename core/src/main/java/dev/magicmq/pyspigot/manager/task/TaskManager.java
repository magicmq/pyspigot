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
import dev.magicmq.pyspigot.util.ScriptContext;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with a server platform's scheduler. Primarily used by scripts to register and unregister tasks.
 * @param <T> The platform-specific scheduled task type, returned by the platform's scheduler. For example, {@code BukkitTask} for Bukkit, and {@code ScheduledTask} for BungeeCord
 */
public abstract class TaskManager<T> {

    private static TaskManager<?> instance;

    private final HashMap<Script, List<Task<T>>> activeTasks;

    protected TaskManager() {
        instance = this;

        activeTasks = new HashMap<>();
    }

    /**
     * Schedule a new synchronous task using the platform-specific scheduler.
     * @param task The task to schedule
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runTaskImpl(Task<T> task);

    /**
     * Schedule a new asynchronous task using the platform-specific scheduler.
     * @param task The task to schedule
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runTaskAsyncImpl(Task<T> task);

    /**
     * Schedule a new synchronous task to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runTaskLaterImpl(Task<T> task, long delay);

    /**
     * Schedule a new asynchronous task to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runTaskLaterAsyncImpl(Task<T> task, long delay);

    /**
     * Schedule a new synchronous repeating task using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @param interval The interval, in ticks, that the task should be executed
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T scheduleRepeatingTaskImpl(RepeatingTask<T> task, long delay, long interval);

    /**
     * Schedule a new asynchronous repeating task using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @param interval The interval, in ticks, that the task should be executed
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T scheduleAsyncRepeatingTaskImpl(RepeatingTask<T> task, long delay, long interval);

    /**
     * Schedule a new asynchronous task with a synchronous callback using the platform-specific scheduler.
     * @param task The task to schedule
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runSyncCallbackTaskImpl(SyncCallbackTask<T> task);

    /**
     * Schedule a new asynchronous task with a synchronous callback to run at a later point in time using the platform-specific scheduler.
     * @param task The task to schedule
     * @param delay The delay, in ticks, that the scheduler should wait before executing the task
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runSyncCallbackTaskLaterImpl(SyncCallbackTask<T> task, long delay);

    /**
     * Run the synchronous callback portionof a SyncCallbackTask using the platform-specific scheduler.
     * @param runnable The synchronous callback
     * @return The platform-specific task object returned by the platform's scheduler
     */
    protected abstract T runSyncCallbackImpl(Runnable runnable);

    /**
     * Stop a task using the platform-specific scheduler.
     * @param platformTask The platform-specific task object to stop
     */
    protected abstract void stopTaskImpl(T platformTask);

    /**
     * Describes a platform-specific task object by returning its ID and/or some other unique information.
     * @return A string that describes the task
     */
    protected abstract String describeTask(T task);

    /**
     * Schedule a new synchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return A Task object representing the registered task
     */
    public synchronized Task<T> runTask(PyFunction function, Object... functionArgs) {
        Script script = ScriptContext.require();
        Task<T> task = new Task<>(script, function, functionArgs, false, 0);
        addTask(task);
        task.setPlatformTask(runTaskImpl(task));
        return task;
    }

    /**
     * Schedule a new asynchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return A Task object representing the registered task
     */
    public synchronized Task<T> runTaskAsync(PyFunction function, Object... functionArgs) {
        Script script = ScriptContext.require();
        Task<T> task = new Task<>(script, function, functionArgs, true, 0);
        addTask(task);
        task.setPlatformTask(runTaskAsyncImpl(task));
        return task;
    }

    /**
     * Schedule a new synchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the synchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return A Task object representing the registered task
     */
    public synchronized Task<T> runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptContext.require();
        Task<T> task = new Task<>(script, function, functionArgs, false, delay);
        addTask(task);
        task.setPlatformTask(runTaskLaterImpl(task, delay));
        return task;
    }

    /**
     * Schedule a new asynchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return A Task object representing the registered task
     */
    public synchronized Task<T> runTaskLaterAsync(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptContext.require();
        Task<T> task = new Task<>(script, function, functionArgs, true, delay);
        addTask(task);
        task.setPlatformTask(runTaskLaterAsyncImpl(task, delay));
        return task;
    }

    /**
     * Schedule a new synchronous repeating task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the synchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this synchronous repeating task
     * @param interval The interval, in ticks, that the synchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return A RepeatingTask object representing the registered task
     */
    public synchronized RepeatingTask<T> scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptContext.require();
        RepeatingTask<T> task = new RepeatingTask<>(script, function, functionArgs, false, delay, interval);
        addTask(task);
        task.setPlatformTask(scheduleRepeatingTaskImpl(task, delay, interval));
        return task;
    }

    /**
     * Schedule a new asynchronous repeating task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the asynchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this asynchronous repeating task
     * @param interval The interval, in ticks, that the asynchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return A RepeatingTask object representing the registered task
     */
    public synchronized RepeatingTask<T> scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptContext.require();
        RepeatingTask<T> task = new RepeatingTask<>(script, function, functionArgs, true, delay, interval);
        addTask(task);
        task.setPlatformTask(scheduleAsyncRepeatingTaskImpl(task, delay, interval));
        return task;
    }

    /**
     * Schedule a new asynchronous task with a synchronous callback. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param functionArgs Any arguments that should be passed to the function
     * @return A SyncCallbackTask object representing the registered task
     */
    public synchronized SyncCallbackTask<T> runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        Script script = ScriptContext.require();
        SyncCallbackTask<T> task = new SyncCallbackTask<>(script, function, callback, functionArgs, 0);
        addTask(task);
        task.setPlatformTask(runSyncCallbackTaskImpl(task));
        return task;
    }

    /**
     * Schedule a new asynchronous task with a synchronous callback to run at a later point in time. Data returned from the initially called function (asynchronous portion) is automatically passed to the synchronous callback function as a function argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return A SyncCallbackTask object representing the registered task
     */
    public synchronized SyncCallbackTask<T> runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        Script script = ScriptContext.require();
        SyncCallbackTask<T> task = new SyncCallbackTask<>(script, function, callback, functionArgs, delay);
        addTask(task);
        task.setPlatformTask(runSyncCallbackTaskLaterImpl(task, delay));
        return task;
    }

    /**
     * Terminate the task associated with the function.
     * @param function The function whose task should be cancelled
     */
    public synchronized void stopTask(PyFunction function) {
        Script script = ScriptContext.require();
        List<Task<T>> tasks = getTasks(script);
        for (Task<T> task : tasks) {
            if (task.getFunction().equals(function)) {
                stopTask(task);
            }
        }
    }

    /**
     * Terminate the given task.
     * @param task The task to terminate
     */
    public synchronized void stopTask(Task<T> task) {
        stopTaskImpl(task.getPlatformTask());
        removeTask(task);
    }

    /**
     * Terminate all scheduled tasks belonging to a script.
     * @param script The script whose scheduled tasks should be terminated
     */
    public synchronized void stopTasks(Script script) {
        for (Task<T> task : getTasks(script)) {
            stopTaskImpl(task.getPlatformTask());
        }
        activeTasks.remove(script);
    }

    /**
     * Get all scheduled tasks associated with a script.
     * @param script The script whose scheduled tasks should be gotten
     * @return An immutable list containing all scheduled tasks associated with the script. Will return an empty list if the script has no scheduled tasks
     */
    public synchronized List<Task<T>> getTasks(Script script) {
        List<Task<T>> scriptTasks = activeTasks.get(script);
        return scriptTasks != null ? List.copyOf(scriptTasks) : List.of();
    }

    protected synchronized void taskFinished(Task<T> task) {
        removeTask(task);
    }

    protected synchronized void addTask(Task<T> task) {
        Script script = task.getScript();
        if (activeTasks.containsKey(script))
            activeTasks.get(script).add(task);
        else {
            List<Task<T>> scriptTasks = new ArrayList<>();
            scriptTasks.add(task);
            activeTasks.put(script, scriptTasks);
        }
    }

    protected synchronized void removeTask(Task<T> task) {
        Script script = task.getScript();
        List<Task<T>> scriptTasks = activeTasks.get(script);
        scriptTasks.remove(task);
        if (scriptTasks.isEmpty())
            activeTasks.remove(script);
    }

    /**
     * Get the singleton instance of this TaskManager.
     * @return The instance
     */
    public static TaskManager<?> get() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    protected static <T> TaskManager<T> getTyped() {
        return (TaskManager<T>) instance;
    }
}
