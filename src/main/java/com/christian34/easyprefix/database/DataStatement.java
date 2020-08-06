package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class DataStatement {
    private PreparedStatement preparedStatement;
    private SQLException exception;

    public DataStatement(String sqlQuery) {
        String sql = sqlQuery;
        EasyPrefix instance = EasyPrefix.getInstance();
        Database database = instance.getStorageType() == StorageType.SQL ? instance.getSqlDatabase() : instance.getLocalDatabase();
        try {
            sql = sql.replace("%p%", database.getTablePrefix());
            this.preparedStatement = database.getConnection().prepareStatement(sql);
        } catch (SQLException ex) {
            Debug.captureException(ex);
            ex.printStackTrace();
        }
    }

    public void setObject(int index, Object value) {
        try {
            if (value == null) {
                this.preparedStatement.setNull(index, Types.VARCHAR);
            } else {
                this.preparedStatement.setObject(index, value);
            }
        } catch (SQLException ex) {
            this.exception = ex;
        }
    }

    public boolean execute() {
        try {
            preparedStatement.executeUpdate();
            preparedStatement.close();
            return true;
        } catch (SQLException ex) {
            Debug.captureException(ex);
            this.exception = ex;
            return false;
        }
    }

    public SQLException getException() {
        return this.exception;
    }

}