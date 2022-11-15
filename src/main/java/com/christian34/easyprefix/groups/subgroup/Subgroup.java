package com.christian34.easyprefix.groups.subgroup;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.database.DatabaseType;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.GroupHandler;
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

    private final SubgroupData subgroupData;

    private final EasyPrefix instance;
    private final ChatColor groupColor;
    private String prefix, suffix;

    public Subgroup(GroupHandler groupHandler, String name) {
        this.NAME = name;
        this.instance = groupHandler.getInstance();

        if (instance.getDatabaseManager().getDatabaseType().equals(DatabaseType.MYSQL)) {
            this.subgroupData = new SubgroupSqlData(this);
        } else {
            this.subgroupData = new SubgroupFileData(this);
        }

        String prefix = subgroupData.getPrefix();
        if (prefix != null) {
            this.prefix = prefix.replace("§", "&");
        }

        String suffix = subgroupData.getSuffix();
        if (suffix != null) {
            this.suffix = suffix.replace("§", "&");
        }

        this.groupColor = getGroupColor(prefix);
    }

    @Override
    public void setPrefix(@Nullable String prefix) {
        if (prefix != null) {
            prefix = prefix.replace("§", "&");
        }
        this.prefix = prefix;
        this.subgroupData.setPrefix(prefix);
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
    public void setSuffix(@Nullable String suffix) {
        if (suffix != null) {
            suffix = suffix.replace("§", "&");
        }
        this.suffix = suffix;
        this.subgroupData.setSuffix(suffix);
    }

    @Override
    @Nullable
    public String getSuffix() {
        return suffix;
    }

    @Override
    public void delete() {
        this.subgroupData.delete();
        instance.getGroupHandler().getSubgroups().remove(this);
        instance.reloadUsers();
    }

    @Override
    @NotNull
    public ChatColor getGroupColor() {
        return groupColor;
    }

    public SubgroupData getSubgroupData() {
        return subgroupData;
    }

}
