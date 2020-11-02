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
        guiRespond.addIcon(XMaterial.CHEST.parseItem(), "§5Groups", 2, 3).onClick(this::groupsList);

        guiRespond.addIcon(Material.NETHER_STAR, Message.SETTINGS_TITLE_MAIN.toString(), 2, 5).onClick(this::pluginSettingsGui);

        ItemStack icon = (VersionController.getMinorVersion() <= 12) ? XMaterial.CHEST.parseItem() : XMaterial.WRITABLE_BOOK.parseItem();
        guiRespond.addIcon(icon, Message.BTN_SUBGROUPS.toString(), 2, 7).onClick(this::openSubgroupsList);

        guiRespond.addCloseButton();
        guiRespond.openInventory();
    }

    public void pluginSettingsGui() {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» " + Message.SETTINGS_TITLE_MAIN.toString(), 3);
        ConfigData configData = EasyPrefix.getInstance().getFileManager().getConfig();

        String langName = "§aLanguage §7(§a" + Messages.getLanguage().getName() + "§7)";
        guiRespond.addIcon(XMaterial.OAK_SIGN.parseItem(), langName, 2, 2).setLore("Click me to switch the language", "Available: English, Deutsch, Italiano").onClick(() -> {
            Messages.Language language = Messages.getLanguage();
            Messages.setLanguage(language.getNext());
            pluginSettingsGui();
        });

        boolean useCp = ConfigKeys.CUSTOM_LAYOUT.toBoolean();
        String cpText = "§aCustom Layout §7(" + ((useCp) ? Message.ENABLED.toString() : Message.DISABLED.toString()) + "§7)";
        guiRespond.addIcon(Material.BEACON, cpText, 2, 4).setLore("§5Enable/Disable custom prefixes and suffixes").onClick(() -> {
            configData.set(ConfigKeys.CUSTOM_LAYOUT.getPath(), !useCp);
            EasyPrefix.getInstance().reload();
            pluginSettingsGui();
        });

        boolean useGender = ConfigKeys.USE_GENDER.toBoolean();
        String genderText = "§aGender §7(" + ((useGender) ? Message.ENABLED.toString() : Message.DISABLED.toString()) + "§7)";
        guiRespond.addIcon(Material.CHAINMAIL_HELMET, genderText, 2, 6).setLore("§5Enable/Disable players gender").onClick(() -> {
            boolean use = !useGender;
            configData.set(ConfigKeys.USE_GENDER.getPath(), use);
            EasyPrefix.getInstance().reload();
            pluginSettingsGui();
        });

        boolean useColors = ConfigKeys.HANDLE_COLORS.toBoolean();
        String colorsText = "§aHandle colors §7(" + ((useColors) ? Message.ENABLED.toString() : Message.DISABLED.toString()) + "§7)";
        guiRespond.addIcon(XMaterial.LIME_DYE.parseItem(), colorsText, 2, 8).setLore("§5Translate color and formatting codes").onClick(() -> {
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
        ChatRespond responder = new ChatRespond(user, "§5What should be the name of the new group?");

        responder.addInputReader((answer) -> {
            if (answer.split(" ").length != 1) {
                user.sendMessage("§cPlease enter one word!");
                return false;
            }

            if (groupHandler.isGroup(answer)) {
                user.sendMessage("§cThis group already exists!");
                return false;
            }

            return true;
        });

        responder.getInput((respond) -> {
            if (groupHandler.createGroup(respond)) {
                user.sendMessage("§aGroup has been created!");
            }
        });
    }

    public void groupsList() {
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» §8Groups", 5);
        final String divider = "§7-------------------------------";
        for (Group group : groupHandler.getGroups()) {
            String prefix = group.getPrefix(null, false);
            String suffix = group.getSuffix(null, false);
            ChatColor prefixColor = group.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add(divider);
            if (prefix.length() > 25) {
                lore.add(Message.LORE_PREFIX.toString() + "§7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Message.LORE_PREFIX.toString() + "§7«§f" + prefix + "§7»");
            }
            lore.add(Message.LORE_SUFFIX.toString() + "§7«§f" + suffix + "§7»");

            String groupChatColor = group.getChatColor().getCode();
            if (group.getChatFormatting() != null) {
                if (group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                    groupChatColor += group.getChatFormatting().getCode();
                } else {
                    groupChatColor = Message.FORMATTING_RAINBOW.toString();
                }
            }

            lore.add("§7Color: §f" + groupChatColor.replace("§", "&"));
            lore.add(Message.LORE_PERMISSION.toString() + "EasyPrefix.group." + group.getName());

            guiRespond.addIcon(XMaterial.CHEST.parseItem(), prefixColor + group.getName()).setLore(lore).onClick(() -> openGroupProfile(group));
        }
        guiRespond.addIcon(Material.NETHER_STAR, "§2Add Group", 5, 9).onClick(this::createGroup);

        guiRespond.addCloseButton().onClick(this::mainPage);
        guiRespond.openInventory();
    }

    public void openSubgroupsList() {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» §8Subgroups", 5);
        GroupHandler groupHandler = EasyPrefix.getInstance().getGroupHandler();
        for (final Subgroup subgroup : groupHandler.getSubgroups()) {
            String prefix = subgroup.getPrefix(null, false);
            if (prefix == null) {
                prefix = "-";
            }

            String suffix = subgroup.getSuffix(null, false);
            if (suffix == null) {
                suffix = "-";
            }

            ChatColor prefixColor = subgroup.getGroupColor();
            List<String> lore = new ArrayList<>();
            lore.add("§7-------------------------");
            if (prefix.length() > 25) {
                lore.add(Message.LORE_PREFIX.toString() + "§7«§f" + prefix.substring(0, 25));
                lore.add("§f" + prefix.substring(26) + "§7»");
            } else {
                lore.add(Message.LORE_PREFIX.toString() + "§7«§f" + prefix + "§7»");
            }
            lore.add(Message.LORE_SUFFIX.toString() + "§7«§f" + suffix + "§7»");
            lore.add(Message.LORE_PERMISSION.toString() + "EasyPrefix.subgroup." + subgroup.getName());

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
        prefixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getPrefix(null, false) + "§7»", " "));
        prefixIcon.onClick(() -> this.guiModifyingGroups.editPrefix(group));

        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX, 2, 3);
        suffixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getSuffix(null, false) + "§7»", " "));
        suffixIcon.onClick(() -> this.guiModifyingGroups.editSuffix(group));

        String groupChatColor = group.getChatColor().getCode().replace("§", "&");
        if (group.getChatFormatting() != null) {
            if (group.getChatFormatting().equals(ChatFormatting.RAINBOW)) {
                groupChatColor = ChatFormatting.RAINBOW.toString();
            } else {
                groupChatColor += group.getChatFormatting().getCode().replace("§", "&");
            }
        }

        List<String> loreChatColor = Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + groupChatColor, " ");
        guiRespond.addIcon(XMaterial.LIME_DYE.parseItem(), "§aChange Chatcolor", 2, 4).setLore(loreChatColor).onClick(() -> this.guiModifyingGroups.editChatColor(group));

        ItemStack joinMsgItem = Icon.getCustomPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh" +
                "0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkZDIwYmU5MzUyMDk0OWU2Y2U3ODlkYzRmNDNlZmFlYjI4YzcxN2VlNmJm" +
                "Y2JiZTAyNzgwMTQyZjcxNiJ9fX0=");
        Icon joinMsgIcon = guiRespond.addIcon(joinMsgItem, "§aJoin Message", 2, 6);
        joinMsgIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getJoinMessageText() + "§7»", " "));
        joinMsgIcon.onClick(() -> this.guiModifyingGroups.editJoinMessage(group));

        ItemStack quitMsgItem = Icon.getCustomPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh" +
                "0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ4YTk5ZGIyYzM3ZWM3MWQ3MTk5Y2Q1MjYzOTk4MWE3NTEzY2U5Y2NhOTYy" +
                "NmEzOTM2Zjk2NWIxMzExOTMifX19");
        Icon quitMsgIcon = guiRespond.addIcon(quitMsgItem, "§aQuit Message", 2, 7);
        quitMsgIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + group.getQuitMessageText() + "§7»", " "));
        quitMsgIcon.onClick(() -> this.guiModifyingGroups.editQuitMessage(group));

        Icon genderedLayoutIcon = guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), "Gendered Layout", 2, 9);
        genderedLayoutIcon.onClick(() -> this.guiModifyingGroups.modifyGenderedLayout(group));

        if (!group.getName().equals("default")) {
            guiRespond.addIcon(Material.BARRIER, "§4Delete", 3, 9).onClick(() -> this.guiModifyingGroups.deleteConfirmation(group));
        }

        guiRespond.addCloseButton().onClick(this::groupsList);
        guiRespond.openInventory();
    }

    public void openSubgroupProfile(Subgroup subgroup) {
        GuiRespond guiRespond = new GuiRespond(user, "§5EasyPrefix §8» §7" + subgroup.getGroupColor() + subgroup.getName(), 3);

        Icon prefixIcon = guiRespond.addIcon(Material.IRON_INGOT, Message.BTN_CHANGE_PREFIX.toString(), 2, 3);
        prefixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + subgroup.getPrefix(null, false) + "§7»", " "));
        prefixIcon.onClick(() -> this.guiModifyingGroups.editPrefix(subgroup));

        Icon suffixIcon = guiRespond.addIcon(Material.GOLD_INGOT, Message.BTN_CHANGE_SUFFIX.toString(), 2, 5);
        suffixIcon.setLore(Arrays.asList(this.DIVIDER, Message.LORE_GROUP_DETAIL.toString() + "§7«§f" + subgroup.getSuffix(null, false) + "§7»", " "));
        suffixIcon.onClick(() -> this.guiModifyingGroups.editSuffix(subgroup));

        Icon genderedLayoutIcon = guiRespond.addIcon(Icon.playerHead(user.getPlayer().getName()), "Gendered Layout", 2, 8);
        genderedLayoutIcon.onClick(() -> this.guiModifyingGroups.modifyGenderedLayout(subgroup));

        guiRespond.addCloseButton().onClick(this::openSubgroupsList);
        guiRespond.openInventory();
    }

}
