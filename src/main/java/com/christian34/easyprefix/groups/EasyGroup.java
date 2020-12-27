package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public abstract class EasyGroup {

    /**
     * @return String returns the name/id
     */
    public abstract String getName();

    /**
     * @param user      target user
     * @param translate set colors/formattings and placeholders
     * @return String
     */
    public abstract String getPrefix();

    public abstract String getPrefix(Gender gender);

    @Nullable
    public abstract GenderedLayout getGenderedLayout();

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(@Nullable String prefix);

    public abstract void setPrefix(@Nullable String prefix, @NotNull Gender gender);

    /**
     * @param user      target user
     * @param translate set placeholders and formattings
     * @return suffix
     */
    public abstract String getSuffix();

    public abstract String getSuffix(Gender gender);

    /**
     * @param suffix unformatted suffix
     */
    public abstract void setSuffix(@Nullable String suffix);

    public abstract void setSuffix(@Nullable String suffix, @NotNull Gender gender);

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
        if (prefix != null && prefix.contains("&")) {
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