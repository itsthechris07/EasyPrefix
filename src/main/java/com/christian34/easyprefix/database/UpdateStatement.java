package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class UpdateStatement {
    private final HashMap<String, Object> values;
    private final HashMap<String, String> conditions;
    private final String table;

    public UpdateStatement(String table) {
        this.table = table;
        this.values = new HashMap<>();
        this.conditions = new HashMap<>();
    }

    public UpdateStatement setValue(String column, Object value) {
        this.values.put(column, value);
        return this;
    }

    public UpdateStatement addCondition(String column, String value) {
        this.conditions.put(column, value);
        return this;
    }

    public PreparedStatement buildStatement() throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE ");
        SQLDatabase database = EasyPrefix.getInstance().getSqlDatabase();

        query.append("`").append(database.getTablePrefix()).append(this.table).append("`");

        int i = 1;
        for (String column : values.keySet()) {
            if (i == 1) {
                query.append(" SET ");
            } else {
                query.append(", ");
            }
            query.append(" `").append(column).append("`=?");
            i++;
        }

        i = 1;
        for (String key : conditions.keySet()) {
            if (i == 1) {
                query.append(" WHERE ");
            } else {
                query.append(" AND ");
            }
            query.append("`").append(key).append("` = ?");
            i++;
        }

        Bukkit.broadcastMessage(query.toString());
        PreparedStatement stmt = database.getConnection().prepareStatement(query.toString());
        i = 1;
        for (String value : values.keySet()) {
            stmt.setObject(i, values.get(value));
            i++;
        }

        for (String value : conditions.values()) {
            stmt.setObject(i, value);
            i++;
        }
        return stmt;
    }

    public boolean execute() {
        try {
            buildStatement().executeUpdate();
            buildStatement().close();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

}
