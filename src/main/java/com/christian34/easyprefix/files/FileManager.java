package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;

import java.io.File;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class FileManager {
    private static ConfigData configData;
    private static GroupsData groupsData;
    private final EasyPrefix instance;

    public FileManager(EasyPrefix instance) {
        this.instance = instance;
        try {
            load();
        } catch (Exception ex) {
            Debug.captureException(ex);
        }
    }

    public static File getPluginFolder() {
        return new File("plugins/EasyPrefix");
    }

    public void load() {
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) userFolder.mkdirs();
        configData = new ConfigData(this.instance).load();
        groupsData = new GroupsData(this.instance);
        if (!configData.getData().getBoolean("config.sql.enabled")) {
            groupsData.load();
        }
    }

    public ConfigData getConfig() {
        return configData;
    }

    public GroupsData getGroupsData() {
        return groupsData;
    }

}