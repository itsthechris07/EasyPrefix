package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.FileManager;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
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
        Random rand = new Random();
        return colors.get(rand.nextInt(colors.size()));
    }

    public static ArrayList<ChatColor> getRainbowColors() {
        if (rainbowColors == null || rainbowColors.isEmpty()) {
            ArrayList<ChatColor> enabledColors = new ArrayList<>();
            ConfigData configData = FileManager.getConfig();
            List<String> colors = configData.getFileData().getStringList(ConfigData.Values.COLOR_RAINBOW_COLORS.toString());
            for (String color : colors) {
                try {
                    enabledColors.add(ChatColor.valueOf(color));
                } catch(Exception ignored) {
                }
            }
            rainbowColors = enabledColors;
        }
        return rainbowColors;
    }

}