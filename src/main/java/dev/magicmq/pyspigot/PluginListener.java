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
import dev.magicmq.pyspigot.util.StringUtils;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Main listener of the plugin. Used only for notifying if using an outdated version of the plugin on server join.
 */
public class PluginListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!PluginConfig.shouldSuppressUpdateMessages()) {
            Player player = event.getPlayer();
            if (player.hasPermission("pyspigot.admin")) {
                Bukkit.getScheduler().runTaskLater(PySpigot.get(), () -> PySpigot.get().checkVersion((version) -> {
                    StringUtils.Version thisVersion = new StringUtils.Version(PySpigot.get().getDescription().getVersion());
                    StringUtils.Version latestVersion = new StringUtils.Version(version);
                    if (thisVersion.compareTo(latestVersion) < 0) {
                        player.spigot().sendMessage(buildMessage(version));
                    }
                }), 10L);
            }
        }
    }

    private BaseComponent[] buildMessage(String version) {
        TextComponent pluginPage = new TextComponent("Download the latest version here.");
        pluginPage.setColor(net.md_5.bungee.api.ChatColor.RED);
        pluginPage.setUnderlined(true);
        pluginPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://spigotmc.org/resources/pyspigot.111006/"));
        pluginPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(net.md_5.bungee.api.ChatColor.GOLD + "Click to go to the PySpigot plugin page")));

        ComponentBuilder builder = new ComponentBuilder();
        builder.append("You're running an outdated version of PySpigot. The latest version is " + version + ". ").color(net.md_5.bungee.api.ChatColor.RED).append(pluginPage).reset();
        return builder.create();
    }
}
