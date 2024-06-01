/*
 *    Copyright 2023 magicmq
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

import dev.magicmq.pyspigot.manager.script.Script;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * Extension of the {@link ScriptRedisClient} that provides ability to issue commands.
 * @see io.lettuce.core.api.StatefulRedisConnection
 */
public class RedisCommandClient extends ScriptRedisClient {

    private StatefulRedisConnection<String, String> connection;

    /**
     *
     * @param script The script to which this ScriptRedisCommandClient belongs
     * @param redisURI The URI that specifies the connection details to the server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the RedisClient
     */
    public RedisCommandClient(Script script, RedisURI redisURI, ClientOptions clientOptions) {
        super(script, redisURI, clientOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        super.open();
        connection = client.connect();
    }

    /**
     * Get the underlying connection for this RedisCommandClient.
     * @return The connection associated with this RedisCommandClient
     */
    public StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }

    /**
     * Get the {@link io.lettuce.core.api.sync.RedisCommands} object for executing commands <b>synchronously</b>.
     * @return A RedisCommands object for executing commands
     * @see io.lettuce.core.api.StatefulRedisConnection#sync()
     */
    public RedisCommands<String, String> getCommands() {
        return connection.sync();
    }

    /**
     * Get the {@link io.lettuce.core.api.async.RedisAsyncCommands} object for executing commands <b>asynchronously</b>.
     * @return A RedisAsyncCommands object for executing commands
     * @see io.lettuce.core.api.StatefulRedisConnection#async()
     */
    public RedisAsyncCommands<String, String> getAsyncCommands() {
        return connection.async();
    }

    /**
     * Prints a representation of this RedisCommandClient in string format, including listeners
     * @return A string representation of the RedisCommandClient
     */
    @Override
    public String toString() {
        return String.format("RedisCommandClient[ID: %d, Connection: %s]", getClientId(), connection.toString());
    }
}
