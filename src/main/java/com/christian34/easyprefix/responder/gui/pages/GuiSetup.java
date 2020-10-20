package com.christian34.easyprefix.responder.gui.pages;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.files.ConfigKeys;
import com.christian34.easyprefix.groups.EasyGroup;
import com.christian34.easyprefix.groups.Group;
import com.christian34.easyprefix.groups.GroupHandler;
import com.christian34.easyprefix.groups.Subgroup;
import com.christian34.easyprefix.messages.Message;
import com.christian34.easyprefix.messages.Messages;
import com.christian34.easyprefix.responder.ChatRespond;
import com.christian34.easyprefix.responder.GuiRespond;
import com.christian34.easyprefix.responder.gui.Icon;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.VersionController;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public class GuiSetup {
    private final User user;
    private final String DIVIDER = "§7-------------------------";
    private final GuiModifyingGroups guiModifyingGroups;

    public GuiSetup(User user) {
        this.user = user;
        this.guiModifyingGroups = new GuiModifyingGroups(user);
    }

    public void mainPage() {
        GuiRespond guiRespond = new GuiRespond(user, Message.SETTINGS_TITLE_MAIN.toString(), 3);
        guiRespond.addIcon(XMaterial.CHEST.parseItem(), Message.BTN_GROUPS.toString(), 2, 3).onClick(this::groupsList);

        guiRespond.addIcon(Material.NETHER_STAR, Message.SETTINGS_TITLE_MAIN.toString(), 2, 5).onClick(this::pluginSettingsGui);

        ItemStack icon = (VersionController.getMinorVersion() <= 12) ? XMaterial.CHEST.parseItem() : XMaterial.WRITABLE_BOOK.parseItem();
        guiRespond.addIcon(icon, Message.BTN_SUBGROUPS.toString(), 2, 7).onClick(this::openSubgroupsList);

        guiRespond.addCloseButton();
        guiRespond.openInventory();
    }

    public void pluginSettingsGui() {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» " + Message.SETTINGS_TITLE_MAIN.toString(), 3);
        ConfigData configData = EasyPrefix.getInstance().getFileManager().getConfig();

        String langName = Message.BTN_CHANGE_LANG.toString().replace("%lang%", Messages.langToName());
        guiRespond.addIcon(XMaterial.OAK_SIGN.parseItem(), langName, 2, 2).setLore(Messages.getList(Message.LORE_CHANGE_LANG)).onClick(() -> {
            String crntLang = Messages.getLanguage();
            String nextLang = "en_EN";
            switch (crntLang) {
                case "en_EN":
                    nextLang = "de_DE";
                    break;
                case "de_DE":
                    nextLang = "it_IT";
                    break;
            }
            Messages.setLanguage(nextLang);
            pluginSettingsGui();
        });

        boolean useCp = ConfigKeys.CUSTOM_LAYOUT.toBoolean();
        String cpText = Message.BTN_SWITCH_CP.toString().replace("%active%", (useCp) ? Message.ENABLED.toString() : Message.DISABLED.toString());
        guiRespond.addIcon(Material.BEACON, cpText, 2, 4).setLore(Collections.singletonList(Message.LORE_SWITCH_CP.toString())).onClick(() -> {
            configData.set(ConfigKeys.CUSTOM_LAYOUT.getPath(), !useCp);
            EasyPrefix.getInstance().reload();
            pluginSettingsGui();
        });

        boolean useGender = ConfigKeys.USE_GENDER.toBoolean();
        String genderText = Message.BTN_SWITCH_GENDER.toString().replace("%active%", (useGender) ? Message.ENABLED.toString() : Message.DISABLED.toString());
        guiRespond.addIcon(Material.CHAINMAIL_HELMET, genderText, 2, 6).setLore(Collections.singletonList(Message.LORE_SWITCH_GENDER.toString())).onClick(() -> {
            boolean use = !useGender;
            configData.set(ConfigKeys.USE_GENDER.getPath(), use);
            EasyPrefix.getInstance().reload();
            pluginSettingsGui();
        });

        boolean useColors = ConfigKeys.HANDLE_COLORS.toBoolean();
        String colorsText = Message.BTN_SWITCH_COLOR.toString().replace("%active%", (useColors) ? Message.ENABLED.toString() : Message.DISABLED.toString());
        guiRespond.addIcon(XMaterial.LIME_DYE.parseItem(), colorsText, 2, 8).setLore(Collections.singletonList(Message.LORE_SWITCH_COLOR.toString())).onClick(() -> {
            boolean use = !useColors;
            configData.set(ConfigKeys.HANDLE_COLORS.getPath(), use);
            EasyPrefix.getInstance().reload();
            pluginSettingsGui();
        });

        guiRespond.addCloseButton().onClick(this::mainPage);
        guiRespond.openInventory();
    }

    public void createGroup() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        ChatRespond responder = new ChatRespond(user, Message.CHAT_GROUP.toString());

        responder.addInputReader((answer) -> {
            if (answer.split(" ").length != 1) {
                user.sendMessage("§cPlease enter one word!");
                return false;
            }

            if (groupHandler.isGroup(answer)) {
                user.sendMessage(Message.GROUP_EXISTS.toString());
                return false;
            }

            return true;
        });

        responder.getInput((respond) -> {
            if (groupHandler.createGroup(respond)) {
                user.sendMessage(Message.GROUP_CREATED.toString());
            }
        });
    }

    public void groupsList() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» " + Message.SETUP_GROUPS_TITLE.toString(), 5);
        final String divider = "§7-------------------------------";
        for (Group group : groupHandler.getGroups()) {
            String prefix = group.getPrefix(null, false);
            String suffix = group.getSuffix(null, false);
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(divider);
            if (prefix.length() > 25) {
                lore.add(Messages.getAndSet(Message.LORE_PREFIX, "§7«§f" + prefix.substring(0, 25)));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Messages.getAndSet(Message.LORE_PREFIX, "§7«§f" + prefix + "§7»"));
            }
            lore.add(Messages.getAndSet(Message.LORE_SUFFIX, "§7«§f" + suffix + "§7»"));

            String groupChatColor = (group.getChatColor() != null) ? group.getChatColor().getCode() : "-";
            if (group.getChatColor() != null && group.getChatFormatting() != null) {
                if (!group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    groupChatColor += group.getChatFormatting().getCode();
                }
            } else {
                if (group.getChatFormatting() != null && group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    groupChatColor = Message.FORMATTING_RAINBOW.toString();
                }
            }

            lore.add(Messages.getAndSet(Message.LORE_COLOR, groupChatColor.replace("§", "&")));
            lore.add(Messages.getAndSet(Message.LORE_PERMISSION, "EasyPrefix.group." + group.getName()));

            guiRespond.addIcon(XMaterial.CHEST.parseItem(), prefixColor + group.getName()).setLore(lore).onClick(() -> openGroupProfile(group));
        }
        guiRespond.addIcon(Material.NETHER_STAR, Message.BTN_ADDGROUP, 5, 9).onClick(this::createGroup);

        guiRespond.addCloseButton().onClick(this::mainPage);
        guiRespond.openInventory();
    }

    public void openSubgroupsList() {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» " + Message.TITLE_SUBGROUPS.toString(), 5);
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        for (final Subgroup subgroup : groupHandler.getSubgroups()) {
            String prefix = subgroup.getPrefix(null, false);
            String suffix = subgroup.getSuffix(null, false);
            suffix = (suffix == null) ? "-" : suffix;
            ChatColor prefixColor = subgroup.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add("§7-------------------------");
            if (prefix.length() > 25) {
                lore.add(Message.LORE_PREFIX.toString().replace("%value%", "§7«§f" + prefix.substring(0, 25)));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Message.LORE_PREFIX.toString().replace("%value%", "§7«§f" + prefix + "§7»"));
            }
            lore.add(Message.LORE_SUFFIX.toString().replace("%value%", "§7«§f" + suffix + "§7»"));
            lore.add(Message.LORE_PERMISSION.toString().replace("%value%", "EasyPrefix.subgroup." + subgroup.getName()));

            ItemStack sgBtn = VersionController.getMinorVersion() <= 12
                    ? XMaterial.CHEST.parseItem()
                    : XMaterial.WRITABLE_BOOK.parseItem();
            guiRespond.addIcon(sgBtn, prefixColor + subgroup.getName()).setLore(lore).onClick(() -> openSubgroupProfile(subgroup));
        }

        guiRespond.addCloseButton().onClick(this::mainPage);
        guiRespond.openInventory();
    }

    public void openProfile(EasyGroup easyGroup) {
        if (easyGroup instanceof Group) {
            openGroupProfile((Group) easyGroup);
        } else {
            openSubgroupProfile((Subgroup) easyGroup);
        }
    }

    public void openGroupProfile(Group group) {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» §7" + group.getGroupColor() + group.getName(), 3);
        Icon prefixIcon = guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX, 2, 2);
        prefixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getPrefix(null, false) + "§7»", " ", Message.LORE_EDIT.toString()));
        prefixIcon.onClick(() -> this.guiModifyingGroups.editPrefix(group));

        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX, 2, 3);
        suffixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getSuffix(null, false) + "§7»", " ", Message.LORE_EDIT.toString()));
        suffixIcon.onClick(() -> this.guiModifyingGroups.editSuffix(group));

        String groupChatColor = "-";
        if (group.getChatColor() != null) {
            groupChatColor = group.getChatColor().getCode();
            if (group.getChatFormatting() != null && !group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor += group.getChatFormatting().getCode();
            }
        } else {
            if (group.getChatFormatting() != null && group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor = Message.FORMATTING_RAINBOW.toString();
            }
        }
        List<String> loreChatColor = Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + groupChatColor.replace("§", "&"), " ", Message.LORE_EDIT.toString());
        guiRespond.addIcon(XMaterial.LIME_DYE.parseItem(), Message.BTN_CHANGE_CHATCOLOR, 2, 4).setLore(loreChatColor).onClick(() -> this.guiModifyingGroups.editChatColor(group));

        ItemStack joinMsgItem = Icon.getCustomPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh" +
                "0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJm" +
                "Y2JiZTAyNzgwMTQyZjcxNiJ9fX0=");
        Icon joinMsgIcon = guiRespond.addIcon(joinMsgItem, "§aJoin Message", 2, 6);
        joinMsgIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getJoinMessageText() + "§7»", " ", Message.LORE_EDIT.toString()));
        joinMsgIcon.onClick(() -> this.guiModifyingGroups.editJoinMessage(group));

        ItemStack quitMsgItem = Icon.getCustomPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh" +
                "0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYy" +
                "NmEzOTM2Zjk2NWIxMzExOTMifX19");
        Icon quitMsgIcon = guiRespond.addIcon(quitMsgItem, "§aQuit Message", 2, 7);
        quitMsgIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getQuitMessageText() + "§7»", " ", Message.LORE_EDIT.toString()));
        quitMsgIcon.onClick(() -> this.guiModifyingGroups.editQuitMessage(group));

        Icon genderedLayoutIcon = guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), "Gendered Layout", 2, 9);
        genderedLayoutIcon.onClick(() -> this.guiModifyingGroups.modifyGenderedLayout(group));

        if (!group.getName().equals("default")) {
            guiRespond.addIcon(Material.BARRIER, Message.BTN_DELETE, 3, 9).onClick(() -> this.guiModifyingGroups.deleteConfirmation(group));
        }

        guiRespond.addCloseButton().onClick(this::groupsList);
        guiRespond.openInventory();
    }

    public void openSubgroupProfile(Subgroup subgroup) {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» §7" + subgroup.getGroupColor() + subgroup.getName(), 3);

        Icon prefixIcon = guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX.toString(), 2, 3);
        prefixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + subgroup.getPrefix(null, false) + "§7»", " ", Message.LORE_EDIT.toString()));
        prefixIcon.onClick(() -> this.guiModifyingGroups.editPrefix(subgroup));

        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX.toString(), 2, 5);
        suffixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + subgroup.getSuffix(null, false) + "§7»", " ", Message.LORE_EDIT.toString()));
        suffixIcon.onClick(() -> this.guiModifyingGroups.editSuffix(subgroup));

        Icon genderedLayoutIcon = guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), "Gendered Layout", 2, 8);
        genderedLayoutIcon.onClick(() -> this.guiModifyingGroups.modifyGenderedLayout(subgroup));

        guiRespond.addCloseButton().onClick(this::openSubgroupsList);
        guiRespond.openInventory();
    }

}
