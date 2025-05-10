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

import dev.magicmq.pyspigot.manager.redis.client.RedisCommandClient;
import dev.magicmq.pyspigot.manager.redis.client.RedisPubSubClient;
import dev.magicmq.pyspigot.manager.redis.client.ScriptRedisClient;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisURI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Manager to interface with remote redis servers. Used by scripts to subscribe to pub/sub messaging and publish messages.
 */
public class RedisManager {

    private static RedisManager instance;

    private final HashMap<Script, List<ScriptRedisClient>> activeClients;

    private RedisManager() {
        activeClients = new HashMap<>();
    }

    /**
     * Get a new RedisURI builder for use when opening a new script redis client.
     * @return A {@link io.lettuce.core.RedisURI.Builder} object used to build a URI with connection information
     */
    public RedisURI.Builder newRedisURI() {
        return RedisURI.builder();
    }

    /**
     * Get a new client options builder for use when opening a new script redis client.
     * @return A {@link io.lettuce.core.ClientOptions.Builder} object used to build ClientOptions for the RedisClient
     */
    public ClientOptions.Builder newClientOptions() {
        return ClientOptions.builder();
    }

    /**
     * Initialize a new {@link RedisPubSubClient} with a connection to a remote redis server with the specified ip, port, and password, using the default client options. The connection to the remote redis server will be opened automatically when the client is created.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param clientType The type of redis client to open, such as pub/sub or command
     * @param ip The IP of the redis server to connect to
     * @param port The port of the redis server to connect to
     * @param password The password for the redis server
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(ClientType clientType, String ip, String port, String password) {
        ClientOptions options = ClientOptions.builder()
                .autoReconnect(true)
                .pingBeforeActivateConnection(true)
                .build();
        return openRedisClient(clientType, ip, port, password, options);
    }

    /**
     * Initialize a new {@link RedisPubSubClient} with a connection to a remote redis server with the specified ip, port, and password, using the specified {@link io.lettuce.core.ClientOptions}. The connection to the remote redis server will be opened automatically when the client is created.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param clientType The type of redis client to open, such as pub/sub or command
     * @param ip The IP of the redis server to connect to
     * @param port The port of the redis server to connect to
     * @param password The password for the redis server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the {@link io.lettuce.core.RedisClient}
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(ClientType clientType, String ip, String port, String password, ClientOptions clientOptions) {
        RedisURI redisURI;
        if (password != null)
            redisURI = RedisURI.Builder
                    .redis(ip, Integer.parseInt(port))
                    .withPassword(password.toCharArray())
                    .build();
        else
            redisURI = RedisURI.Builder
                    .redis(ip, Integer.parseInt(port))
                    .build();

        return openRedisClient(clientType, redisURI, clientOptions);
    }

    /**
     * Initialize a new {@link RedisPubSubClient} with a connection to a remote redis server with the specified {@link io.lettuce.core.RedisURI}. The connection to the remote redis server will be opened automatically when the client is created.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param clientType The type of redis client to open, such as pub/sub or command
     * @param redisURI The URI specifying the connection details to the redis server
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(ClientType clientType, RedisURI redisURI) {
        ClientOptions options = ClientOptions.builder()
                .autoReconnect(true)
                .pingBeforeActivateConnection(true)
                .build();
        return openRedisClient(clientType, redisURI, options);
    }

    /**
     * Initialize a new {@link RedisPubSubClient} with a connection to a remote redis server with the specified {@link io.lettuce.core.RedisURI} and {@link io.lettuce.core.ClientOptions}. The connection to the remote redis server will be opened automatically when the client is created.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param clientType The type of redis client to open, such as pub/sub or command
     * @param redisURI The URI specifying the connection details to the redis server
     * @param clientOptions The ClientOptions that should be used for the {@link io.lettuce.core.RedisClient}
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(ClientType clientType, RedisURI redisURI, ClientOptions clientOptions) {
        Script script = ScriptUtils.getScriptFromCallStack();

        ScriptRedisClient client;
        if (clientType == ClientType.COMMAND)
            client = new RedisCommandClient(script, redisURI, clientOptions);
        else if (clientType == ClientType.PUB_SUB)
            client = new RedisPubSubClient(script, redisURI, clientOptions);
        else /*if (clientType == ClientType.BASIC)*/
            client = new ScriptRedisClient(script, redisURI, clientOptions);

        try {
            client.open();
            addClient(client);
            return client;
        } catch (RedisConnectionException e) {
            client.close();
            throw e;
        }
    }

    /**
     * Close the specified ScriptRedisClient synchronously. This method will block until the redis client is closed.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param client The client to close
     */
    public void closeRedisClient(ScriptRedisClient client) {
        removeClient(client);
        client.close();
    }

    /**
     * Close the specified ScriptRedisClient asynchronously, without blocking.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param client The client to close
     * @return A {@link CompletableFuture} that completes when closing the client is finished
     */
    public CompletableFuture<Void> closeRedisClientAsync(ScriptRedisClient client) {
        removeClient(client);
        return client.closeAsync();
    }

    /**
     * Close all ScriptRedisClients belonging to a script.
     * @param script The script whose ScriptRedisClients should be closed
     * @param async Whether the client should be closed asynchronously
     */
    public void closeRedisClients(Script script, boolean async) {
        List<ScriptRedisClient> scriptClients = activeClients.get(script);
        if (scriptClients != null) {
            for (ScriptRedisClient client : scriptClients) {
                if (async)
                    client.closeAsync();
                else
                    client.close();
            }
            activeClients.remove(script);
        }
    }

    /**
     * Get all open ScriptRedisClients belonging to a script.
     * @param script The script to get ScriptRedisClients from
     * @return An immutable list of {@link ScriptRedisClient} containing all clients associated with the script. Will return an empty list if there are no clients associated with the script
     */
    public List<ScriptRedisClient> getRedisClients(Script script) {
        List<ScriptRedisClient> scriptClients = activeClients.get(script);
        return scriptClients != null ? List.copyOf(scriptClients) : List.of();
    }

    /**
     * Get all ScriptRedisClients belonging to a script of the given client type.
     * @param script The script to get ScriptRedisClients from
     * @param type The type of redis client to filter by
     * @return An immutable List of {@link ScriptRedisClient} containing all ScriptRedisClients of the given type associated with the script. Will return an empty list if there are no clients of the given type associated with the script
     */
    public List<ScriptRedisClient> getRedisClients(Script script, ClientType type) {
        return getRedisClients(script)
                .stream()
                .filter(connection -> connection.getClass() == type.getClientClass())
                .toList();
    }

    private void addClient(ScriptRedisClient client) {
        Script script = client.getScript();
        if (activeClients.containsKey(script))
            activeClients.get(script).add(client);
        else {
            List<ScriptRedisClient> scriptClients = new ArrayList<>();
            scriptClients.add(client);
            activeClients.put(script, scriptClients);
        }
    }

    private void removeClient(ScriptRedisClient client) {
        Script script = client.getScript();
        List<ScriptRedisClient> scriptClients = activeClients.get(script);
        scriptClients.remove(client);
        if (scriptClients.isEmpty())
            activeClients.remove(script);
    }

    /**
     * Get the singleton instance of this RedisManager.
     * @return The instance
     */
    public static RedisManager get() {
        if (instance == null)
            instance = new RedisManager();
        return instance;
    }
}
