package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;

import java.io.File;
import java.io.IOException;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class GroupsData extends PluginFile {

    public GroupsData() {
        super(new File(FileManager.getPluginFolder(), "groups.yml"), null);
    }

    @Override
    public void createFile() throws IOException {
        try {
            EasyPrefix.getInstance().getPlugin().saveResource("groups.yml", true);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public void update() throws IOException {
        //no updates necessary
    }

}
