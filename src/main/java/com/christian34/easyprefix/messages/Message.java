package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.files.MessageData;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Message {
    PREFIX_ALT("info.prefix"),
    DISABLED("§cdisabled"),
    ENABLED("§aenabled"),
    LORE_PERMISSION("§7Permission: &f"),
    LORE_PREFIX("§7Prefix: §f"),
    LORE_SUFFIX("§7Suffix: §f"),
    BTN_BACK("$gui.backBtn"),
    BTN_CHANGE_PREFIX("$gui.changePrefix"),
    BTN_CHANGE_SUFFIX("$gui.changeSuffix"),
    BTN_CUSTOM_PREFIX("$gui.customPrefixBtn"),
    BTN_MY_FORMATTINGS("$gui.myFormattingsBtn"),
    BTN_MY_PREFIXES("$gui.myPrefixesBtn"),
    BTN_RESET("$gui.resetBtn"),
    BTN_SELECT_PREFIX("$gui.selectPrefix"),
    BTN_SUBGROUPS("$gui.subgroupsBtn"),
    CHANGE_GENDER("$gui.changeGender"),
    CHAT_BTN_CONFIRM("$chat.confirmBtn"),
    CHAT_HEADER("$chat.header"),
    CHAT_INPUT_PREFIX("$chat.prefix"),
    CHAT_INPUT_SUFFIX("$chat.suffix"),
    COLOR_AQUA("$colors.aqua"),
    COLOR_BLACK("$colors.black"),
    COLOR_DARK_AQUA("$colors.cyan"),
    COLOR_DARK_BLUE("$colors.blue"),
    COLOR_DARK_GREEN("$colors.green"),
    COLOR_DARK_RED("$colors.darkred"),
    COLOR_GOLD("$colors.gold"),
    COLOR_GRAY("$colors.gray"),
    COLOR_LIGHT_GRAY("$colors.lightgray"),
    COLOR_MAGENTA("$colors.magenta"),
    COLOR_PURPLE("$colors.purple"),
    COLOR_RED("$colors.red"),
    COLOR_WHITE("$colors.white"),
    COLOR_YELLOW("$colors.yellow"),
    FORMATTING_BOLD("$colors.formattings.bold"),
    FORMATTING_ITALIC("$colors.formattings.italic"),
    FORMATTING_RAINBOW("$colors.formattings.rainbow"),
    FORMATTING_STRIKETHROUGH("$colors.formattings.strikethrough"),
    FORMATTING_UNDERLINE("$colors.formattings.underline"),
    GROUP_NOT_FOUND("$info.groupNotFound"),
    INPUT_CANCELLED("$gui.inputCancelled"),
    INPUT_SAVED("$gui.inputSaved"),
    LAYOUT_ERROR("$chat.layoutError"),
    LORE_CHANGE_GENDER("$gui.changeGender2"),
    LORE_GROUP_DETAIL("$gui.currentValue"),
    LORE_SELECT_COLOR("$gui.lore.selectcolor"),
    LORE_SELECT_COLOR_NC("$gui.lore.info_not_compatible"),
    NOTIFY_GENDER_BTN("$info.gender.button"),
    NOTIFY_GENDER_TEXT("$info.gender.text"),
    NO_PERMS("$info.noperms"),
    PAGE_NEXT("$gui.next"),
    PAGE_PREVIOUS("$gui.previous"),
    PLAYER_NOT_FOUND("$info.playerNotFound"),
    PLAYER_ONLY("$info.playerOnly"),
    CHAT_TAGS_HEADER("$tags.chat_header"),
    RESET_PLAYER_PREFIX("$chat.resetPlayerPrefix"),
    RESET_PLAYER_SUFFIX("$chat.resetPlayerSuffix"),
    SETTINGS_TITLE("$gui.settingsTitle"),
    SETTINGS_TITLE_FORMATTINGS("$gui.title.formattings"),
    SETTINGS_TITLE_LAYOUT("$gui.title.layout"),
    SETTINGS_TITLE_MAIN("$gui.title.settings"),
    SETUP_CANCELLED("$info.setupCancelled"),
    SET_PREFIX("$chat.prefix"),
    SUBMIT_PREFIX("$chat.confirmPlayerPrefix"),
    SUBMIT_SUFFIX("$chat.confirmPlayerSuffix"),
    SUCCESS("$info.success"),
    CHAT_TAGS_AVAILABLE("$tags.list_info_available"),
    SUCCESS_PLAYER_PREFIX("$chat.playerPrefix"),
    SUCCESS_PLAYER_SUFFIX("$chat.playerSuffix"),
    TAGS_ITEM_TITLE("$tags.item_title"),
    TAGS_ITEM_LORE("$tags.item_lore"),
    TAGS_INVALID_NAME("$tags.tag_not_found"),
    TAGS_PLAYER_SELECT("$tags.tag_selected"),
    TAG_SET_TO_PLAYER("$tags.set_to_player"),
    TITLE_GENDER("$gui.title.gender");

    private static final String PREFIX = "§7[§5EasyPrefix§7] ";
    private static MessageData messageData;
    private final String message;
    private final boolean isKey;
    private final boolean requiresPrefix;

    Message(String message) {
        this(message, false);
    }

    Message(String message, boolean requiresPrefix) {
        if (message.startsWith("$")) {
            isKey = true;
            this.message = message.substring(1);
        } else {
            isKey = false;
            this.message = message;
        }
        this.requiresPrefix = requiresPrefix;
    }

    public static void setData(MessageData data) {
        messageData = data;
    }

    @NotNull
    public static String getPrefix() {
        return PREFIX;
    }

    @Nullable
    public static String setColors(@Nullable String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Returns the translation with colors, but without prefix.
     *
     * @return the translation with colors
     */
    public String getText() {
        String text = (isKey) ? messageData.getText(message) : message;

        if (text.contains("%prefix%")) {
            text = text.replace("%prefix%", getPrefix());
        } else {
            if (this.requiresPrefix) {
                text = getPrefix() + text;
            }
        }

        text = text.replace("%newline%", "\n");
        return setColors(text);
    }

    /**
     * @return the translation with prefix
     */
    public String getMessage() {
        String text = getText();

        if (text.contains("%prefix%")) {
            text = text.replace("%prefix%", getPrefix());
            return text;
        }
        return getPrefix() + text;
    }

    @NotNull
    public List<String> getList() {
        List<String> list = messageData.getList(getPath());
        for (String line : list) {
            list.add(setColors(line));
        }
        return list;
    }

    public String getPath() {
        return message;
    }

}
