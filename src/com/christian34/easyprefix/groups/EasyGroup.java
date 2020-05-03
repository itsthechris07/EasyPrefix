package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.placeholderapi.PlaceholderAPI;
import com.christian34.easyprefix.user.User;
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
     * @param user
     * @param translate set colors/formattings and placeholders
     * @return String
     */
    public abstract String getPrefix(User user, boolean translate);

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(String prefix);

    /**
     * @param user
     * @param translate
     * @return
     */
    public abstract String getSuffix(User user, boolean translate);

    /**
     * @param suffix unformatted suffix
     */
    public abstract void setSuffix(String suffix);

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

    /**
     * apply colors/formattings and placeholders to prefix or suffix
     *
     * @param text
     * @param user
     * @return String
     */
    public String translate(String text, User user) {
        if (text == null) return null;

        if (user != null) {
            if (!PlaceholderAPI.isEnabled()) {
                String sgPrefix = (user.getSubgroup() != null) ? user.getSubgroup().getPrefix(user, false) : "";
                String sgSuffix = (user.getSubgroup() != null) ? user.getSubgroup().getSuffix(user, false) : "";
                text = text.replace("%ep_user_prefix%", user.getPrefix()).replace("%ep_user_suffix%", user.getSuffix()).replace("%ep_user_group%", user.getGroup().getName()).replace("%ep_user_subgroup_prefix%", sgPrefix).replace("%ep_user_subgroup_suffix%", sgSuffix);
            } else {
                text = PlaceholderAPI.setPlaceholder(user.getPlayer(), text);
            }
            text = text.replace("%player%", user.getPlayer().getDisplayName());
        }

        text = ChatColor.translateAlternateColorCodes('&', text);
        return text;
    }

}
