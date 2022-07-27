package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.sql.*;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
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
            if (groupsData != null) {
                this.prefix = groupsData.getString(getFileKey() + "prefix");
                this.suffix = groupsData.getString(getFileKey() + "suffix");
            }
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

    @Override
    public void delete() {
        if (instance.getStorageType() == StorageType.LOCAL) {
            groupsData.save("subgroups." + getName(), null);
        } else {
            DeleteStatement deleteStatement = new DeleteStatement("subgroups").addCondition("group", getName());
            if (!deleteStatement.execute()) {
                Debug.log(String.format("§cCouldn't delete subgroup '%s'!", getName()));
            }
        }
        instance.getGroupHandler().getSubgroups().remove(this);
        instance.reloadUsers();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getPrefix(Gender gender) {
        if (this.groupHandler.handleGenders() && gender != null) {
            return this.genderedLayout.getPrefix(gender);
        }
        return getPrefix();
    }

    @Override
    @Nullable
    public GenderedLayout getGenderedLayout() {
        return genderedLayout;
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }
        this.prefix = prefix;
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
            groupsData.save(getFileKey() + "genders." + gender.getName() + ".prefix", prefix);
        }
        groupHandler.reloadGroup(this);
    }

    @Override
    @Nullable
    public String getSuffix() {
        return suffix;
    }

    @Override
    public String getSuffix(Gender gender) {
        if (this.groupHandler.handleGenders() && gender != null) {
            return this.genderedLayout.getSuffix(gender);
        }
        return getSuffix();
    }

    @Override
    public void setSuffix(@Nullable String suffix) {
        if (suffix != null) {
            suffix = suffix.replace("§", "&");
        }
        this.suffix = suffix;
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
            groupsData.save(getFileKey() + "genders." + gender.getName() + ".suffix", suffix);
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

    private void saveData(String key, Object value) {
        Debug.recordAction(String.format("Saving subgroup '%s'", getName()));
        if (value instanceof String) value = ((String) value).replace("§", "&");
        if (instance.getStorageType() == StorageType.LOCAL) {
            key = key.replace("_", "-");
            groupsData.save(getFileKey() + key, value);
        } else {
            UpdateStatement updateStatement = new UpdateStatement("subgroups")
                    .addCondition("group", getName())
                    .setValue(key.replace("-", "_"), value);
            if (!updateStatement.execute()) {
                Debug.log("Couldn't save data to database! Error SDB1");
            }
        }
    }

}
