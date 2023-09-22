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
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.bukkit.Bukkit;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager to interface with Bukkit's scheduler. Primarily used by scripts to register and unregister tasks.
 */
public class TaskManager {

    private static TaskManager manager;

    private final List<Task> activeTasks;

    private TaskManager() {
        activeTasks = new ArrayList<>();
    }

    /**
     * Schedule a new synchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @return An ID representing the synchronous task that was scheduled
     */
    public int runTask(PyFunction function) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function);
        activeTasks.add(task);
        task.runTask(PySpigot.get());
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @return An ID representing the asynchronous task that was scheduled
     */
    public int runTaskAsync(PyFunction function) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function);
        activeTasks.add(task);
        task.runTaskAsynchronously(PySpigot.get());
        return task.getTaskId();
    }

    /**
     * Schedule a new synchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the synchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the synchronous task
     * @return An ID representing the synchronous task that was scheduled
     */
    public int runTaskLater(PyFunction function, long delay) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function);
        activeTasks.add(task);
        task.runTaskLater(PySpigot.get(), delay);
        return task.getTaskId();
    }

    /**
     * Schedule a new asynchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @return An ID representing the asynchronous task that was scheduled
     */
    public int runTaskLaterAsync(PyFunction function, long delay) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function);
        activeTasks.add(task);
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
     * @return An ID representing the synchronous task that was scheduled
     */
    public int scheduleRepeatingTask(PyFunction function, long delay, long interval) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new RepeatingTask(script, function);
        activeTasks.add(task);
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
     * @return An ID representing the asynchronous task that was scheduled
     */
    public int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new RepeatingTask(script, function);
        activeTasks.add(task);
        task.runTaskTimerAsynchronously(PySpigot.get(), delay, interval);
        return task.getTaskId();
    }

    /**
     * Terminate a task with the given task ID.
     * @param taskId The ID of the task to terminate
     */
    public void stopTask(int taskId) {
        Task task = getTask(taskId);
        stopTask(task);
    }

    /**
     * Terminate a scheduled task.
     * @param task The scheduled task to terminate
     */
    public void stopTask(Task task) {
        task.cancel();
        activeTasks.remove(task);
    }

    /**
     * Terminate all scheduled tasks belonging to a script.
     * @param script The script whose scheduled tasks should be terminated
     */
    public void stopTasks(Script script) {
        List<Task> associatedTasks = getTasks(script);
        associatedTasks.forEach(this::stopTask);
    }

    /**
     * Get a scheduled task from its ID.
     * @param taskId The task ID
     * @return The scheduled task associated with the task ID, null if no task was found with the given ID
     */
    public Task getTask(int taskId) {
        for (Task task : activeTasks) {
            if (task.getTaskId() == taskId)
                return task;
        }
        return null;
    }

    /**
     * Get all scheduled tasks associated with a script.
     * @param script The script whose scheduled tasks should be gotten
     * @return An immutable list containing all scheduled tasks associated with the script. Returns an empty list if the script has no scheduled tasks
     */
    public List<Task> getTasks(Script script) {
        List<Task> toReturn = new ArrayList<>();
        for (Task task : activeTasks) {
            if (task.getScript().equals(script))
                toReturn.add(task);
        }
        return toReturn;
    }

    protected void taskFinished(Task task) {
        if (!Bukkit.isPrimaryThread()) {
            //This could be called from an asynchronous context (from an asynchronous task, for example), so we need to run it sync to avoid issues
            Bukkit.getScheduler().runTask(PySpigot.get(), () -> TaskManager.this.taskFinished(task));
        } else
            activeTasks.remove(task);
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
