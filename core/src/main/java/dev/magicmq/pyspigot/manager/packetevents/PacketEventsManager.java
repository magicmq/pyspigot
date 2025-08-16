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


import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with PacketEvents. Primarily used by scripts to register and unregister packet listeners.
 * <p>
 * <b>Note:</b> This manager is platform-agnostic, however, it requires the appropriate PacketEvents platform-specific plugin to be present.
 * @see com.github.retrooper.packetevents.PacketEventsAPI
 */
public class PacketEventsManager {

    private static PacketEventsManager instance;

    private final PacketEventsAPI<?> packetEvents;
    private final HashMap<Script, List<ScriptPacketListener>> registeredListeners;

    private PacketEventsManager() {
        this.packetEvents = PacketEvents.getAPI();
        this.registeredListeners = new HashMap<>();
    }

    /**
     * Get the underlying PacketEvents API instance.
     * @return The PacketEvents API instance
     */
    public PacketEventsAPI<?> getPacketEventsAPI() {
        return packetEvents;
    }

    /**
     * Register a new packet listener with default priority.
     * <p>
     * This method will automatically register a {@link PacketReceiveListener} or a {@link PacketSendListener}, depending on if the provided packet type is incoming or outgoing, respectively.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the packet event occurs. Use {@link com.github.retrooper.packetevents.protocol.packettype.PacketType} to select a packet type
     * @param type The packet type to listen for
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener registerPacketListener(PyFunction function, PacketTypeCommon type) {
        return registerPacketListener(function, type, PacketListenerPriority.NORMAL);
    }

    /**
     * Register a new packet listener with the specified priority.
     * <p>
     * This method will automatically register a {@link PacketReceiveListener} or a {@link PacketSendListener}, depending on if the provided packet type is incoming or outgoing, respectively.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when the packet event occurs. Use {@link com.github.retrooper.packetevents.protocol.packettype.PacketType} to select a packet type
     * @param type The packet type to listen for
     * @param priority The priority of the packet listener, relative to other packet listeners
     * @return A {@link ScriptPacketListener} representing the packet listener that was registered
     */
    public ScriptPacketListener registerPacketListener(PyFunction function, PacketTypeCommon type, PacketListenerPriority priority) {
        Script script = ScriptUtils.getScriptFromCallStack();
        if (getPacketListener(script, type) == null) {
            ScriptPacketListener listener = null;
            if (type.getSide() == PacketSide.CLIENT) {
                listener = new PacketReceiveListener(script, function, type);
                addPacketListener(listener);
                listener.setRegisteredListener(packetEvents.getEventManager().registerListener(listener, priority));
            } else if (type.getSide() == PacketSide.SERVER) {
                listener = new PacketSendListener(script, function, type);
                addPacketListener(listener);
                listener.setRegisteredListener(packetEvents.getEventManager().registerListener(listener, priority));
            }
            return listener;
        } else
            throw new ScriptRuntimeException(script, "Script already has a packet listener for '" + type.getName() + "' registered");
    }

    /**
     * Unregister a packet listener.
     * @param listener The listener to unregister
     */
    public void unregisterPacketListener(ScriptPacketListener listener) {
        packetEvents.getEventManager().unregisterListener(listener.getRegisteredListener());
        removePacketListener(listener);
    }

    /**
     * Unregister all packet listeners belonging to a script.
     * @param script The script whose packet listeners should be unregistered
     */
    public void unregisterPacketListeners(Script script) {
        for (ScriptPacketListener listener : getPacketListeners(script)) {
            packetEvents.getEventManager().unregisterListener(listener.getRegisteredListener());
        }
        registeredListeners.remove(script);
    }

    /**
     * Get all packet listeners belonging to a particular script.
     * @param script The script whose packet listeners should be obtained
     * @return An immutable list of {@link ScriptPacketListener} containing all packet listeners associated with the script. Will return an empty list if there are no normal packet listeners associated with the script
     */
    public List<ScriptPacketListener> getPacketListeners(Script script) {
        List<ScriptPacketListener> scriptPacketListeners = registeredListeners.get(script);
        return scriptPacketListeners != null ? List.copyOf(scriptPacketListeners) : List.of();
    }

    /**
     * Get the packet listener for a particular packet type associated with a script.
     * @param script The script whose packet listener should be obtained
     * @param packetType The packet type of the listener
     * @return The {@link ScriptPacketListener} associated with the script and packet type, or null if there is none
     */
    public ScriptPacketListener getPacketListener(Script script, PacketTypeCommon packetType) {
        for (ScriptPacketListener listener : getPacketListeners(script)) {
            if (listener.getPacketType() == packetType)
                return listener;
        }
        return null;
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

    public static PacketEventsManager get() {
        if (instance == null)
            instance = new PacketEventsManager();
        return instance;
    }
}
