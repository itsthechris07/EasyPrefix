package com.christian34.easyprefix.placeholderapi;

import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

class CustomPlaceholder extends PlaceholderExpansion {

    static void enable() {
        me.clip.placeholderapi.PlaceholderAPI.registerExpansion(new CustomPlaceholder());
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
        return "1.0.3";
    }

    @Override
    public String onRequest(OfflinePlayer op, String identifier) {
        User user = User.getUser(op.getPlayer());
        switch (identifier) {
            case "user_prefix":
                return PlaceholderAPI.setPlaceholder(user.getPlayer(), user.getPrefix());
            case "user_chatcolor":
                String color;
                if (user.getChatColor() != null) {
                    color = user.getChatColor().getCode();
                    if (user.getChatFormatting() != null) {
                        color += user.getChatFormatting().getCode();
                    }
                } else {
                    if (user.getChatFormatting() != null && user.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        color = Messages.getText(Message.FORMATTING_RAINBOW);
                    } else {
                        color = user.getGroup().getChatColor().getCode();
                        if (user.getGroup().getChatFormatting() != null) {
                            color += user.getGroup().getChatFormatting().getCode();
                        }
                    }
                }
                return color.replace("&", "§");
            case "user_suffix":
                return PlaceholderAPI.setPlaceholder(user.getPlayer(), user.getSuffix());
            case "user_group":
                return user.getGroup().getName();
            case "user_gender":
                if (user.getGender() != null) {
                    return user.getGender().getName();
                }
                return "";
            case "user_subgroup_prefix":
                if (user.getSubgroup() != null && user.getSubgroup().getPrefix(user.getGender()) != null) {
                    return PlaceholderAPI.setPlaceholder(user.getPlayer(), user.getSubgroup().getPrefix(user.getGender()));
                }
                return "";
            case "user_subgroup_suffix":
                if (user.getSubgroup() != null && user.getSubgroup().getSuffix(user.getGender()) != null) {
                    return PlaceholderAPI.setPlaceholder(user.getPlayer(), user.getSubgroup().getSuffix(user.getGender()));
                }
                return "";
        }
        return null;
    }

}