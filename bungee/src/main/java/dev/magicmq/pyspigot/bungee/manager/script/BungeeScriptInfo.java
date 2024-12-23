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

import dev.magicmq.pyspigot.bungee.PyBungee;
import dev.magicmq.pyspigot.bungee.manager.protocol.ProtocolManager;
import dev.magicmq.pyspigot.bungee.manager.protocol.ScriptPacketListener;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptInfo;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class BungeeScriptInfo extends ScriptInfo {

    @Override
    public void printPlatformManagerInfo(Script script, StringBuilder appendTo) {
        if (PyBungee.get().isProtocolizeAvailable()) {
            List<ScriptPacketListener<?>> registeredPacketListeners = ProtocolManager.get().getPacketListeners(script);
            List<String> packetTypes = new ArrayList<>();
            if (registeredPacketListeners != null)
                registeredPacketListeners.forEach(listener -> packetTypes.add(listener.toString()));
            appendTo.append(ChatColor.GOLD + "Listening to packets: " + ChatColor.RESET + packetTypes + "\n");
        }
    }
}

