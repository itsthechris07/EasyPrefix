package com.christian34.easyprefix.setup.responder;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.gui.Icon;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiRespond {
    private final User holder;
    private final Icon nextPage, prevPage;
    private final int guiLines;
    private final int maxSlots;
    private ListenUp LISTENER = new ListenUp();
    private boolean preventClose;
    private Inventory inventory;
    private int page = 1;
    private Icon closeInventoryIcon;
    private ArrayList<GuiPage> pages = new ArrayList<>();

    public GuiRespond(User holder, String title, int lines) {
        this.holder = holder;
        this.guiLines = lines;
        this.inventory = Bukkit.createInventory(holder.getPlayer(), lines * 9, title);
        this.maxSlots = (guiLines - 2) * 9;
        this.nextPage = new Icon(new ItemStack(Material.ARROW), Message.PAGE_NEXT.toString()).setSlot(lines, 6);
        this.prevPage = new Icon(new ItemStack(Material.ARROW), Message.PAGE_PREVIOUS.toString()).setSlot(lines, 4);
        Bukkit.getPluginManager().registerEvents(LISTENER, EasyPrefix.getInstance().getPlugin());
    }

    public void openInventory() {
        addGlassFrame();
        openPage(1);
        this.holder.getPlayer().openInventory(this.inventory);
    }

    public void openPage(int pageId) {
        clearInventory();
        GuiPage page = getPage(pageId);
        int slotItr = 9;
        ItemStack[] contents = inventory.getContents();
        for (Icon icon : page.getIcons()) {
            int slot = (icon.getSlot() == 0) ? slotItr : icon.getSlot();
            contents[slot] = icon.getItemStack();
            slotItr++;
        }
        if (pages.size() > 1) {
            if (pageId < pages.size()) {
                contents[nextPage.getSlot()] = nextPage.getItemStack();
            } else {
                contents[nextPage.getSlot()] = getPlaceholder();
            }
            if (pageId > 1) {
                contents[prevPage.getSlot()] = prevPage.getItemStack();
            } else {
                contents[prevPage.getSlot()] = getPlaceholder();
            }
        }
        inventory.setContents(contents);
        this.page = pageId;
    }

    public Icon addIcon(ItemStack itemStack, String displayName, int line, int slot) {
        Icon icon = new Icon(itemStack, displayName).setSlot(line, slot);
        getPage(1).getIcons().add(icon);
        return icon;
    }

    public Icon addIcon(Material material, String displayName, int line, int slot) {
        return addIcon(new ItemStack(material), displayName, line, slot);
    }

    public Icon addIcon(Material material, Message displayName, int line, int slot) {
        return addIcon(new ItemStack(material), displayName.toString(), line, slot);
    }

    public Icon addIcon(ItemStack itemStack, Message displayName, int line, int slot) {
        return addIcon(itemStack, displayName.toString(), line, slot);
    }

    /**
     * for pages
     *
     * @param itemStack   itemStack
     * @param displayName meta name
     * @return Icon
     */
    public Icon addIcon(ItemStack itemStack, String displayName) {
        Icon icon = new Icon(itemStack, displayName);
        int pageId = 1;

        while (getPage(pageId).getIcons().size() == maxSlots) {
            pageId++;
        }

        GuiPage page = getPage(pageId);
        page.getIcons().add(icon);

        return icon;
    }

    public GuiPage getPage(int page) {
        for (GuiPage p : this.pages) {
            if (p.getPage() == page) return p;
        }
        GuiPage newPage = new GuiPage(page);
        this.pages.add(newPage);
        return newPage;
    }

    public void preventClose(boolean preventClose) {
        this.preventClose = preventClose;
    }

    public Icon addCloseButton() {
        Icon icon = new Icon(Icon.playerHead("MHF_ArrowLeft"), Message.BTN_BACK.toString()).setSlot(guiLines, 1);
        getPage(1).getIcons().add(icon);
        this.closeInventoryIcon = icon;
        return icon;
    }

    private void clearInventory() {
        ItemStack[] contents = inventory.getContents();
        for (int i = 9; i <= (guiLines - 1) * 9 - 1; i++) {
            contents[i] = new ItemStack(Material.AIR);
        }
        inventory.setContents(contents);
    }

    private void unregister() {
        HandlerList.unregisterAll(LISTENER);
        this.inventory = null;
        this.pages = null;
        this.LISTENER = null;
    }

    private void addGlassFrame() {
        ItemStack[] contents = inventory.getContents();
        ItemStack glass = getPlaceholder();

        int[] rows = {1, this.guiLines};
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

    @SuppressWarnings("deprecation")
    private ItemStack getPlaceholder() {
        ItemStack glass;
        if (VersionController.getMinorVersion() < 13) {
            glass = new ItemStack(Material.valueOf("STAINED_GLASS_PANE"), 1, (byte) 15);
        } else {
            glass = new ItemStack(Material.valueOf("GRAY_STAINED_GLASS_PANE"), 1);
        }
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) meta.setDisplayName("§0 ");
        glass.setItemMeta(meta);
        return glass;
    }

    private static class GuiPage {
        private final int page;
        private final ArrayList<Icon> icons = new ArrayList<>();

        public GuiPage(int page) {
            this.page = page;
        }

        public int getPage() {
            return page;
        }

        public ArrayList<Icon> getIcons() {
            return icons;
        }

        public Icon getIcon(ItemStack itemStack) {
            for (Icon icon : icons) {
                String itemName = itemStack.getType().name();
                if (itemName.equals("PLAYER_HEAD") || itemName.equals("SKULL_ITEM")) {
                    SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                    if (meta == null) return null;
                    if (icon.getDisplayName().equals(meta.getDisplayName())) return icon;
                } else {
                    if (icon.getItemStack().equals(itemStack)) {
                        return icon;
                    } else {
                        ItemMeta meta = itemStack.getItemMeta();
                        if (meta == null) return null;
                        if (icon.getDisplayName().equals(meta.getDisplayName())) return icon;
                    }
                }
            }
            return null;
        }

    }

    private class ListenUp implements Listener {

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null || e.getClickedInventory() == null)
                return;
            e.setCancelled(true);

            String displayName = e.getCurrentItem().getItemMeta().getDisplayName();
            Icon clickedIcon = getPage(page).getIcon(e.getCurrentItem());

            if (closeInventoryIcon != null && displayName.equals(closeInventoryIcon.getDisplayName())) {
                if (closeInventoryIcon.getClickAction() == null) {
                    e.getWhoClicked().closeInventory();
                    unregister();
                    return;
                } else {
                    clickedIcon = closeInventoryIcon;
                }
            } else if (displayName.equals(nextPage.getDisplayName())) {
                openPage(page + 1);
                return;
            } else if (displayName.equals(prevPage.getDisplayName())) {
                openPage(page - 1);
                return;
            }

            if (clickedIcon == null || clickedIcon.getClickAction() == null) return;

            try {
                preventClose = false;
                clickedIcon.getClickAction().execute();
            } catch (Exception ex) {
                e.getWhoClicked().closeInventory();
                e.getWhoClicked().sendMessage(Messages.getPrefix() + "§cHey there! This page isn't available. Please try again later!");
                Messages.log("&cAn error occurred while opening gui. If you think this is an error, please report following exception on spigotmc.org;");
                Messages.log("&c------ ERROR ------");
                ex.printStackTrace();
                Messages.log("&c------ END OF ERROR ------");
            }

        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().equals(inventory)) {
                if (preventClose) {
                    Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance().getPlugin(), () -> holder.getPlayer().openInventory(inventory), 1);
                } else {
                    unregister();
                }
            }
        }

    }

}