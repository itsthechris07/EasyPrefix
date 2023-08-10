package com.christian34.easyprefix.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public enum YMaterial {
    BLACK_TERRACOTTA(15, "STAINED_CLAY"), BLUE_TERRACOTTA(11, "STAINED_CLAY"), CYAN_TERRACOTTA(9, "STAINED_CLAY"), GRAY_TERRACOTTA(7, "STAINED_CLAY"), GREEN_TERRACOTTA(13, "STAINED_CLAY"), LIGHT_BLUE_TERRACOTTA(3, "STAINED_CLAY"), LIGHT_GRAY_TERRACOTTA(8, "STAINED_CLAY"), LIME_TERRACOTTA(5, "STAINED_CLAY"), MAGENTA_TERRACOTTA(2, "STAINED_CLAY"), ORANGE_TERRACOTTA(1, "STAINED_CLAY"), PINK_TERRACOTTA(6, "STAINED_CLAY"), PURPLE_TERRACOTTA(10, "STAINED_CLAY"), RED_TERRACOTTA(14, "STAINED_CLAY"), WHITE_TERRACOTTA(0, "STAINED_CLAY"), YELLOW_TERRACOTTA(4, "STAINED_CLAY"), ARROW(0), GRAY_STAINED_GLASS_PANE(7, "THIN_GLASS", "STAINED_GLASS_PANE"), CHEST(0, "LOCKED_CHEST"), NETHER_STAR(0), BOOK(0), IRON_INGOT(0), GOLD_INGOT(0), LIME_DYE(10, "INK_SACK"), WRITABLE_BOOK(0, "BOOK_AND_QUILL"), BLAZE_ROD(0), STICK(0);

    private static final int VERSION;

    static {
        String version = Bukkit.getVersion();
        Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(version);

        if (matcher.find()) VERSION = Integer.parseInt(matcher.group(1));
        else throw new IllegalArgumentException("Failed to parse server version from: " + version);
    }

    private final byte data;
    @Nullable
    private final Material material;

    YMaterial(int data, @Nonnull String... legacy) {
        this.data = (byte) data;

        Material material;
        if ((material = Material.getMaterial(this.name())) == null) {
            for (int i = legacy.length - 1; i >= 0; i--) {
                material = Material.getMaterial(legacy[i]);
                if (material != null) break;
            }
        }
        this.material = material;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public ItemStack getItem() {
        Material material = this.getMaterial();
        if (material == null) return null;
        return (VERSION >= 13) ? new ItemStack(material) : new ItemStack(material, 1, this.data);
    }

    @Nullable
    public Material getMaterial() {
        return this.material;
    }

}
