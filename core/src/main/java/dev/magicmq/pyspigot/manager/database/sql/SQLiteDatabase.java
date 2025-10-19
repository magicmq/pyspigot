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
import java.sql.SQLException;
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
    public List<Map<String, Object>> select(String sql, Object[] values) throws SQLException {
        return select(connection, sql, values);
    }

    @Override
    public int update(String sql, Object[] values) throws SQLException {
        return update(connection, sql, values);
    }

    /**
     * Prints a representation of this SQLiteDatabase in string format, including the ID, and database file.
     * @return A string representation of the SQLiteDatabase
     */
    @Override
    public String toString() {
        return String.format("SQLiteDatabase[ID: %d, File: %s]", getDatabaseId(), uri);
    }
}
