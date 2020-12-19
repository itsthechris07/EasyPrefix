package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class DeleteStatement {
    private static final Database database;
    private static final EasyPrefix instance;

    static {
        instance = EasyPrefix.getInstance();
        database = instance.getStorageType() == StorageType.SQL
                ? instance.getSqlDatabase()
                : instance.getLocalDatabase();
    }

    private final String table;
    private final Map<String, String> conditions;

    public DeleteStatement(String table) {
        this.table = table;
        this.conditions = new HashMap<>();
    }

    public DeleteStatement addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    private PreparedStatement buildStatement() throws SQLException {
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

        PreparedStatement stmt = database.getConnection().prepareStatement(query.toString());
        i = 1;
        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

    public boolean execute() {
        try (PreparedStatement stmt = buildStatement()) {
            stmt.executeUpdate();
            if (!this.table.equals("options") && database instanceof SQLDatabase) {
                instance.getSqlDatabase().getSqlSynchronizer().sendSyncInstruction();
            }
            return true;
        } catch (SQLException ex) {
            Debug.catchException(ex);
            return false;
        }
    }

}
