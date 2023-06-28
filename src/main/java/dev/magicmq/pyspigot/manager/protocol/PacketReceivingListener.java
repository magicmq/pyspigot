package dev.magicmq.pyspigot.manager.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import dev.magicmq.pyspigot.manager.script.Script;
import org.python.core.PyFunction;

public class PacketReceivingListener extends ScriptPacketListener {

    public PacketReceivingListener(Script script, PyFunction function, PacketType type, ListenerPriority listenerPriority, ListenerType listenerType) {
        super(script, function, type, listenerPriority, listenerType);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        super.callToScript(event);
    }
}
