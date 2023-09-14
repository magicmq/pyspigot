package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.database.mongo.MongoDatabase;
import dev.magicmq.pyspigot.manager.database.sql.SqlDatabase;
import dev.magicmq.pyspigot.manager.script.Script;
import dev.magicmq.pyspigot.manager.script.ScriptManager;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static DatabaseManager instance;

    private List<Database> activeDatabases;

    private DatabaseManager() {
        activeDatabases = new ArrayList<>();
    }

    public Database openDatabase(DatabaseType type, String host, String port, String database, String username, String password) {
        Script script = ScriptManager.get().getScriptFromCallStack();
        if (script == null)
            throw new RuntimeException("No script found when opening database");

        if (type == DatabaseType.SQL) {
            String uri = String.format(DatabaseType.SQL.getUri(), host, port, database, username, password);
            Database db = new SqlDatabase(script, uri);
            db.open();
            activeDatabases.add(db);
            return db;
        } else /*if (type == DatabaseType.MONGO_DB) */ {
            String uri = String.format(DatabaseType.MONGO_DB.getUri(), username, password, host, port);
            Database db = new MongoDatabase(script, uri);
            db.open();
            activeDatabases.add(db);
            return db;
        }
    }

    public boolean closeDatabase(Database database) {
        boolean result = database.close();
        activeDatabases.remove(database);
        return result;
    }

    public void closeDatabases(Script script) {
        List<Database> associatedDatabases = getDatabases(script);
        associatedDatabases.forEach(this::closeDatabase);
    }

    public List<Database> getDatabase(Script script, DatabaseType type) {
        List<Database> toReturn = new ArrayList<>();
        for (Database database : activeDatabases) {
            if (database.getClass() == type.getDbClass() && database.getScript().equals(script))
                toReturn.add(database);
        }
        return toReturn;
    }

    public List<Database> getDatabases(Script script) {
        List<Database> toReturn = new ArrayList<>();
        for (Database database : activeDatabases) {
            if (database.getScript().equals(script))
                toReturn.add(database);
        }
        return toReturn;
    }

    public static DatabaseManager get() {
        if (instance == null)
            instance = new DatabaseManager();
        return instance;
    }
}
