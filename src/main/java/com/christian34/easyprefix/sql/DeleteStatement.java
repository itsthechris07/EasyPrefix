package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * EasyPrefix 2026.
 * <p>
 * Builds and runs a DELETE statement on a table of the current storage.
 *
 * @author Christian34
 */
public class DeleteStatement {
    private final Database database;
    private final EasyPrefix instance;
    private final String table;
    private final Map<String, String> conditions;

    public DeleteStatement(String table) {
        this.table = table;
        this.conditions = new HashMap<>();
        this.instance = EasyPrefix.getInstance();
        this.database = instance.getStorageType() == StorageType.SQL
                ? instance.getSqlDatabase()
                : instance.getLocalDatabase();
    }

    public DeleteStatement addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    private PreparedStatement buildStatement(Connection connection) throws SQLException {
        StringBuilder query = new StringBuilder("DELETE FROM ");
        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String key : conditions.keySet()) {
            if (i == 1) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            query.append("`").append(key).append("` = ?");
            i++;
        }

        PreparedStatement stmt = connection.prepareStatement(query.toString());
        i = 1;
        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

    public boolean execute() {
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = buildStatement(connection)) {
            stmt.executeUpdate();
            if (database instanceof SQLDatabase sql) sql.announce(table, conditions.get("uuid"));
            return true;
        } catch (SQLException ex) {
            Debug.catchException(ex);
            return false;
        }
    }

}
