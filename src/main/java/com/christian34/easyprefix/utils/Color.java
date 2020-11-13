package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.messages.Message;
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
    BLACK("0", Message.COLOR_BLACK, XMaterial.BLACK_TERRACOTTA),
    DARK_BLUE("1", Message.COLOR_DARK_BLUE, XMaterial.BLUE_TERRACOTTA),
    DARK_GREEN("2", Message.COLOR_DARK_GREEN, XMaterial.GREEN_TERRACOTTA),
    DARK_AQUA("3", Message.COLOR_DARK_AQUA, XMaterial.LIGHT_BLUE_TERRACOTTA),
    DARK_RED("4", Message.COLOR_DARK_RED, XMaterial.RED_TERRACOTTA),
    DARK_PURPLE("5", Message.COLOR_PURPLE, XMaterial.PURPLE_TERRACOTTA),
    GOLD("6", Message.COLOR_GOLD, XMaterial.ORANGE_TERRACOTTA),
    GRAY("7", Message.COLOR_LIGHT_GRAY, XMaterial.LIGHT_GRAY_TERRACOTTA),
    DARK_GRAY("8", Message.COLOR_GRAY, XMaterial.GRAY_TERRACOTTA),
    BLUE("9", Message.COLOR_DARK_BLUE, XMaterial.CYAN_TERRACOTTA),
    GREEN("a", Message.COLOR_DARK_GREEN, XMaterial.LIME_TERRACOTTA),
    AQUA("b", Message.COLOR_AQUA, XMaterial.CYAN_TERRACOTTA),
    RED("c", Message.COLOR_RED, XMaterial.PINK_TERRACOTTA),
    LIGHT_PURPLE("d", Message.COLOR_MAGENTA, XMaterial.MAGENTA_TERRACOTTA),
    YELLOW("e", Message.COLOR_YELLOW, XMaterial.YELLOW_TERRACOTTA),
    WHITE("f", Message.COLOR_WHITE, XMaterial.WHITE_TERRACOTTA),
    UNDEFINED("r", null, XMaterial.BLACK_TERRACOTTA);

    private final String code;
    private final Message name;
    private final ItemStack itemStack;

    Color(String code, Message name, XMaterial material) {
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

    @Override
    public String toString() {
        return getCode() + name.getText();
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
