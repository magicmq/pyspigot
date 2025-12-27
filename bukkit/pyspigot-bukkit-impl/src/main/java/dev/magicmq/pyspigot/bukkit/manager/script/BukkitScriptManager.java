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

package dev.magicmq.pyspigot.bukkit.manager.script;

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.bukkit.config.BukkitProjectOptionsConfig;
import dev.magicmq.pyspigot.bukkit.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.bukkit.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.bukkit.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.bukkit.manager.messaging.PluginMessageManager;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
import dev.magicmq.pyspigot.manager.script.ScriptLoadService;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.python.core.PyException;

import java.nio.file.Path;

/**
 * The Bukkit-specific implementation of the script manager.
 */
public class BukkitScriptManager extends ScriptManager {

    private static BukkitScriptManager instance;

    private BukkitTask startScriptTask;
    private BukkitTask scriptLoadService;

    private BukkitScriptManager() {
        super(new BukkitScriptInfo());
    }

    @Override
    protected void scheduleStartScriptTask() {
        startScriptTask = Bukkit.getScheduler().runTaskLater(PySpigot.get().getPlugin(), () -> this.loadScripts(), PyCore.get().getConfig().getScriptLoadDelay());
    }

    @Override
    protected void cancelStartScriptTask() {
        if (startScriptTask != null) {
            startScriptTask.cancel();
        }
    }

    @Override
    protected void scheduleScriptLoadService(ScriptLoadService service) {
        scriptLoadService = Bukkit.getScheduler().runTaskTimer(PySpigot.get().getPlugin(), service, 0L, 1L);
    }

    @Override
    protected void cancelScriptLoadService() {
        if (scriptLoadService != null) {
            scriptLoadService.cancel();
        }
    }

    @Override
    protected boolean isPluginDependencyMissing(String dependency) {
        return Bukkit.getPluginManager().getPlugin(dependency) == null;
    }

    @Override
    protected boolean callScriptExceptionEvent(Script script, PyException exception) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return event.doReportException();
    }

    @Override
    protected void callScriptLoadEvent(Script script) {
        ScriptLoadEvent event = new ScriptLoadEvent(script);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    protected void callScriptUnloadEvent(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    protected ScriptOptions newScriptOptions(Path scriptPath) {
        return new BukkitScriptOptions(scriptPath);
    }

    @Override
    protected ScriptOptions newProjectOptions(Path projectConfigPath) {
        if (projectConfigPath != null)
            return new BukkitScriptOptions(new BukkitProjectOptionsConfig(projectConfigPath));
        else
            return new BukkitScriptOptions((BukkitProjectOptionsConfig) null);
    }

    @Override
    protected Script newScript(Path path, String name, ScriptOptions options, boolean project) {
        return new BukkitScript(path, name, (BukkitScriptOptions) options, project);
    }

    @Override
    protected void initScriptPermissions(Script script) {
        ((BukkitScript) script).initPermissions();
    }

    @Override
    protected void removeScriptPermissions(Script script) {
        ((BukkitScript) script).removePermissions();
    }

    @Override
    protected void unregisterFromPlatformManagers(Script script) {
        if (PySpigot.get().isProtocolLibAvailable()) {
            ProtocolManager.get().unregisterPacketListeners(script);
            ProtocolManager.get().asyncManager().unregisterAsyncPacketListeners(script);
        }

        if (PySpigot.get().isPlaceholderApiAvailable()) {
            PlaceholderManager.get().unregisterPlaceholder(script);
        }

        PluginMessageManager.get().unregisterListeners(script);
    }

    @Override
    protected void unloadScriptOnMainThread(Script script, boolean error) {
        if (!Bukkit.isPrimaryThread())
            Bukkit.getScheduler().runTask(PySpigot.get().getPlugin(), () -> unloadScript(script, error));
        else
            unloadScript(script, error);
    }

    /**
     * Get the singleton instance of this BukkitScriptManager.
     * @return The instance
     */
    public static BukkitScriptManager get() {
        if (instance == null)
            instance = new BukkitScriptManager();
        return instance;
    }
}
