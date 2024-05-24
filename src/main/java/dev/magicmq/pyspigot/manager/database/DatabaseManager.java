package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.database.mongo.MongoDatabase;
import dev.magicmq.pyspigot.manager.database.sql.SqlDatabase;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.util.ScriptUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 * Manager that allows connection to and interact with a variety of database types. Primarily used by scripts to interact with external databases.
 */
public class DatabaseManager {

    private static DatabaseManager instance;

    private final HashMap<Script, List<Database>> activeDatabases;

    private DatabaseManager() {
        activeDatabases = new HashMap<>();
    }

    /**
     * Open a new connection with a database of the specified type.
     * @param type The type of database to open
     * @param host The host URL or IP of the database
     * @param port The port of the database
     * @param database The name of the database. Only used for the SQL database type
     * @param username The username of the database
     * @param password The password of the database
     * @return A database object representing the open connection to the database.
     */
    public Database openDatabase(DatabaseType type, String host, String port, String database, String username, String password) {
        Script script = ScriptUtils.getScriptFromCallStack();

        Database db;
        if (type == DatabaseType.SQL) {
            String uri = URLEncoder.encode(String.format(DatabaseType.SQL.getUri(), host, port, database, username, password), StandardCharsets.UTF_8);
            db = new SqlDatabase(script, uri);
        } else /*if (type == DatabaseType.MONGO_DB) */ {
            String uri = URLEncoder.encode(String.format(DatabaseType.MONGO_DB.getUri(), username, password, host, port), StandardCharsets.UTF_8);
            db = new MongoDatabase(script, uri);
        }

        if (db.open()) {
            addDatabase(db);
            return db;
        } else {
            script.getLogger().log(Level.SEVERE, "Failed to open a connection to the " + type + " database.");
            return null;
        }
    }

    /**
     * Close the provided database. Should be called when no longer using the database connection.
     * @param database The database to close
     * @return True if the database connection was successfully closed, false if otherwise
     */
    public boolean closeDatabase(Database database) {
        removeDatabase(database);
        return database.close();
    }

    /**
     * Close all database connections belonging to a certain script.
     * @param script The script whose database connections should be closed
     * @return True if all database connections were successfully closed, false if one or more connections were not closed successfully or if the script had no database connections to close
     */
    public boolean closeDatabases(Script script) {
        boolean toReturn = false;
        List<Database> scriptDatabases = activeDatabases.get(script);
        if (scriptDatabases != null) {
            for (Database database : scriptDatabases) {
                toReturn = database.close();
            }
            activeDatabases.remove(script);
        }
        return toReturn;
    }

    /**
     * Get all database connnections belonging to a script.
     * @param script The script to get database connections from
     * @return An immutable List of {@link Database} containing all database connections associated with the script. Will return null if there are no open database connections associated with the script
     */
    public List<Database> getDatabases(Script script) {
        List<Database> scriptDatabases = activeDatabases.get(script);
        if (scriptDatabases != null)
            return new ArrayList<>(scriptDatabases);
        else
            return null;
    }

    /**
     * Get all database connnections belonging to a script of the given type.
     * @param script The script to get database connections from
     * @param type The type of database connection to filter by
     * @return An immutable List of {@link Database} containing all database connections of the given type associated with the script. Will return null if there are no open database connections of the given type associated with the script
     */
    public List<Database> getDatabases(Script script, DatabaseType type) {
        List<Database> scriptDatabases = getDatabases(script);
        if (scriptDatabases != null) {
            List<Database> toReturn = new ArrayList<>(scriptDatabases);
            toReturn.removeIf(database -> database.getClass() != type.getDbClass());
            return toReturn;
        } else
            return null;
    }

    private void addDatabase(Database database) {
        Script script = database.getScript();
        if (activeDatabases.containsKey(script))
            activeDatabases.get(script).add(database);
        else {
            List<Database> scriptDatabases = new ArrayList<>();
            scriptDatabases.add(database);
            activeDatabases.put(script, scriptDatabases);
        }
    }

    private void removeDatabase(Database database) {
        Script script = database.getScript();
        List<Database> scriptDatabases = activeDatabases.get(script);
        scriptDatabases.remove(database);
        if (scriptDatabases.isEmpty())
            activeDatabases.remove(script);
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
