package com.christian34.easyprefix.responder.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.gender.Gender;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.ChatRespond;
import com.christian34.easyprefix.responder.GuiRespond;
import com.christian34.easyprefix.responder.gui.Icon;
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
public class GuiModifyingGroups {
    private final User user;
    private final EasyPrefix instance;

    public GuiModifyingGroups(User user) {
        this.user = user;
        this.instance = EasyPrefix.getInstance();
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
        String title = group.getGroupColor() + group.getName() + " §8» " + Message.SETTINGS_TITLE_FORMATTINGS.toString();
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
            List<String> lore = Messages.getList(Message.LORE_SELECT_COLOR);
            if (chatFormatting.equals(ChatFormatting.RAINBOW)) {
                lore.remove(lore.size() - 1);
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
        GuiRespond guiRespond = new GuiRespond(user, "Layout", 3);

        List<Gender> genders = instance.getGroupHandler().getGenderTypes();
        for (int i = 0; i < genders.size(); i++) {
            Gender gender = genders.get(i);
            Icon icon = guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), gender.getDisplayName(), 2, (genders.size() == 3 ? 4 : 2) + i);
            icon.onClick(() -> modifyLayout(easyGroup, gender));
        }

        guiRespond.addCloseButton().onClick(() -> new GuiSetup(user).openProfile(easyGroup));
        guiRespond.openInventory();
    }

    private void modifyLayout(EasyGroup easyGroup, Gender gender) {
        GuiRespond guiRespond = new GuiRespond(user, "Layout " + easyGroup.getName() + " " + gender.getName(), 3);

        Icon prefixIcon = guiRespond.addIcon(Material.IRON_INGOT, "prefix", 2, 4);
        prefixIcon.onClick(() -> {
            ChatRespond chatRespond = new ChatRespond(user, "prefix");
            chatRespond.getInput(input -> {
                easyGroup.setPrefix(input, gender);
                user.sendMessage("success");
            });
        });

        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, "suffix", 2, 6);
        suffixIcon.onClick(() -> {
            ChatRespond chatRespond = new ChatRespond(user, "suffix");
            chatRespond.getInput(input -> {
                easyGroup.setSuffix(input, gender);
                user.sendMessage("success");
            });
        });

        guiRespond.addCloseButton().onClick(() -> modifyGenderedLayout(easyGroup));
        guiRespond.openInventory();
    }

    public void deleteConfirmation(EasyGroup easyGroup) {
        GuiRespond guiRespond = new GuiRespond(user, Message.SETUP_GROUP_TITLE_DELETE.toString().replace("%group%", easyGroup.getName()), 3);
        guiRespond.addIcon(Color.GREEN.toItemStack(), Message.BTN_CONFIRM, 2, 4).onClick(() -> {
            easyGroup.delete();
            new GuiSetup(user).groupsList();
        });
        guiRespond.addIcon(Color.RED.toItemStack(), Message.BTN_CANCEL, 2, 6).onClick(() ->
                new GuiSetup(user).openProfile(easyGroup)
        );
        guiRespond.preventClose(true);
        guiRespond.openInventory();
    }

}
