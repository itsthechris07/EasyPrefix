package com.christian34.easyprefix.groups.subgroup;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableSubgroup;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class SubgroupSqlData extends SubgroupData {
    private final EasyPrefix instance;
    private final Dao<TableSubgroup, String> subgroupDao;
    private TableSubgroup database;

    public SubgroupSqlData(Subgroup subgroup) {
        this.instance = EasyPrefix.getInstance();
        this.subgroupDao = instance.getDatabaseManager().getTableSubgroupDao();
        try {
            this.database = subgroupDao.queryBuilder().where().eq("name", subgroup.getName()).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPrefix() {
        return this.database.getPrefix();
    }

    @Override
    public void setPrefix(String prefix) {
        this.database.setPrefix(prefix);
        save();
    }

    @Override
    public String getSuffix() {
        return this.database.getSuffix();
    }

    @Override
    public void setSuffix(String suffix) {
        this.database.setSuffix(suffix);
        save();
    }

    @Override
    public void delete() {
        try {
            this.subgroupDao.delete(this.database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refresh() throws SQLException {
        this.subgroupDao.refresh(this.database);
    }

    private void save() {
        try {
            this.subgroupDao.update(this.database);
            this.instance.getDatabaseManager().getSynchronizer().sendSyncInstruction();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
