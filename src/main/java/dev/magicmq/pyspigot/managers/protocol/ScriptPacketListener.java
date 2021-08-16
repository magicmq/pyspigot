package dev.magicmq.pyspigot.managers.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.magicmq.pyspigot.PySpigot;
import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
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
            ScriptManager.get().handleScriptException(script, e, "Error when calling packet listener belonging to script");
        }
    }
}
