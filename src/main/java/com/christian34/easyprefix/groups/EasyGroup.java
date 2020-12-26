package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.extensions.ExpansionManager;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.user.User;
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
    public abstract String getPrefix(User user, boolean translate);

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
    public abstract String getSuffix(User user, boolean translate);

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
            String sgPrefix = (user.getSubgroup() != null) ? user.getSubgroup().getPrefix(user, false) : "";
            if (sgPrefix == null) {
                sgPrefix = "";
            }

            String sgSuffix = (user.getSubgroup() != null) ? user.getSubgroup().getSuffix(user, false) : "";
            if (sgSuffix == null) {
                sgSuffix = "";
            }

            text = text
                    .replace("%ep_user_prefix%", user.getGroup().getPrefix(null, false))
                    .replace("%ep_user_suffix%", user.getGroup().getSuffix(null, false))
                    .replace("%ep_user_group%", user.getGroup().getName())
                    .replace("%ep_user_subgroup_prefix%", sgPrefix)
                    .replace("%ep_tag_prefix%", sgPrefix)
                    .replace("%ep_user_subgroup_suffix%", sgSuffix)
                    .replace("%ep_tag_suffix%", sgSuffix);

            ExpansionManager expansionManager = EasyPrefix.getInstance().getExpansionManager();
            if (expansionManager.isUsingPapi()) {
                text = expansionManager.setPapi(user.getPlayer(), text);
            }
            text = text.replace("%player%", user.getPlayer().getDisplayName());
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

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