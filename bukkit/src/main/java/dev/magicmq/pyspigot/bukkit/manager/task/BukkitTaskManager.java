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

/**
 * The Bukkit-specific implementation of the task manager.
 */
public class BukkitTaskManager extends TaskManager {

    private static BukkitTaskManager instance;

    private BukkitTaskManager() {
        super();
    }

    @Override
    public void runTaskImpl(Task task) {
        task.setTaskId(Bukkit.getScheduler().runTask(PySpigot.get(), task).getTaskId());
    }

    @Override
    public void runTaskAsyncImpl(Task task) {
        task.setTaskId(Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task).getTaskId());
    }

    @Override
    public void runTaskLaterImpl(Task task, long delay) {
        task.setTaskId(Bukkit.getScheduler().runTaskLater(PySpigot.get(), task, delay).getTaskId());
    }

    @Override
    public void runTaskLaterAsyncImpl(Task task, long delay) {
        task.setTaskId(Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay).getTaskId());
    }

    @Override
    public void scheduleRepeatingTaskImpl(RepeatingTask task, long delay, long interval) {
        task.setTaskId(Bukkit.getScheduler().runTaskTimer(PySpigot.get(), task, delay, interval).getTaskId());
    }

    @Override
    public void scheduleAsyncRepeatingTaskImpl(RepeatingTask task, long delay, long interval) {
        task.setTaskId(Bukkit.getScheduler().runTaskTimerAsynchronously(PySpigot.get(), task, delay, interval).getTaskId());
    }

    @Override
    public void runSyncCallbackTaskImpl(SyncCallbackTask task) {
        task.setTaskId(Bukkit.getScheduler().runTaskAsynchronously(PySpigot.get(), task).getTaskId());
    }

    @Override
    public void runSyncCallbackTaskLaterImpl(SyncCallbackTask task, long delay) {
        task.setTaskId(Bukkit.getScheduler().runTaskLaterAsynchronously(PySpigot.get(), task, delay).getTaskId());
    }

    @Override
    public int runSyncCallbackImpl(Runnable runnable) {
        return Bukkit.getScheduler().runTask(PySpigot.get(), runnable).getTaskId();
    }

    @Override
    public void stopTaskImpl(int taskId) {
        Bukkit.getScheduler().cancelTask(taskId);
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
