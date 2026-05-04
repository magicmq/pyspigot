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

package dev.magicmq.pyspigot.bungee.manager.protocol;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import dev.simplix.protocolize.api.Direction;
import dev.simplix.protocolize.api.listener.AbstractPacketListener;
import dev.simplix.protocolize.api.listener.PacketReceiveEvent;
import dev.simplix.protocolize.api.listener.PacketSendEvent;
import jep.JepException;
import jep.python.PyCallable;

/**
 * A script listener that listens for BungeeCord packets.
 * @param <T> The packet to listen for
 */
public class ScriptPacketListener<T> extends AbstractPacketListener<T> {

    private final Script script;
    private final PyCallable receiveFunction;
    private final PyCallable sendFunction;

    /**
     *
     * @param script The script associated with this packet listener
     * @param receiveFunction The function to be called when the packet is received
     * @param sendFunction The function to be called when the packet is sent
     * @param packet The packet to listen for
     * @param direction The {@link dev.simplix.protocolize.api.Direction} of the listener
     * @param priority The priority of the listener
     */
    public ScriptPacketListener(Script script, PyCallable receiveFunction, PyCallable sendFunction, Class<T> packet, Direction direction, int priority) {
        super(packet, direction, priority);
        this.script = script;
        this.receiveFunction = receiveFunction;
        this.sendFunction = sendFunction;
    }

    /**
     * Get the receive function associated with this packet listener.
     * @return The receive function
     */
    public PyCallable getReceiveFunction() {
        return receiveFunction;
    }

    /**
     * Get the send function associated with this packet listener.
     * @return The send function
     */
    public PyCallable getSendFunction() {
        return sendFunction;
    }

    /**
     * Called internally when the packet is received.
     * @param event The packet receive event
     */
    @Override
    public void packetReceive(PacketReceiveEvent<T> event) {
        try {
            ScriptManager.get().getInterpreter().call(script, () -> receiveFunction.call(event));
        } catch (JepException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when calling packet receive listener");
        }
    }

    /**
     * Called internally when the packet is sent.
     * @param event The packet send event
     */
    @Override
    public void packetSend(PacketSendEvent<T> event) {
        try {
            ScriptManager.get().getInterpreter().call(script, () -> sendFunction.call(event));
        } catch (JepException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when calling packet send listener");
        }
    }

    /**
     * Get the script associated with this listener.
     * @return The script associated with this listener
     */
    public Script getScript() {
        return script;
    }

    /**
     * Prints a representation of this ScriptPacketListener in string format, including the packet type listened to by the listener
     * @return A string representation of the ScriptPacketListener
     */
    @Override
    public String toString() {
        return String.format("ScriptPacketListener[Packet: %s]", type().toString());
    }
}
