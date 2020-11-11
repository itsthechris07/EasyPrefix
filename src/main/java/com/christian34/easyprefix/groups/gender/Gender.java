package com.christian34.easyprefix.groups.gender;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Gender {
    private final String name;
    private String displayName = null;

     public Gender(@NotNull String name) {
         this.name = name.toLowerCase();
         EasyPrefix instance = EasyPrefix.getInstance();
         ConfigData config = instance.getFileManager().getConfig();

         String displayName = config.getData().getString("config.gender.types." + name + ".displayname");
         if (displayName == null) {
             Debug.log("You haven't set a display name for gender '" + name + "'!");
             instance.getGroupHandler().getGenderTypes().remove(this);
             return;
         }
        this.displayName = ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

}