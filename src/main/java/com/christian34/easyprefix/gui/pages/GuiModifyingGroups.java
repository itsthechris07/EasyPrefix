package com.christian34.easyprefix.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.groups.gender.GenderedLayout;
import com.christian34.easyprefix.gui.GuiRespond;
import com.christian34.easyprefix.gui.Icon;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.ChatRespond;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiModifyingGroups {
    private final User user;
    private final EasyPrefix instance;

    public GuiModifyingGroups(User user) {
        this.user = user;
        this.instance = EasyPrefix.getInstance();
    }

    public void editPrefix(EasyGroup easyGroup) {
        String text = Message.PREFIX + "§aPlease type the prefix in the chat!%newline%§7Current: §7«§f" + easyGroup.getPrefix(null, false) + "§7»";
        ChatRespond responder = new ChatRespond(user, text);
        responder.getInput((respond) -> {
            easyGroup.setPrefix(respond);
            if (respond == null) {
                user.sendAdminMessage(Message.CHAT_INPUT_VALUE_RESET);
            } else {
                user.sendAdminMessage(Message.INPUT_SAVED);
            }
        });
    }

    public void editSuffix(EasyGroup easyGroup) {
        String text = Message.PREFIX + "§aPlease type the suffix in the chat!%newline%§7Current: §7«§f" + easyGroup.getSuffix(null, false) + "§7»";
        ChatRespond responder = new ChatRespond(user, text);
        responder.getInput((respond) -> {
            easyGroup.setSuffix(respond);
            if (respond == null) {
                user.sendAdminMessage(Message.CHAT_INPUT_VALUE_RESET);
            } else {
                user.sendAdminMessage(Message.INPUT_SAVED);
            }
        });
    }

    public void editJoinMessage(Group group) {
        ChatRespond responder = new ChatRespond(user, Message.PREFIX +
                "§aPlease type in the join message!%newline%§7Current: §7«§f" + group.getJoinMessageText() + "§7»");
        responder.getInput((respond) -> {
            group.setJoinMessage(respond);
            if (respond == null) {
                user.sendAdminMessage(Message.CHAT_INPUT_VALUE_RESET);
            } else {
                user.sendAdminMessage(Message.INPUT_SAVED);
            }
        });
    }

    public void editQuitMessage(Group group) {
        ChatRespond responder = new ChatRespond(user, Message.PREFIX +
                "§aPlease type in the join message!%newline%§7Current: §7«§f" + group.getQuitMessageText() + "§7»");
        responder.getInput((respond) -> {
            group.setQuitMessage(respond);
            if (respond == null) {
                user.sendAdminMessage(Message.CHAT_INPUT_VALUE_RESET);
            } else {
                user.sendAdminMessage(Message.INPUT_SAVED);
            }
        });
    }

    public void editChatColor(EasyGroup easyGroup) {
        if (!(easyGroup instanceof Group)) return;
        Group group = (Group) easyGroup;
        String title = group.getGroupColor() + group.getName() + " §8» " + Message.GUI_SETTINGS_TITLE_FORMATTINGS.getText();
        GuiRespond guiRespond = new GuiRespond(user, title, 5);

        int line = 2, slot = 1;
        for (Color color : Color.getValues()) {
            if (line == 3 && slot == 1) slot++;

            ItemStack itemStack = color.toItemStack();
            if (group.getChatColor().equals(color) && (group.getChatFormatting() == null || !group.getChatFormatting().equals(ChatFormatting.RAINBOW))) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }

            guiRespond.addIcon(itemStack, "§r" + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName(), line, slot).onClick(() -> {
                group.setChatColor(color);
                editChatColor(easyGroup);
            });
            slot++;
            if (slot == 10) {
                slot = 1;
                line++;
            }
        }

        line = 4;
        slot = 3;
        for (ChatFormatting chatFormatting : ChatFormatting.getValues()) {
            List<String> lore = Message.LORE_SELECT_COLOR.getList();
            if (!chatFormatting.equals(ChatFormatting.RAINBOW)) {
                lore.add(Message.LORE_SELECT_COLOR_NC.getText());
            }
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            guiRespond.addIcon(itemStack, "§r" + chatFormatting.toString(), line, slot).setLore(lore).onClick(() -> {
                ChatFormatting formatting = chatFormatting;
                if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                    if (!group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        formatting = null;
                    }
                }
                group.setChatFormatting(formatting);
                editChatColor(easyGroup);
            });
            slot++;
        }

        guiRespond.addCloseButton().onClick(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.openInventory();
    }

    public void modifyGenderedLayout(EasyGroup easyGroup) {
        GuiRespond guiRespond = new GuiRespond(user, "§8Select a gender", 3);
        GenderedLayout genderedLayout = easyGroup.getGenderedLayout();

        List<Gender> genders = instance.getGroupHandler().getGenderTypes();
        for (int i = 0; i < genders.size(); i++) {
            Gender gender = genders.get(i);
            ItemStack head = Icon.playerHead(user.getPlayer().getName());

            String prefix = "-/-";
            String suffix = "-/-";
            if (genderedLayout != null) {
                prefix = genderedLayout.getPrefix(gender);
                suffix = genderedLayout.getSuffix(gender);
            }

            List<String> lore = Arrays.asList(" ", "§7Prefix: §7«§f" + prefix + "§7»", "§7Suffix: §7«§f" + suffix + "§7»");
            guiRespond.addIcon(head, gender.getDisplayName(), 2, (genders.size() == 3 ? 4 : 2) + i)
                    .setLore(lore)
                    .onClick(() -> modifyLayout(easyGroup, gender));
        }

        guiRespond.addCloseButton().onClick(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.openInventory();
    }

    private void modifyLayout(EasyGroup easyGroup, Gender gender) {
        GuiRespond guiRespond = new GuiRespond(user, "§9Group §8» §8" + easyGroup.getName() + " (" + gender.getName() + ")", 3);
        GenderedLayout genderedLayout = easyGroup.getGenderedLayout();

        String prefix = null, suffix = null;
        if (genderedLayout != null) {
            prefix = genderedLayout.getPrefix(gender);
            suffix = genderedLayout.getSuffix(gender);
        }

        if (prefix == null) {
            prefix = "-/-";
        }
        String DIVIDER = "§7-------------------------";
        Icon prefixIcon = guiRespond.addIcon(Material.IRON_INGOT, "§aChange Prefix", 2, 4)
                .setLore(DIVIDER, "§7Current: §7«§f" + prefix + "§7»", " ");
        String finalPrefix = prefix.replace("§", "&");
        prefixIcon.onClick(() -> {
            ChatRespond chatRespond = new ChatRespond(user, "§aPlease write the prefix in the chat! §7Current: \""
                    + finalPrefix + "\"");
            chatRespond.getInput(input -> {
                easyGroup.setPrefix(input, gender);
                user.sendAdminMessage("The prefix for group " + easyGroup.getName() + " (" + gender.getName() + ") has been updated!");
            });
        });

        if (suffix == null) {
            suffix = "-/-";
        }
        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, "§aChange Suffix", 2, 6)
                .setLore(DIVIDER, "§7Current: §7«§f" + suffix + "§7»", " ");
        String finalSuffix = suffix.replace("§", "&");
        suffixIcon.onClick(() -> {
            ChatRespond chatRespond = new ChatRespond(user, "§aPlease write the suffix in the chat! §7Current: \""
                    + finalSuffix + "\"");
            chatRespond.getInput(input -> {
                easyGroup.setSuffix(input, gender);
                user.sendAdminMessage("The prefix for group §b" + easyGroup.getName() + " §7(§b" + gender.getName() + "§7) has been updated!");
            });
        });

        guiRespond.addCloseButton().onClick(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.openInventory();
    }

    public void deleteConfirmation(EasyGroup easyGroup) {
        GuiRespond guiRespond = new GuiRespond(user, "§4Delete " + easyGroup.getName() + "?", 3);
        guiRespond.addIcon(Color.GREEN.toItemStack(), "§aYes", 2, 4).onClick(() -> {
            easyGroup.delete();
            new GuiSetup(user).groupsList();
        });
        guiRespond.addIcon(Color.RED.toItemStack(), "§cNo", 2, 6).onClick(() ->
                new GuiSetup(user).openProfile(easyGroup)
        );
        guiRespond.preventClose(true);
        guiRespond.openInventory();
    }

}
