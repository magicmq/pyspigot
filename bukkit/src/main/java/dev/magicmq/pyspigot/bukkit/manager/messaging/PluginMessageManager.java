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


import dev.magicmq.pyspigot.bukkit.PySpigot;
import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.bukkit.Bukkit;
import org.python.core.PyFunction;

import java.util.HashMap;
import java.util.List;

/**
 * Manager to interface with Spigot/Paper's plugin messaging system. Primarily used by scripts to register and unregister plugin message listeners.
 * @see org.bukkit.plugin.messaging.PluginMessageListener
 */
public class PluginMessageManager {

    private static PluginMessageManager instance;

    private final HashMap<Script, HashMap<String, ScriptPluginMessageListener>> registeredListeners;

    private PluginMessageManager() {
        registeredListeners = new HashMap<>();
    }

    /**
     * Register a new plugin message listener to listen on the given channel.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when a message is received on the channel
     * @param channel The channel to listen on
     */
    public void registerListener(PyFunction function, String channel) {
        Script script = ScriptUtils.getScriptFromCallStack();
        ScriptPluginMessageListener listener = getListener(script, channel);
        if (listener == null) {
            listener = new ScriptPluginMessageListener(script, function, channel);
            Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(PySpigot.get(), listener.getFormattedChannel(), listener);
            addListener(script, listener);
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
        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(PySpigot.get(), listener.getFormattedChannel(), listener);
        removeListener(listener.getScript(), listener);
    }

    /**
     * Unregister all plugin message listeners belonging to a script.
     * @param script The script whose plugin message listeners should be unregistered
     */
    public void unregisterListeners(Script script) {
        for (ScriptPluginMessageListener listener : getListeners(script)) {
            Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(PySpigot.get(), listener.getFormattedChannel(), listener);
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
