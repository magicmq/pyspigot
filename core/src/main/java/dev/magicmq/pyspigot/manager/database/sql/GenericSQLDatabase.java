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


import dev.magicmq.pyspigot.manager.database.Database;
import dev.magicmq.pyspigot.manager.script.Script;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstraction for all SQL-type databases, including MySQL, MariaDB, and SQLite.
 */
public abstract class GenericSQLDatabase extends Database {

    /**
     *
     * @param script The script associated with this GenericSQLDatabase
     */
    public GenericSQLDatabase(Script script) {
        super(script);
    }

    /**
     * Select from the SQL database with the provided values that should be inserted into the select statement.
     * <p>
     * If no values should be inserted into the select statement, do not pass anything for the {@code values} argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The select statement
     * @param values Optional argument. The values that should be inserted into the select statement
     * @return A {@link java.util.Map} containing the data returned from the selection. Functionally identical to a python dict, where keys are column names and values are column data, with preserved order
     * @throws SQLException If there was an error when selecting from the database
     */
    public abstract List<Map<String, Object>> select(String sql, Object... values) throws SQLException;

    /**
     * Update the SQL database with the provided values that should be inserted into the update statement.
     * <p>
     * If no values should be inserted into the update statement, do not pass anything for the {@code values} argument.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The update statement
     * @param values Optional argument. The values that should be inserted into the update statement
     * @return The number of rows that were affected by the update statement
     * @throws SQLException If there was an error when updating the database
     */
    public abstract int update(String sql, Object... values) throws SQLException;

    protected List<Map<String, Object>> select(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    statement.setObject(i + 1, values[i]);
                }
            }

            ResultSet result = statement.executeQuery();

            ResultSetMetaData metaData = result.getMetaData();
            int columnCount = metaData.getColumnCount();

            String[] labels = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                labels[i - 1] = metaData.getColumnLabel(i);
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (result.next()) {
                Map<String, Object> row = new LinkedHashMap<>(columnCount);
                for (int i = 1; i<= columnCount; i++) {
                    Object value = result.getObject(i);
                    row.put(labels[i - 1], value);
                }
                rows.add(row);
            }
            result.close();
            return rows;
        }
    }

    protected int update(Connection connection, String sql, Object... values) throws SQLException {
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
