package com.christian34.easyprefix.extensions;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Debug;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
class CustomPlaceholder extends PlaceholderExpansion {
    private final ExpansionManager expansionManager;
    private final EasyPrefix instance;

    public CustomPlaceholder(ExpansionManager expansionManager) {
        this.expansionManager = expansionManager;
        this.instance = EasyPrefix.getInstance();
        register();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "ep";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "Christian34";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.7";
    }

    @Override
    public String onRequest(OfflinePlayer op, @NotNull String identifier) {
        if (op == null) return "";
        User user = instance.getUser(op.getPlayer());
        String text = setPlaceholders(user, identifier);
        if (text != null && text.contains("%ep")) {
            text = expansionManager.setPlaceholders(user.getPlayer(), text);
        }
        return text;
    }

    private String setPlaceholders(User user, String identifier) {
        switch (identifier) {
            case "prefix":
            case "user_prefix":
                return user.getPrefix();
            case "suffix":
            case "user_suffix":
                return user.getSuffix();
            case "user_group":
                return user.getGroup().getName();
            case "user_chatcolor":
                String color = user.getChatColor().getCode();
                ChatFormatting chatFormatting = user.getChatFormatting();
                if (chatFormatting != null && chatFormatting.isBukkit()) {
                    color += user.getChatFormatting().getCode();
                }
                return color.replace("&", "§");
            case "user_gender":
                if (user.getGenderType() != null) {
                    return user.getGenderType().getDisplayName();
                }
                return "";
            case "user_subgroup_prefix":
            case "tag_prefix":
                if (user.getSubgroup() == null) return "";
                return Optional.ofNullable(user.getSubgroup().getPrefix(user.getGenderType())).orElse("");
            case "user_subgroup_suffix":
            case "tag_suffix":
                if (user.getSubgroup() == null) return "";
                return Optional.ofNullable(user.getSubgroup().getSuffix(user.getGenderType())).orElse("");
            default:
                Debug.log("§aWarning: You've used an invalid placeholder! ("
                        + this.getIdentifier() + "_" + identifier + ")");
                return null;
        }
    }

}
