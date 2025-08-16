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

package dev.magicmq.pyspigot.manager.packetevents;


import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

/**
 * A listener that listens for packets sent by the server to the client.
 * @see ScriptPacketListener
 */
public class PacketSendListener extends ScriptPacketListener {

    /**
     *
     * @param script The script associated with this packet listener
     * @param function The function to be called when the packet is sent
     * @param packetType The packet type to listen for
     */
    public PacketSendListener(Script script, PyFunction function, PacketTypeCommon packetType) {
        super(script, function, packetType);
    }

    /**
     * Called internally when the packet is sent.
     */
    @Override
    public void onPacketSend(PacketSendEvent event) {
        super.callToScript(event);
    }
}
