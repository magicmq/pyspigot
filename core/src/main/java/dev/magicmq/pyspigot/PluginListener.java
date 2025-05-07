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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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

    private Component buildMessage(String version) {
        Component pluginPage = Component.text("Download the latest version here.")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.openUrl("https://spigotmc.org/resources/pyspigot.111006/"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to go to the PySpigot plugin page", NamedTextColor.GOLD)));

        return Component.text()
                .append(Component.text("You're running an outdated version of PySpigot. The latest version is " + version + ". ", NamedTextColor.RED))
                .append(pluginPage)
                .build();
    }

}
