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
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

/**
 * The BungeeCord-specific implementation of the {@link dev.magicmq.pyspigot.manager.script.ScriptInfo} class, for printing information related to BungeeCord-specific managers.
 */
public class BungeeScriptInfo extends ScriptInfo {

    @Override
    protected void printPlatformManagerInfo(Script script, TextComponent.Builder appendTo) {
        if (PyBungee.get().isProtocolizeAvailable()) {
            List<String> packetTypes = ProtocolManager.get().getPacketListeners(script)
                    .stream()
                    .map(Object::toString)
                    .toList();
            appendTo.append(Component.text().append(Component.text("Listening to packets: ", NamedTextColor.GOLD)).append(Component.text(packetTypes.toString())));
            appendTo.appendNewline();
        }
    }
}

