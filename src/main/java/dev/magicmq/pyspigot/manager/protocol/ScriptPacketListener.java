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
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.PyException;
import org.python.core.PyFunction;

public abstract class ScriptPacketListener extends PacketAdapter {

    private final Script script;
    private final PyFunction function;
    private final PacketType packetType;
    private final ListenerType listenerType;

    public ScriptPacketListener(Script script, PyFunction function, PacketType packetType, ListenerPriority listenerPriority, ListenerType listenerType) {
        super(PySpigot.get(), listenerPriority, packetType);
        this.script = script;
        this.function = function;
        this.packetType = packetType;
        this.listenerType = listenerType;
    }

    public Script getScript() {
        return script;
    }

    public PyFunction getFunction() {
        return function;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public ListenerType getListenerType() {
        return listenerType;
    }

    public void callToScript(PacketEvent event) {
        try {
            function._jcall(new Object[]{event});
        } catch (PyException e) {
            ScriptManager.get().handleScriptException(script, e, "Error when calling packet listener");
        }
    }
}
