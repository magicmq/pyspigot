package dev.magicmq.pyspigot.manager.redis;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

/**
 * A wrapper class that wraps the RedisPubSubListener from lettuce for use by scripts.
 * @see io.lettuce.core.pubsub.RedisPubSubListener
 */
public class ScriptPubSubListener implements RedisPubSubListener<String, String> {

    private final PyFunction function;
    private final String channel;

    /**
     *
     * @param function The function that should be called when a message is received on the given channel
     * @param channel The channel to listen on
     */
    public ScriptPubSubListener(PyFunction function, String channel) {
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
            PyObject parameter = Py.java2py(message);
            function.__call__(parameter);
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
     * Implemented from {@link RedisPubSubListener}, but unused.
     */
    public String getChannel() {
        return channel;
    }
}
