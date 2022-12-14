package com.christian34.easyprefix.database;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.tables.TableGroup;
import com.christian34.easyprefix.database.tables.TableSubgroup;
import com.christian34.easyprefix.database.tables.TableUser;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.group.GroupFileData;
import com.christian34.easyprefix.groups.subgroup.SubgroupFileData;
import com.christian34.easyprefix.utils.Debug;
import com.j256.ormlite.dao.Dao;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.SQLException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public final class DataMigration {

    public static void fileToSQL() throws SQLException {
        EasyPrefix instance = EasyPrefix.getInstance();

        DatabaseManager dbManager = new DatabaseManager(DatabaseType.MYSQL);
        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.MYSQL)) {
            // dbManager = instance.getDatabaseManager();
        } else {
            //dbManager = new DatabaseManager(DatabaseType.MYSQL);
        }

        try {
            dbManager.connect(DatabaseType.MYSQL);
        } catch (SQLException e) {
            Debug.warn("Couldn't connect to MySQL! Please check your login information.\nAborting migration...");
            e.printStackTrace();
            return;
        }

        instance.getFileManager().setGroupsData(new GroupsData());

        Dao<TableGroup, String> groupDao = dbManager.getTableGroupDao();
        ConfigurationSection groupsSection = instance.getFileManager().getGroupsData().getSection("groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                GroupFileData groupFileData = new GroupFileData(groupName);
                TableGroup tableGroup = new TableGroup(groupFileData.getGroupName());
                tableGroup.setPrefix(groupFileData.getPrefix());
                tableGroup.setSuffix(groupFileData.getSuffix());
                tableGroup.setChatColor(groupFileData.getColor());
                tableGroup.setChatFormatting(groupFileData.getFormatting());
                tableGroup.setJoinMessage(groupFileData.getJoinMessage());
                tableGroup.setQuitMessage(groupFileData.getQuitMessage());
                try {
                    groupDao.createOrUpdate(tableGroup);

                } catch (SQLException ignored) {
                    Debug.warn(String.format("Couldn't upload group '%s', because it already exists!", groupName));
                }
            }
        }

        Dao<TableSubgroup, String> subgroupDao = dbManager.getTableSubgroupDao();
        ConfigurationSection subgroupsSection = instance.getFileManager().getGroupsData().getSection("subgroups");
        if (subgroupsSection != null) {
            for (String subgroupName : subgroupsSection.getKeys(false)) {
                GroupFileData groupFileData = new GroupFileData(subgroupName);
                TableSubgroup tableSubgroup = new TableSubgroup(groupFileData.getGroupName());
                tableSubgroup.setPrefix(groupFileData.getPrefix());
                tableSubgroup.setSuffix(groupFileData.getSuffix());
                subgroupDao.createOrUpdate(tableSubgroup);
            }
        }

        DatabaseManager tempDb = new DatabaseManager(DatabaseType.SQLITE);

        Dao<TableUser, String> userLocalDao = tempDb.getTableUserDao();
        Dao<TableUser, String> userDao = dbManager.getTableUserDao();
        for (TableUser tableUser : userLocalDao.queryBuilder().query()) {
            TableUser localUser = new TableUser(tableUser.getUniqueId());
            localUser.setUsername(tableUser.getUsername());
            localUser.setChatColor(tableUser.getChatColor());
            localUser.setChatFormatting(tableUser.getChatFormatting());
            localUser.setGroup(tableUser.getGroup());
            localUser.setSubgroup(tableUser.getSubgroup());
            localUser.setCustomPrefix(tableUser.getCustomPrefix());
            localUser.setCustomSuffix(tableUser.getCustomSuffix());
            localUser.setForceGroup(tableUser.isForceGroup());
            userDao.createOrUpdate(localUser);
        }
        tempDb.disconnect();
    }

    public static void sqlToFile() throws SQLException {
        EasyPrefix instance = EasyPrefix.getInstance();
        DatabaseManager dbManager;
        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.MYSQL)) {
            dbManager = instance.getDatabaseManager();
        } else {
            dbManager = new DatabaseManager(DatabaseType.MYSQL);
        }

        try {
            dbManager.connect(DatabaseType.MYSQL);
        } catch (SQLException e) {
            Debug.warn("Couldn't connect to MySQL! Please check your login information.\nAborting migration...");
            e.printStackTrace();
            return;
        }

        instance.getFileManager().setGroupsData(new GroupsData());
        Dao<TableGroup, String> groupDao = dbManager.getTableGroupDao();
        for (TableGroup tableGroup : groupDao.queryBuilder().query()) {
            GroupFileData groupFileData = new GroupFileData(tableGroup.getName());
            groupFileData.setAutoSave(false);
            groupFileData.setPrefix(tableGroup.getPrefix());
            groupFileData.setSuffix(tableGroup.getSuffix());
            groupFileData.setColor(tableGroup.getChatColor());
            groupFileData.setFormatting(tableGroup.getChatFormatting());
            groupFileData.setJoinMessage(tableGroup.getJoinMessage());
            groupFileData.setQuitMessage(tableGroup.getQuitMessage());
            groupFileData.save();
        }

        Dao<TableSubgroup, String> subgroupDao = dbManager.getTableSubgroupDao();
        for (TableSubgroup tableSubgroup : subgroupDao.queryBuilder().query()) {
            SubgroupFileData subgroupFileData = new SubgroupFileData(tableSubgroup.getName());
            subgroupFileData.setAutoSave(false);
            subgroupFileData.setPrefix(tableSubgroup.getPrefix());
            subgroupFileData.setSuffix(tableSubgroup.getSuffix());
            subgroupFileData.save();
        }

        DatabaseManager tempDb = new DatabaseManager(DatabaseType.SQLITE);

        Dao<TableUser, String> userDao = dbManager.getTableUserDao();
        Dao<TableUser, String> userLocalDao = tempDb.getTableUserDao();
        for (TableUser tableUser : userDao.queryBuilder().query()) {
            TableUser localUser = new TableUser(tableUser.getUniqueId());
            localUser.setUsername(tableUser.getUsername());
            localUser.setChatColor(tableUser.getChatColor());
            localUser.setChatFormatting(tableUser.getChatFormatting());
            localUser.setGroup(tableUser.getGroup());
            localUser.setSubgroup(tableUser.getSubgroup());
            localUser.setCustomPrefix(tableUser.getCustomPrefix());
            localUser.setCustomSuffix(tableUser.getCustomSuffix());
            localUser.setForceGroup(tableUser.isForceGroup());
            userLocalDao.createOrUpdate(localUser);
        }
        tempDb.disconnect();
    }

    private void backup() {
        File fileGroup = new File(FileManager.getPluginFolder(), "groups.yml");

    }

}
