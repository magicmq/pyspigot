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
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * An abstract class designed to represent a basic script packet listener.
 * @see com.comphenix.protocol.events.PacketAdapter
 */
public abstract class ScriptPacketListener extends PacketAdapter {

    private final Script script;
    private final PyFunction function;
    private final PacketType packetType;
    private final ListenerType listenerType;

    /**
     *
     * @param script The script associated with this packet listener
     * @param function The function to be called when the packet event occurs
     * @param packetType The packet type to listen for
     * @param listenerPriority The {@link com.comphenix.protocol.events.ListenerPriority} of this listener
     * @param listenerType The {@link ListenerType} of this listener
     */
    public ScriptPacketListener(Script script, PyFunction function, PacketType packetType, ListenerPriority listenerPriority, ListenerType listenerType) {
        super(PySpigot.get(), listenerPriority, packetType);
        this.script = script;
        this.function = function;
        this.packetType = packetType;
        this.listenerType = listenerType;
    }

    /**
     * Get the script associated with this listener.
     * @return The script associated with this listener
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the function that should be called when the packet event occurs.
     * @return The function that should be called
     */
    public PyFunction getFunction() {
        return function;
    }

    /**
     * Get the packet type being listener for.
     * @return The packet type beign listened for
     */
    public PacketType getPacketType() {
        return packetType;
    }

    /**
     * The listener type of this listener.
     * @return The {@link ListenerType} of this listener
     */
    public ListenerType getListenerType() {
        return listenerType;
    }

    /**
     * A helper method to call a script's packet listener function when the packet event occurs.
     * @param event The event that occurred, will be passed to the script's function
     */
    public void callToScript(PacketEvent event) {
        try {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject parameter = Py.java2py(event);
            function.__call__(threadState, parameter);
        } catch (PyException exception) {
            ScriptManager.get().handleScriptException(script, exception, "Error when calling packet listener");
        }
    }

    /**
     * Prints a representation of this ScriptPacketListener in string format, including the packet type listened to by the listener
     * @return A string representation of the ScriptPacketListener
     */
    @Override
    public String toString() {
        return String.format("ScriptPacketListener[Packet Type: %s]", packetType.toString());
    }
}
