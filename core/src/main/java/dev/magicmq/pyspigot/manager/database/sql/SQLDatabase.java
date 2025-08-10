package dev.magicmq.pyspigot.manager.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.magicmq.pyspigot.manager.script.Script;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Represents an open connection to an SQL database.
 */
public class SQLDatabase extends GenericSQLDatabase {

    private final HikariConfig hikariConfig;

    private HikariDataSource hikariDataSource;

    /**
     *
     * @param script The script associated with this SQLDatabase
     * @param hikariConfig The configuration options for the SQLDatabase connection
     */
    public SQLDatabase(Script script, HikariConfig hikariConfig) {
        super(script);
        this.hikariConfig = hikariConfig;
    }

    @Override
    public boolean open() {
        hikariConfig.setPoolName(getScript().getName() + "-" + getDatabaseId());
        hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource.isRunning() && !hikariDataSource.isClosed();
    }

    @Override
    public boolean close() {
        hikariDataSource.close();
        return !hikariDataSource.isRunning() && hikariDataSource.isClosed();
    }

    @Override
    public Map<String, List<Object>> select(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return select(connection, sql, values);
        }
    }

    @Override
    public int update(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            return update(connection, sql, values);
        }
    }

    /**
     * Get the underlying {@link com.zaxxer.hikari.HikariDataSource} associated with this SQLDatabase.
     * @return The underlying HikariDataSource
     */
    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    /**
     * Prints a representation of this SQLDatabase in string format, including the ID, URI, and {@link com.zaxxer.hikari.HikariDataSource}
     * @return A string representation of the SQLDatabase
     */
    @Override
    public String toString() {
        return String.format("SQLDatabase[ID: %d, HikariDataSource: %s]", getDatabaseId(), hikariDataSource.toString());
    }
}
