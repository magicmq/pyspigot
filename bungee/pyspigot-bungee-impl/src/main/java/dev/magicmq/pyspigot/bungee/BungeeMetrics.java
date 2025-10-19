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

package dev.magicmq.pyspigot.bungee;


import dev.magicmq.pyspigot.MetricsAdapter;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

/**
 * The bStats metrics implementation for BungeeCord.
 */
public class BungeeMetrics implements MetricsAdapter {

    private Metrics metrics;

    @Override
    public void setup() {
        metrics = new Metrics(PyBungee.get().getPlugin(), 18991);

        metrics.addCustomChart(new SimplePie("all_scripts", () -> {
            int allScripts = ScriptManager.get().getAllScriptPaths().size() + ScriptManager.get().getAllProjectPaths().size();
            return "" + allScripts;
        }));

        metrics.addCustomChart(new SimplePie("loaded_scripts", () -> {
            int loadedScripts = ScriptManager.get().getLoadedScripts().size();
            return "" + loadedScripts;
        }));
    }

    @Override
    public void shutdown() {
        metrics.shutdown();
    }

}
