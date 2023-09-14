package dev.magicmq.pyspigot.manager.redis;

import io.lettuce.core.RedisClient;

import java.util.ArrayList;
import java.util.List;

public class RedisManager {

    private static RedisManager instance;

    private List<ScriptRedisClient> registeredClients;

    private RedisManager() {
        registeredClients = new ArrayList<>();
    }

    public RedisClient newClient(String ip, String port, String password) {
        return null;
    }

    public static RedisManager get() {
        if (instance == null)
            instance = new RedisManager();
        return instance;
    }
}
