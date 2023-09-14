package dev.magicmq.pyspigot.manager.redis;

import io.lettuce.core.pubsub.RedisPubSubListener;
import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

public class ScriptPubSubListener implements RedisPubSubListener<String, String> {

    private final PyFunction function;
    private final String channel;

    public ScriptPubSubListener(PyFunction function, String channel) {
        this.function = function;
        this.channel = channel;
    }

    @Override
    public void message(String channel, String message) {
        if (channel.equals(this.channel)) {
            PyObject parameter = Py.java2py(message);
            function.__call__(parameter);
        }
    }

    @Override
    public void message(String s, String k1, String s2) {}

    @Override
    public void subscribed(String s, long l) {}

    @Override
    public void psubscribed(String s, long l) {}

    @Override
    public void unsubscribed(String s, long l) {}

    @Override
    public void punsubscribed(String s, long l) {}

    public String getChannel() {
        return channel;
    }
}
