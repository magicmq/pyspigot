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

public class SqlDatabase extends Database {

    private static int dbId = 0;

    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    public SqlDatabase(Script script, String uri) {
        super(script);
        
        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl(uri);

        this.hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", 250);
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        this.hikariConfig.addDataSourceProperty("useServerPrepStmts", true);
        this.hikariConfig.addDataSourceProperty("useLocalSessionState", true);
        this.hikariConfig.addDataSourceProperty("rewriteBatchedStatements", true);
        this.hikariConfig.addDataSourceProperty("cacheResultSetMetadata", true);
        this.hikariConfig.addDataSourceProperty("cacheServerConfiguration", true);
        this.hikariConfig.addDataSourceProperty("elideSetAutoCommit", true);
        this.hikariConfig.addDataSourceProperty("maintainTimeStats", false);

        dbId++;
    }

    public void addDataSourceProperty(String property, Object value) {
        hikariConfig.addDataSourceProperty(property, value);
    }

    @Override
    public boolean open() {
        hikariConfig.setPoolName(getScript().getName() + "-" + dbId + "-hikari");
        hikariDataSource = new HikariDataSource(hikariConfig);
        return hikariDataSource.isRunning() && !hikariDataSource.isClosed();
    }

    @Override
    public boolean close() {
        hikariDataSource.close();
        return !hikariDataSource.isRunning() && hikariDataSource.isClosed();
    }

    public Map<String, List<Object>> select(String sql) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
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

    public Map<String, List<Object>> select(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
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

    public int update(String sql) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                return statement.executeUpdate();
            }
        }
    }

    public int update(String sql, Object[] values) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
                }

                return statement.executeUpdate();
            }
        }
    }
}
