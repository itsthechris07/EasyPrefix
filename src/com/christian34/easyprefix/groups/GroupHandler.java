package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.Database;
import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.user.User;
import org.bukkit.configuration.ConfigurationSection;

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

        groups.put("default", new Group("default"));

        if (EasyPrefix.getInstance().getDatabase() == null) {
            if (getGroupsData().getFileData().getString("groups.default.prefix") == null) {
                getGroupsData().set("groups.default.prefix", "&7");
            }
            if (getGroupsData().getFileData().getString("groups.default.suffix") == null) {
                getGroupsData().set("groups.default.suffix", "&f:");
            }
            if (getGroupsData().getFileData().getString("groups.default.chat-color") == null) {
                getGroupsData().set("groups.default.chat-color", "&7");
            }
            if (getGroupsData().getFileData().getString("groups.default.join-msg") == null) {
                getGroupsData().set("groups.default.join-msg", "&8» %ep_user_prefix% %player% &8joined the game");
            }
            if (getGroupsData().getFileData().getString("groups.default.quit-msg") == null) {
                getGroupsData().set("groups.default.quit-msg", "&8« %ep_user_prefix% %player% &8left the game");
            }

            Set<String> groupsList = getGroupsData().getFileData().getConfigurationSection("groups").getKeys(false);
            for (String g : groupsList) {
                if (g.equals("default")) continue;
                Group group = new Group(g);
                groups.put(g.toLowerCase(), group);
            }
            if (groups.isEmpty()) {
                File old = new File(FileManager.getPluginFolder(), "groups.yml");
                File backup = new File(FileManager.getPluginFolder(), "backup-groups.yml");
                if (old.renameTo(backup)) {
                    getGroupsData().load();
                    load();
                }
            }
            if (FileManager.getConfig().getFileData().getBoolean(ConfigData.Values.USE_SUBGROUPS.toString())) {
                ConfigurationSection configurationSection = getGroupsData().getFileData().getConfigurationSection("subgroups");
                if (configurationSection != null) {
                    Set<String> subgroupsList = configurationSection.getKeys(false);
                    for (String g : subgroupsList) {
                        Subgroup group = new Subgroup(g);
                        subgroups.put(g.toLowerCase(), group);
                    }
                }
            }
        } else {
            Database db = EasyPrefix.getInstance().getDatabase();
            ResultSet result = db.getValue("SELECT `group` FROM `%p%groups`");
            ArrayList<String> groupList = new ArrayList<>();
            try {
                while (result.next()) {
                    groupList.add(result.getString("group"));
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }
            for (String name : groupList) {
                if (name.equals("default")) continue;
                Group group = new Group(name);
                groups.put(name.toLowerCase(), group);
            }

            ResultSet result2 = db.getValue("SELECT `group` FROM `%p%subgroups`");
            ArrayList<String> sgList = new ArrayList<>();
            try {
                while (result2.next()) {
                    sgList.add(result2.getString("group"));
                }
            } catch(SQLException e) {
                e.printStackTrace();
                return;
            }
            for (String name : sgList) {
                if (name.equals("default")) continue;
                Subgroup group = new Subgroup(name);
                subgroups.put(name.toLowerCase(), group);
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
        getGroupsData().getFileData().set("groups." + groupName + ".prefix", "&6" + groupName + " &7| &8");
        getGroupsData().getFileData().set("groups." + groupName + ".suffix", "&f:");
        getGroupsData().getFileData().set("groups." + groupName + ".chat-color", "&7");
        getGroupsData().getFileData().set("groups." + groupName + ".chat-formatting", "&o");
        getGroupsData().save();
        getGroups().put(groupName.toLowerCase(), new Group(groupName));
    }

    private static GroupsData getGroupsData() {
        return groupsData;
    }

}