package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import org.bukkit.ChatColor;

import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Gender {
    private final String name;
    private final String displayName;

    public Gender(String name) {
        this.name = name.toLowerCase();
        ConfigData config = EasyPrefix.getInstance().getFileManager().getConfig();
        this.displayName = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getData().getString("config.gender.types." + name + ".displayname")));
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

}