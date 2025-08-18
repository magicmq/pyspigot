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

package dev.magicmq.pyspigot.manager.redis.client;

import dev.magicmq.pyspigot.manager.redis.ScriptPubSubListener;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.python.core.PyFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Extension of the {@link ScriptRedisClient} that provides pub/sub messaging capabilities.
 * @see io.lettuce.core.pubsub.StatefulRedisPubSubConnection
 */
public class RedisPubSubClient extends ScriptRedisClient {

    private final List<ScriptPubSubListener> syncListeners;
    private final List<ScriptPubSubListener> asyncListeners;

    private StatefulRedisPubSubConnection<String, String> connection;

    /**
     *
     * @param script The script to which this RedisPubSubClient belongs
     * @param redisURI The URI that specifies the connection details to the server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the RedisClient
     */
    public RedisPubSubClient(Script script, RedisURI redisURI, ClientOptions clientOptions) {
        super(script, redisURI, clientOptions);
        this.syncListeners = new ArrayList<>();
        this.asyncListeners = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        super.open();
        connection = client.connectPubSub();
    }

    /**
     * Get the underlying connection for this RedisPubSubClient.
     * @return The connection associated with this RedisPubSubClient
     */
    public StatefulRedisPubSubConnection<String, String> getConnection() {
        return connection;
    }

    /**
     * Register a new synchronous listener.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @see RedisPubSubClient#registerSyncListener(PyFunction, String)
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
        Script script = ScriptUtils.getScriptFromCallStack();
        ScriptPubSubListener listener = new ScriptPubSubListener(script, function, channel);
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
        Script script = ScriptUtils.getScriptFromCallStack();
        ScriptPubSubListener listener = new ScriptPubSubListener(script, function, channel);
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
            if (!isListening(listener.getChannel(), true)) {
                connection.sync().unsubscribe(listener.getChannel());
            }
        } else if (asyncListeners.contains(listener)) {
            asyncListeners.remove(listener);
            if (!isListening(listener.getChannel(), false)) {
                connection.async().unsubscribe(listener.getChannel());
            }
        }
    }

    /**
     * Unregister a listener via the function associated with it.
     * @param function The function associated with the listener to unregister
     */
    public void unregisterListener(PyFunction function) {
        List<ScriptPubSubListener> syncListeners = List.copyOf(this.syncListeners);
        for (ScriptPubSubListener listener : syncListeners) {
            if (listener.getFunction().equals(function))
                unregisterListener(listener);
        }

        List<ScriptPubSubListener> asyncListeners = List.copyOf(this.asyncListeners);
        for (ScriptPubSubListener listener : asyncListeners) {
            if (listener.getFunction().equals(function))
                unregisterListener(listener);
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
     * @return The number of clients that received the message
     */
    public Long publish(String channel, String message) {
        return publishSync(channel, message);
    }

    /**
     * Synchronously publish a message to the given channel
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param channel The channel on which the message should be published
     * @param message The message to publish
     * @return The number of clients that received the message
     */
    public Long publishSync(String channel, String message) {
        return connection.sync().publish(channel, message);
    }

    /**
     * Asynchronously publish a message to the given channel
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param channel The channel on which the message should be published
     * @param message The message to publish
     */
    public RedisFuture<Long> publishAsync(String channel, String message) {
        return connection.async().publish(channel, message);
    }

    /**
     * Prints a representation of this RedisPubSubClient in string format, including listeners
     * @return A string representation of the RedisPubSubClient
     */
    @Override
    public String toString() {
        return String.format("RedisPubSubClient[ID: %d, Connection: %s, Sync Listeners: %s, Async Listeners: %s]", getClientId(), connection.toString(), syncListeners, asyncListeners);
    }

    private boolean isListening(String channel, boolean sync) {
        for (ScriptPubSubListener listener : sync ? syncListeners : asyncListeners) {
            if (listener.getChannel().equals(channel))
                return true;
        }
        return false;
    }
}
