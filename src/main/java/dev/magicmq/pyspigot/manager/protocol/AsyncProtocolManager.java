package dev.magicmq.pyspigot.manager.protocol;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.ListenerPriority;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;
import org.python.core.PyBaseCode;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager to interface with ProtocolLib's AsynchronousManager. Primarily used by scripts to register and unregister asynchronous packet listeners.
 * @see AsynchronousManager
 */
public class AsyncProtocolManager {

    private com.comphenix.protocol.AsynchronousManager asynchronousManager;
    private List<ScriptPacketListener> asyncListeners;

    protected AsyncProtocolManager() {
        asynchronousManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        asyncListeners = new ArrayList<>();
    }

    /**
     * Get the current ProtocolLib AsynchronousManager.
     * @return The AsynchronousManager
     * @see AsynchronousManager
     */
    public AsynchronousManager getAsynchronousManager() {
        return asynchronousManager;
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new asynchronous packet listener with default priority.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @return A {@link ScriptPacketListener} representing the asynchronous packet listener that was registered
     */
    public ScriptPacketListener registerAsyncPacketListener(PyFunction function, PacketType type) {
        return registerAsyncPacketListener(function, type, ListenerPriority.NORMAL);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new asynchronous packet listener.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @param priority The priority of the asynchronous packet listener relative to other packet listeners
     * @return A {@link ScriptPacketListener} representing the asynchronous packet listener that was registered
     */
    public ScriptPacketListener registerAsyncPacketListener(PyFunction function, PacketType type, ListenerPriority priority) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        if (getAsyncPacketListener(script, type) == null) {
            ScriptPacketListener listener = null;
            if (type.getSender() == PacketType.Sender.CLIENT) {
                listener = new PacketReceivingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS);
                asyncListeners.add(listener);
                AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                handler.start();
            } else if (type.getSender() == PacketType.Sender.SERVER) {
                listener = new PacketSendingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS);
                asyncListeners.add(listener);
                AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                handler.start();
            }
            return listener;
        } else
            throw new UnsupportedOperationException("Script " + script.getName() + " already has an async packet listener for " + type.name() + " registered");
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new asynchronous timeout packet listener with default priority.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @return A {@link ScriptPacketListener} representing the asynchronous timeout packet listener that was registered
     */
    public ScriptPacketListener registerTimeoutPacketListener(PyFunction function, PacketType type) {
        return registerTimeoutPacketListener(function, type, ListenerPriority.NORMAL);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new asynchronous timeout packet listener.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @param priority The priority of the asynchronous timeout packet listener relative to other asynchronous timeout packet listeners
     * @return A {@link ScriptPacketListener} representing the asynchronous timeout packet listener that was registered
     *
     */
    public ScriptPacketListener registerTimeoutPacketListener(PyFunction function, PacketType type, ListenerPriority priority) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        if (getAsyncPacketListener(script, type) == null) {
            ScriptPacketListener listener = null;
            if (type.getSender() == PacketType.Sender.CLIENT) {
                listener = new PacketReceivingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_TIMEOUT);
                asyncListeners.add(listener);
                asynchronousManager.registerTimeoutHandler(listener);
            } else if (type.getSender() == PacketType.Sender.SERVER) {
                listener = new PacketSendingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS_TIMEOUT);
                asyncListeners.add(listener);
                asynchronousManager.registerTimeoutHandler(listener);
            }
            return listener;
        } else
            throw new UnsupportedOperationException("Script " + script.getName() + " already has an async packet listener for " + type.name() + " registered");
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Unregister an asynchronous packet listener.
     * @param listener The asynchronous packet listener to unregister
     */
    public void unregisterAsyncPacketListener(ScriptPacketListener listener) {
        if (listener.getListenerType() == ListenerType.ASYNCHRONOUS) {
            asynchronousManager.unregisterAsyncHandler(listener);
            asyncListeners.remove(listener);
        } else if (listener.getListenerType() == ListenerType.ASYNCHRONOUS_TIMEOUT) {
            asynchronousManager.unregisterTimeoutHandler(listener);
            asyncListeners.remove(listener);
        }
    }

    /**
     * Unregister all asynchronous packet listeners belonging to a script.
     * @param script The script whose asynchronous packet listeners should be unregistered
     */
    public void unregisterAsyncPacketListeners(Script script) {
        List<ScriptPacketListener> associatedListeners = getAsyncPacketListeners(script);
        for (ScriptPacketListener packetListener : associatedListeners) {
            unregisterAsyncPacketListener(packetListener);
        }
    }

    /**
     * Get all asynchronous packet listeners associated with a script
     * @param script The script to get asynchronous packet listeners from
     * @return A List of {@link ScriptPacketListener} containing all asynchronous packet listeners associated with this script. Will return an empty list if there are no asynchronous packet listeners associated with the script
     */
    public List<ScriptPacketListener> getAsyncPacketListeners(Script script) {
        List<ScriptPacketListener> toReturn = new ArrayList<>();
        for (ScriptPacketListener listener : asyncListeners) {
            if (listener.getScript().equals(script))
                toReturn.add(listener);
        }
        return toReturn;
    }

    /**
     * Get the asynchronous packet listener for a particular packet type associated with a script
     * @param script The script
     * @param packetType The packet type
     * @return The {@link ScriptPacketListener} associated with the script and packet type, null if there is none
     */
    public ScriptPacketListener getAsyncPacketListener(Script script, PacketType packetType) {
        for (ScriptPacketListener listener : asyncListeners) {
            if (listener.getScript().equals(script) && listener.getPacketType().equals(packetType)) {
                return listener;
            }
        }
        return null;
    }
}
