package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Color {
    BLACK("0", 15, Message.COLOR_BLACK), DARK_BLUE("1", 11, Message.COLOR_DARK_BLUE), DARK_GREEN("2", 13, Message.COLOR_DARK_GREEN), DARK_AQUA("3", 9, Message.COLOR_DARK_AQUA), DARK_RED("4", 14, Message.COLOR_DARK_RED), DARK_PURPLE("5", 10, Message.COLOR_PURPLE), GOLD("6", 1, Message.COLOR_GOLD), GRAY("7", 8, Message.COLOR_LIGHT_GRAY), DARK_GRAY("8", 7, Message.COLOR_GRAY), BLUE("9", 3, Message.COLOR_DARK_BLUE), GREEN("a", 5, Message.COLOR_DARK_GREEN), AQUA("b", 3, Message.COLOR_AQUA), RED("c", 6, Message.COLOR_RED), LIGHT_PURPLE("d", 2, Message.COLOR_MAGENTA), YELLOW("e", 4, Message.COLOR_YELLOW), WHITE("f", 0, Message.COLOR_WHITE), UNDEFINED("r", 0, null);

    private final String code;
    private final Message name;
    private final int id;

    Color(String code, int id, Message name) {
        this.code = code;
        this.name = name;
        this.id = id;
    }

    public static Color[] getValues() {
        Color[] colors = new Color[values().length - 1];
        int i = 0;
        for (Color color : values()) {
            if (color == UNDEFINED) continue;
            colors[i] = color;
            i++;
        }
        return colors;
    }

    public static Color getByCode(String code) {
        for (Color color : Color.values()) {
            if (color.code.equals(code)) return color;
        }
        return null;
    }

    @Override
    public String toString() {
        return getCode() + Messages.getText(name);
    }

    public String getCode() {
        return "§" + code;
    }

    public Button toTerracotta() {
        if (VersionController.getMinorVersion() >= 13) {
            return new Button(new ItemStack(getNewTerracotta(this), 1), toString());
        } else {
            Field field = Reflection.getField(Objects.requireNonNull(Reflection.getClass("org.bukkit", "Material")), "STAINED_CLAY");
            assert field != null;
            return new Button(new ItemStack(Material.valueOf(field.getName()), 1, getByte()), toString());
        }
    }

    private Material getNewTerracotta(Color color) {
        switch (color) {
            case BLACK:
                return Material.BLACK_TERRACOTTA;
            case YELLOW:
                return Material.YELLOW_TERRACOTTA;
            case BLUE:
                return Material.CYAN_TERRACOTTA;
            case LIGHT_PURPLE:
                return Material.MAGENTA_TERRACOTTA;
            case DARK_PURPLE:
                return Material.PURPLE_TERRACOTTA;
            case GREEN:
                return Material.LIME_TERRACOTTA;
            case DARK_GREEN:
                return Material.GREEN_TERRACOTTA;
            case DARK_BLUE:
                return Material.BLUE_TERRACOTTA;
            case DARK_GRAY:
                return Material.GRAY_TERRACOTTA;
            case GRAY:
                return Material.LIGHT_GRAY_TERRACOTTA;
            case DARK_AQUA:
                return Material.LIGHT_BLUE_TERRACOTTA;
            case AQUA:
                return Material.CYAN_TERRACOTTA;
            case WHITE:
                return Material.WHITE_TERRACOTTA;
            case DARK_RED:
                return Material.RED_TERRACOTTA;
            case GOLD:
                return Material.ORANGE_TERRACOTTA;
            case RED:
                return Material.PINK_TERRACOTTA;
            default:
                return Material.BLACK_TERRACOTTA;
        }
    }

    private byte getByte() {
        return (byte) id;
    }

}