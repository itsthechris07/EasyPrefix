package com.christian34.easyprefix.setup.responder;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.Button;
import com.christian34.easyprefix.setup.CustomInventory;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.VersionController;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiRespond {
    private final ListenUp LISTENER = new ListenUp();
    private final Consumer<Button> buttonClick;
    private final User holder;
    private final int maxPage;
    private final Button btnNextPage = new Button(new ItemStack(Material.ARROW), Messages.getText(Message.PAGE_NEXT));
    private final Button btnPreviousPage = new Button(new ItemStack(Material.ARROW), Messages.getText(Message.PAGE_PREVIOUS));
    private boolean preventClose;
    private boolean waitingForRespond;
    private Inventory inventory;
    private CustomInventory customInventory;
    private HashMap<String, Button> buttons = new HashMap<>();
    private HashMap<Integer, Collection<Button>> pages = new HashMap<>();
    private int page = 1;

    public GuiRespond(User holder, CustomInventory customInventory, Consumer<Button> consumer) {
        this.holder = holder;
        this.customInventory = customInventory;
        this.inventory = Bukkit.createInventory(holder.getPlayer(), 9 * customInventory.getLines(), customInventory.getTitle());
        this.buttonClick = consumer;
        int count = 1;
        int pageCounter = 1;
        Collection<Button> buttonsPerPage = new ArrayList<>();
        for (Button button : customInventory.getButtons()) {
            if (!(count <= 27)) {
                pages.put(pageCounter, buttonsPerPage);
                buttonsPerPage = new ArrayList<>();
                pageCounter = pageCounter + 1;
                count = 1;
            }
            buttonsPerPage.add(button);
            count++;
        }
        this.pages.put(pageCounter, buttonsPerPage);
        this.maxPage = pages.size();
        holder.getPlayer().openInventory(inventory);
        openPage(1);
        Bukkit.getPluginManager().registerEvents(LISTENER, EasyPrefix.getInstance().getPlugin());
    }

    private void openPage(int page) {
        this.page = page;
        clearInventory();
        for (Button button : this.pages.get(page)) {
            addButton(button, button.getSlot() - (page - 1) * 27);
        }
        showBackButton();
        if (this.page != this.maxPage) {
            addButton(this.btnNextPage, customInventory.getLines() * 9 - 4);
        }
        if (this.page > 1) {
            addButton(this.btnPreviousPage, customInventory.getLines() * 9 - 6);
        }
    }

    public void preventClose(Boolean preventClose) {
        this.preventClose = preventClose;
        if (preventClose) addGlassFrame();
    }

    private void addButton(Button button, int slot) {
        ItemStack[] contents = inventory.getContents();
        contents[slot] = button.getItemStack();
        buttons.put(button.getDisplayName(), button);
        inventory.setContents(contents);
    }

    private void clearInventory() {
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i <= customInventory.getLines() * 9 - 1; i++) {
            contents[i] = new ItemStack(Material.AIR);
        }
        inventory.setContents(contents);
        addGlassFrame();
    }

    private void showBackButton() {
        ItemStack[] contents = inventory.getContents();
        int slot = customInventory.getLines() * 9 - 9;
        Button button = new Button(Button.playerHead("MHF_ArrowLeft"), Messages.getText(Message.BTN_BACK), null);
        contents[slot] = button.getItemStack();
        buttons.put(button.getDisplayName(), button);
        inventory.setContents(contents);
    }

    private void closeInventory() {
        this.buttons = null;
        this.customInventory = null;
        this.inventory = null;
        this.pages = null;
        HandlerList.unregisterAll(LISTENER);
    }

    private void addGlassFrame() {
        ItemStack[] contents = inventory.getContents();
        ItemStack glass;
        if (VersionController.getMinorVersion() < 13) {
            glass = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (byte) 15);
        } else {
            glass = new ItemStack(Material.valueOf("GRAY_STAINED_GLASS_PANE"), 1);
        }

        ItemMeta meta = glass.getItemMeta();
        if (meta != null) meta.setDisplayName("§0 ");
        glass.setItemMeta(meta);

        int[] rows = {1, customInventory.getLines()};
        for (int row : rows) {
            int counter = 0;
            if (row != 1) {
                counter = row * 9 - 9;
            }
            for (int i = counter; i <= counter + 8; i++) {
                contents[i] = glass;
            }
        }
        inventory.setContents(contents);
    }

    private class ListenUp implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getClickedInventory() == null)
                return;
            ItemStack clickedItem = e.getCurrentItem();
            String name = clickedItem.getItemMeta().getDisplayName();
            if (!e.getClickedInventory().equals(inventory)) {
                return;
            }
            if (!buttons.containsKey(name)) {
                e.setCancelled(true);
                return;
            }

            Button clickedButton = buttons.get(name);
            if (clickedButton.equals(btnNextPage)) {
                openPage(page + 1);
            } else if (clickedButton.equals(btnPreviousPage)) {
                openPage(page - 1);
            } else {
                waitingForRespond = true;
                try {
                    buttonClick.accept(buttons.get(name));
                } catch(Exception ex) {
                    e.getWhoClicked().closeInventory();
                    e.getWhoClicked().sendMessage(Messages.getPrefix() + "§cHey there! This page isn't available. Please try again later!");
                    Messages.log("&cAn error occurred while opening gui. If you think this is an error, please report following exception on spigotmc.org;");
                    Messages.log("&c------ ERROR ------");
                    ex.printStackTrace();
                    Messages.log("&c------ END OF ERROR ------");
                }
            }
            e.setCancelled(true);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                if (preventClose && !waitingForRespond) {
                    Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance().getPlugin(), () -> holder.getPlayer().openInventory(inventory), 1);
                } else {
                    closeInventory();
                }
            }
        }

    }

}