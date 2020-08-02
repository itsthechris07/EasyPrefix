package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;

import java.util.List;

/**
 * EasyPrefix 2020.
 *
 * @author Christian34
 */
public enum ConfigKeys {
    COLOR_RAINBOW_COLORS("chat.color.rainbow.colors"), PLUGIN_LANG("lang"), SQL_HOST("sql.host"), SQL_DATABASE("sql.database"), SQL_USERNAME("sql.username"), SQL_PASSWORD("sql.password"), SQL_PORT("sql.port"), SQL_TABLEPREFIX("sql.table-prefix"), CUSTOM_LAYOUT_COOLDOWN("user.custom-layout.cooldown"), CUSTOM_LAYOUT("user.custom-layout.enabled"), ENABLED("enabled"), FORCE_GENDER("gender.force-gender"), GUI_SHOW_ALL_CHATCOLORS("gui.show-all-chatcolors"), HANDLE_CHAT("chat.handle-chat"), HANDLE_COLORS("chat.handle-colors"), HIDE_JOIN_QUIT("join-quit-messages.hide-messages"), JOIN_QUIT_SOUND_RECEIVER("join-quit-messages.sound.receiver"), LANG("lang"), PREFIX_ALIAS("user.custom-layout.alias.prefix"), QUIT_SOUND("join-quit-messages.sound.quit.sound"), SUFFIX_ALIAS("user.custom-layout.alias.suffix"), USE_GENDER("gender.enabled"), USE_JOIN_QUIT("join-quit-messages.enabled"), USE_QUIT_SOUND("join-quit-messages.sound.quit.enabled"), USE_SQL("sql.enabled"), USE_SUBGROUPS("subgroups.enabled");

    private final String KEY;

    ConfigKeys(String key) {
        this.KEY = key;
    }

    public String getPath() {
        return "config." + KEY;
    }

    public String toString() {
        return getConfigData().getData().getString(getPath());
    }

    public String toString(String defaultValue) {
        String val = toString();
        return val == null ? defaultValue : val;
    }

    public int toInt() {
        return getConfigData().getData().getInt(getPath());
    }

    public boolean toBoolean() {
        return getConfigData().getData().getBoolean(getPath());
    }

    public double toDouble() {
        return getConfigData().getData().getDouble(getPath());
    }

    public List<String> toStringList() {
        return getConfigData().getData().getStringList(getPath());
    }

    private ConfigData getConfigData() {
        return EasyPrefix.getInstance().getFileManager().getConfig();
    }

}
