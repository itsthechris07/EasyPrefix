package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public final class RainbowEffect {
    private static List<ChatColor> rainbowColors;

    public static String addRainbowEffect(String text) {
        if (getRainbowColors().size() <= 1) {
            return text;
        }
        String[] letters = text.split("(?<!^)");
        StringBuilder rainbow = new StringBuilder();
        ChatColor last = ChatColor.WHITE;
        for (String letter : letters) {
            ChatColor color = getRandomColor();
            while (color.equals(last)) {
                color = getRandomColor();
            }
            rainbow.append(color).append(letter);
            last = color;
        }
        return rainbow.toString();
    }

    private static ChatColor getRandomColor() {
        List<ChatColor> colors = getRainbowColors();
        return colors.get(new Random().nextInt(colors.size()));
    }

    public static List<ChatColor> getRainbowColors() {
        if (rainbowColors == null || rainbowColors.isEmpty()) {
            rainbowColors = new ArrayList<>();
            ConfigData config = EasyPrefix.getInstance().getFileManager().getConfig();
            for (String color : config.getList(ConfigData.Keys.COLOR_RAINBOW_COLORS)) {
                try {
                    rainbowColors.add(ChatColor.valueOf(color));
                } catch (IllegalArgumentException ignored) {
                    Debug.log("Couldn't find a color with name '" + color + "'!");
                }
            }
        }
        return rainbowColors;
    }

}
