package com.christian34.easyprefix.sql.migrate;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;

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
            instance.setLocalDatabase(new LocalDatabase());
            this.localDatabase = instance.getLocalDatabase();
        }

        SQLDatabase sqlDatabase = instance.getSqlDatabase();
        if (sqlDatabase == null) {
            instance.setSqlDatabase(new SQLDatabase());
        }
        this.connected = true;
    }

    LocalDatabase getLocalDatabase() {
        return localDatabase;
    }

    GroupsData getGroupsData() {
        return groupsData;
    }

    public void upload() {
        if (!connected) {
            connect();
        }
        Debug.log("§cThe data migration has started. This could take a while.");

        if (uploader.sendGroups()) {
            Debug.log("Groups has been migrated.");
        } else {
            Debug.log("§cCouldn't migrate groups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendSubgroups()) {
            Debug.log("Subgroups has been migrated.");
        } else {
            Debug.log("§cCouldn't migrate subgroups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendGenderedGroupsLayout()) {
            Debug.log("Layout data for groups has been migrated.");
        } else {
            Debug.log("§cCouldn't migrate layout data for groups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendGenderedSubgroupsLayout()) {
            Debug.log("Layout data for subgroups has been migrated.");
        } else {
            Debug.log("§cCouldn't migrate layout data for subgroups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendUsers()) {
            Debug.log("Users have been migrated.");
        } else {
            Debug.log("§cCouldn't migrate users! If you think this is an error, please create an issue on github.");
        }
        Debug.log("§aData has been migrated!");
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
