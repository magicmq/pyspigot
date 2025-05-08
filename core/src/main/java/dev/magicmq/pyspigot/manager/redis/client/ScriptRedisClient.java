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

import dev.magicmq.pyspigot.PyCore;
import dev.magicmq.pyspigot.manager.script.Script;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import java.util.concurrent.CompletableFuture;

/**
 * A wrapper class that wraps the RedisClient from lettuce for use by scripts.
 * @see io.lettuce.core.RedisClient
 */
public class ScriptRedisClient {

    private static int clientIdIncrement;

    private final Script script;
    private final int clientId;
    private final RedisURI redisURI;
    private final ClientOptions clientOptions;

    protected RedisClient client;

    /**
     *
     * @param script The script to which this ScriptRedisClient belongs
     * @param redisURI The URI that specifies the connection details to the server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the RedisClient
     */
    public ScriptRedisClient(Script script, RedisURI redisURI, ClientOptions clientOptions) {
        this.script = script;
        this.clientId = clientIdIncrement++;
        this.redisURI = redisURI;
        this.clientOptions = clientOptions;
    }


    /**
     * Initialize a new {@link io.lettuce.core.RedisClient} and open a connection to the remote redis server.
     */
    public void open() {
        client = RedisClient.create(redisURI);
        client.setOptions(clientOptions);
        client.getResources().eventBus().get().subscribe(event -> {
            if (PyCore.get().getConfig().doVerboseRedisLogging() || event.getClass().getSimpleName().equals("ReconnectAttemptEvent") || event.getClass().getSimpleName().equals("ReconnectFailedEvent")) {
                String logMessage = "Event captured on redis event bus for client #" + clientId + ": " + event.getClass().getSimpleName();
                script.getLogger().info(logMessage);
            }
        });
    }

    /**
     * Close the open to the remote redis server synchronously, blocking if necessary.
     */
    public void close() {
        client.shutdown();
    }

    /**
     * Close the open connection to the remote redis server asynchronously.
     * @return A {@link CompletableFuture} that completes when the shutdown is finished
     */
    public CompletableFuture<Void> closeAsync() {
        return client.shutdownAsync();
    }

    /**
     * Get the script associated with this redis client.
     * @return The script associated with this redis client.
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the ID of this redis client.
     * @return The ID
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Get the {@link io.lettuce.core.RedisURI} of this redis client.
     * @return The RedisURI
     */
    public RedisURI getRedisURI() {
        return redisURI;
    }

    /**
     * Get the {@link io.lettuce.core.ClientOptions} of this redis client.
     * @return The ClientOptions
     */
    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Get the underlying lettuce {@link io.lettuce.core.RedisClient} for this ScriptRedisClient.
     * @return The RedisClient associated with this ScriptRedisClient
     */
    public RedisClient getRedisClient() {
        return client;
    }

    /**
     * Prints a representation of this ScriptRedisClient in string format
     * @return A string representation of the ScriptRedisClient
     */
    @Override
    public String toString() {
        return String.format("ScriptRedisClient[ID: %d]", clientId);
    }
}
