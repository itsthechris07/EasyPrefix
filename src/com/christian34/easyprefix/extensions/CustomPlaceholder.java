package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class CustomPlaceholder extends PlaceholderExpansion {
    private final ExpansionManager expansionManager;

    public CustomPlaceholder(ExpansionManager expansionManager) {
        this.expansionManager = expansionManager;
    }

    @Override
    public String getIdentifier() {
        return "ep";
    }

    @Override
    public String getAuthor() {
        return "Christian34";
    }

    @Override
    public String getVersion() {
        return "1.0.5";
    }

    @Override
    public String onRequest(OfflinePlayer op, String identifier) {
        User user = expansionManager.getInstance().getUser(op.getPlayer());
        switch (identifier) {
            case "user_prefix":
                return user.getPrefix();
            case "user_suffix":
                return user.getSuffix();
            case "user_group":
                return user.getGroup().getName();
            case "user_chatcolor":
                String color;
                if (user.getChatColor() != null) {
                    color = user.getChatColor().getCode();
                    if (user.getChatFormatting() != null) {
                        color += user.getChatFormatting().getCode();
                    }
                } else {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        color = Message.FORMATTING_RAINBOW.toString();
                    } else {
                        color = user.getGroup().getChatColor().getCode();
                        if (user.getGroup().getChatFormatting() != null) {
                            color += user.getGroup().getChatFormatting().getCode();
                        }
                    }
                }
                return color.replace("&", "§");
            case "user_gender":
                if (user.getGenderType() != null) {
                    return user.getGenderType().getDisplayName();
                }
                return "";
            case "user_subgroup_prefix":
                if (user.getSubgroup() == null) return "";
                String prefix = user.getSubgroup().getPrefix(user, true);
                return (prefix == null) ? "" : prefix;
            case "user_subgroup_suffix":
                if (user.getSubgroup() == null) return "";
                String suffix = user.getSubgroup().getSuffix(user, true);
                return (suffix == null) ? "" : suffix;
            default:
                Messages.log("§aWarning: You've used an invalid placeholder! (" + this.getIdentifier() + "_" + identifier + ")");
                return null;
        }
    }

}