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

package dev.magicmq.pyspigot.velocity;


import com.velocitypowered.api.proxy.ProxyServer;
import dev.magicmq.pyspigot.MetricsAdapter;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

/**
 * The bStats metrics implementation for Velocity.
 */
public class VelocityMetrics implements MetricsAdapter {

    private Metrics metrics;

    @Override
    public void setup() {
        try {
            metrics = getMetrics();

            metrics.addCustomChart(new SimplePie("all_scripts", () -> {
                int allScripts = ScriptManager.get().getAllScriptPaths().size() + ScriptManager.get().getAllProjectPaths().size();
                return "" + allScripts;
            }));

            metrics.addCustomChart(new SimplePie("loaded_scripts", () -> {
                int loadedScripts = ScriptManager.get().getLoadedScripts().size();
                return "" + loadedScripts;
            }));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            PyVelocity.get().getPlatformLogger().error("Failed to initialize bStats metrics", e);
        }
    }

    @Override
    public void shutdown() {
        metrics.shutdown();
    }

    private Metrics getMetrics() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<Metrics> metricsConstructor = Metrics.class.getDeclaredConstructor(
                Object.class,
                ProxyServer.class,
                Logger.class,
                Path.class,
                int.class
        );

        metricsConstructor.setAccessible(true);

        return metricsConstructor.newInstance(
                PyVelocity.get(),
                PyVelocity.get().getProxy(),
                PyVelocity.get().getPlatformLogger(),
                PyVelocity.get().getDataFolderPath(),
                18991
        );
    }
}
