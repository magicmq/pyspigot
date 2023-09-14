/*
 *    Copyright 2023 magicmq
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

package dev.magicmq.pyspigot.manager.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager to interface with ProtocolLib's ProtocolManager. Primarily used by scripts to register and unregister packet listeners.
 * <p>
 * Do not call this manager if ProtocolLib is not loaded and enabled on the server! It will not work.
 * @see com.comphenix.protocol.ProtocolManager
 */
public class ProtocolManager {

    private static ProtocolManager instance;

    private com.comphenix.protocol.ProtocolManager protocolManager;
    private AsyncProtocolManager asyncProtocolManager;
    private List<ScriptPacketListener> listeners;

    private ProtocolManager() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        asyncProtocolManager = new AsyncProtocolManager();
        listeners = new ArrayList<>();
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
    public AsyncProtocolManager async() {
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
        Script script = ScriptManager.get().getScriptFromCallStack();
        if (getPacketListener(script, type) == null) {
            ScriptPacketListener listener = null;
            if (type.getSender() == PacketType.Sender.CLIENT) {
                listener = new PacketReceivingListener(script, function, type, priority, ListenerType.NORMAL);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            } else if (type.getSender() == PacketType.Sender.SERVER) {
                listener = new PacketSendingListener(script, function, type, priority, ListenerType.NORMAL);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            }
            return listener;
        } else
            throw new RuntimeException("Script already has a packet listener for '" + type.name() + "' registered");
    }

    /**
     * Unregister a packet listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The packet listener to unregister
     */
    public void unregisterPacketListener(ScriptPacketListener listener) {
        protocolManager.removePacketListener(listener);
        listeners.remove(listener);
    }

    /**
     * Unregister all normal packet listeners belonging to a script, excluding asynchronous packet listeners.
     * <p>
     * Use {@link AsyncProtocolManager#unregisterAsyncPacketListeners(Script)} to unregister asynchronous packet listeners.
     * @param script The script whose normal packet listeners should be unregistered
     */
    public void unregisterPacketListeners(Script script) {
        List<ScriptPacketListener> associatedListeners = getPacketListeners(script);
        for (ScriptPacketListener packetListener : associatedListeners) {
            unregisterPacketListener(packetListener);
        }
    }

    /**
     * Get all normal packet listeners associated with a script, excluding asynchronous packet listeners.
     * <p>
     * Use {@link AsyncProtocolManager#getAsyncPacketListeners(Script)} to get a script's asynchronous packet listeners.
     * @param script The script to get normal packet listeners from
     * @return A List of {@link ScriptPacketListener} containing all normal packet listeners associated with this script. Will return an empty list if there are no normal packet listeners associated with the script
     */
    public List<ScriptPacketListener> getPacketListeners(Script script) {
        List<ScriptPacketListener> toReturn = new ArrayList<>();
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script))
                toReturn.add(listener);
        }
        return toReturn;
    }

    /**
     * Get the normal packet listener for a particular packet type associated with a script.
     * <p>
     * Use {@link AsyncProtocolManager#getAsyncPacketListener(Script, PacketType)} to get a script's asynchronous packet listener of a specific packet type.
     * @param script The script
     * @param packetType The packet type
     * @return The {@link ScriptPacketListener} associated with the script and packet type, null if there is none
     */
    public ScriptPacketListener getPacketListener(Script script, PacketType packetType) {
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script) && listener.getPacketType().equals(packetType)) {
                return listener;
            }
        }
        return null;
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
