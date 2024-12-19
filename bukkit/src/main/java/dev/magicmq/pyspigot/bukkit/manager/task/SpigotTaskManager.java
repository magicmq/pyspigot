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

package dev.magicmq.pyspigot.bukkit.manager.task;

import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.task.RepeatingTask;
import dev.magicmq.pyspigot.manager.task.SyncCallbackTask;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.bukkit.Bukkit;
import org.python.core.PyFunction;

import java.util.List;

/**
 * Manager to interface with Bukkit's scheduler. Primarily used by scripts to register and unregister tasks.
 */
public class SpigotTaskManager extends TaskManager {

    private static SpigotTaskManager manager;

    private SpigotTaskManager() {
        super();
    }

    @Override
    public synchronized int runTask(PyFunction function, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, false, 0);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTask(PySpigot.get(), task).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int runTaskAsync(PyFunction function, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, true, 0);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, false, delay);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskLater(PySpigot.get(), task, delay).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int runTaskLaterAsync(PyFunction function, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new Task(script, function, functionArgs, true, delay);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new RepeatingTask(script, function, functionArgs, false, delay, interval);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskTimer(PySpigot.get(), task, delay, interval).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new RepeatingTask(script, function, functionArgs, true, delay, interval);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskTimerAsynchronously(PySpigot.get(), task, delay, interval).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new SyncCallbackTask(script, function, callback, functionArgs, 0);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized int runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        Script script = ScriptUtils.getScriptFromCallStack();
        Task task = new SyncCallbackTask(script, function, callback, functionArgs, delay);
        addTask(task);
        task.setTaskId(Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay).getTaskId());
        return task.getTaskId();
    }

    @Override
    public synchronized void stopTask(int taskId) {
        Task task = getTask(taskId);
        Bukkit.getScheduler().cancelTask(task.getTaskId());
        removeTask(task);
    }

    @Override
    public synchronized void stopTasks(Script script) {
        List<Task> associatedTasks = getTasks(script);
        if (associatedTasks != null) {
            for (Task task : associatedTasks) {
                Bukkit.getScheduler().cancelTask(task.getTaskId());
            }
            removeTasks(script);
        }
    }

    @Override
    public int runSyncCallback(Runnable runnable) {
        return Bukkit.getScheduler().runTask(PySpigot.get(), runnable).getTaskId();
    }

    /**
     * Get the singleton instance of this TaskManager.
     * @return The instance
     */
    public static SpigotTaskManager get() {
        if (manager == null)
            manager = new SpigotTaskManager();
        return manager;
    }
}
