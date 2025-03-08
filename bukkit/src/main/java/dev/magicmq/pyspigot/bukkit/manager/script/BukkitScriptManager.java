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
import dev.magicmq.pyspigot.bukkit.event.ScriptExceptionEvent;
import dev.magicmq.pyspigot.bukkit.event.ScriptLoadEvent;
import dev.magicmq.pyspigot.bukkit.event.ScriptUnloadEvent;
import dev.magicmq.pyspigot.bukkit.manager.placeholder.PlaceholderManager;
import dev.magicmq.pyspigot.bukkit.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.config.ProjectOptionsConfig;
import dev.magicmq.pyspigot.exception.InvalidConfigurationException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.manager.script.ScriptOptions;
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

    private BukkitScriptManager() {
        super(new BukkitScriptInfo());
    }

    @Override
    public void scheduleStartScriptTask() {
        startScriptTask = Bukkit.getScheduler().runTaskLater(PySpigot.get(), this::loadScripts, PyCore.get().getConfig().getScriptLoadDelay());
    }

    @Override
    public void cancelStartScriptTask() {
        if (startScriptTask != null) {
            startScriptTask.cancel();
        }
    }

    @Override
    public boolean isPluginDependencyMissing(String dependency) {
        return Bukkit.getPluginManager().getPlugin(dependency) == null;
    }

    @Override
    public boolean callScriptExceptionEvent(Script script, PyException exception) {
        ScriptExceptionEvent event = new ScriptExceptionEvent(script, exception, !Bukkit.isPrimaryThread());
        Bukkit.getPluginManager().callEvent(event);
        return event.doReportException();
    }

    @Override
    public void callScriptLoadEvent(Script script) {
        ScriptLoadEvent event = new ScriptLoadEvent(script);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public void callScriptUnloadEvent(Script script, boolean error) {
        ScriptUnloadEvent event = new ScriptUnloadEvent(script, error);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public ScriptOptions newScriptOptions() {
        return new BukkitScriptOptions();
    }

    @Override
    public ScriptOptions newScriptOptions(Path scriptPath) throws InvalidConfigurationException {
        return new BukkitScriptOptions(scriptPath);
    }

    @Override
    public ScriptOptions newScriptOptions(ProjectOptionsConfig config) throws InvalidConfigurationException {
        return new BukkitScriptOptions(config);
    }

    @Override
    public Script newScript(Path path, String name, ScriptOptions options, boolean project) {
        return new BukkitScript(path, name, (BukkitScriptOptions) options, project);
    }

    @Override
    public void initScriptPermissions(Script script) {
        ((BukkitScript) script).initPermissions();
    }

    @Override
    public void removeScriptPermissions(Script script) {
        ((BukkitScript) script).removePermissions();
    }

    @Override
    public void unregisterFromPlatformManagers(Script script) {
        if (PySpigot.get().isProtocolLibAvailable()) {
            ProtocolManager.get().unregisterPacketListeners(script);
            ProtocolManager.get().async().unregisterAsyncPacketListeners(script);
        }

        if (PySpigot.get().isPlaceholderApiAvailable()) {
            PlaceholderManager.get().unregisterPlaceholder(script);
        }
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
