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

package dev.magicmq.pyspigot.bukkit.manager.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with ProtocolLib's ProtocolManager. Primarily used by scripts to register and unregister packet listeners on Bukkit/Minecraft.
 * <p>
 * Do not call this manager if ProtocolLib is not loaded and enabled on the server! It will not work.
 * @see com.comphenix.protocol.ProtocolManager
 */
public class ProtocolManager {

    private static ProtocolManager instance;

    private final com.comphenix.protocol.ProtocolManager protocolManager;
    private final AsyncProtocolManager asyncProtocolManager;
    private final HashMap<Script, List<ScriptPacketListener>> registeredListeners;

    private ProtocolManager() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        asyncProtocolManager = new AsyncProtocolManager();
        registeredListeners = new HashMap<>();
    }

    /**
     * Get the current ProtocolLib ProtocolManager.
     * @return The {@link com.comphenix.protocol.ProtocolManager}
     */
    public com.comphenix.protocol.ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * Get the async protocol manager for working with asynchronous listeners.
     * @return The {@link AsyncProtocolManager}
     */
    public AsyncProtocolManager asyncManager() {
        return asyncProtocolManager;
    }

    /**
     * Register a new packet listener with default priority.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener registerPacketListener(PyFunction function, PacketType type) {
        return registerPacketListener(function, type, ListenerPriority.NORMAL);
    }

    /**
     * Register a new packet listener.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @param priority The priority of the packet listener relative to other packet listeners
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener registerPacketListener(PyFunction function, PacketType type, ListenerPriority priority) {
        Script script = ScriptUtils.getScriptFromCallStack();
        if (getPacketListener(script, type) == null) {
            ScriptPacketListener listener = null;
            if (type.getSender() == PacketType.Sender.CLIENT) {
                listener = new PacketReceivingListener(script, function, type, priority, ListenerType.NORMAL);
                addPacketListener(listener);
                protocolManager.addPacketListener(listener);
            } else if (type.getSender() == PacketType.Sender.SERVER) {
                listener = new PacketSendingListener(script, function, type, priority, ListenerType.NORMAL);
                addPacketListener(listener);
                protocolManager.addPacketListener(listener);
            }
            return listener;
        } else
            throw new ScriptRuntimeException(script, "Script already has a packet listener for '" + type.name() + "' registered");
    }

    /**
     * Unregister a packet listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The packet listener to unregister
     */
    public void unregisterPacketListener(ScriptPacketListener listener) {
        protocolManager.removePacketListener(listener);
        removePacketListener(listener);
    }

    /**
     * Unregister a packet listener. Note that multiple packet listeners may be unregistered, if multiple packet listeners are registered to the same function.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function associated with the packet listener to unregister
     */
    public void unregisterPacketListener(PyFunction function) {
        Script script = ScriptUtils.getScriptFromCallStack();
        List<ScriptPacketListener> listeners = getPacketListeners(script);
        for (ScriptPacketListener listener : listeners) {
            if (listener.getFunction().equals(function)) {
                unregisterPacketListener(listener);
            }
        }
    }

    /**
     * Unregister a packet listener. Note that multiple packet listeners may be unregistered, if multiple packet listeners are registered to the same function.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function associated with the packet listener to unregister
     * @param type The packet type associated with the packet listener to unregister
     */
    public void unregisterPacketListener(PyFunction function, PacketType type) {
        Script script = ScriptUtils.getScriptFromCallStack();
        List<ScriptPacketListener> listeners = getPacketListeners(script);
        for (ScriptPacketListener listener : listeners) {
            if (listener.getFunction().equals(function) && listener.getPacketType().equals(type)) {
                unregisterPacketListener(listener);
            }
        }
    }

    /**
     * Unregister all normal packet listeners belonging to a script, excluding asynchronous packet listeners.
     * <p>
     * Use {@link AsyncProtocolManager#unregisterAsyncPacketListeners(Script)} to unregister asynchronous packet listeners.
     * @param script The script whose normal packet listeners should be unregistered
     */
    public void unregisterPacketListeners(Script script) {
        for (ScriptPacketListener listener : getPacketListeners(script)) {
            protocolManager.removePacketListener(listener);
        }
        registeredListeners.remove(script);
    }

    /**
     * Get all normal packet listeners associated with a script, excluding asynchronous packet listeners.
     * <p>
     * Use {@link AsyncProtocolManager#getAsyncPacketListeners(Script)} to get a script's asynchronous packet listeners.
     * @param script The script to get normal packet listeners from
     * @return An immutable list of {@link ScriptPacketListener} containing all normal packet listeners associated with this script. Will return an empty list if there are no normal packet listeners associated with the script
     */
    public List<ScriptPacketListener> getPacketListeners(Script script) {
        List<ScriptPacketListener> scriptPacketListeners = registeredListeners.get(script);
        return scriptPacketListeners != null ? List.copyOf(scriptPacketListeners) : List.of();
    }

    /**
     * Get the normal packet listener for a particular packet type associated with a script.
     * <p>
     * Use {@link AsyncProtocolManager#getAsyncPacketListener(Script, PacketType)} to get a script's asynchronous packet listener of a specific packet type.
     * @param script The script
     * @param packetType The packet type
     * @return The {@link ScriptPacketListener} associated with the script and packet type, or null if there is none
     */
    public ScriptPacketListener getPacketListener(Script script, PacketType packetType) {
        for (ScriptPacketListener listener : getPacketListeners(script)) {
            if (listener.getPacketType() == packetType)
                return listener;
        }
        return null;
    }

    /**
     * Create a new packet with the given type. This method will assign sensible default values to all fields within the packet where a non-null value is required.
     * <p>
     * This method is the preferred way to create a packet that will later be sent or broadcasted.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param type The type of packet to create
     * @return A {@link com.comphenix.protocol.events.PacketContainer} representing the packet that was created.
     */
    public PacketContainer createPacket(PacketType type) {
        return protocolManager.createPacket(type, true);
    }

    /**
     * Send a packet to a player.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param player The player to send the packet to
     * @param packet The packet to send
     */
    public void sendServerPacket(Player player, PacketContainer packet) {
        protocolManager.sendServerPacket(player, packet);
    }

    /**
     * Broadcast a packet to the entire server. The packet will be sent to all online players.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param packet The packet to broadcast
     */
    public void broadcastServerPacket(PacketContainer packet) {
        protocolManager.broadcastServerPacket(packet);
    }

    /**
     * Broadcast a packet to players receiving information about a particular entity. Will also broadcast the packet to the entity, if the entity is a tracker.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @see ProtocolManager#broadcastServerPacket(PacketContainer, Entity, boolean)
     * @param packet The packet to broadcast
     * @param entity The entity whose trackers will be informed
     */
    public void broadcastServerPacket(PacketContainer packet, Entity entity) {
        broadcastServerPacket(packet, entity, true);
    }

    /**
     * Broadcast a packet to players receiving information about a particular entity.
     * <p>
     * Usually, this would be every player in the same world within an observable distance. If the entity is a player, it will be included only if {@code includeTracker} is set to {@code true}.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param packet The packet to broadcast
     * @param entity The entity whose trackers will be informed
     * @param includeTracker Whether to also transmit the packet to the entity, if it is a tracker
     */
    public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
        protocolManager.broadcastServerPacket(packet, entity, includeTracker);
    }

    /**
     * Broadcast a packet to all players within a given max observer distance from an origin location (center point).
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param packet The packet to broadcast
     * @param origin The origin location (center point) to consider when calculating distance to each observer
     * @param maxObserverDistance The maximum distance from origin wherein packets will be broadcasted
     */
    public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
        protocolManager.broadcastServerPacket(packet, origin, maxObserverDistance);
    }

    /**
     * Broadcast a packet to a specified list of players.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param packet The packet to broadcast
     * @param targetPlayers The list of players to which the packet should be broadcasted
     */
    public void broadcastServerPacket(PacketContainer packet, Collection<? extends Player> targetPlayers) {
        protocolManager.broadcastServerPacket(packet, targetPlayers);
    }

    private void addPacketListener(ScriptPacketListener listener) {
        Script script = listener.getScript();
        if (registeredListeners.containsKey(script))
            registeredListeners.get(script).add(listener);
        else {
            List<ScriptPacketListener> scriptPacketListeners = new ArrayList<>();
            scriptPacketListeners.add(listener);
            registeredListeners.put(script, scriptPacketListeners);
        }
    }

    private void removePacketListener(ScriptPacketListener listener) {
        Script script = listener.getScript();
        List<ScriptPacketListener> scriptPacketListeners = registeredListeners.get(script);
        scriptPacketListeners.remove(listener);
        if (scriptPacketListeners.isEmpty())
            registeredListeners.remove(script);
    }

    /**
     * Get the singleton instance of this ProtocolManager.
     * @return The instance
     */
    public static ProtocolManager get() {
        if (instance == null)
            instance = new ProtocolManager();
        return instance;
    }
}
