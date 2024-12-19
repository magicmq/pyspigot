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

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.task.TaskManager;
import org.python.core.PyFunction;

public class BungeeTaskManager extends TaskManager {

    private static BungeeTaskManager manager;

    private BungeeTaskManager() {
        super();
    }

    //TODO

    @Override
    public int runTask(PyFunction function, Object... functionArgs) {
        return 0;
    }

    @Override
    public int runTaskAsync(PyFunction function, Object... functionArgs) {
        return 0;
    }

    @Override
    public int runTaskLater(PyFunction function, long delay, Object... functionArgs) {
        return 0;
    }

    @Override
    public int runTaskLaterAsync(PyFunction function, long delay, Object... functionArgs) {
        return 0;
    }

    @Override
    public int scheduleRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        return 0;
    }

    @Override
    public int scheduleAsyncRepeatingTask(PyFunction function, long delay, long interval, Object... functionArgs) {
        return 0;
    }

    @Override
    public int runSyncCallbackTask(PyFunction function, PyFunction callback, Object... functionArgs) {
        return 0;
    }

    @Override
    public int runSyncCallbackTaskLater(PyFunction function, PyFunction callback, long delay, Object... functionArgs) {
        return 0;
    }

    @Override
    public void stopTask(int taskId) {

    }

    @Override
    public void stopTasks(Script script) {

    }

    @Override
    public int runSyncCallback(Runnable runnable) {
        return 0;
    }

    public static BungeeTaskManager get() {
        if (manager == null)
            manager = new BungeeTaskManager();
        return manager;
    }
}
