package dev.magicmq.pyspigot.manager.redis;

import dev.magicmq.pyspigot.manager.script.Script;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A wrapper class that wraps the RedisClient from lettuce for use by scripts.
 * @see io.lettuce.core.RedisClient
 */
public class ScriptRedisClient {

    private final Script script;
    private final String uri;
    private final ClientOptions clientOptions;
    private final List<ScriptPubSubListener> syncListeners;
    private final List<ScriptPubSubListener> asyncListeners;

    private RedisClient client;
    private StatefulRedisPubSubConnection<String, String> connection;

    /**
     *
     * @param script The script to which this ScriptRedisClient belongs
     * @param uri The uri that specifies the ip, port, and password for connection to the remote redis server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the RedisClient
     */
    public ScriptRedisClient(Script script, String uri, ClientOptions clientOptions) {
        this.script = script;
        this.uri = uri;

        this.clientOptions = clientOptions;
        this.syncListeners = new ArrayList<>();
        this.asyncListeners = new ArrayList<>();
    }

    /**
     * Initialize a new {@link io.lettuce.core.RedisClient} and open a connection to the remote redis server.
     * @return True if the connection was successfully opened, false if otherwise
     */
    public boolean open() {
        client = RedisClient.create(uri);
        if (clientOptions != null)
            client.setOptions(clientOptions);
        else
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

    /**
     * Close the open connection to the remote redis server.
     * @return True if the connection was successfully closed, false if otherwise
     */
    public boolean close() {
        connection.close();
        client.shutdown();
        return !connection.isOpen();
    }

    /**
     * Register a new synchronous listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @see ScriptRedisClient#registerSyncListener(PyFunction, String)
     * @param function The function that should be called when a message on the specified channel is received
     * @param channel The channel to listen on
     * @return A {@link ScriptPubSubListener} representing the listener that was registered
     */
    public ScriptPubSubListener registerListener(PyFunction function, String channel) {
        return registerSyncListener(function, channel);
    }

    /**
     * Register a new synchronous listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when a message on the specified channel is received
     * @param channel The channel to listen on
     * @return A {@link ScriptPubSubListener} representing the listener that was registered
     */
    public ScriptPubSubListener registerSyncListener(PyFunction function, String channel) {
        ScriptPubSubListener listener = new ScriptPubSubListener(function, channel);
        connection.addListener(listener);
        connection.sync().subscribe(channel);

        syncListeners.add(listener);

        return listener;
    }

    /**
     * Register a new asynchronous listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param function The function that should be called when a message on the specified channel is received
     * @param channel The channel to listen on
     * @return A {@link ScriptPubSubListener} representing the listener that was registered
     */
    public ScriptPubSubListener registerAsyncListener(PyFunction function, String channel) {
        ScriptPubSubListener listener = new ScriptPubSubListener(function, channel);
        connection.addListener(listener);
        connection.async().subscribe(channel);

        asyncListeners.add(listener);

        return listener;
    }

    /**
     * Unregister the specified listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param listener The listener to unregister
     */
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

    /**
     * Unregister all listeners (both synchronous and asynchronous) on the given channel
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param channel The channel on which all listeners should be unregistered
     */
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

    /**
     * Synchronously publish a message to the given channel
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param channel The channel on which the message should be published
     * @param message The message to publish
     */
    public void publishSync(String channel, String message) {
        connection.sync().publish(channel, message);
    }

    /**
     * Asynchronously publish a message to the given channel
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param channel The channel on which the message should be published
     * @param message The message to publish
     */
    public void publishAsync(String channel, String message) {
        connection.async().publish(channel, message);
    }

    /**
     * Get the script associated with this ScriptRedisClient.
     * @return The script associated with this ScriptRedisClient.
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the underlying lettuce {@link io.lettuce.core.RedisClient} for this ScriptRedisClient.
     * @return The RedisClient associated with this ScriptRedisClient
     */
    public RedisClient getRedisClient() {
        return client;
    }

    /**
     * Prints a representation of this ScriptRedisClient in string format, including listeners
     * @return A string representation of the ScriptPubSubListener
     */
    @Override
    public String toString() {
        return String.format("ScriptRedisClient[Connection: %s, Sync Listeners: %s, Async Listeners: %s]", connection.toString(), syncListeners, asyncListeners);
    }

    /**
     * Get the underlying lettuce {@link io.lettuce.core.pubsub.StatefulRedisPubSubConnection} for this ScriptRedisClient.
     * @return The StatefulRedisPubSubConnection associated with this ScriptRedisClient
     */
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
