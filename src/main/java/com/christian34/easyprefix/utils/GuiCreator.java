package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * Creates the inventory menus with the same frame: page buttons, back button and filler.
 *
 * @author Christian34
 */
public class GuiCreator {

    public static InventoryGui createStatic(Player player, String title, List<String> pattern) {
        List<String> rows = new ArrayList<>();
        rows.add("         ");
        rows.addAll(pattern);
        rows.add("<  pwn  q");

        InventoryGui gui = new InventoryGui(EasyPrefix.getInstance(), player, title, rows.toArray(new String[0]));
        gui.setFiller(new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        gui.addElement(new GuiPageElement('p', new ItemStack(Material.ARROW), GuiPageElement.PageAction.PREVIOUS, Message.BTN_PREVIOUS.getText()));
        gui.addElement(new GuiPageElement('n', new ItemStack(Material.ARROW), GuiPageElement.PageAction.NEXT, Message.BTN_NEXT.getText()));
        gui.setCloseAction(close -> false);
        return gui;
    }

    public static InventoryGui createStatic(Player player, String title, String pattern) {
        return createStatic(player, title, Collections.singletonList(pattern));
    }

    /**
     * @param slots the pattern chars of the buttons (at most 5), e.g. only the ones the player may use
     * @return a pattern row with the buttons centered and a free slot between them
     */
    public static String centeredRow(List<Character> slots) {
        char[] row = " ".repeat(9).toCharArray();
        int start = 4 - (slots.size() - 1);
        for (int i = 0; i < slots.size(); i++) {
            row[start + i * 2] = slots.get(i);
        }
        return new String(row);
    }

    /**
     * adds a back button to the bottom left corner of the gui
     */
    public static void addBackButton(InventoryGui gui, Runnable action) {
        gui.addElement(new StaticGuiElement('<', new ItemStack(Material.SPECTRAL_ARROW), click -> {
            action.run();
            return true;
        }, Message.BTN_BACK.getText()));
    }

}
