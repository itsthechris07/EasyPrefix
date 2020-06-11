package com.christian34.easyprefix.messages;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum Message {
    NO_PERMS("info.noperms"), PLAYER_NOT_FOUND("info.playerNotFound"), GROUP_NOT_FOUND("info.groupNotFound"), SUCCESS("info.success"), GROUP_EXISTS("info.groupExists"), GROUP_CREATED("info.groupCreated"), SETUP_CANCELLED("info.setupCancelled"), SET_PREFIX("chat.prefix"), RELOAD_COMPLETE("info.reloadComplete"), PLAYER_ONLY("info.playerOnly"), LORE_EDIT("gui.clickToEdit"), BTN_SELECT_PREFIX("gui.selectPrefix"), BTN_BACK("gui.backBtn"), BTN_CHANGE_CHATCOLOR("gui.changeChatColor"), LORE_CHANGE_CHATCOLOR("gui.changeChatColor2"), BTN_CUSTOM_PREFIX("gui.customPrefixBtn"), BTN_CHANGE_PREFIX("gui.changePrefix"), LORE_CHANGE_PREFIX("gui.changePrefix2"), BTN_CHANGE_SUFFIX("gui.changeSuffix"), BTN_DELETE("gui.deleteBtn"), BTN_CONFIRM("gui.confirmBtn"), BTN_CANCEL("gui.cancelBtn"), BTN_RESET("gui.resetBtn"), BTN_MY_PREFIXES("gui.myPrefixesBtn"), BTN_MY_FORMATTINGS("gui.myFormattingsBtn"), BTN_ADDGROUP("gui.addGroupBtn"), INPUT_CANCELLED("gui.inputCancelled"), INPUT_SAVED("gui.inputSaved"), SETTINGS_TITLE("gui.settingsTitle"), SETTINGS_TITLE_MAIN("gui.title.settings"), SETTINGS_TITLE_PREFIXES("gui.title.prefixes"), SETTINGS_TITLE_FORMATTINGS("gui.title.formattings"), SETTINGS_TITLE_CUSTOM("gui.title.customPrefix"), SETUP_GROUPS_TITLE("gui.title.groups"), SETUP_GROUP_TITLE_DELETE("gui.title.deleteGroup"), CHAT_HEADER("chat.header"), CHAT_GROUP("chat.group"), CHAT_INPUT_SUFFIX("chat.suffix"), CHAT_INPUT_PREFIX("chat.prefix"), CHAT_INPUT_WRONGENTRY("chat.wrongEntry"), LORE_GROUP_DETAIL("gui.currentValue"), LORE_PREFIX("gui.lore.prefix"), LORE_SUFFIX("gui.lore.suffix"), LORE_COLOR("gui.lore.color"), LORE_PERMISSION("gui.lore.permission"), COLOR_BLACK("colors.black"), COLOR_DARK_BLUE("colors.blue"), COLOR_DARK_GREEN("colors.green"), COLOR_DARK_AQUA("colors.cyan"), COLOR_DARK_RED("colors.darkred"), COLOR_PURPLE("colors.purple"), COLOR_GOLD("colors.gold"), COLOR_LIGHT_GRAY("colors.lightgray"), COLOR_GRAY("colors.gray"), COLOR_LIGHT_BLUE("colors.lightblue"), COLOR_LIME("colors.lime"), COLOR_AQUA("colors.aqua"), COLOR_RED("colors.red"), COLOR_MAGENTA("colors.magenta"), COLOR_YELLOW("colors.yellow"), COLOR_WHITE("colors.white"), FORMATTING_BOLD("colors.formattings.bold"), FORMATTING_ITALIC("colors.formattings.italic"), FORMATTING_STRIKETHROUGH("colors.formattings.strikethrough"), FORMATTING_UNDERLINE("colors.formattings.underline"), FORMATTING_RAINBOW("colors.formattings.rainbow"), NOTIFY_GENDER_TEXT("info.gender.text"), NOTIFY_GENDER_BTN("info.gender.button"), CHANGE_GENDER("gui.changeGender"), LORE_CHANGE_GENDER("gui.changeGender2"), TITLE_GENDER("gui.title.gender"), TITLE_SUBGROUPS("gui.title.subgroups"), BTN_CHANGE_LANG("gui.changeLangBtn"), LORE_CHANGE_LANG("gui.lore.lang"), BTN_SWITCH_CP("gui.switchCustomPrefixBtn"), ENABLED("info.enabled"), DISABLED("info.disabled"), BTN_GROUPS("gui.groupsBtn"), BTN_SUBGROUPS("gui.subgroupsBtn"), BTN_SWITCH_GENDER("gui.switchGenderBtn"), BTN_SWITCH_COLOR("gui.switchColorsBtn"), LORE_SWITCH_CP("gui.lore.switchCustomPrefixes"), LORE_SWITCH_GENDER("gui.lore.switchGender"), LORE_SWITCH_COLOR("gui.lore.switchColors"), LORE_SELECT_COLOR("gui.lore.selectcolor"), GENDER_FEMALE("gender.female"), GENDER_MALE("gender.male"), GENDER_INTERSEX("gender.intersex"), PAGE_NEXT("gui.next"), PAGE_PREVIOUS("gui.previous");

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