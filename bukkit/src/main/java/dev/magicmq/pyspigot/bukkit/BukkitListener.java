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

package dev.magicmq.pyspigot.bukkit;

import dev.magicmq.pyspigot.PluginListener;
import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.bukkit.util.player.BukkitPlayer;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.player.PlayerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.logging.Level;

/**
 * The Bukkit listener.
 */
public class BukkitListener extends PluginListener implements Listener {

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (PyCore.get().getConfig().doScriptUnloadOnPluginDisable()) {
            for (Script script : ScriptManager.get().getLoadedScripts()) {
                for (String depend : script.getOptions().getPluginDependencies()) {
                    if (event.getPlugin().getName().equals(depend)) {
                        PyCore.get().getLogger().log(Level.WARNING, "Unloading script '" + script.getName() + "' because its plugin dependency '" + event.getPlugin().getName() + "' was unloaded.");
                        ScriptManager.get().unloadScript(script, false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerAdapter bukkitPlayer = new BukkitPlayer(event.getPlayer());
        Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> this.onJoin(bukkitPlayer), 10L);
    }
}
