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
 * Manager to interface with ProtocolLib's ProtocolManager. Primarily used by scripts to register and unregister packet listeners.
 * <p>
 * Do not call this manager unless ProtocolLib is loaded and enabled on the server! It will not work.
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
     * @return The ProtocolManager
     * @see com.comphenix.protocol.ProtocolManager
     */
    public com.comphenix.protocol.ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new packet listener with default priority.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     */
    public void registerPacketListener(PyFunction function, PacketType type) {
        registerPacketListener(function, type, ListenerPriority.NORMAL);
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Register a new packet listener.
     * <p>
     * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
     * @param function The function that should be called when the packet event occurs
     * @param type The packet type to listen for
     * @param priority The priority of the packet listener relative to other packet listeners
     */
    public void registerPacketListener(PyFunction function, PacketType type, ListenerPriority priority) {
        Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
        if (getListener(script, type) == null) {
            if (type.getSender().toSide().isForClient()) {
                ScriptPacketListener listener = new PacketReceivingListener(script, function, type, priority, ListenerType.NORMAL);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            } else if (type.getSender().toSide().isForServer()) {
                ScriptPacketListener listener = new PacketSendingListener(script, function, type, priority, ListenerType.NORMAL);
                listeners.add(listener);
                protocolManager.addPacketListener(listener);
            }
        } else
            throw new UnsupportedOperationException("Script " + script.getName() + " already has a packet listener for " + type.name() + " registered");
    }

    /**
     * <strong>WARNING: This should be called from scripts only!</strong>
     * <p>
     * Unregister a packet listener.
     * @param function The function associated with the packet listener to unregister
     */
    public void unregisterPacketListener(PyFunction function) {
        ScriptPacketListener listener = getListenerFromFunction(function);
        if (listener != null) {
            deregisterListener(listener);
        }
    }

    /**
     * Unregister all packet listeners belonging to a script, including asynchronous listeners.
     * @param script The script whose packet listeners should be unregistered
     */
    public void unregisterPacketListeners(Script script) {
        for (ScriptPacketListener listener : getListeners(script)) {
            deregisterListener(listener);
        }
        for (ScriptPacketListener listener : asyncProtocolManager.getAsyncListeners(script)) {
            if (listener.getListenerType() == ListenerType.ASYNCHRONOUS)
                asyncProtocolManager.deregisterAsyncListener(listener);
            else if (listener.getListenerType() == ListenerType.ASYNCHRONOUS_TIMEOUT)
                asyncProtocolManager.deregisterTimeoutListener(listener);
        }
    }

    /**
     * Get all packet listeners associated with a script
     * @param script The script to get packet listeners from
     * @return A List of {@link ScriptPacketListener} containing all packet listeners associated with this script. Will return an empty list if there are no packet listeners associated with the script
     */
    public List<ScriptPacketListener> getListeners(Script script) {
        List<ScriptPacketListener> toReturn = new ArrayList<>();
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script))
                toReturn.add(listener);
        }
        return toReturn;
    }

    /**
     * Get the packet listener for a particular packet type associated with a script
     * @param script The script
     * @param packetType The packet type
     * @return The {@link ScriptPacketListener} associated with the script and packet type, null if there is none
     */
    public ScriptPacketListener getListener(Script script, PacketType packetType) {
        for (ScriptPacketListener listener : listeners) {
            if (listener.getScript().equals(script) && listener.getPacketType().equals(packetType)) {
                return listener;
            }
        }
        return null;
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

    /**
     * Get the async protocol manager for working with asynchronous listeners.
     * @return The {@link AsyncProtocolManager}
     */
    public AsyncProtocolManager async() {
        return asyncProtocolManager;
    }

    /**
     * Manager to interface with ProtocolLib's AsynchronousManager. Primarily used by scripts to register and unregister asynchronous packet listeners.
     * @see AsynchronousManager
     */
    public static class AsyncProtocolManager {

        private com.comphenix.protocol.AsynchronousManager asynchronousManager;
        private List<ScriptPacketListener> asyncListeners;


        private AsyncProtocolManager() {
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
         */
        public void registerAsyncListener(PyFunction function, PacketType type) {
            registerAsyncListener(function, type, ListenerPriority.NORMAL);
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
         */
        public void registerAsyncListener(PyFunction function, PacketType type, ListenerPriority priority) {
            Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
            if (getAsyncListener(script, type) == null) {
                if (type.getSender().toSide().isForClient()) {
                    ScriptPacketListener listener = new PacketReceivingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS);
                    asyncListeners.add(listener);
                    AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                    handler.start();
                } else if (type.getSender().toSide().isForServer()) {
                    ScriptPacketListener listener = new PacketSendingListener(script, function, type, priority, ListenerType.ASYNCHRONOUS);
                    asyncListeners.add(listener);
                    AsyncListenerHandler handler = asynchronousManager.registerAsyncHandler(listener);
                    handler.start();
                }
            } else
                throw new UnsupportedOperationException("Script " + script.getName() + " already has an async packet listener for " + type.name() + " registered");
        }

        /**
         * <strong>WARNING: This should be called from scripts only!</strong>
         * <p>
         * Unregister an asynchronous packet listener.
         * @param function The function associated with the asynchronous packet listener to unregister
         */
        public void unregisterAsyncListener(PyFunction function) {
            ScriptPacketListener listener = getAsyncListenerFromFunction(function);
            if (listener != null) {
                deregisterAsyncListener(listener);
            } else
                throw new NullPointerException("There was no async packet listener found associated with this function!");
        }

        /**
         * <strong>WARNING: This should be called from scripts only!</strong>
         * <p>
         * Register a new asynchronous timeout packet listener with default priority.
         * <p>
         * This method will automatically register a {@link PacketReceivingListener} or a {@link PacketSendingListener}, depending on if the {@link PacketType} is for the server or client, respectively.
         * @param function The function that should be called when the packet event occurs
         * @param type The packet type to listen for
         */
        public void registerTimeoutListener(PyFunction function, PacketType type) {
            registerTimeoutListener(function, type, ListenerPriority.NORMAL);
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
         *
         */
        public void registerTimeoutListener(PyFunction function, PacketType type, ListenerPriority priority) {
            Script script = ScriptManager.get().getScript(((PyBaseCode) function.__code__).co_filename);
            if (getAsyncListener(script, type) == null) {
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

        /**
         * <strong>WARNING: This should be called from scripts only!</strong>
         * <p>
         * Unregister an asynchronous timeout packet listener.
         * @param function The function associated with the asynchronous timeout packet listener to unregister
         */
        public void unregisterTimeoutListener(PyFunction function) {
            ScriptPacketListener listener = getAsyncListenerFromFunction(function);
            if (listener != null) {
                deregisterTimeoutListener(listener);
            } else
                throw new NullPointerException("There was no async packet listener found associated with this function!");
        }

        /**
         * Get all asynchronous packet listeners associated with a script
         * @param script The script to get packet listeners from
         * @return A List of {@link ScriptPacketListener} containing all asynchronous packet listeners associated with this script. Will return an empty list if there are no asynchronous packet listeners associated with the script
         */
        public List<ScriptPacketListener> getAsyncListeners(Script script) {
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
        public ScriptPacketListener getAsyncListener(Script script, PacketType packetType) {
            for (ScriptPacketListener listener : asyncListeners) {
                if (listener.getScript().equals(script) && listener.getPacketType().equals(packetType)) {
                    return listener;
                }
            }
            return null;
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
