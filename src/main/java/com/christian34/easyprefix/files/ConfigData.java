package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.tchristofferson.configupdater.ConfigUpdater;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class ConfigData extends PluginFile {

    protected ConfigData() {
        super(new File(FileManager.getPluginFolder(), "config.yml"), "config");
    }

    @Override
    public void createFile() throws IOException {
        try {
            EasyPrefix.getInstance().getPlugin().saveResource("config.yml", true);
        } catch (IllegalArgumentException ex) {
            throw new IOException(ex.getMessage());
        }
    }

    @Override
    public void update() throws IOException {
        ConfigUpdater.update(EasyPrefix.getInstance().getPlugin(), "config.yml",
                new File(FileManager.getPluginFolder(), "config.yml"),
                new ArrayList<>(List.of("config.chat")));
    }

    public static final class Keys {
        public static final String CLIENT_ID = "client";
        public static final String CUSTOM_LAYOUT = "user.custom-layout.enabled";
        public static final String CUSTOM_LAYOUT_COOLDOWN = "user.custom-layout.cooldown";
        public static final String ENABLED = "enabled";
        public static final String SQL_ENABLED = "sql.enabled";
        public static final String HANDLE_CHAT = "chat.handle-chat";
        public static final String HIDE_JOIN_QUIT = "join-quit-messages.hide-messages";
        public static final String PREFIX_ALIAS = "user.custom-layout.alias.prefix";
        public static final String SUFFIX_ALIAS = "user.custom-layout.alias.suffix";
        public static final String USE_JOIN_QUIT = "join-quit-messages.enabled";
        public static final String USE_TAGS = "tags.enabled";
        public static final String CUSTOM_LAYOUT_BLACKLIST = "user.custom-layout.blacklist";
    }

}
