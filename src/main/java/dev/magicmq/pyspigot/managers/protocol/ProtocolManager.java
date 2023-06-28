package dev.magicmq.pyspigot.managers.protocol;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerPriority;
import dev.magicmq.pyspigot.managers.script.Script;
import dev.magicmq.pyspigot.managers.script.ScriptManager;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

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

    public com.comphenix.protocol.ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public void registerPacketListener(PyFunction function, PacketType type) {
        registerPacketListener(function, type, ListenerPriority.NORMAL);
    }

    public void registerPacketListener(PyFunction function, PacketType type, ListenerPriority priority) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        if (!doesScriptHaveListener(script, type)) {
            if (type.getSender().toSide().isForClient()) {
                ScriptPacketListener listener = new PacketReceivingListener(script, function, type, priority, ListenerType.SYNCHRONOUS);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            } else if (type.getSender().toSide().isForServer()) {
                ScriptPacketListener listener = new PacketSendingListener(script, function, type, priority, ListenerType.SYNCHRONOUS);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            }
        }
    }

    public void unregisterPacketListener(PyFunction function) {
        ScriptPacketListener listener = getListenerFromFunction(function);
        if (listener != null) {
            deregisterListener(listener);
        }
    }

    public AsyncProtocolManager async() {
        return asyncProtocolManager;
    }

    public void stopScript(Script script) {
        for (ScriptPacketListener listener : getListeners(script)) {
            deregisterListener(listener);
        }
        for (ScriptPacketListener listener : asyncProtocolManager.getListeners(script)) {
            if (listener.getListenerType() == ListenerType.ASYNCHRONOUS_LISTENER)
                asyncProtocolManager.deregisterAsyncListener(listener);
            else if (listener.getListenerType() == ListenerType.ASYNCHRONOUS_TIMEOUT)
                asyncProtocolManager.deregisterTimeoutListener(listener);
        }
    }

    private List<ScriptPacketListener> getListeners(Script script) {
        List<ScriptPacketListener> toReturn = new ArrayList<>();
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script))
                toReturn.add(listener);
        }
        return toReturn;
    }

    private boolean doesScriptHaveListener(Script script, PacketType packetType) {
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script)) {
                if (listener.getPacketType().equals(packetType))
                    return true;
            }
        }
        return false;
    }

    private ScriptPacketListener getListenerFromFunction(PyFunction function) {
        for (ScriptPacketListener listener : listeners) {
            if (listener.getFunction().equals(function))
                return listener;
        }
        return null;
    }

    private void deregisterListener(ScriptPacketListener listener) {
        protocolManager.removePacketListener(listener);
        listeners.remove(listener);
    }

    public static ProtocolManager get() {
        if (instance == null)
            instance = new ProtocolManager();
        return instance;
    }

    public static class AsyncProtocolManager {

        private com.comphenix.protocol.AsynchronousManager asynchronousManager;
        private List<ScriptPacketListener> asyncListeners;


        private AsyncProtocolManager() {
            asynchronousManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
            asyncListeners = new ArrayList<>();
        }

        public AsynchronousManager getAsynchronousManager() {
            return asynchronousManager;
        }

        public void registerAsyncListener(PyFunction function, PacketType type) {
            registerAsyncListener(function, type, ListenerPriority.NORMAL);
        }

        public void registerAsyncListener(PyFunction function, PacketType type, ListenerPriority priority) {
            Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
            if (!doesScriptHaveAsyncListener(script, type)) {
                if (type.getSender().toSide().isForClient()) {
                    ScriptPacketListener listener = new PacketReceivingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_LISTENER);
                    asyncListeners.add(listener);
                    AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                    handler.start();
                } else if (type.getSender().toSide().isForServer()) {
                    ScriptPacketListener listener = new PacketSendingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_LISTENER);
                    asyncListeners.add(listener);
                    AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                    handler.start();
                }
            } else
                throw new UnsupportedOperationException("Script " + script.getName() + " already has an async packet listener for " + type.name() + " registered");
        }

        public void unregisterAsyncListener(PyFunction function) {
            ScriptPacketListener listener = getAsyncListenerFromFunction(function);
            if (listener != null) {
                deregisterAsyncListener(listener);
            } else
                throw new NullPointerException("There was no async packet listener found associated with this function!");
        }

        public void registerTimeoutListener(PyFunction function, PacketType type) {
            registerTimeoutListener(function, type, ListenerPriority.NORMAL);
        }

        public void registerTimeoutListener(PyFunction function, PacketType type, ListenerPriority priority) {
            Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
            if (!doesScriptHaveAsyncListener(script, type)) {
                if (type.getSender().toSide().isForClient()) {
                    ScriptPacketListener listener = new PacketReceivingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_TIMEOUT);
                    asyncListeners.add(listener);
                    asynchronousManager.registerTimeoutHandler(listener);
                } else if (type.getSender().toSide().isForServer()) {
                    ScriptPacketListener listener = new PacketSendingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_TIMEOUT);
                    asyncListeners.add(listener);
                    asynchronousManager.registerTimeoutHandler(listener);
                }
            } else
                throw new UnsupportedOperationException("Script " + script.getName() + " already has an async packet listener for " + type.name() + " registered");
        }

        public void unregisterTimeoutListener(PyFunction function) {
            ScriptPacketListener listener = getAsyncListenerFromFunction(function);
            if (listener != null) {
                deregisterTimeoutListener(listener);
            } else
                throw new NullPointerException("There was no async packet listener found associated with this function!");
        }

        private List<ScriptPacketListener> getListeners(Script script) {
            List<ScriptPacketListener> toReturn = new ArrayList<>();
            for (ScriptPacketListener listener : asyncListeners) {
                if (listener.getScript().equals(script))
                    toReturn.add(listener);
            }
            return toReturn;
        }

        private boolean doesScriptHaveAsyncListener(Script script, PacketType packetType) {
            for (ScriptPacketListener listener : asyncListeners) {
                if (listener.getScript().equals(script)) {
                    if (listener.getPacketType().equals(packetType))
                        return true;
                }
            }
            return false;
        }

        private ScriptPacketListener getAsyncListenerFromFunction(PyFunction function) {
            for (ScriptPacketListener listener : asyncListeners) {
                if (listener.getFunction().equals(function))
                    return listener;
            }
            return null;
        }

        private void deregisterAsyncListener(ScriptPacketListener listener) {
            asynchronousManager.unregisterAsyncHandler(listener);
            asyncListeners.remove(listener);
        }

        private void deregisterTimeoutListener(ScriptPacketListener listener) {
            asynchronousManager.unregisterTimeoutHandler(listener);
            asyncListeners.remove(listener);
        }
    }
}
