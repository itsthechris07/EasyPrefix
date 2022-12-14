package com.christian34.easyprefix.groups.group;

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


public class GroupFileData extends GroupData {
    private static final List<String> keys;

    static {
        keys = Arrays.asList("prefix", "suffix", "chat-color", "chat-formatting", "join-msg", "quit-msg");
    }

    private final GroupsData groupsData;
    private final Map<String, Object> data;
    private final String groupName;

    private boolean autoSave;

    public GroupFileData(Group group) {
        this(group.getName());
    }

    public GroupFileData(String groupName) {
        this.groupName = groupName;
        this.groupsData = EasyPrefix.getInstance().getFileManager().getGroupsData();
        this.data = new HashMap<>();
        this.autoSave = true;

        FileConfiguration fileData = groupsData.getData();
        for (String key : keys) {
            Object val = fileData.getString("groups." + groupName + "." + key);
            if (val != null) data.put(key, val);
        }
    }

    public String getGroupName() {
        return groupName;
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
    public Character getColor() {
        String val = (String) this.data.get("chat-color");
        if (val == null) return null;
        if (val.length() > 1) {
            char c = val.charAt(1);
            setColor(c);
            return c;
        }
        return val.charAt(0);
    }

    @Override
    public void setColor(Character color) {
        String val = (color != null) ? color.toString() : null;
        save("chat-color", val);
    }

    @Override
    public Character getFormatting() {
        String val = (String) this.data.get("chat-formatting");
        if (val == null) return null;
        if (val.length() > 1) {
            char f = val.charAt(1);
            setFormatting(f);
            return f;
        }
        return val.charAt(0);
    }

    @Override
    public void setFormatting(Character formatting) {
        String val = (formatting != null) ? formatting.toString() : null;
        save("chat-formatting", val);
    }

    @Override
    public String getJoinMessage() {
        return (String) this.data.get("join-msg");
    }

    @Override
    public void setJoinMessage(String message) {
        save("join-msg", message);
    }

    @Override
    public String getQuitMessage() {
        return (String) this.data.get("quit-msg");
    }

    @Override
    public void setQuitMessage(String message) {
        save("quit-msg", message);
    }

    @Override
    public void delete() {
        groupsData.save("groups." + this.groupName, null);
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
            groupsData.save("groups." + this.groupName + "." + key, value);
        } else {
            groupsData.set("groups." + this.groupName + "." + key, value);
        }
    }

}
