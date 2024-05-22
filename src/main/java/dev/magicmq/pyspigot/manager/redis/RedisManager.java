package dev.magicmq.pyspigot.manager.redis;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import io.lettuce.core.ClientOptions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manager to interface with remote redis servers. Used by scripts to subscribe to pub/sub messaging and publish messages.
 */
public class RedisManager {

    private static RedisManager instance;

    private List<ScriptRedisClient> activeClients;

    private RedisManager() {
        activeClients = new ArrayList<>();
    }

    /**
     * Initialize a new {@link ScriptRedisClient} with a connection to a remote redis server with the specified ip, port, and password, using the default client options. The connection to the remote redis server will be opened automatically when the client is created.
     * @param ip The IP of the redis server to connect to
     * @param port The port of the redis server to connect to
     * @param password The password for the redis server
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(String ip, String port, String password) {
        return openRedisClient(ip, port, password, null);
    }

    /**
     * Initialize a new {@link ScriptRedisClient} with a connection to a remote redis server with the specified ip, port, and password, using the specified {@link io.lettuce.core.ClientOptions}. The connection to the remote redis server will be opened automatically when the client is created.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param ip The IP of the redis server to connect to
     * @param port The port of the redis server to connect to
     * @param password The password for the redis server
     * @param clientOptions The {@link io.lettuce.core.ClientOptions} that should be used for the {@link io.lettuce.core.RedisClient}
     * @return A {@link ScriptRedisClient} representing a client that is connected to the remote redis server
     */
    public ScriptRedisClient openRedisClient(String ip, String port, String password, ClientOptions clientOptions) {
        Script script = ScriptUtils.getScriptFromCallStack();
        if (script == null)
            throw new RuntimeException("No script found when initializing new redis client");

        String uri = URLEncoder.encode("redis://" + password + "@" + ip + ":" + port + "/0", StandardCharsets.UTF_8);
        ScriptRedisClient client = new ScriptRedisClient(script, uri, clientOptions);
        activeClients.add(client);
        if (!client.open()) {
            script.getLogger().log(Level.SEVERE, "Redis client was not opened!");
            activeClients.remove(client);
            return null;
        }
        return client;
    }

    /**
     * Close the specified ScriptRedisClient.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param client The client to close
     * @return True if the client was appropriately closed, false if otherwise
     */
    public boolean closeRedisClient(ScriptRedisClient client) {
        activeClients.remove(client);
        return client.close();
    }

    /**
     * Close all ScriptRedisClients belonging to a script.
     * @param script The script whose ScriptRedisClients should be closed
     * @return True if all clients were appropriately closed, false if one or more clients were not closed or if the script had no open ScriptRedisClient
     */
    public boolean closeRedisClients(Script script) {
        List<ScriptRedisClient> openClients = getRedisClients(script);
        if (openClients.size() == 0)
            return false;

        boolean closed = true;
        for (ScriptRedisClient client : openClients) {
            closed = closeRedisClient(client);
        }
        return closed;
    }

    /**
     * Get all open ScriptRedisClients belonging to a script.
     * @param script The script to get ScriptRedisClients from
     * @return A List of {@link ScriptRedisClient} containing all clients associated with the script. Will return an empty list if there are no clients associated with the script
     */
    public List<ScriptRedisClient> getRedisClients(Script script) {
        List<ScriptRedisClient> toReturn = new ArrayList<>();
        for (ScriptRedisClient client : activeClients) {
            if (client.getScript().equals(script))
                toReturn.add(client);
        }
        return toReturn;
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
