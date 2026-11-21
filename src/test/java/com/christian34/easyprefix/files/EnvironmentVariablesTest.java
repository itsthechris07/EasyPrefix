package com.christian34.easyprefix.files;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.utils.Debug;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * {@code ${NAME}} in config.yml is replaced with the environment variable.
 *
 * @author Christian34
 */
class EnvironmentVariablesTest extends PluginTestBase {
    private final List<String> warnings = new CopyOnWriteArrayList<>();

    @BeforeEach
    void captureWarnings() {
        EnvironmentVariables.warning = warnings::add;
        EnvironmentVariables.setLookup(Map.of(
                "EP_PASSWORD", "s3cret",
                "EP_PORT", "3307",
                "EP_HOST", "db.example.com",
                "EP_SPECIAL", "pa$$w\\or${d}$1\\\\")::get);
    }

    @AfterEach
    void reset() {
        EnvironmentVariables.resetLookup();
        EnvironmentVariables.warning = Debug::warn;
    }

    @Test
    void replacesASetVariable() {
        assertEquals("s3cret", EnvironmentVariables.resolve("${EP_PASSWORD}", "sql.password"));
        assertEquals("db.example.com:3307", EnvironmentVariables.resolve("${EP_HOST}:${EP_PORT}", "sql.host"));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void keepsPlainValues() {
        assertEquals("password", EnvironmentVariables.resolve("password", "sql.password"));
        assertEquals("pa$s {x} $EP_PASSWORD ${not valid} ${", EnvironmentVariables.resolve("pa$s {x} $EP_PASSWORD ${not valid} ${", "sql.password"));
        assertEquals("", EnvironmentVariables.resolve("", "sql.password"));
        assertNull(EnvironmentVariables.resolve(null, "sql.password"));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void keepsSpecialCharactersOfTheValue() {
        assertEquals("pa$$w\\or${d}$1\\\\", EnvironmentVariables.resolve("${EP_SPECIAL}", "sql.password"));
        assertEquals("x-pa$$w\\or${d}$1\\\\-y", EnvironmentVariables.resolve("x-${EP_SPECIAL}-y", "sql.password"));
    }

    @Test
    void missingVariableIsEmptyAndWarnsOnce() {
        assertEquals("", EnvironmentVariables.resolve("${EP_MISSING}", "sql.password"));
        assertEquals("user-", EnvironmentVariables.resolve("user-${EP_MISSING}", "sql.username"));
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("EP_MISSING"));
        assertTrue(warnings.getFirst().contains("sql.password"));

        assertEquals("", EnvironmentVariables.resolve("${EP_OTHER}", "sql.host"));
        assertEquals(2, warnings.size());
    }

    @Test
    void warningNeverContainsAValue() {
        EnvironmentVariables.resolve("${EP_PASSWORD}${EP_MISSING}", "sql.password");
        assertEquals(1, warnings.size());
        assertFalse(warnings.getFirst().contains("s3cret"));
    }

    @Test
    void warnsOnceFromSeveralThreads() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        List<String> results = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 200; i++) {
            executor.execute(() -> results.add(EnvironmentVariables.resolve("${EP_PASSWORD}${EP_MISSING}", "sql.password")));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertEquals(200, results.size());
        assertTrue(results.stream().allMatch("s3cret"::equals));
        assertEquals(1, warnings.size());
    }

    @Test
    void configResolvesStringsAndNumbers() {
        ConfigData config = plugin.getConfigData();
        config.set("sql.password", "${EP_PASSWORD}");
        config.set("sql.host", "${EP_HOST}");
        config.set("sql.port", "${EP_PORT}");
        config.set("sql.username", "easyprefix");
        assertEquals("s3cret", config.getString("sql.password"));
        assertEquals("db.example.com", config.getString("sql.host"));
        assertEquals(3307, config.getInt("sql.port"));
        assertEquals("easyprefix", config.getString("sql.username"));
        assertEquals("s3cret", config.getString("sql.password", "fallback"));

        config.set("sql.port", 3306);
        assertEquals(3306, config.getInt("sql.port"));
        config.set("sql.port", "${EP_MISSING}");
        assertEquals(0, config.getInt("sql.port"));
    }

    @Test
    void savedFileKeepsTheReference() throws IOException {
        ConfigData config = plugin.getConfigData();
        config.set("sql.password", "${EP_PASSWORD}");
        config.save(ConfigData.Keys.SQL_FALLBACK, false);
        assertEquals("s3cret", config.getString("sql.password"));

        YamlConfiguration file = YamlConfiguration.loadConfiguration(config.getSourceFile());
        assertEquals("${EP_PASSWORD}", file.getString("config.sql.password"));
        assertTrue(Files.readAllLines(config.getSourceFile().toPath()).stream().noneMatch(line -> line.contains("s3cret")));
    }

}
