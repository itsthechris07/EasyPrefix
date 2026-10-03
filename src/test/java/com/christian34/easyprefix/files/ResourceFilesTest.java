package com.christian34.easyprefix.files;

import com.christian34.easyprefix.utils.ColorIcon;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Checks the files bundled in the jar - a broken file only shows up on the server otherwise.
 *
 * @author Christian34
 */
class ResourceFilesTest {

    static String read(String name) throws IOException {
        try (InputStream stream = ResourceFilesTest.class.getClassLoader().getResourceAsStream(name)) {
            assertNotNull(stream, name + " is missing in the jar");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    static YamlConfiguration load(String name) throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(read(name));
        return yaml;
    }

    @Test
    void defaultGroupHasTheBuiltInHover() throws Exception {
        // both are the same text, one for new servers (groups.yml) and one if the default group has none (mysql, old files)
        assertEquals(com.christian34.easyprefix.groups.Group.DEFAULT_HOVER, load("groups.yml").getStringList("groups.default.hover"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"config.yml", "messages.yml", "groups.yml", "plugin.yml"})
    void isValidYaml(String file) {
        assertDoesNotThrow(() -> load(file));
    }

    @Test
    void pluginYmlIsComplete() throws Exception {
        YamlConfiguration plugin = load("plugin.yml");
        assertEquals("EasyPrefix", plugin.getString("name"));
        String version = plugin.getString("version");
        assertNotNull(version);
        assertFalse(version.contains("${"), "version has not been expanded by gradle: " + version);
        assertDoesNotThrow(() -> Class.forName(plugin.getString("main")), "main class does not exist");
    }

    @Test
    void configContainsAllKeys() throws Exception {
        YamlConfiguration config = load("config.yml");
        for (Field field : ConfigData.Keys.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) continue;
            String key = "config." + field.get(null);
            assertTrue(config.isSet(key), "config.yml misses " + key + " (ConfigData.Keys." + field.getName() + ")");
        }
    }

    @Test
    void colorsAreValid() throws Exception {
        ConfigurationSection colors = load("config.yml").getConfigurationSection("config.chat.colors");
        assertNotNull(colors);
        Set<String> codes = new HashSet<>();
        for (String name : colors.getKeys(false)) {
            ConfigurationSection color = colors.getConfigurationSection(name);
            assertNotNull(color);
            assertNotNull(color.getString("display-name"), name + " has no display-name");
            if (name.equals("rainbow")) continue;

            String hex = color.getString("hex");
            assertNotNull(hex, name + " has no hex value");
            TextColor textColor = null;
            try {
                textColor = TextColor.fromHexString(hex);
            } catch (RuntimeException ignored) {
            }
            assertNotNull(textColor, name + " has an invalid hex value: " + hex);

            String code = color.getString("code");
            assertNotNull(code, name + " has no code");
            assertTrue(codes.add(code.toLowerCase(Locale.ROOT)), "code " + code + " is used twice");
        }
    }

    @Test
    void decorationsAreValid() throws Exception {
        ConfigurationSection decorations = load("config.yml").getConfigurationSection("config.chat.decorations");
        assertNotNull(decorations);
        for (String name : decorations.getKeys(false)) {
            assertDoesNotThrow(() -> TextDecoration.valueOf(name.toUpperCase(Locale.ROOT)), name + " is no text decoration");
        }
    }

    @Test
    void defaultColorIconIsValid() throws Exception {
        String value = load("config.yml").getString("config." + ConfigData.Keys.COLOR_ICON);
        assertNotNull(value);
        assertDoesNotThrow(() -> ColorIcon.valueOf(value.toUpperCase(Locale.ROOT)));
    }

    @Test
    void defaultGroupExists() throws Exception {
        YamlConfiguration groups = load("groups.yml");
        assertTrue(groups.isConfigurationSection("groups.default"), "groups.yml must contain the default group");
    }

}
