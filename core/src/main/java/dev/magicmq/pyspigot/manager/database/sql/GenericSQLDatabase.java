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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The select statement
     * @param values The values that should be inserted into the select statement
     * @return A {@link java.util.Map} containing the data returned from the selection. Functionally identical to a python dict, where keys are column names and values are column data, with preserved order
     * @throws SQLException If there was an exception when selecting from the database
     */
    public abstract Map<String, List<Object>> select(String sql, Object[] values) throws SQLException;

    /**
     * Update the SQL database with the provided values that should be inserted into the update statement.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The update statement
     * @param values The values that should be inserted into the update statement
     * @return The number of rows that were affected by the update statement
     * @throws SQLException If there was an exception when updating the database
     */
    public abstract int update(String sql, Object[] values) throws SQLException;

    /**
     * Select from the SQL database.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The select statement
     * @return A {@link java.util.Map} containing the data returned from the selection. Functionally identical to a python dict, where keys are column names and values are column data, with preserved order
     * @throws SQLException If there was an exception when selecting from the database
     */
    public Map<String, List<Object>> select(String sql) throws SQLException {
        return select(sql, null);
    }

    /**
     * Update the SQL database.
     * <p>
     * <b>Note:</b> This should be called from scripts only!
     * @param sql The update statement
     * @return The number of rows that were affected by the update statement
     * @throws SQLException If there was an exception when updating the database
     */
    public int update(String sql) throws SQLException {
        return update(sql, null);
    }

    protected Map<String, List<Object>> select(Connection connection, String sql, Object[] values) throws SQLException {
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
                    String colName = result.getMetaData().getColumnName(i + 1);
                    results.computeIfAbsent(colName, s -> new ArrayList<>());
                    results.get(colName).add(result.getObject(colName));
                }
            }
            result.close();
            return results;
        }
    }

    protected int update(Connection connection, String sql, Object[] values) throws SQLException {
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
