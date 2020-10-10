package com.christian34.easyprefix.sql.migrate;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.InsertStatement;
import com.christian34.easyprefix.sql.UpdateStatement;
import com.christian34.easyprefix.user.UserData;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class Uploader {
    private final DataMigration dataMigration;
    private final GroupsData groupsData;
    private final FileConfiguration data;
    private final EasyPrefix instance;

    public Uploader(DataMigration dataMigration) {
        this.dataMigration = dataMigration;
        this.groupsData = dataMigration.getGroupsData();
        this.data = groupsData.getData();
        this.instance = dataMigration.getInstance();
    }

    boolean sendGroups() {
        Set<String> groupNames = groupsData.getSection("groups");

        if (groupNames.isEmpty()) {
            Messages.log("§cCouldn't find groups to upload!");
            return false;
        }

        for (String name : groupNames) {
            InsertStatement insert = new InsertStatement("groups").setValue("group", name);
            String path = "groups." + name + ".";
            try {
                insert.execute();
            } catch (Exception ignored) {
            }
            UpdateStatement update = new UpdateStatement("groups");
            update.addCondition("group", name);
            update
                    .setValue("prefix", data.getString(path + "prefix"))
                    .setValue("suffix", data.getString(path + "suffix"))
                    .setValue("chat_color", data.getString(path + "chat-color"))
                    .setValue("chat_formatting", data.getString(path + "chat-formatting"))
                    .setValue("join_msg", data.getString(path + "join-msg"))
                    .setValue("quit_msg", data.getString(path + "quit-msg"));
            try {
                update.execute();
            } catch (Exception ex) {
                Messages.log("§cCouldn't upload group '" + name + "'!");
                ex.printStackTrace();
            }
        }
        return true;
    }

    boolean sendSubgroups() {
        Set<String> groupNames = groupsData.getSection("subgroups");

        if (groupNames.isEmpty()) {
            Messages.log("§cCouldn't find any subgroups to upload!");
            return false;
        }

        for (String name : groupNames) {
            InsertStatement insert = new InsertStatement("subgroups").setValue("group", name);
            String path = "subgroups." + name + ".";
            try {
                insert.execute();
            } catch (Exception ignored) {
            }
            UpdateStatement update = new UpdateStatement("subgroups");
            update.addCondition("group", name);
            update
                    .setValue("prefix", data.getString(path + "prefix"))
                    .setValue("suffix", data.getString(path + "suffix"));
            try {
                update.execute();
            } catch (Exception ex) {
                Messages.log("§cCouldn't upload subgroup '" + name + "'!");
                ex.printStackTrace();
            }
        }
        return true;
    }

    boolean sendGenderedGroupsLayout() {
        Set<String> groupNames = groupsData.getSection("groups");

        if (groupNames.isEmpty()) {
            return false;
        }

        for (String name : groupNames) {
            Set<String> genders = groupsData.getSection("groups." + name + ".genders");
            if (genders.isEmpty()) continue;

            for (String genderType : genders) {
                String path = "groups." + name + ".genders." + genderType + ".";

                InsertStatement insert = new InsertStatement("groups_gendered")
                        .setValue("group", name)
                        .setValue("gender", genderType)
                        .setValue("prefix", data.getString(path + "prefix"))
                        .setValue("suffix", data.getString(path + "suffix"));
                try {
                    insert.execute();
                } catch (Exception ex) {
                    Messages.log("§cCouldn't upload the gendered layout for group '" + name + "'!");
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

    boolean sendGenderedSubgroupsLayout() {
        Set<String> groupNames = groupsData.getSection("subgroups");

        if (groupNames.isEmpty()) {
            return false;
        }

        for (String name : groupNames) {
            Set<String> genders = groupsData.getSection("subgroups." + name + ".genders");
            if (genders.isEmpty()) continue;

            for (String genderType : genders) {
                String path = "subgroups." + name + ".genders." + genderType + ".";

                InsertStatement insert = new InsertStatement("subgroups_gendered")
                        .setValue("group", name)
                        .setValue("gender", genderType)
                        .setValue("prefix", data.getString(path + "prefix"))
                        .setValue("suffix", data.getString(path + "suffix"));
                try {
                    insert.execute();
                } catch (Exception ex) {
                    Messages.log("§cCouldn't upload the gendered layout for subgroup '" + name + "'!");
                    ex.printStackTrace();
                }
            }
        }
        return true;
    }

    boolean sendUsers() {
        ResultSet result = dataMigration.getLocalDatabase().getValue("SELECT `uuid` FROM `%p%users`");
        List<UUID> users = new ArrayList<>();
        try {
            while (result.next()) {
                try {
                    UUID uniqueId = UUID.fromString(result.getString("uuid"));
                    users.add(uniqueId);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                result.close();
            }
        } catch (SQLException ignored) {
        }

        if (users.isEmpty()) {
            Messages.log("Couldn't find any users!");
            return false;
        }

        for (UUID uniqueId : users) {
            InsertStatement insert = new InsertStatement("users").setValue("uuid", uniqueId.toString());
            try {
                insert.execute();
            } catch (Exception ignored) {
            }

            UpdateStatement update = new UpdateStatement("users").addCondition("uuid", uniqueId.toString());
            UserData userData = new UserData(uniqueId);
            userData.setDatabase(dataMigration.getLocalDatabase());
            userData.loadData();

            HashMap<String, Object> data = userData.getData().getData();
            for (String key : data.keySet()) {
                update.setValue(key, data.get(key));
            }
            try {
                if (update.execute()) {
                    return true;
                }
            } catch (Exception ex) {
                Messages.log("§cAn error occurred while uploading the data for user '" + uniqueId.toString() + "'!");
            }
        }
        return false;
    }

}
