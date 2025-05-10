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

package dev.magicmq.pyspigot.velocity.manager.script;


import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.config.ProjectOptionsConfig;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import dev.magicmq.pyspigot.velocity.PyVelocity;
import dev.magicmq.pyspigot.velocity.config.VelocityProjectOptionsConfig;
import dev.magicmq.pyspigot.velocity.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.velocity.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.velocity.event.ScriptUnloadEvent;
import org.python.core.PyException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class VelocityScriptManager extends ScriptManager {

    private static VelocityScriptManager instance;

    private ScheduledTask startScriptTask;

    private VelocityScriptManager() {
        super(new VelocityScriptInfo());
    }

    @Override
    protected void scheduleStartScriptTask() {
        startScriptTask = PyVelocity.get().getProxy().getScheduler()
                .buildTask(PyVelocity.get(), this::loadScripts)
                .delay(PyCore.get().getConfig().getScriptLoadDelay() * 50L, TimeUnit.MILLISECONDS)
                .schedule();
    }

    @Override
    protected void cancelStartScriptTask() {
        if (startScriptTask != null) {
            startScriptTask.cancel();
        }
    }

    @Override
    protected boolean isPluginDependencyMissing(String dependency) {
        Optional<PluginContainer> plugin = PyVelocity.get().getProxy().getPluginManager().getPlugin(dependency);
        return plugin.isPresent();
    }

    @Override
    protected boolean callScriptExceptionEvent(Script script, PyException exception) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception);
        try {
            return PyVelocity.get().getProxy().getEventManager().fire(event).get().doReportException();
        } catch (InterruptedException e) {
            PyCore.get().getLogger().error("Interrupted while waiting for ScriptExceptionEvent to complete", e);
            return true;
        } catch (ExecutionException e) {
            PyCore.get().getLogger().error("Error when calling ScriptExceptionEvent", e);
            return true;
        }
    }

    @Override
    protected void callScriptLoadEvent(Script script) {
        ScriptLoadEvent event = new ScriptLoadEvent(script);
        PyVelocity.get().getProxy().getEventManager().fireAndForget(event);
    }

    @Override
    protected void callScriptUnloadEvent(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        PyVelocity.get().getProxy().getEventManager().fireAndForget(event);
    }

    @Override
    protected ScriptOptions newScriptOptions(Path scriptPath) {
        return new ScriptOptions(scriptPath);
    }

    @Override
    protected ScriptOptions newProjectOptions(Path projectConfigPath) {
        if (projectConfigPath != null)
            return new ScriptOptions(new VelocityProjectOptionsConfig(projectConfigPath));
        else
            return new ScriptOptions((ProjectOptionsConfig) null);
    }

    @Override
    protected Script newScript(Path path, String name, ScriptOptions options, boolean project) {
        return new Script(path, name, options, project);
    }

    /**
     * No-op implementation
     */
    @Override
    protected void initScriptPermissions(Script script) {
        //Plugin permissions are not implemented in Velocity
    }

    /**
     * No-op implementation
     */
    @Override
    protected void removeScriptPermissions(Script script) {
        //Plugin permissions are not implemented in Velocity
    }

    @Override
    protected void unregisterFromPlatformManagers(Script script) {
        //TODO Unregister from platform managers if there are any
    }

    @Override
    protected void unloadScriptOnMainThread(Script script, boolean error) {
        //No "main thread" on Velocity
        unloadScript(script, error);
    }

    public static VelocityScriptManager get() {
        if (instance == null)
            instance = new VelocityScriptManager();
        return instance;
    }
}
