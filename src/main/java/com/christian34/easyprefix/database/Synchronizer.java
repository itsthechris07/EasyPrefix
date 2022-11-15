package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableOptions;
import com.christian34.easyprefix.groups.group.Group;
import com.christian34.easyprefix.groups.group.GroupSqlData;
import com.christian34.easyprefix.groups.subgroup.Subgroup;
import com.christian34.easyprefix.groups.subgroup.SubgroupSqlData;
import com.christian34.easyprefix.utils.Debug;
import com.j256.ormlite.dao.Dao;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class Synchronizer {
    private final DatabaseManager databaseManager;
    Dao<TableOptions, String> tableOptionsDao;
    private long lastUpdate;
    private TableOptions optionsDatabase;
    private BukkitTask task;

    protected Synchronizer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.tableOptionsDao = this.databaseManager.getTableOptionsDao();
        try {
            this.optionsDatabase = this.tableOptionsDao.queryBuilder().where().eq("name", "sync").queryForFirst();
            if (this.optionsDatabase == null) {
                TableOptions optionEntry = new TableOptions("sync");
                long time = System.currentTimeMillis();
                this.lastUpdate = time;
                optionEntry.setValue(String.valueOf(time));
                this.tableOptionsDao.create(optionEntry);
                this.optionsDatabase = optionEntry;
            } else {
                try {
                    this.lastUpdate = Long.parseLong(this.optionsDatabase.getValue());
                } catch (NumberFormatException e) {
                    sendSyncInstruction();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void enable() {
        this.task = Bukkit.getScheduler().runTaskTimer(EasyPrefix.getInstance(), () -> {
            try {
                this.tableOptionsDao.refresh(this.optionsDatabase);
                long lastSyncCommand = getTimeFromDatabase();
                if (this.lastUpdate != lastSyncCommand) {
                    Debug.log(" \ndata not equal");
                    Debug.log("last update " + this.lastUpdate);
                    Debug.log("sync command: " + lastSyncCommand);
                    sync();
                    this.lastUpdate = lastSyncCommand;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, 20 * 5, 20 * 30);
    }

    public void disable() {
        if (this.task != null) this.task.cancel();
    }

    public void sendSyncInstruction() {
        Debug.log("Sending sync instruction");
        long time = System.currentTimeMillis();
        this.lastUpdate = time;
        this.optionsDatabase.setValue(String.valueOf(time));
        try {
            this.tableOptionsDao.update(this.optionsDatabase);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sync() {
        EasyPrefix instance = EasyPrefix.getInstance();
        for (Group group : instance.getGroupHandler().getGroups()) {
            try {
                ((GroupSqlData) group.getGroupData()).refresh();
            } catch (SQLException e) {
                Debug.warn(String.format("Couldn't reload group '%s'!", group.getName()));
            }
        }
        for (Subgroup subgroup : instance.getGroupHandler().getSubgroups()) {
            try {
                ((SubgroupSqlData) subgroup.getSubgroupData()).refresh();
            } catch (SQLException e) {
                Debug.warn(String.format("Couldn't reload subgroup '%s'!", subgroup.getName()));
            }
        }
        Debug.log("Synchronized data!");
    }

    private long getTimeFromDatabase() {
        try {
            this.tableOptionsDao.refresh(this.optionsDatabase);
            return Long.parseLong(this.optionsDatabase.getValue());
        } catch (NumberFormatException | SQLException e) {
            Debug.warn("Invalid timestamp in database!");
            sendSyncInstruction();
        }
        return 0;
    }

}
