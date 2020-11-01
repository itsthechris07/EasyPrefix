package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.sql.*;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.user.User;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Subgroup extends EasyGroup {
    private final String NAME;
    private final GroupHandler groupHandler;
    private final EasyPrefix instance;
    private final ChatColor groupColor;
    private String prefix, suffix;
    private GroupsData groupsData;
    private GenderedLayout genderedLayout = null;

    public Subgroup(GroupHandler groupHandler, String name) {
        this.NAME = name;
        this.groupHandler = groupHandler;
        this.instance = groupHandler.getInstance();
        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery selectQuery = new SelectQuery("subgroups", "prefix", "suffix")
                    .addCondition("group", name);
            Data data = selectQuery.getData();
            this.prefix = data.getString("prefix");
            this.suffix = data.getString("suffix");
        } else {
            this.groupsData = groupHandler.getInstance().getFileManager().getGroupsData();
            this.prefix = groupsData.getString(getFileKey() + "prefix");
            this.suffix = groupsData.getString(getFileKey() + "suffix");
        }

        if (groupHandler.handleGenders()) {
            this.genderedLayout = new GenderedLayout(this);
        }

        if (prefix != null) {
            this.prefix = prefix.replace("§", "&");
        }

        if (suffix != null) {
            this.suffix = suffix.replace("§", "&");
        }

        this.groupColor = getGroupColor(prefix);
    }

    private void saveData(String key, Object value) {
        if (value instanceof String) value = ((String) value).replace("§", "&");
        if (instance.getStorageType() == StorageType.LOCAL) {
            key = key.replace("_", "-");
            groupsData.setAndSave(getFileKey() + key, value);
        } else {
            UpdateStatement updateStatement = new UpdateStatement("subgroups")
                    .addCondition("group", getName())
                    .setValue(key.replace("-", "_"), value);
            if (!updateStatement.execute()) {
                Messages.log("Couldn't save data to database! Error SDB1");
            }
        }
        instance.getGroupHandler().load();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @Nullable
    public String getPrefix(User user, boolean translate) {
        String prefix;
        if (this.groupHandler.handleGenders() && user != null) {
            prefix = this.genderedLayout.getPrefix(user.getGenderType());
            if (prefix == null) prefix = this.prefix;
        } else {
            prefix = this.prefix;
        }
        if (translate) prefix = translate(prefix, user);
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix.replace("§", "&");
        saveData("prefix", this.prefix);
    }

    @Override
    public void setPrefix(@Nullable String prefix, @NotNull Gender gender) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }

        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery select = new SelectQuery("subgroups_gendered", "id")
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            if (select.getData().isEmpty()) {
                InsertStatement insert = new InsertStatement("subgroups_gendered");
                insert
                        .setValue("group", getName())
                        .setValue("gender", gender.getName());
                insert.execute();
            }

            UpdateStatement update = new UpdateStatement("subgroups_gendered");
            update.setValue("prefix", prefix);
            update
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            update.execute();
        } else {
            groupsData.setAndSave(getFileKey() + "genders." + gender.getName() + ".prefix", prefix);
        }
        groupHandler.reloadGroup(this);
    }

    @Override
    @Nullable
    public String getSuffix(User user, boolean translate) {
        String suffix;
        if (this.groupHandler.handleGenders() && user != null) {
            suffix = this.genderedLayout.getSuffix(user.getGenderType());
            if (suffix == null) suffix = this.suffix;
        } else {
            suffix = this.suffix;
        }
        if (translate) suffix = translate(suffix, user);
        return suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix.replace("§", "&");
        saveData("suffix", this.suffix);
    }

    @Override
    public void setSuffix(@Nullable String suffix, @NotNull Gender gender) {
        if (suffix != null) {
            suffix = suffix.replace("§", "&");
        }

        if (instance.getStorageType() == StorageType.SQL) {
            SelectQuery select = new SelectQuery("subgroups_gendered", "id")
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            if (select.getData().isEmpty()) {
                InsertStatement insert = new InsertStatement("subgroups_gendered");
                insert
                        .setValue("group", getName())
                        .setValue("gender", gender.getName());
                insert.execute();
            }

            UpdateStatement update = new UpdateStatement("subgroups_gendered");
            update.setValue("suffix", suffix);
            update
                    .addCondition("group", getName())
                    .addCondition("gender", gender.getName());
            update.execute();
        } else {
            groupsData.setAndSave(getFileKey() + "genders." + gender.getName() + ".suffix", suffix);
        }
        groupHandler.reloadGroup(this);
    }

    @Override
    @NotNull
    public ChatColor getGroupColor() {
        return groupColor;
    }

    @Override
    public String getFileKey() {
        return "subgroups." + getName() + ".";
    }

    @Override
    public void delete() {
        if (instance.getStorageType() == StorageType.LOCAL) {
            groupsData.setAndSave("subgroups." + getName(), null);
        } else {
            DeleteStatement deleteStatement = new DeleteStatement("subgroups").addCondition("group", getName());
            if (!deleteStatement.execute()) {
                Messages.log("§cCouldn't delete subgroup '" + getName() + "'!");
            }
        }
        instance.getGroupHandler().getSubgroups().remove(this);
        instance.getUsers().clear();
    }

}
