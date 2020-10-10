package com.christian34.easyprefix.sql.migrate;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class DataMigration {
    private final EasyPrefix instance;
    private final Uploader uploader;
    private final GroupsData groupsData;
    boolean connected = false;
    private LocalDatabase localDatabase;
    private SQLDatabase sqlDatabase;

    public DataMigration(EasyPrefix instance) {
        this.instance = instance;

        this.groupsData = instance.getFileManager().getGroupsData();
        if (groupsData.getData() == null) {
            groupsData.load();
        }

        this.uploader = new Uploader(this);
    }

    private void connect() {
        this.localDatabase = instance.getLocalDatabase();
        if (localDatabase == null) {
            instance.setLocalDatabase(new LocalDatabase(instance));
            this.localDatabase = instance.getLocalDatabase();
        }

        this.sqlDatabase = instance.getSqlDatabase();
        if (sqlDatabase == null) {
            instance.setSqlDatabase(new SQLDatabase(instance));
            this.sqlDatabase = instance.getSqlDatabase();
        }
        this.connected = true;
    }

    LocalDatabase getLocalDatabase() {
        return localDatabase;
    }

    SQLDatabase getSqlDatabase() {
        return sqlDatabase;
    }

    EasyPrefix getInstance() {
        return instance;
    }

    GroupsData getGroupsData() {
        return groupsData;
    }

    public void upload() {
        if (!connected) {
            connect();
        }
        Messages.log("§cThe data migration has started. This could take a while.");

        if (uploader.sendGroups()) {
            Messages.log("Groups has been migrated.");
        } else {
            Messages.log("§aCouldn't migrate groups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendSubgroups()) {
            Messages.log("Subgroups has been migrated.");
        } else {
            Messages.log("§aCouldn't migrate subgroups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendGenderedGroupsLayout()) {
            Messages.log("Layout data for groups has been migrated.");
        } else {
            Messages.log("§aCouldn't migrate layout data for groups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendGenderedSubgroupsLayout()) {
            Messages.log("Layout data for subgroups has been migrated.");
        } else {
            Messages.log("§aCouldn't migrate layout data for subgroups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendUsers()) {
            Messages.log("Users have been migrated.");
        } else {
            Messages.log("§aCouldn't migrate users! If you think this is an error, please create an issue on github.");
        }
        Messages.log("§aData has been migrated!");
        close();
    }


    private void close() {
        if (instance.getStorageType() == StorageType.SQL) {
            instance.getLocalDatabase().close();
            instance.setLocalDatabase(null);
        } else {
            instance.getSqlDatabase().close();
            instance.setSqlDatabase(null);
        }
        this.connected = false;
    }

}
