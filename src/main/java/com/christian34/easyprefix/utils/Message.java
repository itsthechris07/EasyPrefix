package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.MessageData;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public enum Message {
    BTN_BACK("gui_button_back"),
    BTN_CHANGE_PREFIX("gui_button_change_prefix"),
    BTN_CHANGE_SUFFIX("gui_button_change_suffix"),
    BTN_CUSTOM_LAYOUT("gui_button_custom_layout"),
    BTN_CUSTOM_LAYOUT_LORE("gui_button_custom_layout_lore"),
    BTN_MY_FORMATTINGS("gui_button_user_formattings"),
    BTN_MY_PREFIXES("gui_button_user_prefixes"),
    BTN_NEXT("gui_button_next"),
    BTN_PREVIOUS("gui_button_previous"),
    BTN_RESET("gui_button_reset"),
    BTN_SELECT_PREFIX_LORE("gui_button_select_layout_lore"),
    BTN_SETTINGS_SELECT_TAG("gui_button_lore_select_tag"),
    BTN_SETTINGS_TAGS("gui_button_tags"),
    CHAT_BTN_CONFIRM("chat_button_confirm"),
    CHAT_COLOR_HEADER("chat_cmd_color_help_head"),
    CHAT_GROUP_NOT_FOUND("group_not_found"),
    CHAT_INPUT_PREFIX_CONFIRM("chat_input_prefix_confirm"),
    CHAT_INPUT_PREFIX_SAVED("chat_input_prefix_saved"),
    GUI_INPUT_PREFIX("gui_input_prefix"),
    GUI_INPUT_SUFFIX("gui_input_suffix"),
    CHAT_INPUT_SUFFIX_CONFIRM("chat_input_suffix_confirm"),
    CHAT_INPUT_SUFFIX_SAVED("chat_input_suffix_saved"),
    CHAT_LAYOUT_UPDATE_COOLDOWN("chat_layout_update_cooldown"),
    CHAT_NO_PERMS("no_permissions"),
    CHAT_PLAYER_NOT_FOUND("player_not_found"),
    CHAT_TAGS_AVAILABLE("chat_cmd_tags_list_info"),
    CHAT_TAGS_AVAILABLE_OTHERS("chat_cmd_tags_list_info_others"),
    CHAT_TAGS_HEADER("chat_cmd_tags_help_head"),
    CHAT_CMD_HELP_HEAD("chat_cmd_help_head"),
    CHAT_CMD_DESCRIPTION("chat_cmd_description"),
    CHAT_CMD_HELP_QUERY("chat_cmd_help_query"),
    CHAT_CMD_HELP_NEXT("chat_cmd_help_next"),
    CHAT_CMD_HELP_PREVIOUS("chat_cmd_help_previous"),
    CHAT_CMD_HELP_AVAILABLE("chat_cmd_help_available"),
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
    COLOR_NOT_FOUND("chat_cmd_color_not_found"),
    COLOR_PLAYER_SELECT("chat_cmd_color_selected"),
    COLOR_PURPLE("colors.purple"),
    COLOR_RED("colors.red"),
    COLOR_SET_TO_PLAYER("chat_cmd_color_set_player"),
    COLOR_WHITE("colors.white"),
    COLOR_YELLOW("colors.yellow"),
    FORMATTING_BOLD("formattings.bold"),
    FORMATTING_ITALIC("formattings.italic"),
    FORMATTING_RAINBOW("formattings.rainbow"),
    FORMATTING_STRIKETHROUGH("formattings.strikethrough"),
    FORMATTING_UNDERLINE("formattings.underline"),
    GUI_SETTINGS_TITLE("gui_title"),
    GUI_SETTINGS_TITLE_FORMATTINGS("gui_title_formattings"),
    GUI_SETTINGS_TITLE_LAYOUT("gui_title_layout"),
    GUI_SETTINGS_TITLE_MAIN("gui_title_settings"),
    GUI_SETTINGS_TITLE_TAGS("gui_title_tags"),
    INPUT_SAVED("chat_value_save"),
    LORE_CHANGE_PREFIX("gui_button_change_prefix_lore"),
    LORE_CHANGE_SUFFIX("gui_button_change_suffix_lore"),
    PREFIX_ALT("prefix_alt"),
    TAGS_CLEARED_FOR_PLAYER("chat_cmd_tags_cleared_player"),
    TAGS_INVALID_NAME("chat_cmd_tags_not_found"),
    TAGS_ITEM_LORE("chat_cmd_tags_hover"),
    TAGS_ITEM_TITLE("chat_cmd_tags_name"),
    TAGS_PLAYER_SELECT("chat_cmd_tags_selected"),
    TAG_SET_TO_PLAYER("chat_cmd_tags_set_player"),
    CHATLAYOUT_INVALID("chat_input_invalid");

    public static final String PREFIX = "§7[§9EasyPrefix§7] ";
    public static final String CHAT_PLAYER_ONLY = "§cYou can't use this from console!";
    private final MessageData messageData;
    private final String key;

    Message(String key) {
        this.key = key;
        this.messageData = EasyPrefix.getInstance().getFileManager().getMessageData();
    }

    @Nullable
    public static String setColors(@Nullable String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @Contract("!null -> !null; null -> null")
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

    @NotNull
    public String get(@NotNull String varName, @NotNull String value) {
        varName = "%" + varName + "%";
        String text = getText(true);
        if (text == null) return "";
        return text.replace(varName, value);
    }

    public String getText(boolean setPlaceholders) {
        String text = messageData.getText(key);

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
        return key;
    }

}
