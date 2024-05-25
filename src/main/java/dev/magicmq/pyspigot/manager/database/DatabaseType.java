package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.database.mongo.MongoDatabase;
import dev.magicmq.pyspigot.manager.database.sql.SqlDatabase;

/**
 * Utility enum to represent different database types available for scripts to use.
 */
public enum DatabaseType {

    /**
     * An SQL database type.
     */
    SQL(SqlDatabase.class, /*Host, port, database, user, password*/ "jdbc:mysql://%s:%s/%s?user=%s&password=%s"),

    /**
     * A MongoDB database type.
     */
    MONGO_DB(MongoDatabase.class, /*User, password, host, port*/ "mongodb://%s:%s@%s:%s");

    private final Class<? extends Database> dbClass;
    private final String uri;

    /**
     *
     * @param dbClass The class associated with the database
     * @param uri The URI scheme used to connect to the database
     */
    DatabaseType(Class<? extends Database> dbClass, String uri) {
        this.dbClass = dbClass;
        this.uri = uri;
    }

    /**
     * Get the class that pertains to the database type. Will be a subclass of {@link Database}
     * @return The class associated with the database type
     */
    public Class<? extends Database> getDbClass() {
        return dbClass;
    }

    /**
     * Get the URI scheme associated with the database type.
     * @return The URI scheme associated with the database type
     */
    public String getUri() {
        return uri;
    }
}
