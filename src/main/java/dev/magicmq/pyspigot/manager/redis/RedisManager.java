package dev.magicmq.pyspigot.manager.redis;

import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;
import io.lettuce.core.ClientOptions;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

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

        String uri = URLEncoder.encode("redis://" + password + "@" + ip + ":" + port + "/0", StandardCharsets.UTF_8);
        ScriptRedisClient client = new ScriptRedisClient(script, uri, clientOptions);

        if (!client.open()) {
            script.getLogger().log(Level.SEVERE, "Redis client was not opened!");
            return null;
        }

        addClient(client);
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
        removeClient(client);
        return client.close();
    }

    /**
     * Close all ScriptRedisClients belonging to a script.
     * @param script The script whose ScriptRedisClients should be closed
     * @return True if all clients were appropriately closed, false if one or more clients were not closed or if the script had no open ScriptRedisClient
     */
    public boolean closeRedisClients(Script script) {
        boolean toReturn = false;
        List<ScriptRedisClient> scriptClients = activeClients.get(script);
        if (scriptClients != null) {
            for (ScriptRedisClient client : scriptClients) {
                toReturn = client.close();
            }
            activeClients.remove(script);
        }
        return toReturn;
    }

    /**
     * Get all open ScriptRedisClients belonging to a script.
     * @param script The script to get ScriptRedisClients from
     * @return A List of {@link ScriptRedisClient} containing all clients associated with the script. Will return an empty list if there are no clients associated with the script
     */
    public List<ScriptRedisClient> getRedisClients(Script script) {
        List<ScriptRedisClient> scriptClients = activeClients.get(script);
        if (scriptClients != null)
            return new ArrayList<>(scriptClients);
        else
            return null;
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
