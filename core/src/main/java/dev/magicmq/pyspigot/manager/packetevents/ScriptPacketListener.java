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


import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * A packet listener belonging to a script, which encompasses both sending and receiving packet listeners.
 */
public class ScriptPacketListener implements PacketListener {

    private final Script script;
    private final PyFunction function;
    private final PacketTypeCommon packetType;

    private PacketListenerCommon registeredListener;

    /**
     *
     * @param script The script associated with this packet listener
     * @param function The function to be called when the packet event occurs
     * @param packetType The packet type to listen for
     */
    public ScriptPacketListener(Script script, PyFunction function, PacketTypeCommon packetType) {
        this.script = script;
        this.function = function;
        this.packetType = packetType;
    }

    /**
     * Get the script associated with this packet event.
     * @return The script
     */
    public Script getScript() {
        return script;
    }

    /**
     * get the packet type this listener is listening for
     * @return The packet type
     */
    public PacketTypeCommon getPacketType() {
        return packetType;
    }

    /**
     * Get the registered PacketEvents listener that corresponds to this listener.
     * @return The PacketEvents {@link com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon} registered listener
     */
    public PacketListenerCommon getRegisteredListener() {
        return registeredListener;
    }

    /**
     * Set the registered PacketEvents listener that corresponds to this listener.
     * @param registeredListener The PacketEvents listener to associate with this listener
     */
    public void setRegisteredListener(PacketListenerCommon registeredListener) {
        this.registeredListener = registeredListener;
    }

    protected void callToScript(ProtocolPacketEvent event) {
        if (event.getPacketType().equals(packetType)) {
            try {
                Py.setSystemState(script.getInterpreter().getSystemState());
                ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
                PyObject parameter = Py.java2py(event);
                function.__call__(threadState, parameter);
            } catch (PyException exception) {
                ScriptManager.get().handleScriptException(script, exception, "Error when calling packet events listener");
            }
        }
    }
}
