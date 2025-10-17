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

package dev.magicmq.pyspigot.bungee;

import dev.magicmq.pyspigot.PluginListener;
import dev.magicmq.pyspigot.bungee.util.player.BungeePlayer;
import dev.magicmq.pyspigot.util.player.PlayerAdapter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

/**
 * The BungeeCord listener.
 */
public class BungeeListener extends PluginListener implements Listener {

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        PlayerAdapter bungeePlayer = new BungeePlayer(event.getPlayer());
        ProxyServer.getInstance().getScheduler().schedule(PyBungee.get().getPlugin(), () -> this.onJoin(bungeePlayer), 500L, TimeUnit.MILLISECONDS);
    }
}
