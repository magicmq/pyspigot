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
        return ProxyServer.getInstance().getScheduler().schedule(PyBungee.get(), task, ticksToMillis(delay), TimeUnit.MILLISECONDS);
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
        return ProxyServer.getInstance().getScheduler().schedule(PyBungee.get(), task, ticksToMillis(delay), ticksToMillis(interval), TimeUnit.MILLISECONDS);
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
