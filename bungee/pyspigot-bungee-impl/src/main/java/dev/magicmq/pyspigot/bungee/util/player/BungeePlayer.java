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

package dev.magicmq.pyspigot.bungee.util.player;


import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.util.player.PlayerAdapter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * A wrapper for the BungeeCord {@link net.md_5.bungee.api.connection.ProxiedPlayer} class.
 */
public class BungeePlayer implements PlayerAdapter {

    private final ProxiedPlayer player;

    /**
     *
     * @param player The BungeeCord ProxiedPlayer
     */
    public BungeePlayer(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(Component message) {
        Audience player = PyBungee.get().getAdventure().sender(this.player);
        player.sendMessage(message);
    }

}
