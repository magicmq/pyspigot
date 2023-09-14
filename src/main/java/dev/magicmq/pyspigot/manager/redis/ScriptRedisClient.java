package dev.magicmq.pyspigot.manager.redis;

import dev.magicmq.pyspigot.manager.script.Script;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScriptRedisClient {

    private final Script script;
    private final String uri;

    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;
    private List<ScriptPubSubListener> syncListeners;
    private List<ScriptPubSubListener> asyncListeners;

    public ScriptRedisClient(Script script, String uri) {
        this.script = script;
        this.uri = uri;

        this.syncListeners = new ArrayList<>();
        this.asyncListeners = new ArrayList<>();
    }

    public boolean open() {
        client = RedisClient.create(uri);
        client.setOptions(ClientOptions.builder()
                .autoReconnect(true)
                .pingBeforeActivateConnection(true)
                .build());
        client.getResources().eventBus().get().subscribe(event -> {
            //TODO Log errors
        });
        connection = client.connectPubSub();
        return connection.isOpen();
    }

    public boolean close() {
        connection.close();
        client.shutdown();
        return !connection.isOpen();
    }

    public ScriptPubSubListener registerListener(PyFunction function, String channel) {
        return registerSyncListener(function, channel);
    }

    public ScriptPubSubListener registerSyncListener(PyFunction function, String channel) {
        ScriptPubSubListener listener = new ScriptPubSubListener(function, channel);
        connection.addListener(listener);
        connection.sync().subscribe(channel);

        syncListeners.add(listener);

        return listener;
    }

    public ScriptPubSubListener registerAsyncListener(PyFunction function, String channel) {
        ScriptPubSubListener listener = new ScriptPubSubListener(function, channel);
        connection.addListener(listener);
        connection.async().subscribe(channel);

        asyncListeners.add(listener);

        return listener;
    }

    public void unregisterListener(ScriptPubSubListener listener) {
        connection.removeListener(listener);
        if (syncListeners.contains(listener)) {
            syncListeners.remove(listener);
            if (!stillListening(listener.getChannel(), true)) {
                connection.sync().unsubscribe(listener.getChannel());
            }
        } else if (asyncListeners.contains(listener)) {
            asyncListeners.remove(listener);
            if (!stillListening(listener.getChannel(), false)) {
                connection.async().unsubscribe(listener.getChannel());
            }
        }
    }

    public void unregisterListeners(String channel) {
        for (Iterator<ScriptPubSubListener> iterator = syncListeners.iterator(); iterator.hasNext();) {
            ScriptPubSubListener listener = iterator.next();
            if (listener.getChannel().equals(channel)) {
                connection.removeListener(listener);
                iterator.remove();
            }
        }
        connection.sync().unsubscribe(channel);

        for (Iterator<ScriptPubSubListener> iterator = asyncListeners.iterator(); iterator.hasNext();) {
            ScriptPubSubListener listener = iterator.next();
            if (listener.getChannel().equals(channel)) {
                connection.removeListener(listener);
                iterator.remove();
            }
        }
        connection.async().unsubscribe(channel);
    }

    public void publishSync(String channel, String message) {
        connection.sync().publish(channel, message);
    }

    public void publishAsync(String channel, String message) {
        connection.async().publish(channel, message);
    }

    public Script getScript() {
        return script;
    }

    public RedisClient getClient() {
        return client;
    }

    public StatefulRedisPubSubConnection<String, String> getConnection() {
        return connection;
    }

    private boolean stillListening(String channel, boolean sync) {
        for (ScriptPubSubListener listener : sync ? syncListeners : asyncListeners) {
            if (listener.getChannel().equals(channel))
                return true;
        }
        return false;
    }
}
