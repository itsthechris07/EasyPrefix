package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.utils.Debug;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class FileManager {
    private static final File pluginFolder = new File("plugins/EasyPrefix");
    private final EasyPrefix instance;
    private ConfigData configData;
    private GroupsData groupsData;
    private MessageData messageData;

    public FileManager(EasyPrefix instance) {
        this.instance = instance;
        try {
            load();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static File getPluginFolder() {
        return pluginFolder;
    }

    public MessageData getMessageData() {
        return messageData;
    }

    public void load() {
        Debug.recordAction("loading files");
        File userFolder = new File(getPluginFolder() + "/user");
        if (!userFolder.exists()) {
            if (!userFolder.mkdirs()) throw new RuntimeException("Couldn't create folder 'users'!");
        }
        this.configData = new ConfigData();
        if (!configData.getData().getBoolean("config.sql.enabled")) {
            this.groupsData = new GroupsData();
        }
        this.messageData = new MessageData(this.instance);
    }

    public ConfigData getConfig() {
        return configData;
    }

    @Nullable
    public GroupsData getGroupsData() {
        return groupsData;
    }

}