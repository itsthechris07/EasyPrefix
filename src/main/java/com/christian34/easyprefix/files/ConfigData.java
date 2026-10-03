package com.christian34.easyprefix.files;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.sql.database.SharedConfig;
import com.christian34.easyprefix.sql.database.StorageType;
import com.christian34.easyprefix.utils.TaskManager;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EasyPrefix 2026.
 * <p>
 * The config.yml - {@link Keys} contains the paths of all settings (relative to 'config.').
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
        File file = new File(FileManager.getPluginFolder(), "config.yml");
        // user defined colors/decorations are kept as they are; missing sections (older configs) are
        // filled with the defaults, as ConfigUpdater rejects ignored sections that don't exist
        YamlConfiguration current = YamlConfiguration.loadConfiguration(file);
        List<String> ignoredSections = new ArrayList<>();
        for (String section : List.of("config.chat.colors", "config.chat.decorations")) {
            if (current.isConfigurationSection(section)) ignoredSections.add(section);
        }
        ConfigUpdater.update(EasyPrefix.getInstance().getPlugin(), "config.yml", file, ignoredSections);
    }

    /**
     * shared settings (e.g. changed in the setup GUI) are uploaded to the database as well
     */
    @Override
    public void save(String path, Object value) {
        super.save(path, value);
        EasyPrefix instance = EasyPrefix.getInstance();
        if (instance.getStorageType() == StorageType.SQL && SharedConfig.isShared(path)) {
            TaskManager.async(() -> SharedConfig.upload(instance));
        }
    }

    public static final class Keys {
        public static final String CUSTOM_LAYOUT = "user.custom-layout.enabled";
        public static final String CUSTOM_LAYOUT_COOLDOWN = "user.custom-layout.cooldown";
        public static final String ENABLED = "enabled";
        public static final String SQL_ENABLED = "sql.enabled";
        public static final String HANDLE_CHAT = "chat.handle-chat";
        public static final String NAME_HOVER = "chat.name-hover";
        public static final String NAME_CLICK = "chat.name-click";
        public static final String DATE_FORMAT = "chat.date-format";
        public static final String HIDE_JOIN_QUIT = "join-quit-messages.hide-messages";
        public static final String PREFIX_ALIAS = "user.custom-layout.alias.prefix";
        public static final String SUFFIX_ALIAS = "user.custom-layout.alias.suffix";
        public static final String USE_JOIN_QUIT = "join-quit-messages.enabled";
        public static final String USE_TAGS = "tags.enabled";
        public static final String CUSTOM_LAYOUT_BLACKLIST = "user.custom-layout.blacklist";
        public static final String COLOR_ICON = "chat.color-icon";
        public static final String DISPLAY_TAB_LIST = "display.tab-list";
        public static final String DISPLAY_SORT_TAB_LIST = "display.sort-tab-list";
        public static final String DISPLAY_NAME_TAGS = "display.name-tags";
        public static final String DISPLAY_UPDATE_INTERVAL = "display.update-interval";
        public static final String DISPLAY_TAB_LIST_LAYOUT = "display.tab-list-layout";
        public static final String DISPLAY_NAME_TAG_PREFIX = "display.name-tag-prefix";
        public static final String DISPLAY_NAME_TAG_SUFFIX = "display.name-tag-suffix";
        public static final String DISPLAY_EXCLUDED_WORLDS = "display.excluded-worlds";
    }

}
