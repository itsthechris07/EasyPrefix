package com.christian34.easyprefix.files;

import java.io.File;

public class FileManager {
    private static ConfigData configData;
    private static GroupsData groupsData;

    public static void load() {
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        configData = new ConfigData().load();
        groupsData = new GroupsData().load();
    }

    public static ConfigData getConfig() {
        return configData;
    }

    public static GroupsData getGroups() {
        return groupsData;
    }

    public static File getPluginFolder() {
        return new File("plugins/EasyPrefix");
    }

}