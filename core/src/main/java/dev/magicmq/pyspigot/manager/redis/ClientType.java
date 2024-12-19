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

/**
 * Utility enum to represent different types of redis clients available for scripts to use.
 */
public enum ClientType {

    /**
     * A basic client type, used for initiating a standard RedisClient for further custom usage.
     */
    BASIC(ScriptRedisClient.class),

    /**
     * A command client type, used for executing redis commands.
     */
    COMMAND(RedisCommandClient.class),

    /**
     * A pub/sub client type, used for publishing and subscribing to redis messaging.
     */
    PUB_SUB(RedisPubSubClient.class);

    private final Class<? extends ScriptRedisClient> clientClass;

    /**
     *
     * @param clientClass The class associated with the client type
     */
    ClientType(Class<? extends ScriptRedisClient> clientClass) {
        this.clientClass = clientClass;
    }

    /**
     * Get the class that pertains to the client type. Will be a subclass of {@link ScriptRedisClient}
     * @return The class associated with the client type
     */
    public Class<? extends ScriptRedisClient> getClientClass() {
        return clientClass;
    }
}
