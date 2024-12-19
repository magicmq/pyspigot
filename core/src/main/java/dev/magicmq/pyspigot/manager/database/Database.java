package dev.magicmq.pyspigot.manager.database;

import dev.magicmq.pyspigot.manager.script.Script;

/**
 * Represents a database to which a script is connected and can read/write.
 */
public abstract class Database {

    private static int dbIdIncrement;

    private final Script script;
    private final int databaseId;

    /**
     *
     * @param script The script associated with this database connection
     */
    public Database(Script script) {
        this.script = script;
        this.databaseId = dbIdIncrement++;
    }

    /**
     * Opens a connection to the database.
     * @return True if opening the connection to the database was successful, false if otherwise
     */
    public abstract boolean open();

    /**
     * Closes a connection to the database.
     * @return True if closing the connection to the database was successful, false if otherwise
     */
    public abstract boolean close();

    /**
     * Get the script associated with this database connection.
     * @return The script associated with this database connection
     */
    public Script getScript() {
        return script;
    }

    /**
     * Get the ID of this database connection.
     * @return The ID
     */
    public int getDatabaseId() {
        return databaseId;
    }

    /**
     * Prints a representation of this Database in string format.
     * @return A string representation of this Database
     */
    public abstract String toString();
}
