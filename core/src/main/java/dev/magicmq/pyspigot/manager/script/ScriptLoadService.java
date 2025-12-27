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

package dev.magicmq.pyspigot.manager.script;


import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.exception.ScriptInitializationException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

/**
 * A service scheduled with a platform-specific scheduler which loads a batch of scripts/projects, adding an interval
 * delay between each load operation.
 */
public class ScriptLoadService implements Runnable {

    private final Queue<LoadableScript> toLoad;

    private long ticksSinceLoad;

    /**
     *
     * @param scripts The scripts to load
     */
    public ScriptLoadService(Set<Script> scripts) {
        this.toLoad = new ArrayDeque<>();
        scripts.forEach(script -> toLoad.add(new LoadableScript(script)));

        this.ticksSinceLoad = PyCore.get().getConfig().getScriptLoadInterval();
    }

    /**
     * Called each time the platform-specific task runs. Loads a script/project if the configured number of ticks has
     * passed since the previous load operation.
     * <p>
     * This method also self-cancels the underlying task when there are no more scripts/projects to load.
     */
    @Override
    public void run() {
        if (!toLoad.isEmpty()) {
            if (ticksSinceLoad >= PyCore.get().getConfig().getScriptLoadInterval()) {
                LoadableScript script = toLoad.poll();
                script.load();
                ticksSinceLoad = 1;
            } else
                ticksSinceLoad++;
        } else
            ScriptManager.get().finishScriptLoading();
    }

    private record LoadableScript(Script script) {

        public void load() {
            try {
                if (script.isProject())
                    ScriptManager.get().loadProject(script);
                else
                    ScriptManager.get().loadScript(script);
            } catch (ScriptInitializationException e) {
                PyCore.get().getLogger().error("Error when loading script/project '{}'", script.getName(), e);
            }
        }
    }
}
