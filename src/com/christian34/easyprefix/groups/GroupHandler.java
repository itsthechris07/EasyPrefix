package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.sun.istack.internal.Nullable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupHandler {
    private static ConcurrentHashMap<String, Group> groups = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Subgroup> subgroups = new ConcurrentHashMap<>();
    private static GroupsData groupsData;

    public static void load() {
        groupsData = FileManager.getGroups();
        groups = new ConcurrentHashMap<>();
        User.getUsers().clear();

        loadDefaults();

        groups.put("default", new Group("default"));

        ArrayList<String> groupNames = new ArrayList<>();
        ArrayList<String> subgroupNames = new ArrayList<>();

        if (EasyPrefix.getInstance().getDatabase() == null) {
            FileConfiguration groupsData = getGroupsData().getFileData();
            Set<String> groupsList = groupsData.getConfigurationSection("groups").getKeys(false);
            groupNames = new ArrayList<>(groupsList);
            if (groupNames.isEmpty()) {
                File old = new File(FileManager.getPluginFolder(), "groups.yml");
                if (old.renameTo(new File(FileManager.getPluginFolder(), "backup-groups.yml"))) {
                    getGroupsData().load();
                    load();
                }
            }
            if (FileManager.getConfig().getFileData().getBoolean(ConfigData.Values.USE_SUBGROUPS.toString())) {
                ConfigurationSection configurationSection = groupsData.getConfigurationSection("subgroups");
                if (configurationSection != null) {
                    subgroupNames.addAll(configurationSection.getKeys(false));
                }
            }
        } else {
            Database database = EasyPrefix.getInstance().getDatabase();
            ResultSet groupsResult = database.getValue("SELECT `group` FROM `%p%groups`");
            try {
                while (groupsResult.next()) {
                    String value = groupsResult.getString("group");
                    if (!value.equals("default")) groupNames.add(value);
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }

            ResultSet subgroupsResult = database.getValue("SELECT `group` FROM `%p%subgroups`");
            try {
                while (subgroupsResult.next()) {
                    String value = subgroupsResult.getString("group");
                    subgroupNames.add(value);
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }
        }

        groupNames.remove("default");
        for (String name : groupNames) {
            groups.put(name.toLowerCase(), new Group(name));
        }

        for (String name : subgroupNames) {
            subgroups.put(name.toLowerCase(), new Subgroup(name));
        }
    }

    private static void loadDefaults() {
        if (EasyPrefix.getInstance().getDatabase() == null) {
            GroupsData groupsData = getGroupsData();
            FileConfiguration fileData = groupsData.getFileData();
            if (fileData.getString("groups.default.prefix") == null) {
                groupsData.set("groups.default.prefix", "&7");
            }
            if (fileData.getString("groups.default.suffix") == null) {
                groupsData.set("groups.default.suffix", "&f:");
            }
            if (fileData.getString("groups.default.chat-color") == null) {
                groupsData.set("groups.default.chat-color", "&7");
            }
            if (fileData.getString("groups.default.join-msg") == null) {
                groupsData.set("groups.default.join-msg", "&8» %ep_user_prefix% %player% &8joined the game");
            }
            if (fileData.getString("groups.default.quit-msg") == null) {
                groupsData.set("groups.default.quit-msg", "&8« %ep_user_prefix% %player% &8left the game");
            }
        } else {
            Database database = EasyPrefix.getInstance().getDatabase();
            if (!database.exists("SELECT `prefix` FROM `%p%groups` WHERE `group` = 'default'")) {
                database.update("INSERT INTO `%p%groups`(`group`, `prefix`, `suffix`, `chat_color`, `join_msg`, " + "`quit_msg`) " + "VALUES ('default','&7','&f:','&7','&8» %ep_user_prefix%%player% &7joined the game','&8« %ep_user_prefix%%player% &7left the game')");
                Messages.log("&cError: You haven't uploaded any data to the sql database yet. Please upload your data" + " with: /easyprefix database migrate");
            }
        }
    }

    public static Group getGroup(String name) {
        return getGroups().getOrDefault(name.toLowerCase(), groups.get("default"));
    }

    public static Subgroup getSubgroup(String name) {
        return getSubgroups().getOrDefault(name.toLowerCase(), null);
    }

    public static Boolean isGroup(String group) {
        return getGroups().containsKey(group.toLowerCase());
    }

    public static Boolean isSubgroup(String group) {
        return getSubgroups().containsKey(group.toLowerCase());
    }

    public static ConcurrentHashMap<String, Group> getGroups() {
        return groups;
    }

    public static ConcurrentHashMap<String, Subgroup> getSubgroups() {
        return subgroups;
    }

    public static void createGroup(String groupName) {
        if (EasyPrefix.getInstance().getDatabase() == null) {
            getGroupsData().getFileData().set("groups." + groupName + ".prefix", "&6" + groupName + " &7| &8");
            getGroupsData().getFileData().set("groups." + groupName + ".suffix", "&f:");
            getGroupsData().getFileData().set("groups." + groupName + ".chat-color", "&7");
            getGroupsData().getFileData().set("groups." + groupName + ".chat-formatting", "&o");
            getGroupsData().save();
        } else {
            Database database = EasyPrefix.getInstance().getDatabase();
            database.update("INSERT INTO `%p%groups`(`group`) VALUES ('" + groupName + "')");
        }
        getGroups().put(groupName.toLowerCase(), new Group(groupName));
    }

    @Nullable
    private static GroupsData getGroupsData() {
        return groupsData;
    }

}