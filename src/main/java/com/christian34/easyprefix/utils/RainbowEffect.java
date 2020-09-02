package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.messages.Messages;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.Random;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class RainbowEffect {
    private static ArrayList<ChatColor> rainbowColors = null;

    public static String addRainbowEffect(String text) {
        String[] letters = text.split("(?<!^)");
        StringBuilder rainbow = new StringBuilder();
        ChatColor last = ChatColor.WHITE;
        for (String letter : letters) {
            ChatColor color = getRandomColor();
            while (color.equals(last)) {
                color = getRandomColor();
            }
            rainbow.append(color.toString()).append(letter);
            last = color;
        }
        return rainbow.toString();
    }

    private static ChatColor getRandomColor() {
        ArrayList<ChatColor> colors = getRainbowColors();
        return colors.get(new Random().nextInt(colors.size()));
    }

    public static ArrayList<ChatColor> getRainbowColors() {
        if (rainbowColors == null || rainbowColors.isEmpty()) {
            ArrayList<ChatColor> enabledColors = new ArrayList<>();
            for (String color : ConfigKeys.COLOR_RAINBOW_COLORS.toStringList()) {
                try {
                    enabledColors.add(ChatColor.valueOf(color));
                } catch (Exception ignored) {
                    Messages.log("Couldn't find color with name '" + color + "'");
                }
            }
            rainbowColors = enabledColors;
        }
        return rainbowColors;
    }

}