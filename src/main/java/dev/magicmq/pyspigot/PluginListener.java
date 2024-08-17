/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot;

import dev.magicmq.pyspigot.config.PluginConfig;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.magicmq.pyspigot.util.StringUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.logging.Level;

/**
 * Main listener of the plugin. Currently used to listen for plugin disable (to disable scripts that depend on a disabled plugin) and to listen for player join to send PySpigot update messages.
 */
public class PluginListener implements Listener {

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (PluginConfig.doScriptUnloadOnPluginDisable()) {
            for (Script script : ScriptManager.get().getLoadedScripts()) {
                for (String depend : script.getOptions().getPluginDependencies()) {
                    if (event.getPlugin().getName().equals(depend)) {
                        PySpigot.get().getLogger().log(Level.WARNING, "Unloading script '" + script.getName() + "' because its plugin dependency '" + event.getPlugin().getName() + "' was unloaded.");
                        ScriptManager.get().unloadScript(script, false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (PluginConfig.shouldShowUpdateMessages()) {
            Player player = event.getPlayer();
            if (player.hasPermission("pyspigot.admin")) {
                String latest = PySpigot.get().getSpigotVersion();
                if (latest != null) {
                    Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> {
                        StringUtils.Version currentVersion = new StringUtils.Version(PySpigot.get().getDescription().getVersion());
                        StringUtils.Version latestVersion = new StringUtils.Version(latest);
                        if (currentVersion.compareTo(latestVersion) < 0) {
                            player.spigot().sendMessage(buildMessage(latest));
                        }
                    }, 10L);
                }
            }
        }
    }

    private BaseComponent[] buildMessage(String version) {
        TextComponent pluginPage = new TextComponent("Download the latest version here.");
        pluginPage.setColor(net.md_5.bungee.api.ChatColor.RED);
        pluginPage.setUnderlined(true);
        pluginPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://spigotmc.org/resources/pyspigot.111006/"));
        pluginPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&6Click to go to the PySpigot plugin page"))));

        ComponentBuilder builder = new ComponentBuilder("");
        builder.append("You're running an outdated version of PySpigot. The latest version is " + version + ". ").color(net.md_5.bungee.api.ChatColor.RED).append(pluginPage);
        return builder.create();
    }
}
