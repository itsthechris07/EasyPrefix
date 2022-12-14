package com.christian34.easyprefix.groups.group;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableGroup;
import com.j256.ormlite.dao.Dao;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class GroupSqlData extends GroupData {
    private final EasyPrefix instance;
    private final Dao<TableGroup, String> groupDao;
    private TableGroup database;

    public GroupSqlData(Group group) {
        this.instance = EasyPrefix.getInstance();
        this.groupDao = instance.getDatabaseManager().getTableGroupDao();
        try {
            this.database = groupDao.queryBuilder().where().eq("name", group.getName()).queryForFirst();
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
    public Character getColor() {
        return this.database.getChatColor();
    }

    @Override
    public void setColor(@Nullable Character color) {
        this.database.setChatColor(color);
        save();
    }

    @Override
    public Character getFormatting() {
        return this.database.getChatFormatting();
    }

    @Override
    public void setFormatting(@Nullable Character formatting) {
        this.database.setChatFormatting(formatting);
        save();
    }

    @Override
    public String getJoinMessage() {
        return this.database.getJoinMessage();
    }

    @Override
    public void setJoinMessage(String message) {
        this.database.setJoinMessage(message);
        save();
    }

    @Override
    public String getQuitMessage() {
        return this.database.getQuitMessage();
    }

    @Override
    public void setQuitMessage(String message) {
        this.database.setQuitMessage(message);
        save();
    }

    @Override
    public void delete() {
        try {
            this.groupDao.delete(this.database);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refresh() throws SQLException {
        this.groupDao.refresh(this.database);
    }

    private void save() {
        try {
            this.groupDao.update(this.database);
            this.instance.getDatabaseManager().getSynchronizer().sendSyncInstruction();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
