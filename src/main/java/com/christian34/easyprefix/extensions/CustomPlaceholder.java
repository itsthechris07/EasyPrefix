package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Debug;
import com.christian34.easyprefix.utils.Message;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class CustomPlaceholder extends PlaceholderExpansion {
    private final ExpansionManager expansionManager;

    public CustomPlaceholder(ExpansionManager expansionManager) {
        this.expansionManager = expansionManager;
        register();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ep";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Christian34";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.6";
    }

    @Override
    public String onRequest(OfflinePlayer op, @NotNull String identifier) {
        if (op == null) return "";
        User user = expansionManager.getInstance().getUser(op.getPlayer());
        switch (identifier) {
            case "user_prefix":
                return user.getPrefix();
            case "user_suffix":
                return user.getSuffix();
            case "user_group":
                return user.getGroup().getName();
            case "user_chatcolor":
                String color = user.getChatColor().getCode();
                if (user.getChatFormatting() != null) {
                    if (user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        color = Message.FORMATTING_RAINBOW.getText();
                    } else {
                        color += user.getChatFormatting().getCode();
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
                Debug.log("§aWarning: You've used an invalid placeholder! (" + this.getIdentifier() + "_" + identifier + ")");
                return null;
        }
    }

}
