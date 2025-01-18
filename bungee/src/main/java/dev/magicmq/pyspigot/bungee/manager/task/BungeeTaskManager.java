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

package dev.magicmq.pyspigot.bungee.manager.task;

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.manager.task.RepeatingTask;
import dev.magicmq.pyspigot.manager.task.SyncCallbackTask;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import net.md_5.bungee.api.ProxyServer;
import org.python.core.PyFunction;

import java.util.concurrent.TimeUnit;

/**
 * The BungeeCord-specific implementation of the task manager.
 */
public class BungeeTaskManager extends TaskManager {

    private static BungeeTaskManager instance;

    private BungeeTaskManager() {
        super();
    }

    /**
     * <b>DO NOT USE.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @param function The function that should be called when the synchronous task executes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    @Override
    public synchronized int runTask(PyFunction function, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>DO NOT USE.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @param function The function that should be called when the synchronous task executes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the synchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    @Override
    public synchronized int runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>DO NOT USE.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @param function The function that should be called each time the synchronous task executes
     * @param delay The delay, in ticks, to wait before beginning this synchronous repeating task
     * @param interval The interval, in ticks, that the synchronous repeating task should be executed
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    @Override
    public synchronized int scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>DO NOT USE.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    @Override
    public synchronized int runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>DO NOT USE.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @param function The function that should be called when the asynchronous task executes
     * @param callback The function that should be called for the synchronous callback once the asynchronous portion of the task finishes
     * @param delay The delay, in ticks, that the scheduler should wait before executing the asynchronous task
     * @param functionArgs Any arguments that should be passed to the function
     * @return An ID representing the synchronous task that was scheduled
     */
    @Override
    public synchronized int runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    @Override
    protected synchronized void runTaskImpl(Task task) {
        //Synchronous tasks not implemented in BungeeCord
    }

    @Override
    protected synchronized void runTaskAsyncImpl(Task task) {
        task.setTaskId(ProxyServer.getInstance().getScheduler().runAsync(PyBungee.get(), task).getId());
    }

    @Override
    protected synchronized void runTaskLaterImpl(Task task, long delay) {
        //Synchronous tasks not implemented in BungeeCord
    }

    @Override
    protected synchronized void runTaskLaterAsyncImpl(Task task, long delay) {
        task.setTaskId(ProxyServer.getInstance().getScheduler().schedule(PyBungee.get(), task, ticksToMillis(delay), TimeUnit.MILLISECONDS).getId());
    }

    @Override
    protected synchronized void scheduleRepeatingTaskImpl(RepeatingTask task, long delay, long interval) {
        //Synchronous tasks not implemented in BungeeCord
    }

    @Override
    protected synchronized void scheduleAsyncRepeatingTaskImpl(RepeatingTask task, long delay, long interval) {
        task.setTaskId(ProxyServer.getInstance().getScheduler().schedule(PyBungee.get(), task, ticksToMillis(delay), ticksToMillis(interval), TimeUnit.MILLISECONDS).getId());
    }

    @Override
    protected synchronized void runSyncCallbackTaskImpl(SyncCallbackTask task) {
        //Synchronous tasks not implemented in BungeeCord
    }

    @Override
    protected synchronized void runSyncCallbackTaskLaterImpl(SyncCallbackTask task, long delay) {
        //Synchronous tasks not implemented in BungeeCord
    }

    @Override
    protected synchronized int runSyncCallbackImpl(Runnable runnable) {
        //Synchronous tasks not implemented in BungeeCord
        return 0;
    }

    @Override
    protected void stopTaskImpl(int taskId) {
        ProxyServer.getInstance().getScheduler().cancel(taskId);
    }

    private long ticksToMillis(long ticks) {
        return (ticks * 1000) / 20;
    }

    /**
     * Get the singleton instance of this BungeeTaskManager.
     * @return The instance
     */
    public static BungeeTaskManager get() {
        if (instance == null)
            instance = new BungeeTaskManager();
        return instance;
    }
}
