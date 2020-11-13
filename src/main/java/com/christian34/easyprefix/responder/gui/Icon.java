package com.christian34.easyprefix.responder.gui;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Icon implements Cloneable {
    private final ItemStack itemStack;
    private final String displayName;
    private ClickAction clickAction;
    private int slot;

    public Icon(ItemStack itemStack, String displayName) {
        this.clickAction = null;
        this.itemStack = itemStack;
        this.displayName = displayName;
        ItemMeta itemMeta = itemStack.getItemMeta();
        Objects.requireNonNull(itemMeta).setDisplayName(displayName);
        itemStack.setItemMeta(itemMeta);
    }

    @SuppressWarnings("deprecation")
    public static ItemStack playerHead(String owningPlayer) {
        ItemStack itemStack = XMaterial.PLAYER_HEAD.parseItem();
        if (itemStack == null) return new ItemStack(Material.AIR);
        try {
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.setOwner(owningPlayer);
            }
            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (Exception ignored) {
            return itemStack;
        }
    }

    public Icon setLore(@NotNull List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
        return this;
    }

    public Icon setLore(@NotNull String... lore) {
        setLore(Arrays.asList(lore));
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ClickAction getClickAction() {
        return clickAction;
    }

    public Icon onClick(ClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public int getSlot() {
        return slot;
    }

    public Icon setSlot(int line, int slot) {
        this.slot = (line - 1) * 9 + slot - 1;
        return this;
    }

    public Icon clone() {
        try {
            return (Icon) super.clone();
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }

}
