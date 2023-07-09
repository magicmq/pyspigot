package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.database.mongo.MongoDatabase;
import dev.magicmq.pyspigot.manager.database.sql.SqlDatabase;

public enum DatabaseType {

    //Host, port, database, user, password
    SQL(SqlDatabase.class, "jdbc:mysql://%s:%s/%s?user=%s&password=%s"),
    //User, password, host, port
    MONGO_DB(MongoDatabase.class, "mongodb://%s:%s@%s:%s");

    private Class<? extends Database> dbClass;
    private String uri;

    DatabaseType(Class<? extends Database> dbClass, String uri) {
        this.dbClass = dbClass;
        this.uri = uri;
    }

    public Class<? extends Database> getDbClass() {
        return dbClass;
    }

    public String getUri() {
        return uri;
    }
}
