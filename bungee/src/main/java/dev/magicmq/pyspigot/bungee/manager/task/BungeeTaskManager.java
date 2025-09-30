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
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.task.RepeatingTask;
import dev.magicmq.pyspigot.manager.task.SyncCallbackTask;
import dev.magicmq.pyspigot.manager.task.Task;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import dev.magicmq.pyspigot.util.ScriptContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.python.core.PyFunction;

import java.util.concurrent.TimeUnit;

/**
 * The BungeeCord-specific implementation of the task manager.
 */
public class BungeeTaskManager extends TaskManager<ScheduledTask> {

    private static BungeeTaskManager instance;

    private BungeeTaskManager() {
        super();
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work. Instead, use {@link TaskManager#runTaskAsync(PyFunction, Object...)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized Task<ScheduledTask> runTask(PyFunction function, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work. Instead, use {@link TaskManager#runTaskLaterAsync(PyFunction, long, Object...)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized Task<ScheduledTask> runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work. Instead, use {@link TaskManager#scheduleAsyncRepeatingTask(PyFunction, long, long, Object...)}
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized RepeatingTask<ScheduledTask> scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized SyncCallbackTask<ScheduledTask> runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * <b>Unsupported operation.</b>
     * <p>
     * Synchronous tasks are not implemented in BungeeCord, so this method will not work.
     * @throws UnsupportedOperationException always
     */
    @Override
    public synchronized SyncCallbackTask<ScheduledTask> runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        throw new UnsupportedOperationException("BungeeCord does not support synchronous tasks.");
    }

    /**
     * Schedule a new asynchronous task to run at a later point in time.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the asynchronous task executes
     * @param delay The delay that the scheduler should wait before executing the asynchronous task
     * @param delayUnit The time unit for the delay
     * @param functionArgs Any arguments that should be passed to the function
     * @return A Task object representing the registered task
     */
    public synchronized Task<ScheduledTask> runTaskLaterAsync(PyFunction function, long delay, TimeUnit delayUnit, Object... functionArgs) {
        Script script = ScriptContext.require();
        Task<ScheduledTask> task = new Task<>(script, function, functionArgs, true, delay);
        addTask(task);
        task.setPlatformTask(runTaskLaterAsyncImpl(task, delay, delayUnit));
        return task;
    }

    /**
     * Schedule a new asynchronous repeating task.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called each time the asynchronous task executes
     * @param delay The delay to wait before beginning this asynchronous repeating task
     * @param interval The interval that the asynchronous repeating task should be executed
     * @param intervalUnit The time unit for the interval
     * @param functionArgs Any arguments that should be passed to the function
     * @return A RepeatingTask object representing the registered task
     */
    public synchronized RepeatingTask<ScheduledTask> scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, TimeUnit intervalUnit, Object... functionArgs) {
        Script script = ScriptContext.require();
        RepeatingTask<ScheduledTask> task = new RepeatingTask<>(script, function, functionArgs, true, delay, interval);
        addTask(task);
        task.setPlatformTask(scheduleAsyncRepeatingTaskImpl(task, delay, interval, intervalUnit));
        return task;
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask runTaskImpl(Task<ScheduledTask> task) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    @Override
    protected synchronized ScheduledTask runTaskAsyncImpl(Task<ScheduledTask> task) {
        return ProxyServer.getInstance().getScheduler().runAsync(PyBungee.get(), task);
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask runTaskLaterImpl(Task<ScheduledTask> task, long delay) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    @Override
    protected synchronized ScheduledTask runTaskLaterAsyncImpl(Task<ScheduledTask> task, long delay) {
        return runTaskLaterAsyncImpl(task, ticksToMillis(delay), TimeUnit.MILLISECONDS);
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask scheduleRepeatingTaskImpl(RepeatingTask<ScheduledTask> task, long delay, long interval) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    @Override
    protected synchronized ScheduledTask scheduleAsyncRepeatingTaskImpl(RepeatingTask<ScheduledTask> task, long delay, long interval) {
        return scheduleAsyncRepeatingTaskImpl(task, ticksToMillis(delay), ticksToMillis(interval), TimeUnit.MILLISECONDS);
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask runSyncCallbackTaskImpl(SyncCallbackTask<ScheduledTask> task) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask runSyncCallbackTaskLaterImpl(SyncCallbackTask<ScheduledTask> task, long delay) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    /**
     * No-op implementation
     */
    @Override
    protected synchronized ScheduledTask runSyncCallbackImpl(Runnable runnable) {
        //Synchronous tasks not implemented in BungeeCord
        return null;
    }

    @Override
    protected void stopTaskImpl(ScheduledTask platformTask) {
        ProxyServer.getInstance().getScheduler().cancel(platformTask);
    }

    @Override
    protected String describeTask(ScheduledTask platformTask) {
        return String.format("ScheduledTask[id: %d]", platformTask.getId());
    }

    private ScheduledTask runTaskLaterAsyncImpl(Task<ScheduledTask> task, long delay, TimeUnit unit) {
        return ProxyServer.getInstance()
                .getScheduler()
                .schedule(PyBungee.get(), task, delay, unit);
    }

    private ScheduledTask scheduleAsyncRepeatingTaskImpl(Task<ScheduledTask> task, long delay, long interval, TimeUnit unit) {
        return ProxyServer.getInstance()
                .getScheduler()
                .schedule(PyBungee.get(), task, delay, interval, unit);
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
