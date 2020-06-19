package com.christian34.easyprefix.messages;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Message {
    BTN_ADDGROUP("gui.addGroupBtn"), BTN_BACK("gui.backBtn"), BTN_CANCEL("gui.cancelBtn"), BTN_CHANGE_CHATCOLOR("gui.changeChatColor"), BTN_CHANGE_LANG("gui.changeLangBtn"), BTN_CHANGE_PREFIX("gui.changePrefix"), BTN_CHANGE_SUFFIX("gui.changeSuffix"), BTN_CONFIRM("gui.confirmBtn"), BTN_CUSTOM_PREFIX("gui.customPrefixBtn"), BTN_DELETE("gui.deleteBtn"), BTN_GROUPS("gui.groupsBtn"), BTN_MY_FORMATTINGS("gui.myFormattingsBtn"), BTN_MY_PREFIXES("gui.myPrefixesBtn"), BTN_RESET("gui.resetBtn"), BTN_SELECT_PREFIX("gui.selectPrefix"), BTN_SUBGROUPS("gui.subgroupsBtn"), BTN_SWITCH_COLOR("gui.switchColorsBtn"), BTN_SWITCH_CP("gui.switchCustomPrefixBtn"), BTN_SWITCH_GENDER("gui.switchGenderBtn"), CHANGE_GENDER("gui.changeGender"), CHAT_GROUP("chat.group"), CHAT_HEADER("chat.header"), CHAT_INPUT_PREFIX("chat.prefix"), CHAT_INPUT_SUFFIX("chat.suffix"), CHAT_INPUT_WRONGENTRY("chat.wrongEntry"), COLOR_AQUA("colors.aqua"), COLOR_BLACK("colors.black"), COLOR_DARK_AQUA("colors.cyan"), COLOR_DARK_BLUE("colors.blue"), COLOR_DARK_GREEN("colors.green"), COLOR_DARK_RED("colors.darkred"), COLOR_GOLD("colors.gold"), COLOR_GRAY("colors.gray"), COLOR_LIGHT_GRAY("colors.lightgray"), COLOR_MAGENTA("colors.magenta"), COLOR_PURPLE("colors.purple"), COLOR_RED("colors.red"), COLOR_WHITE("colors.white"), COLOR_YELLOW("colors.yellow"), DISABLED("info.disabled"), ENABLED("info.enabled"), FORMATTING_BOLD("colors.formattings.bold"), FORMATTING_ITALIC("colors.formattings.italic"), FORMATTING_RAINBOW("colors.formattings.rainbow"), FORMATTING_STRIKETHROUGH("colors.formattings.strikethrough"), FORMATTING_UNDERLINE("colors.formattings.underline"), GROUP_CREATED("info.groupCreated"), GROUP_EXISTS("info.groupExists"), GROUP_NOT_FOUND("info.groupNotFound"), INPUT_CANCELLED("gui.inputCancelled"), INPUT_SAVED("gui.inputSaved"), LORE_CHANGE_GENDER("gui.changeGender2"), LORE_CHANGE_LANG("gui.lore.lang"), LORE_COLOR("gui.lore.color"), LORE_EDIT("gui.clickToEdit"), LORE_GROUP_DETAIL("gui.currentValue"), LORE_PERMISSION("gui.lore.permission"), LORE_PREFIX("gui.lore.prefix"), LORE_SELECT_COLOR("gui.lore.selectcolor"), LORE_SUFFIX("gui.lore.suffix"), LORE_SWITCH_COLOR("gui.lore.switchColors"), LORE_SWITCH_CP("gui.lore.switchCustomPrefixes"), LORE_SWITCH_GENDER("gui.lore.switchGender"), NOTIFY_GENDER_BTN("info.gender.button"), NOTIFY_GENDER_TEXT("info.gender.text"), NO_PERMS("info.noperms"), PAGE_NEXT("gui.next"), PAGE_PREVIOUS("gui.previous"), PLAYER_NOT_FOUND("info.playerNotFound"), PLAYER_ONLY("info.playerOnly"), RELOAD_COMPLETE("info.reloadComplete"), SETTINGS_TITLE("gui.settingsTitle"), SETTINGS_TITLE_FORMATTINGS("gui.title.formattings"), SETTINGS_TITLE_LAYOUT("gui.title.layout"), SETTINGS_TITLE_MAIN("gui.title.settings"), SETUP_CANCELLED("info.setupCancelled"), SETUP_GROUPS_TITLE("gui.title.groups"), SETUP_GROUP_TITLE_DELETE("gui.title.deleteGroup"), SET_PREFIX("chat.prefix"), SUCCESS("info.success"), SUCCESS_PLAYER_PREFIX("chat.playerPrefix"), SUCCESS_PLAYER_SUFFIX("chat.playerSuffix"), TITLE_GENDER("gui.title.gender"), TITLE_SUBGROUPS("gui.title.subgroups");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return Messages.getText(message);
    }

    public String getPath() {
        return message;
    }

}
