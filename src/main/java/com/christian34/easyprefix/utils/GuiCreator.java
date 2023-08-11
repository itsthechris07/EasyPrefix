package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class GuiCreator {

    public static InventoryGui createStatic(Player player, String title, List<String> pattern) {
        List<String> rows = new ArrayList<>();
        rows.add("         ");
        rows.addAll(pattern);
        rows.add("   pwn  q");

        InventoryGui gui = new InventoryGui(EasyPrefix.getInstance(), player, title, rows.toArray(new String[0]));
        gui.setFiller(YMaterial.GRAY_STAINED_GLASS_PANE.getItem());
        gui.addElement(new GuiPageElement('p', YMaterial.ARROW.getItem(), GuiPageElement.PageAction.PREVIOUS, Message.BTN_PREVIOUS.getText()));
        gui.addElement(new GuiPageElement('n', YMaterial.ARROW.getItem(), GuiPageElement.PageAction.NEXT, Message.BTN_NEXT.getText()));
        gui.setCloseAction(close -> false);
        return gui;
    }

    public static InventoryGui createStatic(Player player, String title, String pattern) {
        return createStatic(player, title, Collections.singletonList(pattern));
    }

}
