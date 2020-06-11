package com.christian34.easyprefix.setup.responder.gui;

import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.utils.VersionController;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Icon {
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
    public static ItemStack getCustomPlayerHead(String base, Material alternative) {
        try {
            ItemStack skull;
            if (VersionController.getMinorVersion() > 12) {
                skull = new ItemStack(Material.valueOf("PLAYER_HEAD"), 1);
            } else {
                skull = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
            }
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base));

            Field profileField = Objects.requireNonNull(skullMeta).getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);

            skull.setItemMeta(skullMeta);
            return skull;
        } catch (Exception ignored) {
            return new ItemStack(alternative, 1);
        }
    }

    @SuppressWarnings("deprecation")
    public static ItemStack playerHead(String owningPlayer) {
        ItemStack itemStack;
        if (VersionController.getMinorVersion() >= 13) {
            itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        } else {
            itemStack = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
        }

        try {
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.setOwner(owningPlayer);
            }
            itemStack.setItemMeta(meta);
            return itemStack;
        } catch (Exception ignored) {
            Messages.log("&cWarning: You're using an unsupported version. Please upgrade to Spigot 1.13 or higher!");
            return new ItemStack(Material.BARRIER);
        }
    }

    public Icon setLore(List<String> lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setLore(lore);
        }
        itemStack.setItemMeta(itemMeta);
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

    public int getSlot() {
        return slot;
    }

    public Icon addClickAction(ClickAction clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public Icon setSlot(int line, int slot) {
        this.slot = (line - 1) * 9 + slot - 1;
        return this;
    }

}
