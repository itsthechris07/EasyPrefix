package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.files.FileManager;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.sql.InsertStatement;
import com.christian34.easyprefix.sql.SelectQuery;
import com.christian34.easyprefix.sql.database.SQLDatabase;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GroupHandler {
    private final EasyPrefix instance;
    private final GroupsData groupsData;
    private ArrayList<Group> groups;
    private ArrayList<Subgroup> subgroups;
    private ArrayList<Gender> genders = new ArrayList<>();
    private Group defaultGroup;
    private SQLDatabase database;

    public GroupHandler(EasyPrefix instance) {
        this.instance = instance;
        this.groupsData = instance.getFileManager().getGroupsData();

        if (instance.getStorageType() == StorageType.LOCAL) {
            GroupsData groupsData = getGroupsData();
            FileConfiguration fileData = groupsData.getData();
            if (fileData.getString("groups.default.join-msg") == null) {
                groupsData.set("groups.default.join-msg", "&8» %ep_user_prefix%%player% &8joined the game");
            }
            if (fileData.getString("groups.default.quit-msg") == null) {
                groupsData.set("groups.default.quit-msg", "&8« %ep_user_prefix%%player% &8left the game");
            }
            groupsData.save();
        } else {
            this.database = instance.getSqlDatabase();
            SelectQuery selectQuery = new SelectQuery("groups", "prefix").addCondition("group", "default");
            if (selectQuery.getData().isEmpty()) {
                InsertStatement insertStatement = new InsertStatement("groups")
                        .setValue("group", "default")
                        .setValue("prefix", "&7")
                        .setValue("suffix", "&f:")
                        .setValue("chat_color", "&7")
                        .setValue("join_msg", "&8» %ep_user_prefix%%player% &7joined the game")
                        .setValue("quit_msg", "&8« %ep_user_prefix%%player% &7left the game");
                if (!insertStatement.execute()) {
                    Debug.log("Couldn't upload default group to database!");
                }
                Debug.log("&cError: You haven't uploaded any data to the sql database yet. Please upload your data with: /easyprefix database upload");
            }
        }
    }

    public void load() {
        this.groups = new ArrayList<>();
        this.subgroups = new ArrayList<>();
        this.instance.getUsers().clear();
        if (ConfigKeys.USE_GENDER.toBoolean()) {
            loadGenders();
        }
        this.defaultGroup = new Group(this, "default");
        groups.add(defaultGroup);

        List<String> groupNames = new ArrayList<>();
        List<String> subgroupNames = new ArrayList<>();

        if (instance.getStorageType() == StorageType.LOCAL) {
            GroupsData groupsData = getGroupsData();
            groupNames = new ArrayList<>(getGroupsData().getSection("groups"));
            if (groupNames.isEmpty()) {
                File folder = FileManager.getPluginFolder();
                File old = new File(folder, "groups.yml");
                if (old.renameTo(new File(folder, "backup-groups.yml"))) {
                    groupsData.load();
                    load();
                }
            }
            if (ConfigKeys.USE_SUBGROUPS.toBoolean()) {
                subgroupNames.addAll(groupsData.getSection("subgroups"));
            }
        } else {
            ResultSet groupsResult = database.getValue("SELECT `group` FROM `%p%groups`");
            try {
                while (groupsResult.next()) {
                    String value = groupsResult.getString("group");
                    if (!value.equals("default")) groupNames.add(value);
                }
            } catch (SQLException e) {
                Debug.captureException(e);
                return;
            }

            ResultSet subgroupsResult = database.getValue("SELECT `group` FROM `%p%subgroups`");
            try {
                while (subgroupsResult.next()) {
                    String value = subgroupsResult.getString("group");
                    subgroupNames.add(value);
                }
            } catch (SQLException e) {
                Debug.captureException(e);
                return;
            }
        }

        groupNames.remove("default");
        for (String name : groupNames) {
            try {
                groups.add(new Group(this, name));
            } catch (Exception ex) {
                Debug.captureException(ex);
            }
        }

        for (String name : subgroupNames) {
            try {
                subgroups.add(new Subgroup(this, name));
            } catch (Exception ex) {
                Debug.captureException(ex);
            }
        }
    }

    public boolean handleGenders() {
        return ConfigKeys.USE_GENDER.toBoolean();
    }

    public void loadGenders() {
        this.genders = new ArrayList<>();
        for (String name : ConfigKeys.GENDER_TYPES.toSection()) {
            try {
                this.genders.add(new Gender(name));
            } catch (Exception ex) {
                Debug.captureException(ex);
            }
        }
    }

    public List<Gender> getGenderTypes() {
        return genders;
    }

    @Nullable
    public Gender getGender(String name) {
        if (name == null) return null;
        return genders.stream().filter(gender -> gender.getName().equalsIgnoreCase(name)).findAny().orElse(null);
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

    public List<Group> getGroups() {
        return groups;
    }

    public List<Subgroup> getSubgroups() {
        return subgroups;
    }

    public EasyPrefix getInstance() {
        return instance;
    }

    public boolean createGroup(String groupName) {
        if (database == null) {
            String path = "groups." + groupName + ".";
            getGroupsData().set(path + "prefix", "&6" + groupName + " &7| &8");
            getGroupsData().set(path + "suffix", "&f:");
            getGroupsData().set(path + "chat-color", "&7");
            getGroupsData().set(path + "chat-formatting", "&o");
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

    public void reloadGroup(EasyGroup easyGroup) {
        if (easyGroup instanceof Group) {
            Group group = (Group) easyGroup;
            groups.remove(group);
            groups.add(new Group(this, easyGroup.getName()));
        } else {
            Subgroup subgroup = (Subgroup) easyGroup;
            subgroups.remove(subgroup);
            subgroups.add(new Subgroup(this, easyGroup.getName()));
        }
    }

    private GroupsData getGroupsData() {
        return groupsData;
    }

}
