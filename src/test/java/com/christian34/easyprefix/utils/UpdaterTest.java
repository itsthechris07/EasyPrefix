package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.PluginTestBase;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the version comparison of the update check, reading the GitHub release and the message on join.
 *
 * @author Christian34
 */
class UpdaterTest extends PluginTestBase {
    private static final Updater.Release RELEASE = new Updater.Release("99.0.0",
            "https://github.com/itsthechris07/EasyPrefix/releases/tag/v99.0.0");

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
    void readsTheGitHubRelease() {
        Updater.Release release = Updater.parse("""
                {"tag_name": "v2.1.0", "html_url": "https://github.com/itsthechris07/EasyPrefix/releases/tag/v2.1.0"}""");
        assertNotNull(release);
        assertEquals("2.1.0", release.version());
        assertEquals("https://github.com/itsthechris07/EasyPrefix/releases/tag/v2.1.0", release.url());
    }

    @Test
    void releaseWithoutUrlLinksTheReleases() {
        Updater.Release release = Updater.parse("{\"tag_name\": \"2.1.0\"}");
        assertNotNull(release);
        assertEquals(Updater.RELEASES_URL, release.url());
    }

    @Test
    void invalidAnswerIsNoRelease() {
        assertNull(Updater.parse("{\"message\": \"Not Found\"}"));
        assertNull(Updater.parse("not json"));
    }

    @Test
    void adminsGetTheMessageOnEveryJoin() {
        plugin.getUpdater().setLatest(RELEASE);
        for (int i = 0; i < 2; i++) {
            PlayerMock admin = addAdmin("Admin" + i);
            server.getScheduler().performTicks(100);
            List<String> messages = messages(admin);
            assertContains(messages, "A new version of EasyPrefix is available!");
            assertContains(messages, "99.0.0");
            assertContains(messages, RELEASE.url());
            admin.disconnect();
        }
    }

    @Test
    void messageLinksTheRelease() {
        plugin.getUpdater().setLatest(RELEASE);
        Component message = Message.UPDATE_AVAILABLE.component("current", "2.0.0", "latest", "99.0.0", "url", RELEASE.url());
        assertTrue(PlainTextComponentSerializer.plainText().serialize(message).contains("[EasyPrefix]"));
        assertTrue(hasClick(message, ClickEvent.openUrl(RELEASE.url())));
    }

    private static boolean hasClick(Component component, ClickEvent click) {
        if (click.equals(component.clickEvent())) return true;
        return component.children().stream().anyMatch(child -> hasClick(child, click));
    }

    @Test
    void playersDontGetTheMessage() {
        plugin.getUpdater().setLatest(RELEASE);
        PlayerMock player = addPlayer("Steve");
        server.getScheduler().performTicks(100);
        assertNotContains(messages(player), "A new version of EasyPrefix is available!");
    }

    @Test
    void noMessageWithoutUpdate() {
        PlayerMock admin = addAdmin("Admin");
        server.getScheduler().performTicks(100);
        assertNotContains(messages(admin), "A new version of EasyPrefix is available!");
    }

}
