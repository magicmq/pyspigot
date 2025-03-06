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

package dev.magicmq.pyspigot;


import dev.magicmq.pyspigot.util.StringUtils;
import dev.magicmq.pyspigot.util.player.PlayerAdapter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * The primary listener for PySpigot. Methods called in this class are called via platform-specific listeners.
 */
public class PluginListener {

    protected void onJoin(PlayerAdapter player) {
        if (PyCore.get().getConfig().shouldShowUpdateMessages()) {
            if (player.hasPermission("pyspigot.admin")) {
                String latest = PyCore.get().getSpigotVersion();
                if (latest != null) {
                    StringUtils.Version currentVersion = new StringUtils.Version(PyCore.get().getVersion());
                    StringUtils.Version latestVersion = new StringUtils.Version(latest);
                    if (currentVersion.compareTo(latestVersion) < 0) {
                        player.sendMessage(buildMessage(latest));
                    }
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
