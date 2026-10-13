package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.MessageData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * EasyPrefix 2026.
 * <p>
 * All texts of messages.yml (MiniMessage), with the placeholders %prefix% and %newline% replaced.
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
    BTN_MY_FORMATTINGS_LORE("gui_button_user_formattings_lore"),
    BTN_MY_PREFIXES("gui_button_user_prefixes"),
    BTN_MY_PREFIXES_LORE("gui_button_user_prefixes_lore"),
    BTN_USER_INFO("gui_button_user_info"),
    BTN_USER_INFO_LORE("gui_button_user_info_lore"),
    BTN_PREVIEW("gui_button_preview"),
    BTN_PREVIEW_LORE("gui_button_preview_lore"),
    BTN_RESET_LORE("gui_button_reset_lore"),
    BTN_PLUGIN_SETTINGS("gui_button_plugin_settings"),
    BTN_PLUGIN_SETTINGS_LORE("gui_button_plugin_settings_lore"),
    GUI_VALUE_NONE("gui_value_none"),
    BTN_NEXT("gui_button_next"),
    BTN_PREVIOUS("gui_button_previous"),
    BTN_RESET("gui_button_reset"),
    BTN_SELECT_PREFIX_LORE("gui_button_select_layout_lore"),
    BTN_SETTINGS_SELECT_TAG("gui_button_lore_select_tag"),
    BTN_SETTINGS_TAGS("gui_button_tags"),
    BTN_MENTIONS("gui_button_mentions"),
    BTN_MENTIONS_LORE("gui_button_mentions_lore"),
    GUI_VALUE_ENABLED("gui_value_enabled"),
    GUI_VALUE_DISABLED("gui_value_disabled"),
    MENTIONS_ENABLED("chat_mentions_enabled"),
    MENTIONS_DISABLED("chat_mentions_disabled"),
    MENTIONS_DISABLED_GLOBALLY("chat_mentions_disabled_globally"),
    CHAT_BTN_CONFIRM("chat_button_confirm"),
    CHAT_COLOR_HEADER("chat_cmd_color_help_head"),
    CHAT_GROUP_NOT_FOUND("group_not_found"),
    CHAT_INPUT_PREFIX_CONFIRM("chat_input_prefix_confirm"),
    CHAT_INPUT_PREFIX_SAVED("chat_input_prefix_saved"),
    GUI_INPUT_PREFIX("gui_input_prefix"),
    GUI_INPUT_SUFFIX("gui_input_suffix"),
    CHAT_INPUT_SUFFIX_CONFIRM("chat_input_suffix_confirm"),
    CHAT_INPUT_SUFFIX_SAVED("chat_input_suffix_saved"),
    CHAT_INPUT_CANCELLED("chat_input_cancelled"),
    CHAT_INPUT_TIMEOUT("chat_input_timeout"),
    CHAT_INPUT_QUIT_HINT("chat_input_quit_hint"),
    CHAT_INPUT_CURRENT("chat_input_current"),
    DIALOG_PREVIEW("dialog_input_preview"),
    DIALOG_BTN_SAVE("dialog_button_save"),
    DIALOG_BTN_PREVIEW("dialog_button_preview"),
    DIALOG_BTN_CANCEL("dialog_button_cancel"),
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
    COLOR_RESET("chat_cmd_color_reset"),
    COLOR_RESET_PLAYER("chat_cmd_color_reset_player"),
    COLOR_PREVIEW("chat_cmd_color_preview"),
    COLOR_PREVIEW_TEXT("chat_cmd_color_preview_text"),
    COLOR_WHITE("colors.white"),
    COLOR_YELLOW("colors.yellow"),
    FORMATTING_NOT_FOUND("chat_cmd_formatting_not_found"),
    FORMATTING_SELECTED("chat_cmd_formatting_selected"),
    FORMATTING_SET_TO_PLAYER("chat_cmd_formatting_set_player"),
    FORMATTING_REMOVED("chat_cmd_formatting_removed"),
    FORMATTING_REMOVED_PLAYER("chat_cmd_formatting_removed_player"),
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
    CHATLAYOUT_INVALID("chat_input_invalid"),

    // admin commands
    CMD_INFO("chat_cmd_info"),
    CMD_INVALID_SYNTAX("chat_cmd_invalid_syntax"),
    CMD_PLAYER_ONLY("chat_cmd_player_only"),
    CMD_INVALID_SENDER("chat_cmd_invalid_sender"),
    CMD_RELOADED("chat_cmd_reloaded"),
    CMD_GROUP_INFO("chat_cmd_group_info"),
    CMD_GROUP_PREFIX_SET("chat_cmd_group_prefix_set"),
    CMD_GROUP_SUFFIX_SET("chat_cmd_group_suffix_set"),
    CMD_GROUP_PRIORITY_SET("chat_cmd_group_priority_set"),
    CMD_USER_INFO("chat_cmd_user_info"),
    CMD_USER_INFO_CUSTOM("chat_cmd_user_info_custom"),
    CMD_USER_UPDATED("chat_cmd_user_updated"),
    CMD_USER_PREFIX_SET("chat_cmd_user_prefix_set"),
    CMD_USER_SUFFIX_SET("chat_cmd_user_suffix_set"),
    CMD_DOWNLOAD_START("chat_cmd_database_download_start"),
    CMD_DOWNLOAD_FAILED("chat_cmd_database_download_failed"),
    CMD_DOWNLOAD_DONE("chat_cmd_database_download_done"),
    CMD_DOWNLOAD_HINT("chat_cmd_database_download_hint"),
    CMD_DOWNLOAD_REQUIRES_SQL("chat_cmd_database_download_requires_sql"),
    CMD_UPLOAD_START("chat_cmd_database_upload_start"),
    CMD_UPLOAD_FAILED("chat_cmd_database_upload_failed"),
    CMD_UPLOAD_DONE("chat_cmd_database_upload_done"),
    CMD_UPLOAD_REQUIRES_SQL("chat_cmd_database_upload_requires_sql"),

    // setup menus
    SETUP_TITLE("setup_title"),
    SETUP_TITLE_GROUPS("setup_title_groups"),
    SETUP_TITLE_TAGS("setup_title_tags"),
    SETUP_TITLE_SETTINGS("setup_title_settings"),
    SETUP_TITLE_COLOR_ICONS("setup_title_color_icons"),
    SETUP_TITLE_BLACKLIST("setup_title_blacklist"),
    SETUP_TITLE_GROUP("setup_title_group"),
    SETUP_TITLE_TAG("setup_title_tag"),
    SETUP_TITLE_DELETE("setup_title_delete"),
    SETUP_BTN_GROUPS("setup_button_groups"),
    SETUP_BTN_SETTINGS("setup_button_settings"),
    SETUP_BTN_TAGS("setup_button_tags"),
    SETUP_BTN_ADD_GROUP("setup_button_add_group"),
    SETUP_BTN_ADD_TAG("setup_button_add_tag"),
    SETUP_BTN_ADD_WORD("setup_button_add_word"),
    SETUP_BTN_DELETE("setup_button_delete"),
    SETUP_BTN_YES("setup_button_yes"),
    SETUP_BTN_NO("setup_button_no"),
    SETUP_GROUP("setup_group"),
    SETUP_GROUP_PREVIEW_TEXT("setup_group_preview_text"),
    SETUP_TAG("setup_tag"),
    SETUP_BTN_PREFIX("setup_button_prefix"),
    SETUP_BTN_SUFFIX("setup_button_suffix"),
    SETUP_BTN_COLOR("setup_button_color"),
    SETUP_BTN_JOIN_MESSAGE("setup_button_join_message"),
    SETUP_BTN_QUIT_MESSAGE("setup_button_quit_message"),
    SETUP_BTN_PRIORITY("setup_button_priority"),
    SETUP_BTN_HOVER("setup_button_hover"),
    SETUP_HOVER_CURRENT("setup_hover_current"),
    SETUP_HOVER_CURRENT_BUILTIN("setup_hover_current_builtin"),
    SETUP_HOVER_CURRENT_DEFAULT("setup_hover_current_default"),
    SETUP_HOVER_LINE("setup_hover_line"),
    SETUP_HOVER_MORE("setup_hover_more"),
    SETUP_HOVER_RESET_BUILTIN("setup_hover_reset_builtin"),
    SETUP_HOVER_RESET_DEFAULT("setup_hover_reset_default"),
    SETUP_COLOR_PERMISSION("setup_color_permission"),
    SETUP_INPUT_PREFIX("setup_input_prefix"),
    SETUP_INPUT_SUFFIX("setup_input_suffix"),
    SETUP_INPUT_JOIN_MESSAGE("setup_input_join_message"),
    SETUP_INPUT_QUIT_MESSAGE("setup_input_quit_message"),
    SETUP_INPUT_HOVER_LINE("setup_input_hover_line"),
    SETUP_INPUT_HOVER_LINES("setup_input_hover_lines"),
    SETUP_INPUT_GROUP_NAME("setup_input_group_name"),
    SETUP_INPUT_TAG_NAME("setup_input_tag_name"),
    SETUP_INPUT_BLACKLIST("setup_input_blacklist"),
    SETUP_INPUT_ALIAS("setup_input_alias"),
    SETUP_GROUP_CREATED("setup_group_created"),
    SETUP_GROUP_NOT_CREATED("setup_group_not_created"),
    SETUP_TAG_CREATED("setup_tag_created"),
    SETUP_TAG_NOT_CREATED("setup_tag_not_created"),
    SETUP_ALIAS_CHANGED("setup_alias_changed"),
    SETUP_TOGGLE_HANDLE_CHAT("setup_toggle_handle_chat"),
    SETUP_TOGGLE_TAGS("setup_toggle_tags"),
    SETUP_TOGGLE_JOIN_QUIT("setup_toggle_join_quit"),
    SETUP_TOGGLE_HIDE_JOIN_QUIT("setup_toggle_hide_join_quit"),
    SETUP_TOGGLE_TAB_LIST("setup_toggle_tab_list"),
    SETUP_TOGGLE_SORT_TAB_LIST("setup_toggle_sort_tab_list"),
    SETUP_TOGGLE_NAME_TAGS("setup_toggle_name_tags"),
    SETUP_TOGGLE_MENTIONS("setup_toggle_mentions"),
    SETUP_TOGGLE_CUSTOM_LAYOUT("setup_toggle_custom_layout"),
    SETUP_TOGGLE_ENABLED("setup_toggle_enabled"),
    SETUP_TOGGLE_DISABLED("setup_toggle_disabled"),
    SETUP_BTN_COLOR_ICONS("setup_button_color_icons"),
    SETUP_COLOR_ICON("setup_color_icon"),
    SETUP_COLOR_ICON_SELECTED("setup_color_icon_selected"),
    SETUP_COLOR_ICON_SELECT("setup_color_icon_select"),
    SETUP_COLOR_ICON_PREVIEW("setup_color_icon_preview"),
    SETUP_BTN_COOLDOWN("setup_button_cooldown"),
    SETUP_COOLDOWN_NONE("setup_cooldown_none"),
    SETUP_COOLDOWN_MINUTES("setup_cooldown_minutes"),
    SETUP_COOLDOWN_HOURS("setup_cooldown_hours"),
    SETUP_BTN_BLACKLIST("setup_button_blacklist"),
    SETUP_BLACKLIST_WORD("setup_blacklist_word"),
    SETUP_BLACKLIST_MORE("setup_blacklist_more"),
    SETUP_BLACKLIST_EMPTY("setup_blacklist_empty"),
    SETUP_BLACKLIST_ENTRY("setup_blacklist_entry"),
    SETUP_BTN_ALIAS("setup_button_alias"),
    SETUP_ALIAS_PREFIX("setup_alias_prefix"),
    SETUP_ALIAS_SUFFIX("setup_alias_suffix"),
    SETUP_ALIAS_DISABLED("setup_alias_disabled"),
    SETUP_BTN_STORAGE("setup_button_storage"),
    SETUP_STORAGE_LOCAL("setup_storage_local"),
    SETUP_STORAGE_MYSQL("setup_storage_mysql"),
    SETUP_BTN_RELOAD("setup_button_reload"),
    COLOR_ICON_WOOL("color_icons.wool"),
    COLOR_ICON_GLASS("color_icons.glass"),
    COLOR_ICON_DYE("color_icons.dye"),
    COLOR_ICON_LEATHER("color_icons.leather"),
    COLOR_ICON_FIREWORK_STAR("color_icons.firework_star"),
    COLOR_ICON_BOOK("color_icons.book"),
    COLOR_ICON_CLOSEST_COLOR("color_icons.closest_color"),
    COLOR_ICON_EXACT_COLOR("color_icons.exact_color"),
    COLOR_ICON_NO_COLOR("color_icons.no_color");

    /**
     * the prefix of messages for admins, players get the one of messages.yml (prefix_alt)
     */
    public static final String PREFIX = "§7[§9EasyPrefix§7] ";
    private static final Pattern LEGACY_AMPERSAND = Pattern.compile("&([0-9a-fk-orxA-FK-ORX])");
    /**
     * the texts of messages.yml with legacy colors, each one is only parsed once
     */
    private static final Map<String, String> COLORIZED = new ConcurrentHashMap<>();
    private final String key;

    Message(String key) {
        this.key = key;
    }

    /**
     * looked up on every use, as /ep reload loads messages.yml again
     */
    private static MessageData messageData() {
        return EasyPrefix.getInstance().getFileManager().getMessageData();
    }

    @Nullable
    public static String setColors(@Nullable String text) {
        if (text == null) return null;
        return LEGACY_AMPERSAND.matcher(text).replaceAll("§$1");
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

    /**
     * @param text a text of messages.yml: MiniMessage, or legacy colors (&amp;c) in the files of older versions
     * @return the text with legacy colors (§c), as the menus need them
     */
    @NotNull
    static String colorize(@NotNull String text) {
        return COLORIZED.computeIfAbsent(text, raw -> {
            if (raw.indexOf('§') >= 0 || LEGACY_AMPERSAND.matcher(raw).find()) return setColors(raw);
            return TextUtils.getLegacySerializer().serialize(MiniMessage.miniMessage().deserialize(raw));
        });
    }

    /**
     * @param replacements names of placeholders, each followed by its value ("player", "Steve" replaces %player%)
     */
    private static String replace(String text, String... replacements) {
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("%" + replacements[i] + "%", replacements[i + 1]);
        }
        return text;
    }

    private static String render(String raw, String prefix, String... replacements) {
        return replace(colorize(raw).replace("%prefix%", prefix).replace("%newline%", "\n"), replacements);
    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * @return the text with legacy colors (§c), %prefix% and %newline% replaced
     */
    public String getText() {
        return get();
    }

    /**
     * @param replacements names of placeholders, each followed by its value ("player", "Steve" replaces %player%)
     * @return the text like {@link #getText()} with the placeholders replaced
     */
    @NotNull
    public String get(@NotNull String... replacements) {
        String raw = messageData().getText(key);
        if (this == PREFIX_ALT) return colorize(raw);
        return render(raw, PREFIX_ALT.getText(), replacements);
    }

    /**
     * @return the text for admins: with the prefix of the plugin instead of the one of messages.yml
     */
    @NotNull
    public String admin(@NotNull String... replacements) {
        String raw = messageData().getText(key);
        String text = render(raw, PREFIX.trim(), replacements);
        return raw.contains("%prefix%") ? text : PREFIX + text;
    }

    /**
     * @return the lines of a list like {@link #get(String...)}; a value with line breaks becomes several lines and a
     * line that only consists of placeholders without a value is left out
     */
    @NotNull
    public List<String> getList(@NotNull String... replacements) {
        List<String> list = new ArrayList<>();
        for (String raw : messageData().getList(key)) {
            String line = render(raw, PREFIX_ALT.getText(), replacements);
            if (line.isEmpty() && !raw.isEmpty()) continue;
            list.addAll(List.of(line.split("\n", -1)));
        }
        return list;
    }

    /**
     * @return {@link #getList(String...)} as the name and lore of a button
     */
    @NotNull
    public String[] lines(@NotNull String... replacements) {
        return getList(replacements).toArray(new String[0]);
    }

    public String getPath() {
        return key;
    }

}
