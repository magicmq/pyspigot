package dev.magicmq.pyspigot.manager.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.zaxxer.hikari.HikariConfig;
import dev.magicmq.pyspigot.manager.database.mongo.MongoDatabase;
import dev.magicmq.pyspigot.manager.database.sql.SqlDatabase;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ArgParser;
import dev.magicmq.pyspigot.util.ScriptUtils;
import org.python.core.PyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Manager that allows connection to and interact with a variety of database types. Primarily used by scripts to interact with external databases, such as SQL and MongoDB.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private final HashMap<Script, List<Database>> activeConnections;

    private DatabaseManager() {
        activeConnections = new HashMap<>();
    }

    /**
     * Get a new {@link com.zaxxer.hikari.HikariConfig} for specifying configuration options.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @return A new HikariConfig
     */
    public HikariConfig newHikariConfig() {
        return new HikariConfig();
    }

    /**
     * Open a new connection with an SQL database, using the specified options. At minimum, connection information must be specified via one of the following:
     * <ul>
     * <li>By setting the Jdbc URL in a HikariConfig (via {@link com.zaxxer.hikari.HikariConfig#setJdbcUrl}) and passing the HikariConfig</li>
     * <li>By passing a Jdbc URL as the {@code uri} parameter</li>
     * <li>By passing a host, port, database, username, and password via their respective arguments</li>
     * </ul>
     * <p>
     * If connection information is specified in multiple ways simultaneously, parameters in the HikariConfig take precedence, followed by the URI parameter, and finally the host, port, database, username, and password parameters.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code hikari_config} (Optional): A {@link com.zaxxer.hikari.HikariConfig} object representing the configuration options for the connection</li>
     * <li>{@code uri} (Optional): The connection string to define the connection. May include options</li>
     * <li>{@code host} (Optional): The host URL or IP of the SQL database</li>
     * <li>{@code port} (Optional): The port of the SQL database</li>
     * <li>{@code database} (Optional): The name of the SQL database</li>
     * <li>{@code username} (Optional): The username of the SQL database</li>
     * <li>{@code password} (Optional): The password of the SQL database</li>
     * </ul>
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(PyObject[] args, String[] keywords) {
        ArgParser argParser = new ArgParser(
                "connectSql",
                args,
                keywords,
                new String[]{
                        "hikari_config",
                        "uri",
                        "host",
                        "port",
                        "database",
                        "username",
                        "password"
                },
                1
        );

        HikariConfig hikariConfig = argParser.getJavaObject(0, HikariConfig.class, null);
        if (hikariConfig == null) {
            hikariConfig = new HikariConfig();
            hikariConfig.addDataSourceProperty("cachePrepStmts", true);
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        }

        if (hikariConfig.getJdbcUrl() == null) {
            String uri = argParser.getString(1, null);
            if (uri == null) {
                String host = argParser.getString(2);
                String port = argParser.getString(3);
                String database = argParser.getString(4);
                String username = argParser.getString(5);
                String password = argParser.getString(6);
                uri = String.format(DatabaseType.SQL.getUri(), host, port, database, username, password);
                hikariConfig.setJdbcUrl(uri);
            }
        }

        Script script = ScriptUtils.getScriptFromCallStack();

        SqlDatabase connection = new SqlDatabase(script, hikariConfig);

        if (connection.open()) {
            addConnection(connection);
            return connection;
        } else
            throw new RuntimeException("Failed to open a connection to the SQL database.");
    }

    /**
     * Open a new connection with an SQL database, using the default configuration options.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param host The host URL or IP of the SQL database
     * @param port The port of the SQL database
     * @param database The name of the SQL database
     * @param username The username of the SQL database
     * @param password The password of the SQL database
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(String host, String port, String database, String username, String password) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        return connectSql(host, port, database, username, password, hikariConfig);
    }

    /**
     * Open a new connection with an SQL database, using the specified configuration options.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param host The host URL or IP of the SQL database
     * @param port The port of the SQL database
     * @param database The name of the SQL database
     * @param username The username of the SQL database
     * @param password The password of the SQL database
     * @param hikariConfig A {@link com.zaxxer.hikari.HikariConfig} object representing the configuration options for the connection
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(String host, String port, String database, String username, String password, HikariConfig hikariConfig) {
        String uri = String.format(DatabaseType.SQL.getUri(), host, port, database, username, password);
        return connectSql(uri, hikariConfig);
    }

    /**
     * Open a new connection with an SQL database, using the provided connection URI.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param uri The connection string to define the connection, including options
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(String uri) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        hikariConfig.setJdbcUrl(uri);

        return connectSql(hikariConfig);
    }

    /**
     * Open a new connection with an SQL database, using the provided connection URI and configuration options.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param uri The connection string to define the connection, including options
     * @param hikariConfig A {@link com.zaxxer.hikari.HikariConfig} object representing the configuration options for the connection
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(String uri, HikariConfig hikariConfig) {
        hikariConfig.setJdbcUrl(uri);
        return connectSql(hikariConfig);
    }

    /**
     * Open a new connection with an SQL database, using the provided configuration.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param hikariConfig The configuration for the connection
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public SqlDatabase connectSql(HikariConfig hikariConfig) {
        Script script = ScriptUtils.getScriptFromCallStack();

        SqlDatabase connection = new SqlDatabase(script, hikariConfig);

        if (connection.open()) {
            addConnection(connection);
            return connection;
        } else
            throw new RuntimeException("Failed to open a connection to the SQL database.");
    }

    /**
     * Get a new {@link com.mongodb.MongoClientSettings.Builder} for specifying client settings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @return A new MongoClientSettings builder
     */
    public MongoClientSettings.Builder newMongoClientSettings() {
        return MongoClientSettings.builder();
    }

    /**
     * Open a new connection with a Mongo database, using the specified options. At minimum, connection information must be specified via one of the following:
     * <ul>
     * <li>By setting the Jdbc URL in a {@link com.mongodb.MongoClientSettings} and passing the object</li>
     * <li>By passing a Jdbc URL as the {@code uri} parameter</li>
     * <li>By passing a host, port, username, and password via their respective arguments</li>
     * </ul>
     * <p>
     * If connection information is specified in multiple ways simultaneously, the host, port, username, and password parameters take precedence, followed by the URI parameter, and finally the MongoClientSettings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * <p>
     * Arguments:
     * <ul>
     * <li>{@code mongo_settings} (Optional): A {@link com.mongodb.MongoClientSettings} object representing the configuration options for the client</li>
     * <li>{@code uri} (Optional): The connection string to define the connection. May include options</li>
     * <li>{@code host} (Optional): The host URL or IP of the Mongo database</li>
     * <li>{@code port} (Optional): The port of the Mongo database</li>
     * <li>{@code username} (Optional): The username of the Mongo database</li>
     * <li>{@code password} (Optional): The password of the Mongo database</li>
     * </ul>
     * @return An {@link SqlDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(PyObject[] args, String[] keywords) {
        ArgParser argParser = new ArgParser(
                "connectSql",
                args,
                keywords,
                new String[]{
                        "mongo_settings",
                        "uri",
                        "host",
                        "port",
                        "username",
                        "password"
                },
                1
        );

        MongoClientSettings.Builder newBuilder;

        MongoClientSettings clientSettings = argParser.getJavaObject(0, MongoClientSettings.class, null);
        if (clientSettings == null)
            newBuilder = MongoClientSettings.builder();
        else
            newBuilder = MongoClientSettings.builder(clientSettings);

        String uri = argParser.getString(1, null);
        if (uri != null) {
            String host = argParser.getString(2, null);
            if (host != null) {
                String port = argParser.getString(3);
                String username = argParser.getString(4, null);
                String password = argParser.getString(5, null);
                if (username == null)
                    uri = String.format(DatabaseType.MONGO_DB_NO_AUTH.getUri(), host, port);
                else
                    uri = String.format(DatabaseType.MONGO_DB.getUri(), username, password, host, port);
                newBuilder.applyConnectionString(new ConnectionString(uri));
            } else
                newBuilder.applyConnectionString(new ConnectionString(uri));
        }

        Script script = ScriptUtils.getScriptFromCallStack();

        MongoDatabase connection = new MongoDatabase(script, newBuilder.build());

        if (connection.open()) {
            addConnection(connection);
            return connection;
        } else
            throw new RuntimeException("Failed to open a connection to the Mongo database.");
    }

    /**
     * Open a new connection with a Mongo database, using the default client settings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param host The host URL or IP of the Mongo database
     * @param port The port of the Mongo database
     * @param username The username of the Mongo database
     * @param password The password of the Mongo database
     * @return An {@link MongoDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(String host, String port, String username, String password) {
        MongoClientSettings clientSettings = MongoClientSettings.builder().build();
        return connectMongo(host, port, username, password, clientSettings);
    }

    /**
     * Open a new connection with a Mongo database, using the provided client settings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param host The host URL or IP of the Mongo database
     * @param port The port of the Mongo database
     * @param username The username of the Mongo database
     * @param password The password of the Mongo database
     * @param clientSettings A {@link com.mongodb.MongoClientSettings} object representing the client settings for the connection
     * @return An {@link MongoDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(String host, String port, String username, String password, MongoClientSettings clientSettings) {
        String uri;
        if (username == null)
            uri = String.format(DatabaseType.MONGO_DB_NO_AUTH.getUri(), host, port);
        else
            uri = String.format(DatabaseType.MONGO_DB.getUri(), username, password, host, port);

        return connectMongo(uri, clientSettings);
    }

    /**
     * Open a new connection with a Mongo database, using the provided connection string URI.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param uri The connection string to define the connection, including options
     * @return An {@link MongoDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(String uri) {
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
        return connectMongo(clientSettings);
    }

    /**
     * Open a new connection with a Mongo database, using the provided connection string URI and client settings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param uri The connection string to define the connection, including options
     * @param clientSettings A {@link com.mongodb.MongoClientSettings} object representing the client settings for the connection
     * @return An {@link MongoDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(String uri, MongoClientSettings clientSettings) {
        MongoClientSettings newClientSettings = MongoClientSettings.builder(clientSettings)
                .applyConnectionString(new ConnectionString(uri))
                .build();
        return connectMongo(newClientSettings);
    }

    /**
     * Open a new connection with a Mongo database, using the provided client settings.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param clientSettings The client settings for the connection
     * @return An {@link MongoDatabase} object representing an open connection to the database
     */
    public MongoDatabase connectMongo(MongoClientSettings clientSettings) {
        Script script = ScriptUtils.getScriptFromCallStack();

        MongoDatabase connection = new MongoDatabase(script, clientSettings);

        if (connection.open()) {
            addConnection(connection);
            return connection;
        } else
            throw new RuntimeException("Failed to open a connection to the Mongo database.");
    }

    /**
     * Disconnect from the provided database connection. Should be called when no longer using the database connection.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param connection The database connection to disconnect from
     * @return True if the disconnection was successful, false if otherwise
     */
    public boolean disconnect(Database connection) {
        removeConnection(connection);
        return connection.close();
    }

    /**
     * Disconnect from all database connections belonging to a certain script.
     * @param script The script whose database connections should be disconnected
     * @return True if all disconnections were successful, false if one or more connections were not closed successfully or if the script had no database connections to close
     */
    public boolean disconnectAll(Script script) {
        boolean toReturn = false;
        List<Database> scriptConnections = activeConnections.get(script);
        if (scriptConnections != null) {
            for (Database connection : scriptConnections) {
                toReturn = connection.close();
            }
            activeConnections.remove(script);
        }
        return toReturn;
    }

    /**
     * Get all database connnections belonging to a script.
     * @param script The script to get database connections from
     * @return An immutable List of {@link Database} containing all database connections associated with the script. Will return null if there are no open database connections associated with the script
     */
    public List<Database> getConnections(Script script) {
        List<Database> scriptConnections = activeConnections.get(script);
        if (scriptConnections != null)
            return new ArrayList<>(scriptConnections);
        else
            return null;
    }

    /**
     * Get all database connnections belonging to a script of the given type.
     * @param script The script to get database connections from
     * @param type The type of database connection to filter by
     * @return An immutable List of {@link Database} containing all database connections of the given type associated with the script. Will return null if there are no open database connections of the given type associated with the script
     */
    public List<Database> getConnections(Script script, DatabaseType type) {
        List<Database> scriptConnections = getConnections(script);
        if (scriptConnections != null) {
            List<Database> toReturn = new ArrayList<>(scriptConnections);
            toReturn.removeIf(connection -> connection.getClass() != type.getDbClass());
            return toReturn;
        } else
            return null;
    }

    private void addConnection(Database connection) {
        Script script = connection.getScript();
        if (activeConnections.containsKey(script))
            activeConnections.get(script).add(connection);
        else {
            List<Database> scriptConnections = new ArrayList<>();
            scriptConnections.add(connection);
            activeConnections.put(script, scriptConnections);
        }
    }

    private void removeConnection(Database connection) {
        Script script = connection.getScript();
        List<Database> scriptConnections = activeConnections.get(script);
        scriptConnections.remove(connection);
        if (scriptConnections.isEmpty())
            activeConnections.remove(script);
    }

    /**
     * Get the singleton instance of this DatabaseManager.
     * @return The instance
     */
    public static DatabaseManager get() {
        if (instance == null)
            instance = new DatabaseManager();
        return instance;
    }
}
