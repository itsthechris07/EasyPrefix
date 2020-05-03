package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;

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
     * @return String returns the unformatted prefix
     */
    public abstract String getRawPrefix();

    /**
     * @return String returns the prefix
     */
    public abstract String getPrefix();

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(String prefix);

    /**
     * @param gender target gender
     * @return String returns the formatted prefix for param gender
     */
    public abstract String getPrefix(Gender gender);

    /**
     * @return String returns the unformatted suffix
     */
    public abstract String getRawSuffix();

    /**
     * @return String returns the formatted suffix
     */
    public abstract String getSuffix();

    /**
     * @param suffix unformatted suffix
     */
    public abstract void setSuffix(String suffix);

    /**
     * @param gender target gender
     * @return String returns the formatted suffix for param gender
     */
    public abstract String getSuffix(Gender gender);

    /**
     * @return ChatColor returns the automatic generated color
     */
    public abstract ChatColor getGroupColor();

    /**
     * @return Color returns chatcolor
     */
    @Nullable
    public abstract Color getChatColor();

    /**
     * @param color target color
     */
    public abstract void setChatColor(Color color);

    /**
     * @return ChatFormatting returns formatting for chat
     */
    @Nullable
    public abstract ChatFormatting getChatFormatting();

    /**
     * @param chatFormatting target formatting
     */
    public abstract void setChatFormatting(ChatFormatting chatFormatting);

    /**
     * @return String returns the key for group in FileConfiguration
     */
    public abstract String getFilePath();

    /**
     * deletes the group recursively
     */
    public abstract void delete();

}
