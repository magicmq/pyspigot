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

package dev.magicmq.pyspigot.bukkit.manager.messaging;


import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.python.core.PyFunction;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with Spigot/Paper's plugin messaging system. Primarily used by scripts to register and unregister plugin message listeners. Can also be used to send plugin messages.
 * @see org.bukkit.plugin.messaging.PluginMessageListener
 */
public class PluginMessageManager {

    private static PluginMessageManager instance;

    private final HashMap<Script, HashMap<String, ScriptPluginMessageListener>> registeredListeners;

    private PluginMessageManager() {
        registeredListeners = new HashMap<>();
    }

    /**
     * Send a plugin message of the specified message type/subchannel on the "BungeeCord" channel with the specified payload.
     * <p>
     * The payload will differ depending on the message type or subchannel and is typically a server name or player name.
     * @param player The player to use when sending the plugin message
     * @param messageType The message type or subchannel, for example, "Connect" or "PlayerCount"
     * @param payload The payload to send with the message
     */
    public void sendBungeeMessage(Player player, String messageType, Object... payload) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(messageType);

        switch (messageType) {
            case "IP":
            case "GetServers":
            case "GetServer":
            case "UUID":
                break;
            case "Connect":
            case "IPOther":
            case "PlayerCount":
            case "PlayerList":
            case "GetPlayerServer":
            case "UUIDOther":
            case "ServerIp":
                if (payload.length < 1)
                    throw new IllegalArgumentException("The message type '" + messageType + "' requires at least 1 argument");
                out.writeUTF(payload[0].toString());
                break;
            case "ConnectOther":
            case "Message":
            case "MessageRaw":
            case "KickPlayer":
            case "KickPlayerRaw":
                if (payload.length < 2)
                    throw new IllegalArgumentException("The message type '" + messageType + "' requires at least 2 arguments");
                out.writeUTF((String) payload[0]);
                out.writeUTF((String) payload[1]);
                break;
            case "Forward":
            case "ForwardToPlayer":
                if (payload.length < 3)
                    throw new IllegalArgumentException("The message type '" + messageType + "' requires at least 3 arguments");
                out.writeUTF((String) payload[0]);
                out.writeUTF((String) payload[1]);
                try {
                    byte[] innerPayload = writeToBytes(((List<Object>) payload[2]).toArray());
                    out.writeShort(innerPayload.length);
                    out.write(innerPayload);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown message type '" + messageType + "'");
        }

        sendRawMessage(player, "BungeeCord", out.toByteArray());
    }

    /**
     * Send a plugin message on the specified channel with the specified payload. Allowed types for the payload include String,
     * Integer, Short, Byte, Boolean, and a byte array.
     * @param player The player to use when sending the plugin message
     * @param channel The channel to send the message on
     * @param payload The payload to send with the message
     */
    public void sendMessage(Player player, String channel, Object... payload) {
        try {
            sendRawMessage(player, channel, writeToBytes(payload));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a plugin message on the specified channel with the specified payload. The payload should be passed as a raw byte
     * array.
     * @param player The player to use when sending the plugin message
     * @param channel The channel to send the message on
     * @param payload The payload to send with the message
     */
    public void sendRawMessage(Player player, String channel, byte[] payload) {
        if (!Bukkit.getServer().getMessenger().isOutgoingChannelRegistered(PySpigot.get(), channel))
            Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(PySpigot.get(), channel);

        player.sendPluginMessage(PySpigot.get(), channel, payload);
    }

    /**
     * Register a new plugin message listener to listen on the given channel.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when a message is received on the channel
     * @param channel The channel to listen on
     * @return The plugin message listener that was registered
     */
    public ScriptPluginMessageListener registerListener(PyFunction function, String channel) {
        Script script = ScriptContext.require();
        ScriptPluginMessageListener listener = getListener(script, channel);
        if (listener == null) {
            listener = new ScriptPluginMessageListener(script, function, channel);
            Bukkit.getServer().getMessenger().registerIncomingPluginChannel(PySpigot.get(), listener.getChannel(), listener);
            addListener(script, listener);
            return listener;
        } else {
            throw new ScriptRuntimeException(script, "Script already has a plugin message listener registered for the channel '" + channel + "'");
        }
    }

    /**
     * Unregister a plugin message listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The listener to unregister
     */
    public void unregisterListener(ScriptPluginMessageListener listener) {
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(PySpigot.get(), listener.getChannel(), listener);
        removeListener(listener.getScript(), listener);
    }

    /**
     * Unregister all plugin message listeners belonging to a script.
     * @param script The script whose plugin message listeners should be unregistered
     */
    public void unregisterListeners(Script script) {
        for (ScriptPluginMessageListener listener : getListeners(script)) {
            Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(PySpigot.get(), listener.getChannel(), listener);
        }
        registeredListeners.remove(script);
    }

    /**
     * Get the plugin message listener for a particular channel associated with a script
     * @param script The script
     * @param channel The channel
     * @return The listener associated with the script and channel, or null if there is none
     */
    public ScriptPluginMessageListener getListener(Script script, String channel) {
        for (ScriptPluginMessageListener listener : getListeners(script)) {
            if (listener.getChannel().equals(channel))
                return listener;
        }
        return null;
    }

    /**
     * Get all plugin message listeners associated with a script.
     * @param script The script to get plugin message listeners from
     * @return An immutable List containing all plugin message listeners associated with the script. Will return an empty list if there are no plugin message listeners associated with the script
     */
    public List<ScriptPluginMessageListener> getListeners(Script script) {
        HashMap<String, ScriptPluginMessageListener> scriptListeners = registeredListeners.get(script);
        return scriptListeners != null ? List.copyOf(scriptListeners.values()) : List.of();
    }

    private void addListener(Script script, ScriptPluginMessageListener listener) {
        if (registeredListeners.containsKey(script))
            registeredListeners.get(script).put(listener.getChannel(), listener);
        else {
            HashMap<String, ScriptPluginMessageListener> scriptListeners = new HashMap<>();
            scriptListeners.put(listener.getChannel(), listener);
            registeredListeners.put(script, scriptListeners);
        }
    }

    private void removeListener(Script script, ScriptPluginMessageListener listener) {
        HashMap<String, ScriptPluginMessageListener> scriptListeners = registeredListeners.get(script);
        scriptListeners.remove(listener.getChannel());
        if (scriptListeners.isEmpty())
            registeredListeners.remove(script);
    }

    private byte[] writeToBytes(Object[] payload) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        for (Object arg : payload) {
            switch (arg) {
                case String s -> dout.writeUTF(s);
                case Integer i -> dout.writeInt(i);
                case Short i -> dout.writeShort(i);
                case Byte b -> dout.writeByte(b);
                case Boolean b -> dout.writeBoolean(b);
                case byte[] bytes -> dout.write(bytes);
                case null -> throw new IllegalArgumentException("Unsupported payload type: null");
                default -> throw new IllegalArgumentException("Unsupported payload type: " + arg.getClass().getName());
            }
        }

        return out.toByteArray();
    }

    /**
     * Get the singleton instance of this PluginMessageManager.
     * @return The instance
     */
    public static PluginMessageManager get() {
        if (instance == null)
            instance = new PluginMessageManager();
        return instance;
    }
}
