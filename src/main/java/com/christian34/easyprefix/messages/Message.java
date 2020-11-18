package com.christian34.easyprefix.messages;

import com.christian34.easyprefix.files.MessageData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Message {
    PREFIX_ALT("prefix_alt"),
    CHAT_NO_PERMS("no_permissions"),
    CHAT_PLAYER_NOT_FOUND("player_not_found"),
    CHAT_GROUP_NOT_FOUND("group_not_found"),
    CHAT_SETUP_CANCELLED("setup_cancelled"),
    CHAT_NOTIFY_GENDER_TEXT("select_gender_join_notification"),
    CHAT_NOTIFY_GENDER_BTN("select_gender_button"),
    BTN_BACK("gui_button_back"),
    BTN_RESET("gui_button_reset"),
    BTN_MY_PREFIXES("gui_button_user_prefixes"),
    BTN_MY_FORMATTINGS("gui_button_user_formattings"),
    BTN_CHANGE_GENDER("gui_button_user_gender"),
    BTN_NEXT("gui_button_next"),
    BTN_PREVIOUS("gui_button_previous"),
    GUI_SETTINGS_TITLE_MAIN("gui_title_settings"),
    GUI_SETTINGS_TITLE_FORMATTINGS("gui_title_formattings"),
    GUI_SETTINGS_TITLE_LAYOUT("gui_title_layout"),
    GUI_SETTINGS_TITLE_GENDER("gui_title_gender"),
    GUI_SETTINGS_TITLE_TAGS("gui_title_tags"),
    GUI_SETTINGS_TITLE("gui_title"),
    BTN_SELECT_PREFIX("gui_button_lore_select_prefix"),
    BTN_SETTINGS_TAGS("gui_button_tags"),
    BTN_SETTINGS_SELECT_TAG("gui_button_lore_select_tag"),
    BTN_CHANGE_PREFIX("gui_button_change_prefix"),
    LORE_CHANGE_PREFIX("gui_button_change_prefix_lore"),
    BTN_CHANGE_SUFFIX("gui_button_change_suffix"),
    LORE_CHANGE_SUFFIX("gui_button_change_suffix_lore"),
    BTN_CUSTOM_LAYOUT("gui_button_custom_layout"),
    BTN_CUSTOM_LAYOUT_LORE("gui_button_custom_layout_lore"),
    CHAT_INPUT_VALUE_RESET("chat_value_reset"),
    INPUT_SAVED("chat_value_save"),
    LORE_SELECT_COLOR("gui_button_lore_select_formatting"),
    LORE_SELECT_COLOR_NC("gui_button_lore_select_formatting_nc"),

    COLOR_AQUA("colors.aqua"),
    COLOR_BLACK("colors.black"),
    COLOR_DARK_AQUA("colors.cyan"),
    COLOR_DARK_BLUE("colors.blue"),
    COLOR_DARK_GREEN("colors.green"),
    COLOR_DARK_RED("colors.dark_red"),
    COLOR_GOLD("colors.gold"),
    COLOR_GRAY("colors.gray"),
    COLOR_LIGHT_GRAY("colors.light_gray"),
    COLOR_MAGENTA("colors.magenta"),
    COLOR_PURPLE("colors.purple"),
    COLOR_RED("colors.red"),
    COLOR_WHITE("colors.white"),
    COLOR_YELLOW("colors.yellow"),
    FORMATTING_BOLD("formattings.bold"),
    FORMATTING_ITALIC("formattings.italic"),
    FORMATTING_RAINBOW("formattings.rainbow"),
    FORMATTING_STRIKETHROUGH("formattings.strikethrough"),
    FORMATTING_UNDERLINE("formattings.underline"),

    CHAT_BTN_CONFIRM("chat_button_confirm"),
    CHAT_INPUT_PREFIX("chat_input_prefix"),
    CHAT_INPUT_PREFIX_CONFIRM("chat_input_prefix_confirm"),
    CHAT_INPUT_PREFIX_RESET("chat_input_prefix_reset"),
    CHAT_INPUT_PREFIX_SAVED("chat_input_prefix_saved"),
    CHAT_INPUT_SUFFIX("chat_input_suffix"),
    CHAT_INPUT_SUFFIX_CONFIRM("chat_input_suffix_confirm"),
    CHAT_INPUT_SUFFIX_RESET("chat_input_suffix_reset"),
    CHAT_INPUT_SUFFIX_SAVED("chat_input_suffix_saved"),
    CHAT_INPUT_LAYOUT("chat_input_layout"),
    CHAT_LAYOUT_UPDATE_COOLDOWN("chat_layout_update_cooldown"),
    CHAT_TAGS_HEADER("chat_cmd_tags_help_head"),
    CHAT_TAGS_AVAILABLE("chat_cmd_tags_list_info"),
    TAGS_ITEM_TITLE("chat_cmd_tags_name"),
    TAGS_ITEM_LORE("chat_cmd_tags_hover"),
    TAGS_INVALID_NAME("chat_cmd_tags_not_found"),
    TAGS_PLAYER_SELECT("chat_cmd_tags_selected"),
    TAG_SET_TO_PLAYER("chat_cmd_tags_set_player"),
    TAGS_CLEARED_FOR_PLAYER("chat_cmd_tags_cleared_player");

    public static final String PREFIX = "§7[§5EasyPrefix§7] ";
    public static final String CHAT_PLAYER_ONLY = "&cYou can''t use this from console!";
    private static MessageData messageData;
    private final String message;


    Message(String message) {
        this.message = message;
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

    @Nullable
    public static String setPlaceholders(@Nullable String text) {
        if (text == null) return null;

        text = setColors(text);

        return text
                .replace("%prefix%", PREFIX_ALT.getText())
                .replace("%newline%", "\n");
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
        if (this == PREFIX_ALT) {
            return setColors(getText(false));
        }
        return getText(true);
    }

    public String getText(boolean setPlaceholders) {
        String text = messageData.getText(message);

        if (text.isEmpty()) {
            Bukkit.broadcastMessage("MESSAGE NULL: " + this.name() + "/" + this.getPath());
        }

        if (!setPlaceholders) {
            return text;
        }
        return setPlaceholders(text);
    }

    @NotNull
    public List<String> getList() {
        List<String> list = new ArrayList<>();
        for (String line : messageData.getList(getPath())) {
            list.add(setPlaceholders(line));
        }
        return list;
    }

    public String getPath() {
        return message;
    }

}
