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

package dev.magicmq.pyspigot.bungee.manager.script;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.bungee.config.BungeeProjectOptionsConfig;
import dev.magicmq.pyspigot.bungee.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.bungee.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.bungee.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.bungee.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.python.core.PyException;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * The BungeeCord-specific implementation of the script manager.
 */
public class BungeeScriptManager extends ScriptManager {

    private static BungeeScriptManager instance;

    private ScheduledTask startScriptTask;

    private BungeeScriptManager() {
        super(new BungeeScriptInfo());
    }

    @Override
    protected void scheduleStartScriptTask() {
        startScriptTask = ProxyServer.getInstance().getScheduler().schedule(PyBungee.get(), this::loadScripts, PyCore.get().getConfig().getScriptLoadDelay() * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void cancelStartScriptTask() {
        if (startScriptTask != null) {
            startScriptTask.cancel();
        }
    }

    @Override
    protected boolean isPluginDependencyMissing(String dependency) {
        return ProxyServer.getInstance().getPluginManager().getPlugin(dependency) == null;
    }

    @Override
    protected boolean callScriptExceptionEvent(Script script, PyException exception) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        return event.doReportException();
    }

    @Override
    protected void callScriptLoadEvent(Script script) {
        ScriptLoadEvent event = new ScriptLoadEvent(script);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    @Override
    protected void callScriptUnloadEvent(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
    }

    @Override
    protected ScriptOptions newScriptOptions() {
        return new ScriptOptions();
    }

    @Override
    protected ScriptOptions newScriptOptions(Path scriptPath) {
        return new ScriptOptions(scriptPath);
    }

    @Override
    protected ScriptOptions newProjectOptions(Path projectConfigPath) {
        return new ScriptOptions(new BungeeProjectOptionsConfig(projectConfigPath));
    }

    @Override
    protected Script newScript(Path path, String name, ScriptOptions options, boolean project) {
        return new Script(path, name, options, project);
    }

    @Override
    protected void initScriptPermissions(Script script) {
        //Plugin permissions are not implemented in BungeeCord
    }

    @Override
    protected void removeScriptPermissions(Script script) {
        //Plugin permissions are not implemented in BungeeCord
    }

    @Override
    protected void unregisterFromPlatformManagers(Script script) {
        if (PyBungee.get().isProtocolizeAvailable())
            ProtocolManager.get().unregisterPacketListeners(script);
    }

    @Override
    protected void unloadScriptOnMainThread(Script script, boolean error) {
        //No "main thread" on BungeeCord
        unloadScript(script, error);
    }

    /**
     * Get the singleton instance of this BungeeScriptManager.
     * @return The instance
     */
    public static BungeeScriptManager get() {
        if (instance == null)
            instance = new BungeeScriptManager();
        return instance;
    }
}
