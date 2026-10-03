package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.files.ConfigData;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the items of the color menu.
 *
 * @author Christian34
 */
class ColorIconTest extends PluginTestBase {

    private static Color color(String name) {
        Color color = Color.of(name);
        assertNotNull(color, "color " + name + " is not loaded");
        return color;
    }

    @ParameterizedTest
    @CsvSource({
            "black, BLACK_WOOL", "dark_blue, BLUE_WOOL", "dark_green, GREEN_WOOL", "dark_aqua, CYAN_WOOL",
            "dark_red, RED_WOOL", "dark_purple, PURPLE_WOOL", "gold, ORANGE_WOOL", "gray, LIGHT_GRAY_WOOL",
            "dark_gray, GRAY_WOOL", "blue, BLUE_WOOL", "green, LIME_WOOL", "aqua, LIGHT_BLUE_WOOL",
            "red, RED_WOOL", "light_purple, MAGENTA_WOOL", "yellow, YELLOW_WOOL", "white, WHITE_WOOL"
    })
    void chatColorsMatchWool(String colorName, Material expected) {
        assertEquals(expected, ColorIcon.WOOL.create(color(colorName)).getType());
    }

    @Test
    void usesTheSameDyeForAllKinds() {
        Color red = color("red");
        assertEquals(Material.RED_STAINED_GLASS, ColorIcon.GLASS.create(red).getType());
        assertEquals(Material.RED_DYE, ColorIcon.DYE.create(red).getType());
    }

    @Test
    void customColorsUseClosestDye() {
        // rainbow has no chat color code, so the nearest dye to its hex value is used
        assertNotNull(ColorIcon.WOOL.create(color("rainbow")).getType());
    }

    @Test
    void leatherHasExactColor() {
        Color gold = color("gold");
        ItemStack item = ColorIcon.LEATHER.create(gold);
        assertEquals(Material.LEATHER_HELMET, item.getType());
        DyedItemColor dyed = item.getData(DataComponentTypes.DYED_COLOR);
        assertNotNull(dyed);
        assertEquals(gold.getTextColor().value(), dyed.color().asRGB());
    }

    @Test
    void fireworkStarHasExactColor() {
        Color gold = color("gold");
        ItemStack item = ColorIcon.FIREWORK_STAR.create(gold);
        assertEquals(Material.FIREWORK_STAR, item.getType());
        var explosion = item.getData(DataComponentTypes.FIREWORK_EXPLOSION);
        assertNotNull(explosion);
        assertEquals(gold.getTextColor().value(), explosion.getColors().getFirst().asRGB());
    }

    @ParameterizedTest
    @EnumSource(ColorIcon.class)
    void everyIconWorksForEveryColor(ColorIcon icon) {
        for (Color color : plugin.getColors()) {
            assertNotNull(icon.create(color), icon + " failed for " + color.getName());
        }
    }

    @ParameterizedTest
    @EnumSource(ColorIcon.class)
    void readsIconFromConfig(ColorIcon icon) {
        plugin.getConfigData().save(ConfigData.Keys.COLOR_ICON, icon.getConfigValue());
        assertEquals(icon, ColorIcon.fromConfig());
    }

    @Test
    void invalidConfigFallsBackToBooks() {
        plugin.getConfigData().save(ConfigData.Keys.COLOR_ICON, "diamonds");
        assertEquals(ColorIcon.BOOK, ColorIcon.fromConfig());
    }

}
