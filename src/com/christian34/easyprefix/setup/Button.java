package com.christian34.easyprefix.setup;

import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.utils.VersionController;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class Button {
    private final ItemStack ITEMSTACK;
    private final HashMap<String, String> DATA = new HashMap<>();
    private int slot;

    public Button(ItemStack itemStack, String displayName, List<String> lore) {
        this.ITEMSTACK = itemStack;
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) meta.setLore(lore);
        }
        this.ITEMSTACK.setItemMeta(meta);
    }

    public Button(Material material, String displayName, List<String> lore) {
        this(new ItemStack(material, 1), displayName, lore);
    }

    public Button(ItemStack itemStack, String displayName) {
        this(itemStack, displayName, null);
    }

    public Button(Material material, String displayName) {
        this(new ItemStack(material, 1), displayName, null);
    }

    public static ItemStack getCustomPlayerHead(String base, Material alternative) {
        try {
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();

            GameProfile profile = new GameProfile(UUID.randomUUID(), "");
            profile.getProperties().put("textures", new Property("textures", base));

            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);

            skull.setItemMeta(skullMeta);
            return skull;
        } catch(Exception ignored) {
            return new ItemStack(alternative, 1);
        }
    }

    public static ItemStack playerHead(String owningPlayer) {
        if (VersionController.getMinorVersion() >= 13) {
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.setOwner(owningPlayer);
            }
            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            try {
                ItemStack itemStack = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short) 3);
                SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                if (meta != null) {
                    meta.setOwner(owningPlayer);
                }
                itemStack.setItemMeta(meta);
                return itemStack;
            } catch(Exception ex) {
                Messages.log("&cWarning: You're using an unsupported version. Please upgrade to Spigot 1.13 or higher!");
                return new ItemStack(Material.BARRIER);
            }
        }
    }

    public Material getMaterial() {
        return this.ITEMSTACK.getType();
    }

    public int getSlot() {
        return slot;
    }

    public Button setSlot(int slot) {
        this.slot = slot;
        return this;
    }

    public Button setSlot(int line, int slot) {
        this.slot = (line - 1) * 9 + slot - 1;
        return this;
    }

    public ItemStack getItemStack() {
        return ITEMSTACK;
    }

    public String getDisplayName() {
        return getItemMeta().getDisplayName();
    }

    public void setDisplayName(String displayName) {
        ItemMeta itemMeta = ITEMSTACK.getItemMeta();
        if (itemMeta != null) itemMeta.setDisplayName(displayName);
        this.ITEMSTACK.setItemMeta(itemMeta);
    }

    public void setData(String key, String value) {
        DATA.put(key, value);
    }

    public String getData(String key) {
        return DATA.get(key);
    }

    public void setLore(List<String> lore) {
        ItemMeta itemMeta = getItemMeta();
        itemMeta.setLore(lore);
        this.ITEMSTACK.setItemMeta(itemMeta);
    }

    private ItemMeta getItemMeta() {
        return this.ITEMSTACK.getItemMeta();
    }

    public Button setLore(String... lines) {
        ArrayList<String> lore = new ArrayList<>();
        for (final String line : lines) {
            if (line.length() > 35) {
                String color = ChatColor.getLastColors(line.substring(0, 35));
                lore.add(line.substring(0, 35));
                lore.add(color + line.substring(35));
            } else {
                lore.add(line);
            }
        }
        ItemMeta itemMeta = getItemMeta();
        itemMeta.setLore(lore);
        this.ITEMSTACK.setItemMeta(itemMeta);
        return this;
    }

    public void addEnchantment() {
        this.ITEMSTACK.addUnsafeEnchantment(Enchantment.LUCK, 1);
    }

}