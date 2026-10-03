package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * EasyPrefix 2026.
 * <p>
 * Item that represents a chat color in the color menu, configured with 'chat.color-icon' in config.yml.
 *
 * @author Christian34
 */
public enum ColorIcon {
    WOOL("Wool", "closest of the 16 minecraft colors"),
    GLASS("Stained Glass", "closest of the 16 minecraft colors"),
    DYE("Dye", "closest of the 16 minecraft colors"),
    LEATHER("Leather Helmet", "exact color"),
    FIREWORK_STAR("Firework Star", "exact color"),
    BOOK("Book", "no color");

    private final String displayName;
    private final String description;

    ColorIcon(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    @NotNull
    public static ColorIcon fromConfig() {
        String value = EasyPrefix.getInstance().getConfigData().getString(ConfigData.Keys.COLOR_ICON, "dye");
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            Debug.warn(String.format("Unknown color-icon '%s' in config.yml, using books instead!", value));
            return BOOK;
        }
    }

    /**
     * @return the material of the given kind (e.g. RED_WOOL) that matches the color best
     */
    @NotNull
    private static Material dyed(@NotNull Color color, @NotNull String suffix) {
        return Material.valueOf(dyeColor(color).name() + "_" + suffix);
    }

    @NotNull
    private static DyeColor dyeColor(@NotNull Color color) {
        // the 16 chat colors are mapped by hand, as minecraft's dye colors are much darker (red would become orange)
        String code = color.getColorCode();
        if (code != null) {
            switch (code.toLowerCase(Locale.ROOT)) {
                case "0":
                    return DyeColor.BLACK;
                case "1", "9":
                    return DyeColor.BLUE;
                case "2":
                    return DyeColor.GREEN;
                case "3":
                    return DyeColor.CYAN;
                case "4", "c":
                    return DyeColor.RED;
                case "5":
                    return DyeColor.PURPLE;
                case "6":
                    return DyeColor.ORANGE;
                case "7":
                    return DyeColor.LIGHT_GRAY;
                case "8":
                    return DyeColor.GRAY;
                case "a":
                    return DyeColor.LIME;
                case "b":
                    return DyeColor.LIGHT_BLUE;
                case "d":
                    return DyeColor.MAGENTA;
                case "e":
                    return DyeColor.YELLOW;
                case "f":
                    return DyeColor.WHITE;
            }
        }
        // custom colors: closest dye color
        org.bukkit.Color rgb = org.bukkit.Color.fromRGB(color.getTextColor().value());
        return Arrays.stream(DyeColor.values())
                .min(Comparator.comparingDouble(dye -> distance(dye.getColor(), rgb)))
                .orElse(DyeColor.WHITE);
    }

    /**
     * "redmean" approximation of the perceived distance between two colors
     */
    private static double distance(@NotNull org.bukkit.Color a, @NotNull org.bukkit.Color b) {
        double redMean = (a.getRed() + b.getRed()) / 2.0;
        int red = a.getRed() - b.getRed(), green = a.getGreen() - b.getGreen(), blue = a.getBlue() - b.getBlue();
        return (2 + redMean / 256) * red * red + 4 * green * green + (2 + (255 - redMean) / 256) * blue * blue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return the value as it is written to config.yml
     */
    public String getConfigValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    @NotNull
    public ItemStack create(@NotNull Color color) {
        org.bukkit.Color rgb = org.bukkit.Color.fromRGB(color.getTextColor().value());
        return switch (this) {
            case WOOL -> new ItemStack(dyed(color, "WOOL"));
            case GLASS -> new ItemStack(dyed(color, "STAINED_GLASS"));
            case DYE -> new ItemStack(dyed(color, "DYE"));
            case LEATHER -> {
                ItemStack item = new ItemStack(Material.LEATHER_HELMET);
                item.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(rgb));
                item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.DYED_COLOR, DataComponentTypes.ATTRIBUTE_MODIFIERS));
                yield item;
            }
            case FIREWORK_STAR -> {
                ItemStack item = new ItemStack(Material.FIREWORK_STAR);
                item.setData(DataComponentTypes.FIREWORK_EXPLOSION, FireworkEffect.builder().withColor(rgb).build());
                item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.FIREWORK_EXPLOSION));
                yield item;
            }
            case BOOK -> new ItemStack(Material.BOOK);
        };
    }

}
