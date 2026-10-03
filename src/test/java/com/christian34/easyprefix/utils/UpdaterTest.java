package com.christian34.easyprefix.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the version comparison of the update check.
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

}
