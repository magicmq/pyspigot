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
import dev.magicmq.pyspigot.util.ScriptUtils;
import dev.simplix.protocolize.api.Direction;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.packet.AbstractPacket;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ProtocolManager {

    private static ProtocolManager instance;

    private final HashMap<Script, List<ScriptPacketListener<?>>> registeredListeners;

    private ProtocolManager() {
        registeredListeners = new HashMap<>();
    }

    /**
     * Register a new packet listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param receiveFunction The function that should be called when the packet is received
     * @param sendFunction The function that should be called when the packet is sent
     * @param packet The packet to listen to
     * @param direction The direction (either {@link Direction#UPSTREAM} or {@link Direction#DOWNSTREAM}
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener<?> registerPacketListener(PyFunction receiveFunction, PyFunction sendFunction, Class<?> packet, Direction direction) {
        return registerPacketListener(receiveFunction, sendFunction, packet, direction, 0);
    }

    /**
     * Register a new packet listener with default priority.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param receiveFunction The function that should be called when the packet is received
     * @param sendFunction The function that should be called when the packet is sent
     * @param packet The packet to listen to
     * @param direction The direction (either {@link Direction#UPSTREAM} or {@link Direction#DOWNSTREAM}
     * @param priority The priority of the listener
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener<?> registerPacketListener(PyFunction receiveFunction, PyFunction sendFunction, Class<?> packet, Direction direction, int priority) {
        Script script = ScriptUtils.getScriptFromCallStack();
        if (getPacketListener(script, packet) == null) {
            ScriptPacketListener<?> listener = new ScriptPacketListener<>(script, receiveFunction, sendFunction, packet, direction, priority);
            addPacketListener(listener);
            Protocolize.listenerProvider().registerListener(listener);
            return listener;
        } else
            throw new RuntimeException("Script already has a packet listener for '" + packet.getSimpleName() + "' registered");
    }

    /**
     * Unregister a packet listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The packet listener to unregister
     */
    public void unregisterPacketListener(ScriptPacketListener<?> listener) {
        Protocolize.listenerProvider().unregisterListener(listener);
        removePacketListener(listener);
    }

    /**
     * Unregister all packet listeners belonging to a script.
     * @param script The script whose normal packet listeners should be unregistered
     */
    public void unregisterPacketListeners(Script script) {
        List<ScriptPacketListener<?>> scriptPacketListeners = registeredListeners.get(script);
        if (scriptPacketListeners != null) {
            for (ScriptPacketListener<?> listener : scriptPacketListeners) {
                Protocolize.listenerProvider().unregisterListener(listener);
            }
            registeredListeners.remove(script);
        }
    }

    /**
     * Get all packet listeners associated with a script.
     * @param script The script to get normal packet listeners from
     * @return A List of {@link ScriptPacketListener} containing all packet listeners associated with this script. Will return null if there are no packet listeners associated with the script
     */
    public List<ScriptPacketListener<?>> getPacketListeners(Script script) {
        return registeredListeners.get(script);
    }

    /**
     * Get the packet listener for a particular packet associated with a script.
     * @param script The script
     * @param packet The packet
     * @return The {@link ScriptPacketListener} associated with the script and packet type, null if there is none
     */
    public ScriptPacketListener<?> getPacketListener(Script script, Class<?> packet) {
        List<ScriptPacketListener<?>> scriptPacketListeners = registeredListeners.get(script);
        if (scriptPacketListeners != null) {
            for (ScriptPacketListener<?> listener : scriptPacketListeners) {
                if (listener.type() == packet)
                    return listener;
            }
        }
        return null;
    }

    /**
     * Send a Protocolize packet to the player with the given UUID.
     * @param playerUUID The UUID of the player to send the packet to
     * @param packet The packet to send
     */
    public void sendPacket(UUID playerUUID, AbstractPacket packet) {
        ProtocolizePlayer player = Protocolize.playerProvider().player(playerUUID);
        if (player != null) {
            player.sendPacket(packet);
        } else {
            throw new RuntimeException("No online player found with the UUID '" + playerUUID.toString() + "'");
        }
    }

    /**
     * Send a generic BungeeCord packet to the player with the given UUID.
     * @param playerUUID The UUID of the player to send the packet to
     * @param packet The packet to send
     */
    public void sendPacket(UUID playerUUID, DefinedPacket packet) {
        ProtocolizePlayer player = Protocolize.playerProvider().player(playerUUID);
        if (player != null) {
            player.sendPacket(packet);
        } else {
            throw new RuntimeException("No online player found with the UUID '" + playerUUID.toString() + "'");
        }
    }

    private void addPacketListener(ScriptPacketListener<?> listener) {
        Script script = listener.getScript();
        if (registeredListeners.containsKey(script))
            registeredListeners.get(script).add(listener);
        else {
            List<ScriptPacketListener<?>> scriptPacketListeners = new ArrayList<>();
            scriptPacketListeners.add(listener);
            registeredListeners.put(script, scriptPacketListeners);
        }
    }

    private void removePacketListener(ScriptPacketListener<?> listener) {
        Script script = listener.getScript();
        List<ScriptPacketListener<?>> scriptPacketListeners = registeredListeners.get(script);
        scriptPacketListeners.remove(listener);
        if (scriptPacketListeners.isEmpty())
            registeredListeners.remove(script);
    }

    public static ProtocolManager get() {
        if (instance == null)
            instance = new ProtocolManager();
        return instance;
    }
}
