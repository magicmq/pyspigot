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
import dev.magicmq.pyspigot.manager.task.RepeatingTask;
import dev.magicmq.pyspigot.manager.task.SyncCallbackTask;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Bukkit-specific implementation of the task manager.
 */
public class BukkitTaskManager extends TaskManager<BukkitTask> {

    private static BukkitTaskManager instance;

    private BukkitTaskManager() {
        super();
    }

    @Override
    protected synchronized BukkitTask runTaskImpl(Task<BukkitTask> task) {
        return Bukkit.getScheduler().runTask(PySpigot.get(), task);
    }

    @Override
    protected synchronized BukkitTask runTaskAsyncImpl(Task<BukkitTask> task) {
        return Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task);
    }

    @Override
    protected synchronized BukkitTask runTaskLaterImpl(Task<BukkitTask> task, long delay) {
        return Bukkit.getScheduler().runTaskLater(PySpigot.get(), task, delay);
    }

    @Override
    protected synchronized BukkitTask runTaskLaterAsyncImpl(Task<BukkitTask> task, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay);
    }

    @Override
    protected synchronized BukkitTask scheduleRepeatingTaskImpl(RepeatingTask<BukkitTask> task, long delay, long interval) {
        return Bukkit.getScheduler().runTaskTimer(PySpigot.get(), task, delay, interval);
    }

    @Override
    protected synchronized BukkitTask scheduleAsyncRepeatingTaskImpl(RepeatingTask<BukkitTask> task, long delay, long interval) {
        return Bukkit.getScheduler().runTaskTimerAsynchronously(PySpigot.get(), task, delay, interval);
    }

    @Override
    protected synchronized BukkitTask runSyncCallbackTaskImpl(SyncCallbackTask<BukkitTask> task) {
        return Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task);
    }

    @Override
    protected synchronized BukkitTask runSyncCallbackTaskLaterImpl(SyncCallbackTask<BukkitTask> task, long delay) {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay);
    }

    @Override
    protected synchronized BukkitTask runSyncCallbackImpl(Runnable runnable) {
        return Bukkit.getScheduler().runTask(PySpigot.get(), runnable);
    }

    @Override
    protected synchronized void stopTaskImpl(BukkitTask platformTask) {
        platformTask.cancel();
    }

    @Override
    protected String describeTask(BukkitTask platformTask) {
        return String.format("BukkitTask[taskId: %d, isSync: %b]", platformTask.getTaskId(), platformTask.isSync());
    }

    /**
     * Get the singleton instance of this BukkitTaskManager.
     * @return The instance
     */
    public static BukkitTaskManager get() {
        if (instance == null)
            instance = new BukkitTaskManager();
        return instance;
    }
}
