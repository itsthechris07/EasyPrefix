package com.christian34.easyprefix.setup.responder.gui.pages;

import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.setup.responder.ChatRespond;
import com.christian34.easyprefix.setup.responder.GuiRespond;
import com.christian34.easyprefix.setup.responder.gui.Page;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiModifyingGroups extends Page {
    private final User user;

    public GuiModifyingGroups(User user) {
        super(user);
        this.user = user;
    }

    public void editPrefix(EasyGroup easyGroup) {
        ChatRespond responder = new ChatRespond(user, Message.SET_PREFIX.toString().replace("%prefix%", easyGroup.getPrefix(null, false)));
        responder.getInput((respond) -> {
            easyGroup.setPrefix(respond);
            user.sendMessage(Message.INPUT_SAVED.toString());
        });
    }

    public void editSuffix(EasyGroup easyGroup) {
        ChatRespond responder = new ChatRespond(user, Message.CHAT_INPUT_SUFFIX.toString().replace("%suffix%", easyGroup.getSuffix(null, false)));
        responder.getInput((respond) -> {
            easyGroup.setSuffix(respond);
            user.sendMessage(Message.INPUT_SAVED.toString());
        });
    }

    public void editJoinMessage(Group group) {
        ChatRespond responder = new ChatRespond(user, "§5What should be the new join message?%newline%§5Current: §7" + group.getJoinMessageText());
        responder.getInput((respond) -> {
            group.setJoinMessage(respond);
            user.sendMessage(Message.INPUT_SAVED.toString());
        });
    }

    public void editQuitMessage(Group group) {
        ChatRespond responder = new ChatRespond(user, "§5What should be the new quit message?%newline%§5Current: §7" + group.getQuitMessageText());
        responder.getInput((respond) -> {
            group.setQuitMessage(respond);
            user.sendMessage(Message.INPUT_SAVED.toString());
        });
    }

    public void editChatColor(EasyGroup easyGroup) {
        if (!(easyGroup instanceof Group)) return;
        Group group = (Group) easyGroup;
        GuiRespond guiRespond = new GuiRespond(user, group.getGroupColor() + group.getName() + " §8» " + Message.SETTINGS_TITLE_FORMATTINGS.toString(), 5);

        int line = 2, slot = 1;
        for (Color color : Color.getValues()) {
            if (line == 3 && slot == 1) slot++;
            ItemStack itemStack = color.toItemStack();
            if (group.getChatColor() != null && group.getChatColor().equals(color) && (group.getChatFormatting() == null || !group.getChatFormatting().equals(ChatFormatting.RAINBOW)))
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);

            guiRespond.addIcon(itemStack, "§r" + Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName(), line, slot).setClickAction(() -> {
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
            List<String> lore = Messages.getList(Message.LORE_SELECT_COLOR);
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                lore.remove(lore.size() - 1);
            }
            ItemStack itemStack = new ItemStack(Material.BOOKSHELF);
            if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                itemStack.addUnsafeEnchantment(Enchantment.LUCK, 1);
            }
            guiRespond.addIcon(itemStack, "§r" + chatFormatting.toString(), line, slot).setLore(lore).setClickAction(() -> {
                ChatFormatting formatting = chatFormatting;
                if (group.getChatFormatting() != null && group.getChatFormatting().equals(chatFormatting)) {
                    if (!group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                        formatting = null;
                    }
                }
                if (formatting != null && !formatting.equals(ChatFormatting.RAINBOW) && group.getChatColor() == null) {
                    return;
                }
                group.setChatFormatting(formatting);
                editChatColor(easyGroup);
            });
            slot++;
        }

        guiRespond.addCloseButton().setClickAction(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.openInventory();
    }

    public void deleteConfirmation(EasyGroup easyGroup) {
        GuiRespond guiRespond = new GuiRespond(user, Message.SETUP_GROUP_TITLE_DELETE.toString().replace("%group%", easyGroup.getName()), 3);
        guiRespond.addIcon(Color.GREEN.toItemStack(), Message.BTN_CONFIRM, 2, 4).setClickAction(() -> {
            easyGroup.delete();
            new GuiSetup(user).groupsList();
        });
        guiRespond.addIcon(Color.RED.toItemStack(), Message.BTN_CANCEL, 2, 6).setClickAction(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.preventClose(true);
        guiRespond.openInventory();
    }

}