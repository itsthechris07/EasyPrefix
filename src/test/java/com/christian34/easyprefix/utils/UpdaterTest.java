package com.christian34.easyprefix.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the version comparison of the update check and reading the GitHub release.
 *
 * @author Christian34
 */
class UpdaterTest {

    @Test
    void newerVersionIsAnUpdate() {
        assertTrue(Updater.isNewer("2.0.1", "2.0.0"));
        assertTrue(Updater.isNewer("2.1", "2.0.9"));
        assertTrue(Updater.isNewer("10.0.0", "9.9.9"));
    }

    @Test
    void olderOrSameVersionIsNoUpdate() {
        assertFalse(Updater.isNewer("1.8.13", "2.0.0"));
        assertFalse(Updater.isNewer("2.0.0", "2.0.0"));
        assertFalse(Updater.isNewer("2.0", "2.0.0"));
    }

    @Test
    void releaseIsNewerThanItsBeta() {
        assertTrue(Updater.isNewer("2.0.0", "2.0.0-beta.2"));
        assertFalse(Updater.isNewer("2.0.0-beta.2", "2.0.0"));
    }

    @Test
    void readsTheLatestGitHubRelease() {
        Updater.Release release = Updater.parse("""
                {"tag_name": "v2.1.0", "html_url": "https://github.com/itsthechris07/EasyPrefix/releases/tag/v2.1.0",
                 "name": "EasyPrefix 2.1.0", "prerelease": false}""");
        assertNotNull(release);
        assertEquals("2.1.0", release.version());
        assertEquals("https://github.com/itsthechris07/EasyPrefix/releases/tag/v2.1.0", release.url());
        assertTrue(Updater.isNewer(release.version(), "2.0.2"));
    }

    @Test
    void tagWithoutPrefixAndMissingUrl() {
        Updater.Release release = Updater.parse("{\"tag_name\": \"2.0.3\"}");
        assertNotNull(release);
        assertEquals("2.0.3", release.version());
        assertEquals(Updater.RELEASES_URL, release.url());
    }

    @Test
    void invalidAnswerIsIgnored() {
        assertNull(Updater.parse("{\"message\": \"Not Found\"}"));
        assertNull(Updater.parse("not json"));
        assertNull(Updater.parse("[]"));
    }

}
