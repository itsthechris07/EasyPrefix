package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.files.MessageData;
import com.christian34.easyprefix.sql.InsertStatement;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EasyPrefix 2026.
 * <p>
 * Loads and creates the groups and tags (subgroups) from groups.yml or the database.
 *
 * @author Christian34
 */
public class GroupHandler {
    private final EasyPrefix instance;
    private final GroupsData groupsData;
    private volatile Set<Group> groups = ConcurrentHashMap.newKeySet();
    private volatile Set<Subgroup> subgroups = ConcurrentHashMap.newKeySet();
    private volatile Group defaultGroup;
    private SQLDatabase database;

    public GroupHandler(EasyPrefix instance) {
        this.instance = instance;
        this.groupsData = instance.getFileManager().getGroupsData();

        if (instance.getStorageType() == StorageType.LOCAL && getGroupsData() != null) {
            GroupsData groupsData = getGroupsData();
            if (groupsData.getString("default.join-msg") == null) {
                groupsData.set("groups.default.join-msg", "&8» %ep_user_prefix%%player% &8joined the game");
            }
            if (groupsData.getString("default.quit-msg") == null) {
                groupsData.set("groups.default.quit-msg", "&8« %ep_user_prefix%%player% &8left the game");
            }
            groupsData.save();
        } else {
            this.database = instance.getSqlDatabase();
            SelectQuery selectQuery = new SelectQuery("groups", "prefix").addCondition("group", "default");
            if (selectQuery.getData().isEmpty()) {
                InsertStatement insertStatement = new InsertStatement("groups").setValue("group", "default").setValue("priority", 0).setValue("prefix", "&7").setValue("suffix", "&f:").setValue("chat_color", "gray").setValue("join_msg", "&8» %ep_user_prefix%%player% &7joined the game").setValue("quit_msg", "&8« %ep_user_prefix%%player% &7left the game").setValue("hover", String.join("\n", Group.DEFAULT_HOVER));
                if (!insertStatement.execute()) {
                    Debug.warn("Couldn't upload default group to database!");
                }
            }
        }
    }

    public void load() {
        Debug.recordAction("Loading groups...");
        // the groups are loaded into new sets first, so other threads never see a half loaded state
        Set<Group> groups = ConcurrentHashMap.newKeySet();
        Set<Subgroup> subgroups = ConcurrentHashMap.newKeySet();
        Group defaultGroup = new Group(this, "default");
        groups.add(defaultGroup);

        List<String> groupNames = new ArrayList<>();
        List<String> subgroupNames = new ArrayList<>();

        if (instance.getStorageType() == StorageType.LOCAL) {
            GroupsData groupsData = getGroupsData();
            ConfigurationSection groupsSection = groupsData.getSection("groups");
            if (groupsSection != null) {
                groupNames.addAll(groupsSection.getKeys(false));
            }
            if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
                ConfigurationSection subgroupsSection = groupsData.getSection("subgroups");
                if (subgroupsSection != null) {
                    subgroupNames.addAll(subgroupsSection.getKeys(false));
                }
            }
        } else {
            try {
                groupNames.addAll(readNames("SELECT `group` FROM `%p%groups`"));
                if (instance.getConfigData().getBoolean(ConfigData.Keys.USE_TAGS)) {
                    subgroupNames.addAll(readNames("SELECT `group` FROM `%p%subgroups`"));
                }
            } catch (SQLException e) {
                Debug.handleException(e);
                return;
            }
        }

        groupNames.remove("default");
        for (String name : groupNames) {
            try {
                groups.add(new Group(this, name));
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }

        Collections.sort(subgroupNames);
        for (String name : subgroupNames) {
            try {
                subgroups.add(new Subgroup(name));
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }
        this.groups = groups;
        this.subgroups = subgroups;
        this.defaultGroup = defaultGroup;

        // older versions had one hover text for everybody in messages.yml
        List<String> legacyHover = MessageData.takeLegacyNameHover();
        if (legacyHover != null && defaultGroup.getOwnHover() == null) {
            defaultGroup.setHover(legacyHover);
            Debug.log("The hover text of messages.yml (chat_name_hover) has been moved to the default group.");
        }
    }

    private List<String> readNames(String sql) throws SQLException {
        return database.query(sql, result -> {
            List<String> names = new ArrayList<>();
            while (result.next()) names.add(result.getString("group"));
            return names;
        });
    }

    @NotNull
    public Group getGroup(@NotNull String name) {
        return groups.stream().filter(group -> group.getName().equalsIgnoreCase(name)).findAny().orElse(defaultGroup);
    }

    @Nullable
    public Subgroup getSubgroup(@NotNull String name) {
        return subgroups.stream().filter(subgroup -> subgroup.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public Boolean isGroup(@NotNull String groupName) {
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase(groupName)) return true;
        }
        return false;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public Set<Subgroup> getSubgroups() {
        return subgroups;
    }

    public EasyPrefix getInstance() {
        return instance;
    }

    public boolean createGroup(String groupName) {
        if (isGroup(groupName)) return false;
        if (database == null) {
            String path = "groups." + groupName + ".";
            getGroupsData().set(path + "prefix", "&9" + groupName + " &7| &8");
            getGroupsData().set(path + "suffix", "&f:");
            getGroupsData().set(path + "chat-color", "gray");
            getGroupsData().save();
        } else {
            InsertStatement insertStatement = new InsertStatement("groups").setValue("group", groupName);
            if (!insertStatement.execute()) {
                Debug.log("Couldn't save new group!");
                return false;
            }
        }

        Group group = new Group(this, groupName);
        groups.add(group);
        return true;
    }

    public boolean createSubgroup(String groupName) {
        if (database == null) {
            String path = "subgroups." + groupName + ".";
            getGroupsData().set(path + "prefix", "&6" + groupName + " &7| &8");
            getGroupsData().set(path + "suffix", "&f:");
            getGroupsData().save();
        } else {
            InsertStatement insertStatement = new InsertStatement("subgroups").setValue("group", groupName);
            if (!insertStatement.execute()) {
                Debug.log("Couldn't save new group!");
                return false;
            }
        }

        Subgroup group = new Subgroup(groupName);
        subgroups.add(group);
        return true;
    }

    public void reloadGroup(EasyGroup easyGroup) {
        if (easyGroup instanceof Group group) {
            groups.remove(group);
            groups.add(new Group(this, easyGroup.getName()));
        } else {
            Subgroup subgroup = (Subgroup) easyGroup;
            subgroups.remove(subgroup);
            subgroups.add(new Subgroup(easyGroup.getName()));
        }
    }

    private GroupsData getGroupsData() {
        return groupsData;
    }

}
