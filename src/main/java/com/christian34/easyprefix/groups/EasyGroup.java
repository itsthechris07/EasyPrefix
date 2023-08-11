package com.christian34.easyprefix.groups;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public abstract class EasyGroup {

    /**
     * @return String returns the name/id
     */
    public abstract String getName();

    public abstract String getPrefix();

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(@Nullable String prefix);

    public abstract String getSuffix();

    /**
     * @param suffix unformatted suffix
     */
    public abstract void setSuffix(@Nullable String suffix);

    /**
     * @return ChatColor returns the automatic generated color
     */
    @NotNull
    public abstract ChatColor getGroupColor();

    /**
     * @return String returns the key for group in FileConfiguration
     */
    public abstract String getFileKey();

    /**
     * deletes the group recursively
     */
    public abstract void delete();

    @NotNull
    public ChatColor getGroupColor(String prefix) {
        ChatColor color = null;
        if (prefix != null && prefix.contains("&") && prefix.length() >= 2) {
            if (!prefix.startsWith("&")) {
                String temp = prefix;
                while (!temp.startsWith("&") && !temp.isEmpty()) {
                    temp = temp.substring(1);
                }
                color = ChatColor.getByChar(temp.substring(1, 2));
            } else {
                color = ChatColor.getByChar(prefix.substring(1, 2));
            }
        }
        return (color == null) ? ChatColor.DARK_PURPLE : color;
    }

}