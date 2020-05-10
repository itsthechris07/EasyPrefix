package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;

import java.io.File;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class FileManager {
    private static ConfigData configData;
    private static GroupsData groupsData;
    private EasyPrefix instance;

    public FileManager(EasyPrefix instance) {
        this.instance = instance;
        load();
    }

    public static File getPluginFolder() {
        return new File("plugins/EasyPrefix");
    }

    public void load() {
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) userFolder.mkdirs();
        configData = new ConfigData(this.instance).load();
        groupsData = new GroupsData(this.instance);
        if (!configData.getBoolean(ConfigData.ConfigKeys.USE_SQL)) groupsData.load();
    }

    public ConfigData getConfig() {
        return configData;
    }

    public GroupsData getGroupsData() {
        return groupsData;
    }

}