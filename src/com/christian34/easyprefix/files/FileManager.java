package com.christian34.easyprefix.files;

import java.io.File;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class FileManager {
    private static ConfigData configData;
    private static GroupsData groupsData;

    /*
        todo remove static
     */
    public static void load() {
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) userFolder.mkdirs();
        configData = new ConfigData().load();
        groupsData = new GroupsData();
        if (!configData.getBoolean(ConfigData.ConfigKeys.USE_SQL)) groupsData.load();
    }

    public static ConfigData getConfig() {
        return configData;
    }

    public static GroupsData getGroupsData() {
        return groupsData;
    }

    public static File getPluginFolder() {
        return new File("plugins/EasyPrefix");
    }

}