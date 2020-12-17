package com.christian34.easyprefix.sql.migrate;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.sql.database.LocalDatabase;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.utils.Debug;
import org.jetbrains.annotations.ApiStatus;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "1.8")
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
            this.localDatabase = new LocalDatabase();
            this.localDatabase.connect();
        }

        this.sqlDatabase = instance.getSqlDatabase();
        if (sqlDatabase == null) {
            this.sqlDatabase = new SQLDatabase(instance);
            this.sqlDatabase.connect();
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
            Debug.log("Groups have been migrated.");
        } else {
            Debug.log("§cCouldn't migrate groups! If you think this is an error, please create an issue on github.");
        }

        if (uploader.sendSubgroups()) {
            Debug.log("Subgroups have been migrated.");
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
        if (this.localDatabase != null) {
            this.localDatabase.close();
            this.localDatabase = null;
        }

        if (this.sqlDatabase != null) {
            this.sqlDatabase.close();
            this.sqlDatabase = null;
        }

        this.connected = false;
    }

}
