package dev.magicmq.pyspigot.manager.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.magicmq.pyspigot.manager.database.Database;
import dev.magicmq.pyspigot.manager.script.Script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an open connection to an SQL database.
 */
public class SqlDatabase extends Database {

    private static int dbId = 0;

    private final HikariConfig hikariConfig;
    private final int databaseId;
    private final String uri;

    private HikariDataSource hikariDataSource;

    /**
     *
     * @param script The script associated with this SQLDatabase
     * @param uri The connection URI for this SQLDatabase
     * @param hikariConfig The configuration options for the SQLDatabase connection
     */
    public SqlDatabase(Script script, String uri, HikariConfig hikariConfig) {
        super(script);
        this.hikariConfig = hikariConfig;
        this.databaseId = dbId++;
        this.uri = uri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean open() {
        hikariConfig.setPoolName(getScript().getName() + "-" + databaseId + "-hikari");
        hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource.isRunning() && !hikariDataSource.isClosed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean close() {
        hikariDataSource.close();
        return !hikariDataSource.isRunning() && hikariDataSource.isClosed();
    }

    /**
     * Get the underlying {@link com.zaxxer.hikari.HikariDataSource} associated with this SQLDatabase.
     * @return The underlying HikariDataSource
     */
    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    /**
     * Select from the SQL database.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The select statement
     * @return A {@link java.util.Map} containing the data returned from the selection
     * @throws SQLException If there was an exception when selecting from the database
     */
    public Map<String, List<Object>> select(String sql) throws SQLException {
        return select(sql, null);
    }

    /**
     * Select from the SQL database with the provided values that should be inserted into the select statement.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The select statement
     * @param values The values that should be inserted into the select statement
     * @return A {@link java.util.Map} containing the data returned from the selection
     * @throws SQLException If there was an exception when selecting from the database
     */
    public Map<String, List<Object>> select(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        statement.setObject(i + 1, values[i]);
                    }
                }

                ResultSet result = statement.executeQuery();

                Map<String, List<Object>> results = new HashMap<>();
                while (result.next()) {
                    for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
                        String colName = result.getMetaData().getColumnName(i);
                        results.computeIfAbsent(colName, s -> new ArrayList<>());
                        results.get(colName).add(result.getObject(colName));
                    }
                }
                result.close();
                return results;
            }
        }
    }

    /**
     * Update the SQL database.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The update statement
     * @return The number of rows that were affected by the update statement
     * @throws SQLException If there was an exception when selecting from the database
     */
    public int update(String sql) throws SQLException {
        return update(sql, null);
    }

    /**
     * Update the SQL database with the provided values that should be inserted into the update statement.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The update statement
     * @param values The values that should be inserted into the select statement
     * @return The number of rows that were affected by the update statement
     * @throws SQLException If there was an exception when selecting from the database
     */
    public int update(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        statement.setObject(i + 1, values[i]);
                    }
                }

                return statement.executeUpdate();
            }
        }
    }

    /**
     * Prints a representation of this SqlDatabase in string format, including the ID, URI, and {@link com.zaxxer.hikari.HikariDataSource}
     * @return A string representation of the SqlDatabase
     */
    @Override
    public String toString() {
        return String.format("SqlDatabase[ID: %d, URI: %s, HikariDataSource: %s]", databaseId, uri, hikariDataSource.toString());
    }
}
