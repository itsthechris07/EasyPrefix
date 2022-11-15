package com.christian34.easyprefix.groups.subgroup;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.GroupsData;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */


public class SubgroupFileData extends SubgroupData {
    private static final List<String> keys;

    static {
        keys = Arrays.asList("prefix", "suffix");
    }

    private final String subgroupName;

    private final GroupsData groupsData;
    private final Map<String, Object> data;
    private boolean autoSave;

    public SubgroupFileData(Subgroup subgroup) {
        this(subgroup.getName());
    }

    public SubgroupFileData(String subgroupName) {
        EasyPrefix instance = EasyPrefix.getInstance();
        this.groupsData = instance.getFileManager().getGroupsData();
        this.subgroupName = subgroupName;
        this.data = new HashMap<>();
        this.autoSave = true;

        assert groupsData != null;
        FileConfiguration fileData = groupsData.getData();
        for (String key : keys) {
            Object val = fileData.getString("subgroups." + subgroupName + "." + key);
            if (val != null) data.put(key, val);
        }
    }

    @Override
    public String getPrefix() {
        return (String) this.data.get("prefix");
    }

    @Override
    public void setPrefix(String prefix) {
        save("prefix", prefix);
    }

    @Override
    public String getSuffix() {
        return (String) this.data.get("suffix");
    }

    @Override
    public void setSuffix(String suffix) {
        save("suffix", suffix);
    }

    @Override
    public void delete() {
        groupsData.save("subgroups." + subgroupName, null);
    }

    public void save() {
        groupsData.save();
    }

    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    private void save(String key, String value) {
        if (isAutoSave()) {
            groupsData.save("subgroups." + subgroupName + "." + key, value);
        } else {
            groupsData.set("subgroups." + subgroupName + "." + key, value);
        }
    }

}
