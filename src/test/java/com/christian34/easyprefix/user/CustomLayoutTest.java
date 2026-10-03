package com.christian34.easyprefix.user;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.listeners.ChatListener;
import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests that players can only use the tags they have the permission for in their custom prefix or suffix.
 *
 * @author Christian34
 */
class CustomLayoutTest extends PluginTestBase {

    private User player(String... permissions) {
        return user(addPlayer("Steve", permissions));
    }

    private static String plain(String miniMessage) {
        return PlainTextComponentSerializer.plainText().serialize(TextUtils.miniMessage().deserialize(miniMessage));
    }

    @Test
    void permittedColorsAreKept() {
        User user = player("easyprefix.color.red");
        assertEquals("<red>[Pro] ", CustomLayout.sanitize(user, "<red>[Pro] "));
        assertEquals("<red>[Pro] ", CustomLayout.sanitize(user, "&c[Pro] "));
        assertEquals("<#ff5555>[Pro]", CustomLayout.sanitize(user, "<#ff5555>[Pro]"), "permitted color as hex");
    }

    @Test
    void otherColorsAreShownAsText() {
        User user = player();
        assertEquals("<red>[Pro]", plain(CustomLayout.sanitize(user, "<red>[Pro]")));
        assertEquals("<#123456>[Pro]", plain(CustomLayout.sanitize(user, "<#123456>[Pro]")));
    }

    @Test
    void clickHoverAndLineBreaksAreShownAsText() {
        User user = player("easyprefix.color.red");
        String input = "<click:run_command:'/op Steve'><hover:show_text:'hi'>[A]</hover></click><br><font:uniform>x";
        String sanitized = CustomLayout.sanitize(user, input);
        Component component = TextUtils.miniMessage().deserialize(sanitized);
        assertNull(component.clickEvent());
        assertTrue(component.children().stream().allMatch(child -> child.clickEvent() == null && child.hoverEvent() == null));
        assertFalse(plain(sanitized).contains("\n"));
        assertTrue(plain(sanitized).startsWith("<click:run_command:"));
    }

    @Test
    void placeholdersAreNotResolved() {
        User user = player("EasyPrefix.custom.prefix");
        user.setPrefix(CustomLayout.sanitize(user, "%player% 100% "));
        assertTrue(PlainTextComponentSerializer.plainText().serialize(ChatListener.formatName(user)).startsWith("%player% 100% Steve"));
        assertEquals("%player% 100% ", CustomLayout.sanitize(user, user.getPrefix()).replace("<percent>", "%"), "sanitizing again keeps it");
    }

    @Test
    void extraTagsNeedPermission() {
        String gradient = "<gradient:red:gold>[Pro]</gradient>";
        assertEquals(gradient, plain(CustomLayout.sanitize(player(), gradient)));
        assertEquals("[Pro]", plain(CustomLayout.sanitize(user(addPlayer("Alex", "EasyPrefix.custom.gradient")), gradient)));

        String hex = "<#123456>[Pro]";
        assertEquals("[Pro]", plain(CustomLayout.sanitize(user(addPlayer("Bob", "EasyPrefix.custom.hex")), hex)));

        String shadow = "<shadow:red>[Pro]";
        assertEquals("[Pro]", plain(CustomLayout.sanitize(user(addPlayer("Tom", "EasyPrefix.custom.shadow")), shadow)));
    }

    @Test
    void blacklistChecksVisibleTextAndColors() {
        User user = player("easyprefix.color.dark_red", "easyprefix.color.bold");
        // default blacklist: Admin, Owner, &4
        assertTrue(CustomLayout.isBlocked(user, "Ad<b></b>min", CustomLayout.sanitize(user, "Ad<b></b>min")));
        assertTrue(CustomLayout.isBlocked(user, "<dark_red>Pro", CustomLayout.sanitize(user, "<dark_red>Pro")));
        assertFalse(CustomLayout.isBlocked(user, "Pro", CustomLayout.sanitize(user, "Pro")));

        User bypass = user(addPlayer("Alex", "EasyPrefix.custom.blacklist"));
        assertFalse(CustomLayout.isBlocked(bypass, "Admin", CustomLayout.sanitize(bypass, "Admin")));
    }

    @Test
    void sanitizedPrefixKeepsColor() {
        User user = player("easyprefix.color.red", "EasyPrefix.custom.prefix");
        user.setPrefix(CustomLayout.sanitize(user, "<red>[Pro] "));
        Component prefix = ChatListener.formatPrefix(user);
        assertEquals(NamedTextColor.RED.value(), prefix.children().getFirst().color().value());
    }

}
