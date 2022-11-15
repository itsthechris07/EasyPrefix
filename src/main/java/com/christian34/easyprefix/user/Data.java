package com.christian34.easyprefix.user;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableUser;
import com.christian34.easyprefix.utils.Debug;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.UUID;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */


class Data {
    private final UUID uniqueId;
    private final Dao<TableUser, String> userDao;
    private TableUser database;

    protected Data(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.userDao = EasyPrefix.getInstance().getDatabaseManager().getTableUserDao();
        try {
            this.database = userDao.queryBuilder().where().eq("uuid", uniqueId.toString()).queryForFirst();
            if (this.database == null) {
                TableUser user = new TableUser(uniqueId.toString());
                userDao.create(user);
                this.database = user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public TableUser getDatabase() {
        return database;
    }

    public void save() {
        Debug.log("saving user data");
        try {
            this.userDao.update(this.database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
