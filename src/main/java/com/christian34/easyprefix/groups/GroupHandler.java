package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.DatabaseType;
import com.christian34.easyprefix.database.tables.TableGroup;
import com.christian34.easyprefix.database.tables.TableSubgroup;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.group.Group;
import com.christian34.easyprefix.groups.subgroup.Subgroup;
import com.christian34.easyprefix.utils.Debug;
import com.j256.ormlite.dao.Dao;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class GroupHandler {
    private final EasyPrefix instance;
    private Set<Group> groups;
    private Set<Subgroup> subgroups;
    private Group defaultGroup;

    public GroupHandler(EasyPrefix instance) {
        this.instance = instance;

        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.SQLITE)) {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            String path = "groups.default.";
            groupsData.set(path + "prefix", "&7");
            groupsData.set(path + "suffix", "&f:");
            groupsData.set(path + "chat-color", "7");
            groupsData.save();
        } else {
            Dao<TableGroup, String> groupDao = instance.getDatabaseManager().getTableGroupDao();
            try {
                TableGroup tableGroup = groupDao.queryBuilder().where().eq("name", "default").queryForFirst();
                if (tableGroup == null) {
                    TableGroup defaultGroup = new TableGroup("default");
                    defaultGroup.setPrefix("&7");
                    defaultGroup.setSuffix("&f:");
                    defaultGroup.setChatColor("7");
                    defaultGroup.setJoinMessage("&8» %ep_user_prefix%%player% &7joined the game");
                    defaultGroup.setQuitMessage("&8« %ep_user_prefix%%player% &7left the game");
                    groupDao.create(defaultGroup);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void load() {
        Debug.recordAction("Loading groups...");
        this.groups = new HashSet<>();
        this.subgroups = new HashSet<>();
        this.defaultGroup = new Group(this, "default");
        if (defaultGroup.getJoinMessage() == null) {
            defaultGroup.setJoinMessage("&8» %ep_user_prefix%%player% &8joined the game");
        }
        if (defaultGroup.getQuitMessage() == null) {
            defaultGroup.setQuitMessage("&8« %ep_user_prefix%%player% &8left the game");
        }
        groups.add(defaultGroup);

        List<String> groupNames = new ArrayList<>();
        List<String> subgroupNames = new ArrayList<>();

        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.SQLITE)) {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            assert groupsData != null;
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
                Dao<TableGroup, String> groupDao = instance.getDatabaseManager().getTableGroupDao();
                for (TableGroup groupData : groupDao.queryBuilder().query()) {
                    String value = groupData.getName();
                    if (!value.equals("default")) groupNames.add(value);
                }

                Dao<TableSubgroup, String> subgroupDao = instance.getDatabaseManager().getTableSubgroupDao();
                for (TableSubgroup subgroupData : subgroupDao.queryBuilder().query()) {
                    String value = subgroupData.getName();
                    subgroupNames.add(value);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        Collections.sort(groupNames);
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
                subgroups.add(new Subgroup(this, name));
            } catch (Exception ex) {
                Debug.handleException(ex);
            }
        }
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

    @Deprecated
    public boolean createGroup(String name) {
        if (isGroup(name)) return false;
        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.SQLITE)) {
            GroupsData groupsData = instance.getFileManager().getGroupsData();
            String path = "groups." + name + ".";
            groupsData.set(path + "prefix", "&9" + name + " &7| &8");
            groupsData.set(path + "suffix", "&f:");
            groupsData.set(path + "chat-color", "7");
            groupsData.save();
        } else {
            Dao<TableGroup, String> groupDao = instance.getDatabaseManager().getTableGroupDao();
            try {
                TableGroup tableGroup = groupDao.queryBuilder().where().eq("name", name).queryForFirst();
                if (tableGroup == null) {
                    TableGroup group = new TableGroup(name);
                    group.setPrefix("&9" + name + " &7| &8");
                    group.setSuffix("&f:");
                    group.setChatColor("7");
                    groupDao.create(group);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        Group group = new Group(this, name);
        groups.add(group);
        return true;
    }

    public boolean createSubgroup(String groupName) {
        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.SQLITE)) {
            GroupsData groupsData = this.instance.getFileManager().getGroupsData();
            String path = "subgroups." + groupName + ".";
            groupsData.set(path + "prefix", "&6" + groupName + " &7| &8");
            groupsData.set(path + "suffix", "&f:");
            groupsData.save();
        } else {
            Dao<TableSubgroup, String> subgroupDao = instance.getDatabaseManager().getTableSubgroupDao();
            try {
                TableSubgroup tableSubgroup = subgroupDao.queryBuilder().where().eq("name", groupName).queryForFirst();
                if (tableSubgroup == null) {
                    TableSubgroup subgroup = new TableSubgroup(groupName);
                    subgroup.setPrefix("&7");
                    subgroup.setSuffix("&f:");
                    subgroupDao.create(subgroup);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Subgroup group = new Subgroup(this, groupName);
        subgroups.add(group);
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

}
