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

import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
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

/**
 * Manager to interface with Protocolize. Primarily used by scripts to register and unregister packet listeners on the BungeeCord proxy.
 * <p>
 * Do not call this manager if Protocolize is not loaded and enabled on the server! It will not work.
 * @see dev.simplix.protocolize.api.Protocolize
 */
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
            throw new ScriptRuntimeException(script, "Script already has a packet listener for '" + packet.getSimpleName() + "' registered");
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
        List<ScriptPacketListener<?>> scriptPacketListeners = getPacketListeners(script);
        if (!scriptPacketListeners.isEmpty()) {
            for (ScriptPacketListener<?> listener : scriptPacketListeners) {
                Protocolize.listenerProvider().unregisterListener(listener);
            }
            registeredListeners.remove(script);
        }
    }

    /**
     * Get all packet listeners associated with a script.
     * @param script The script to get normal packet listeners from
     * @return An immutable list of {@link ScriptPacketListener} containing all packet listeners associated with this script. Will return an empty list if there are no packet listeners associated with the script
     */
    public List<ScriptPacketListener<?>> getPacketListeners(Script script) {
        List<ScriptPacketListener<?>> scriptPacketListeners = registeredListeners.get(script);
        return scriptPacketListeners != null ? List.copyOf(scriptPacketListeners) : List.of();
    }

    /**
     * Get the packet listener for a particular packet associated with a script.
     * @param script The script
     * @param packet The packet
     * @return The {@link ScriptPacketListener} associated with the script and packet type, null if there is none
     */
    public ScriptPacketListener<?> getPacketListener(Script script, Class<?> packet) {
        List<ScriptPacketListener<?>> scriptPacketListeners = getPacketListeners(script);
        if (!scriptPacketListeners.isEmpty()) {
            for (ScriptPacketListener<?> listener : scriptPacketListeners) {
                if (listener.type() == packet)
                    return listener;
            }
        }
        return null;
    }

    /**
     * Send a Protocolize packet to the player with the given UUID.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param playerUUID The UUID of the player to send the packet to
     * @param packet The packet to send
     */
    public void sendPacket(UUID playerUUID, AbstractPacket packet) {
        Script script = ScriptUtils.getScriptFromCallStack();
        ProtocolizePlayer player = Protocolize.playerProvider().player(playerUUID);
        if (player != null) {
            player.sendPacket(packet);
        } else {
            script.getLogger().warn("Attempted to send a packet, but no player is online with the provided UUID");
        }
    }

    /**
     * Send a generic BungeeCord packet to the player with the given UUID.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param playerUUID The UUID of the player to send the packet to
     * @param packet The packet to send
     */
    public void sendPacket(UUID playerUUID, DefinedPacket packet) {
        Script script = ScriptUtils.getScriptFromCallStack();
        ProtocolizePlayer player = Protocolize.playerProvider().player(playerUUID);
        if (player != null) {
            player.sendPacket(packet);
        } else {
            script.getLogger().warn("Attempted to send a packet, but no player is online with the provided UUID");
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
