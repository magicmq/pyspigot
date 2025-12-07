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

package dev.magicmq.pyspigot.manager.database.sql;


import dev.magicmq.pyspigot.exception.ScriptRuntimeException;
import dev.magicmq.pyspigot.manager.script.Script;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Represents an open connection to an SQLite database file or an SQLite database in memory.
 */
public class SQLiteDatabase extends GenericSQLDatabase {

    private final String uri;

    private Connection connection;

    /**
     *
     * @param script The script associated with this SQLiteDatabase
     * @param uri The connection URI for the database. This should be in the form "jdbc:sqlite:%s", where %s is the path
     *            to the database file
     */
    public SQLiteDatabase(Script script, String uri) {
        super(script);
        this.uri = uri;
    }

    @Override
    public boolean open() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ScriptRuntimeException(getScript(), "SQLite JDBC driver not found on the class path");
        }

        try {
            this.connection = DriverManager.getConnection(this.uri);
            return !connection.isClosed();
        } catch (SQLException e) {
            throw new ScriptRuntimeException(getScript(), "Error when opening connection to SQLite database", e);
        }
    }

    @Override
    public boolean close() {
        if (connection != null) {
            try {
                connection.close();
                return connection.isClosed();
            } catch (SQLException e) {
                throw new ScriptRuntimeException(getScript(), "Error when closing connection to SQLite database", e);
            }
        } else
            return false;
    }

    @Override
    public List<Map<String, Object>> select(String sql, Object... values) throws SQLException {
        return select(connection, sql, values);
    }

    @Override
    public int update(String sql, Object... values) throws SQLException {
        return update(connection, sql, values);
    }

    /**
     * Get the underlying JDBC {@link java.sql.Connection} object which backs this SQLite database.
     * @return The underlying Connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Prints a representation of this SQLiteDatabase in string format, including the ID, and database URI.
     * @return A string representation of the SQLiteDatabase
     */
    @Override
    public String toString() {
        return String.format("SQLiteDatabase[ID: %d, URI: %s]", getDatabaseId(), uri);
    }

    // Methods to mirror Python sqlite3 library functionality

    /**
     * Execute an SQL statement with optional provided values that should be inserted into the statement.
     * <p>
     * If no values should be inserted into the statement, do not pass anything for the {@code values} argument.
     * @param sql The SQL statement
     * @param values Optional argument. The values that should be inserted into the statement
     * @return A {@link java.util.Map} containing the data the database returned. Functionally identical to a Python
     *         dict, where keys are column names and values are column data, with preserved order. Note that select
     *         statements return {@code null}.
     * @throws SQLException If there was an error when executing the statement
     */
    public List<Map<String, Object>> execute(String sql, Object... values) throws SQLException {
        String trimmed = sql.trim().toUpperCase();
        if (trimmed.startsWith("SELECT") || trimmed.startsWith("PRAGMA")) {
            return select(connection, sql, values);
        } else {
            update(connection, sql, values);
            return null;
        }
    }

    /**
     * Repeatedly execute a parameterized statement, for each set of provided values.
     * @param sql The SQL statement
     * @param values A list of values that should be inserted for each time the statement is executed
     * @throws SQLException If there was an error when executing the statement
     */
    public void executemany(String sql, List<Object[]> values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] value : values) {
                if (value != null) {
                    for (int i = 0; i < value.length; i++) {
                        statement.setObject(i + 1, value[i]);
                    }
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Back up the database to a file. Useful for backing up an in-memory database to a persistent file.
     * @param fileName The path to the database file to back up to. This could be a relative path (relative to the root
     *                 directory of the Minecraft/proxy server), or an absolute path
     * @throws SQLException If there was an error when backing up the database
     */
    public void backup(String fileName) throws SQLException {
        if (connection.isClosed())
            throw new ScriptRuntimeException(getScript(), "Failed to backup database: database connection is closed");

        if (fileName.isEmpty() || fileName.equalsIgnoreCase(":memory:"))
            throw new ScriptRuntimeException(getScript(), "Cannot back up to a temporary/in memory database.");

        commit();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("BACKUP TO '" + fileName + "'");
        }
    }

    /**
     * Restore the database from a file. Useful for loading a database file into an in-memory database.
     * @param fileName The path to the database file to restore from. This could be a relative path (relative to the
     *                 root directory of the Minecraft/proxy server), or an absolute path
     * @throws SQLException If there was an error when backing up the database
     */
    public void restore(String fileName) throws SQLException {
        if (connection.isClosed())
            throw new ScriptRuntimeException(getScript(), "Failed to restore database: database connection is closed");

        if (fileName.isEmpty() || fileName.equalsIgnoreCase(":memory:"))
            throw new ScriptRuntimeException(getScript(), "Cannot restore from a temporary/in memory database.");

        commit();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("RESTORE FROM '" + fileName + "'");
        }
    }

    /**
     * Commit a series of statements. This method has no effect if auto-commit is enabled.
     * @throws SQLException If a database access error occurs, this method is called while participating in a
     *                      distributed transaction, or if this method is called on a closed connection
     */
    public void commit() throws SQLException {
        if (!connection.getAutoCommit())
            connection.commit();
    }

    /**
     * Rollback a series of statements. This method has no effect if auto-commit is enabled.
     * @throws SQLException If a database access error occurs, this method is called while participating in a
     *                      distributed transaction, or if this method is called on a closed database connection
     */
    public void rollback() throws SQLException {
        if (!connection.getAutoCommit())
            connection.rollback();
    }

    /**
     * Enable or disable auto-commit.
     * @param autoCommit Pass true to enable auto-commit, false to disable
     * @throws SQLException If a database access error occurs, setAutoCommit(true) is called while participating in
     *                      a distributed transaction, or this method is called on a closed database connection
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        connection.setAutoCommit(autoCommit);
    }

    /**
     * Get if auto-commit is enabled or disabled.
     * @return True if auto-commit is enabled, false if it is not
     * @throws SQLException If a database access error occurs or this method is called on a closed database connection
     */
    public boolean getAutoCommit() throws SQLException {
        return connection.getAutoCommit();
    }
}
