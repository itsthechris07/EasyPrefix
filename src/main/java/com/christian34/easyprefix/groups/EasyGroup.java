package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.user.User;
import org.bukkit.ChatColor;

import javax.annotation.Nullable;

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
    public abstract String getPrefix(User user, boolean translate);

    /**
     * @param prefix unformatted prefix
     */
    public abstract void setPrefix(String prefix);

    /**
     * @param user      target user
     * @param translate set placeholders and formattings
     * @return suffix
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
     * @param text text to translate
     * @param user user for placeholderapi
     * @return String
     */
    public String translate(@Nullable String text, @Nullable User user) {
        if (text == null) return null;

        if (user != null) {
            ExpansionManager expansionManager = EasyPrefix.getInstance().getExpansionManager();
            if (!expansionManager.isUsingPapi()) {
                String sgPrefix = (user.getSubgroup() != null) ? user.getSubgroup().getPrefix(user, false) : "";
                String sgSuffix = (user.getSubgroup() != null) ? user.getSubgroup().getSuffix(user, false) : "";
                text = text
                        .replace("%ep_user_prefix%", user.getGroup().getPrefix(null, false))
                        .replace("%ep_user_suffix%", user.getGroup().getSuffix(null, false))
                        .replace("%ep_user_group%", user.getGroup().getName())
                        .replace("%ep_user_subgroup_prefix%", sgPrefix)
                        .replace("%ep_user_subgroup_suffix%", sgSuffix);
            } else {
                text = expansionManager.setPapi(user.getPlayer(), text);
            }
            text = text.replace("%player%", user.getPlayer().getDisplayName());
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public ChatColor getGroupColor(String prefix) {
        ChatColor color = null;
        if (prefix != null && prefix.contains("&")) {
            if (!prefix.startsWith("&")) {
                String temp = prefix;
                while (!temp.startsWith("&") && temp.length() > 0) {
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