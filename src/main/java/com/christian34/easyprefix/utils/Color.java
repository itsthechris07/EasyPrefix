package com.christian34.easyprefix.utils;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public enum Color {
    BLACK("0", Message.COLOR_BLACK.getText(), YMaterial.BLACK_TERRACOTTA),
    DARK_BLUE("1", Message.COLOR_DARK_BLUE.getText(), YMaterial.BLUE_TERRACOTTA),
    DARK_GREEN("2", Message.COLOR_DARK_GREEN.getText(), YMaterial.GREEN_TERRACOTTA),
    DARK_AQUA("3", Message.COLOR_DARK_AQUA.getText(), YMaterial.LIGHT_BLUE_TERRACOTTA),
    DARK_RED("4", Message.COLOR_DARK_RED.getText(), YMaterial.RED_TERRACOTTA),
    DARK_PURPLE("5", Message.COLOR_PURPLE.getText(), YMaterial.PURPLE_TERRACOTTA),
    GOLD("6", Message.COLOR_GOLD.getText(), YMaterial.ORANGE_TERRACOTTA),
    GRAY("7", Message.COLOR_LIGHT_GRAY.getText(), YMaterial.LIGHT_GRAY_TERRACOTTA),
    DARK_GRAY("8", Message.COLOR_GRAY.getText(), YMaterial.GRAY_TERRACOTTA),
    BLUE("9", Message.COLOR_DARK_BLUE.getText(), YMaterial.CYAN_TERRACOTTA),
    GREEN("a", Message.COLOR_DARK_GREEN.getText(), YMaterial.LIME_TERRACOTTA),
    AQUA("b", Message.COLOR_AQUA.getText(), YMaterial.CYAN_TERRACOTTA),
    RED("c", Message.COLOR_RED.getText(), YMaterial.PINK_TERRACOTTA),
    LIGHT_PURPLE("d", Message.COLOR_MAGENTA.getText(), YMaterial.MAGENTA_TERRACOTTA),
    YELLOW("e", Message.COLOR_YELLOW.getText(), YMaterial.YELLOW_TERRACOTTA),
    WHITE("f", Message.COLOR_WHITE.getText(), YMaterial.WHITE_TERRACOTTA),
    UNDEFINED("r", "", YMaterial.BLACK_TERRACOTTA),
    NONE(null, "none", YMaterial.BLACK_TERRACOTTA);

    private final String code;
    private final String displayName;
    private final String name;
    private final ItemStack itemStack;

    Color(String code, @NotNull String name, YMaterial material) {
        this.code = code;
        this.displayName = name;
        this.name = StringUtils.deleteWhitespace(name);
        this.itemStack = material.getItem();
    }

    @NotNull
    public static Color[] getValues() {
        Color[] colors = new Color[values().length - 2];
        int i = 0;
        for (Color color : values()) {
            if (color == UNDEFINED || color == NONE) continue;
            colors[i] = color;
            i++;
        }
        return colors;
    }

    @Nullable
    public static Color getByCode(String code) {
        for (Color color : Color.values()) {
            if (color.code.equals(code)) return color;
        }
        return null;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the translated name
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return getCode() + getDisplayName();
    }

    @NotNull
    public String getCode() {
        return "§" + code;
    }

    @NotNull
    public ItemStack toItemStack() {
        ItemStack item = itemStack.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(toString());
        }
        item.setItemMeta(meta);
        return item;
    }

}
