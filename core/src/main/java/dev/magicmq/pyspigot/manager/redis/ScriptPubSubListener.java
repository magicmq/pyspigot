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

package dev.magicmq.pyspigot.manager.redis;

import dev.magicmq.pyspigot.manager.script.Script;
import io.lettuce.core.pubsub.RedisPubSubListener;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.ThreadState;

/**
 * A wrapper class that wraps the RedisPubSubListener from lettuce for use by scripts.
 * @see io.lettuce.core.pubsub.RedisPubSubListener
 */
public class ScriptPubSubListener implements RedisPubSubListener<String, String> {

    private final Script script;
    private final PyFunction function;
    private final String channel;

    /**
     *
     * @param script The script that this PubSubListener belongs to
     * @param function The function that should be called when a message is received on the given channel
     * @param channel The channel to listen on
     */
    public ScriptPubSubListener(Script script, PyFunction function, String channel) {
        this.script = script;
        this.function = function;
        this.channel = channel;
    }

    /**
     * Called internally when a message is received on the given channel.
     * @param channel The channel on which the message was received
     * @param message The message that was received
     */
    @Override
    public void message(String channel, String message) {
        if (channel.equals(this.channel)) {
            Py.setSystemState(script.getInterpreter().getSystemState());
            ThreadState threadState = Py.getThreadState(script.getInterpreter().getSystemState());
            PyObject[] parameters = Py.javas2pys(channel, message);
            function.__call__(threadState, parameters[0], parameters[1]);
        }
    }

    /**
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    @Override
    public void message(String s, String k1, String s2) {}

    /**
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    @Override
    public void subscribed(String s, long l) {}

    /**
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    @Override
    public void psubscribed(String s, long l) {}

    /**
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    @Override
    public void unsubscribed(String s, long l) {}

    /**
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    @Override
    public void punsubscribed(String s, long l) {}

    /**
     * Get the Script that this listener belongs to.
     * @return The script
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the channel for this listener.
     * @return The channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Prints a representation of this ScriptPubSubListener in string format, including the channel being listened to.
     * @return A string representation of the ScriptPubSubListener
     */
    @Override
    public String toString() {
        return String.format("ScriptPubSubListener[Channel: %s]", channel);
    }
}
