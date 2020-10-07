package com.christian34.easyprefix.sql;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.Database;
import com.christian34.easyprefix.sql.database.StorageType;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class InsertStatement {
    private final String table;
    private final HashMap<String, Object> values;

    public InsertStatement(String table) {
        this.table = table;
        this.values = new HashMap<>();
    }

    public InsertStatement setValue(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public boolean execute() {
        try {
            PreparedStatement stmt = buildStatement();
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public PreparedStatement buildStatement() throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO ");
        EasyPrefix instance = EasyPrefix.getInstance();
        Database database;
        if (instance.getStorageType() == StorageType.SQL) {
            database = instance.getSqlDatabase();
        } else {
            database = instance.getLocalDatabase();
        }

        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String column : values.keySet()) {
            if (i == 1) {
                query.append("(");
            } else {
                query.append(", ");
            }
            query.append("`").append(column).append("`");
            i++;
        }
        query.append(") VALUES (");

        for (int slot = 1; slot <= values.size(); slot++) {
            if (slot != 1) {
                query.append(", ");
            }
            query.append("?");
            if (slot == values.size()) {
                query.append(")");
            }
        }
        PreparedStatement stmt = database.getConnection().prepareStatement(query.toString());

        i = 1;
        for (Object value : values.values()) {
            stmt.setObject(i, value);
            i++;
        }

        return stmt;
    }

}
