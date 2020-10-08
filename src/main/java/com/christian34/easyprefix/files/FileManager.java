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
    private static final File pluginFolder = new File("plugins/EasyPrefix");
    private final EasyPrefix instance;
    private ConfigData configData;
    private GroupsData groupsData;

    public FileManager(EasyPrefix instance) {
        this.instance = instance;
        try {
            load();
        } catch (Exception ex) {
            Debug.captureException(ex);
        }
    }

    public static File getPluginFolder() {
        return pluginFolder;
    }

    public void load() {
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) {
            if (!userFolder.mkdirs()) throw new RuntimeException("Couldn't create folder 'users'!");
        }
        this.configData = new ConfigData(this.instance).load();
        this.groupsData = new GroupsData(this.instance);
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