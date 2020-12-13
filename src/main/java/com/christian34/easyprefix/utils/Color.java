package com.christian34.easyprefix.utils;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Color {
    BLACK("0", Message.COLOR_BLACK.getText(), XMaterial.BLACK_TERRACOTTA),
    DARK_BLUE("1", Message.COLOR_DARK_BLUE.getText(), XMaterial.BLUE_TERRACOTTA),
    DARK_GREEN("2", Message.COLOR_DARK_GREEN.getText(), XMaterial.GREEN_TERRACOTTA),
    DARK_AQUA("3", Message.COLOR_DARK_AQUA.getText(), XMaterial.LIGHT_BLUE_TERRACOTTA),
    DARK_RED("4", Message.COLOR_DARK_RED.getText(), XMaterial.RED_TERRACOTTA),
    DARK_PURPLE("5", Message.COLOR_PURPLE.getText(), XMaterial.PURPLE_TERRACOTTA),
    GOLD("6", Message.COLOR_GOLD.getText(), XMaterial.ORANGE_TERRACOTTA),
    GRAY("7", Message.COLOR_LIGHT_GRAY.getText(), XMaterial.LIGHT_GRAY_TERRACOTTA),
    DARK_GRAY("8", Message.COLOR_GRAY.getText(), XMaterial.GRAY_TERRACOTTA),
    BLUE("9", Message.COLOR_DARK_BLUE.getText(), XMaterial.CYAN_TERRACOTTA),
    GREEN("a", Message.COLOR_DARK_GREEN.getText(), XMaterial.LIME_TERRACOTTA),
    AQUA("b", Message.COLOR_AQUA.getText(), XMaterial.CYAN_TERRACOTTA),
    RED("c", Message.COLOR_RED.getText(), XMaterial.PINK_TERRACOTTA),
    LIGHT_PURPLE("d", Message.COLOR_MAGENTA.getText(), XMaterial.MAGENTA_TERRACOTTA),
    YELLOW("e", Message.COLOR_YELLOW.getText(), XMaterial.YELLOW_TERRACOTTA),
    WHITE("f", Message.COLOR_WHITE.getText(), XMaterial.WHITE_TERRACOTTA),
    UNDEFINED("r", null, XMaterial.BLACK_TERRACOTTA);

    private final String code;
    private final String name;
    private final ItemStack itemStack;

    Color(String code, String name, XMaterial material) {
        this.code = code;
        this.name = name;
        this.itemStack = material.parseItem();
    }

    @NotNull
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

    @Nullable
    public static Color getByCode(String code) {
        for (Color color : Color.values()) {
            if (color.code.equals(code)) return color;
        }
        return null;
    }

    /**
     * @return the translated name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getCode() + getName();
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
