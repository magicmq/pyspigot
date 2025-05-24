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


import dev.magicmq.pyspigot.manager.script.Script;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * A class that represents a script plugin message listener listening on a single channel.
 */
public class ScriptPluginMessageListener implements PluginMessageListener {

    private final Script script;
    private final PyFunction function;
    private final String channel;

    /**
     *
     * @param script The script associated with this ScriptPluginMessageListener
     * @param function The function that should be called when a message is received on the given channel
     * @param channel The channel this listener is listening on
     */
    public ScriptPluginMessageListener(Script script, PyFunction function, String channel) {
        this.script = script;
        this.function = function;
        this.channel = channel;
    }

    /**
     * Get the script associated with this ScriptPluginMessageListener.
     * @return The script associated with this ScriptPluginMessageListener
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the channel this listener is listening on.
     * @return The channel being listened on
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Get the channel this listener is listening on, formatted in compliance with Bukkit's NamespacedKey format, for registering/unregistering purposes.
     * @return The channel being listened on, formatted in NamedspacedKey format
     */
    public String getFormattedChannel() {
        return "PySpigot:" + script.getName() + "_" + channel;
    }

    /**
     * Called internally when a message is received on the registered channel.
     * <p>
     * Note that although the channel is passed as a parameter, only messages received on the registered channel will result in a call to this method.
     * @param channel Channel that the message was sent through
     * @param player Source of the message
     * @param message The raw message that was sent
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (channel.equals(getFormattedChannel())) {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(channel, player, message);
            function.__call__(threadState, parameters[0], parameters[1], parameters[2]);
        }
    }
}
