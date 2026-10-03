package com.christian34.easyprefix.listeners;

import com.christian34.easyprefix.PluginTestBase;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Color;
import com.christian34.easyprefix.utils.Decoration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EasyPrefix 2026.
 * <p>
 * Tests the chat formatting.
 *
 * @author Christian34
 */
class ChatListenerTest extends PluginTestBase {

    private AsyncChatEvent chat(Player player, String message) {
        AsyncChatEvent event = event(player, message);
        new ChatListener(plugin).onChat(event);
        return event;
    }

    private AsyncChatEvent event(Player player, String message) {
        Component text = Component.text(message);
        return new AsyncChatEvent(true, player, new HashSet<>(server.getOnlinePlayers()), ChatRenderer.defaultRenderer(),
                text, text, SignedMessage.system(message, text));
    }

    private static Component rendered(AsyncChatEvent event) {
        return event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), event.getPlayer());
    }

    /**
     * the line as the server prints it
     */
    private static String line(AsyncChatEvent event) {
        return PlainTextComponentSerializer.plainText().serialize(rendered(event));
    }

    @Test
    void formatsLineWithPrefixNameAndSuffix() {
        PlayerMock player = addPlayer("Steve");
        user(player).setPrefix(null);
        plugin.getGroupHandler().getGroup("default").setPrefix("[Guest] ");
        plugin.getGroupHandler().getGroup("default").setSuffix(" >");

        assertEquals("[Guest] Steve > hello", line(chat(player, "hello")));
    }

    @Test
    void percentSignDoesNotBreakTheFormat() {
        PlayerMock player = addPlayer("Steve");
        AsyncChatEvent event = chat(player, "I am 100% sure, 50%s %d");
        assertDoesNotThrow(() -> line(event));
        assertTrue(line(event).endsWith("I am 100% sure, 50%s %d"), line(event));
    }

    @Test
    void messageUsesUserColor() {
        PlayerMock player = addPlayer("Steve");
        User user = user(player);
        user.setColor(Color.of("red"));
        user.setDecoration(Decoration.of("bold"));

        Component message = ChatListener.formatMessage(user, "hi");
        assertEquals(NamedTextColor.RED.value(), message.color().value());
        assertTrue(message.hasDecoration(TextDecoration.BOLD));
    }

    @Test
    void playersCannotUseColorsWithoutPermission() {
        PlayerMock player = addPlayer("Steve");
        Component message = ChatListener.formatMessage(user(player), "&chello <red>world");
        String text = PlainTextComponentSerializer.plainText().serialize(message);
        assertFalse(text.contains("§"), text);
        assertTrue(message.children().stream().allMatch(child -> child.color() == null
                || child.color().value() != NamedTextColor.RED.value()), "red was applied without permission");
    }

    @Test
    void playersCanUseAllowedColors() {
        PlayerMock player = addPlayer("Steve", "easyprefix.color.red");
        Component message = ChatListener.formatMessage(user(player), "<red>hello");
        assertEquals("hello", PlainTextComponentSerializer.plainText().serialize(message));
    }

    /**
     * the component with the click event of the link
     */
    private static Component link(Component component) {
        if (component.clickEvent() != null) return component;
        for (Component child : component.children()) {
            Component found = link(child);
            if (found != null) return found;
        }
        return null;
    }

    @Test
    void linksAreKeptAndClickable() {
        PlayerMock player = addPlayer("Steve", "easyprefix.color.aqua");
        String url = "https://example.org/a_b/?c=1&b=2&lol";
        Component message = ChatListener.formatMessage(user(player), "see " + url + ", ok");
        assertEquals("see " + url + ", ok", PlainTextComponentSerializer.plainText().serialize(message));

        Component link = link(message);
        assertNotNull(link);
        assertEquals(url, PlainTextComponentSerializer.plainText().serialize(link));
        assertEquals(ClickEvent.Action.OPEN_URL, link.clickEvent().action());
        assertEquals(url, ((ClickEvent.Payload.Text) link.clickEvent().payload()).value());
        assertNull(link.color(), "the color code in the url was applied");
    }

    @Test
    void wwwLinksOpenWithHttps() {
        PlayerMock player = addPlayer("Steve");
        Component link = link(ChatListener.formatMessage(user(player), "go to www.example.org."));
        assertNotNull(link);
        assertEquals("www.example.org", PlainTextComponentSerializer.plainText().serialize(link));
        assertEquals("https://www.example.org", ((ClickEvent.Payload.Text) link.clickEvent().payload()).value());
    }

    @Test
    void linksInRainbowChat() {
        PlayerMock player = addPlayer("Steve");
        user(player).setColor(Color.of("rainbow"));
        Component message = ChatListener.formatMessage(user(player), "hi https://example.org/?a&b");
        assertEquals("hi https://example.org/?a&b", PlainTextComponentSerializer.plainText().serialize(message));
        assertNotNull(link(message));
    }

    @Test
    void doesNothingWhenChatHandlingIsDisabled() {
        plugin.getConfigData().save(ConfigData.Keys.HANDLE_CHAT, false);
        PlayerMock player = addPlayer("Steve");
        AsyncChatEvent event = event(player, "hello");
        ChatRenderer renderer = event.renderer();
        new ChatListener(plugin).onChat(event);
        assertSame(renderer, event.renderer());
        assertEquals(Component.text("hello"), event.message());
    }

    /**
     * the part of the rendered line that carries the name (prefix, name and suffix)
     */
    private static Component name(AsyncChatEvent event) {
        return rendered(event).children().getFirst();
    }

    @Test
    void nameShowsInformationOnHover() {
        PlayerMock player = addPlayer("Steve");
        HoverEvent<?> hover = name(chat(player, "hello")).hoverEvent();
        assertNotNull(hover);
        String text = PlainTextComponentSerializer.plainText().serialize((Component) hover.value());
        assertTrue(text.contains("Steve"), text);
        assertTrue(text.contains("Rank: default"), text);
        assertFalse(text.contains("%"), "placeholders have not been replaced: " + text);
    }

    @Test
    void messageHasNoHover() {
        PlayerMock player = addPlayer("Steve");
        Component line = rendered(chat(player, "hello"));
        assertNull(line.children().getLast().hoverEvent());
    }

    @Test
    void hoverCanBeDisabled() {
        plugin.getConfigData().save(ConfigData.Keys.NAME_HOVER, false);
        PlayerMock player = addPlayer("Steve");
        assertNull(name(chat(player, "hello")).hoverEvent());
    }

    @Test
    void clickOnNameSuggestsCommand() {
        PlayerMock player = addPlayer("Steve");
        ClickEvent click = name(chat(player, "hello")).clickEvent();
        assertNotNull(click);
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, click.action());
        assertEquals("/msg Steve ", ((ClickEvent.Payload.Text) click.payload()).value());
    }

    @Test
    void clickCanBeDisabled() {
        plugin.getConfigData().save(ConfigData.Keys.NAME_CLICK, "");
        PlayerMock player = addPlayer("Steve");
        assertNull(name(chat(player, "hello")).clickEvent());
    }

    @Test
    void tabListHasNoHover() {
        PlayerMock player = addPlayer("Steve");
        assertNull(ChatListener.formatLayout(user(player), "{prefix}{name}").hoverEvent());
    }

    private static String lineFor(AsyncChatEvent event, Player viewer) {
        return PlainTextComponentSerializer.plainText().serialize(
                event.renderer().render(event.getPlayer(), event.getPlayer().displayName(), event.message(), viewer));
    }

    /**
     * the component that shows the mention in the line
     */
    private static Component mention(Component line, String text) {
        if (line instanceof net.kyori.adventure.text.TextComponent t && t.content().equals(text)) return line;
        for (Component child : line.children()) {
            Component found = mention(child, text);
            if (found != null) return found;
        }
        return null;
    }

    @Test
    void mentionedPlayerSeesHighlightAndHearsSound() {
        PlayerMock steve = addPlayer("Steve");
        PlayerMock alex = addPlayer("Alex");
        PlayerMock bob = addPlayer("Bob");
        AsyncChatEvent event = chat(steve, "hi @alex, look");
        server.getScheduler().performOneTick();

        assertTrue(lineFor(event, alex).startsWith("» "), lineFor(event, alex));
        assertTrue(lineFor(event, alex).endsWith("hi @Alex, look"), lineFor(event, alex));
        assertFalse(lineFor(event, bob).startsWith("»"), lineFor(event, bob));
        assertTrue(lineFor(event, bob).endsWith("hi @Alex, look"), lineFor(event, bob));

        Component highlighted = mention(event.renderer().render(steve, steve.displayName(), event.message(), alex), "@Alex");
        assertNotNull(highlighted);
        assertEquals(NamedTextColor.YELLOW, highlighted.color());
        assertTrue(highlighted.hasDecoration(TextDecoration.BOLD));
        Component formatted = mention(event.renderer().render(steve, steve.displayName(), event.message(), bob), "@Alex");
        assertNotNull(formatted);
        assertEquals(NamedTextColor.AQUA, formatted.color());

        assertEquals(1, alex.getHeardSounds().size());
        assertEquals("minecraft:entity.experience_orb.pickup", alex.getHeardSounds().getFirst().getSound());
        assertTrue(bob.getHeardSounds().isEmpty());
        assertTrue(steve.getHeardSounds().isEmpty());
    }

    @Test
    void mentionNeedsTheWholeName() {
        PlayerMock steve = addPlayer("Steve");
        PlayerMock alex = addPlayer("Alex");
        AsyncChatEvent event = chat(steve, "mail@alexander.com @Alexa");
        server.getScheduler().performOneTick();
        assertFalse(lineFor(event, alex).startsWith("»"));
        assertTrue(alex.getHeardSounds().isEmpty());
    }

    @Test
    void ownNameDoesNotPing() {
        PlayerMock steve = addPlayer("Steve");
        AsyncChatEvent event = chat(steve, "I am @Steve");
        server.getScheduler().performOneTick();
        assertFalse(lineFor(event, steve).startsWith("»"));
        assertTrue(steve.getHeardSounds().isEmpty());
    }

    @Test
    void playersThatDoNotReceiveTheMessageHearNoSound() {
        PlayerMock steve = addPlayer("Steve");
        PlayerMock alex = addPlayer("Alex");
        AsyncChatEvent event = event(steve, "hi @Alex");
        // e.g. alex ignores steve
        event.viewers().remove(alex);
        new ChatListener(plugin).onChat(event);
        server.getScheduler().performOneTick();
        assertTrue(alex.getHeardSounds().isEmpty());
    }

    @Test
    void mentionsCanBeDisabled() {
        plugin.getConfigData().save(ConfigData.Keys.MENTIONS, false);
        PlayerMock steve = addPlayer("Steve");
        PlayerMock alex = addPlayer("Alex");
        AsyncChatEvent event = chat(steve, "hi @alex");
        server.getScheduler().performOneTick();
        assertTrue(lineFor(event, alex).endsWith("hi @alex"));
        assertTrue(alex.getHeardSounds().isEmpty());
    }

    @Test
    void playersCanTurnOffMentions() {
        PlayerMock steve = addPlayer("Steve");
        PlayerMock alex = addPlayer("Alex");
        PlayerMock bob = addPlayer("Bob");
        user(alex).setMentionable(false);
        AsyncChatEvent event = chat(steve, "hi @Alex and @Bob");
        server.getScheduler().performOneTick();

        // the name is still formatted, but alex is not pinged
        assertFalse(lineFor(event, alex).startsWith("»"), lineFor(event, alex));
        Component formatted = mention(event.renderer().render(steve, steve.displayName(), event.message(), alex), "@Alex");
        assertNotNull(formatted);
        assertEquals(NamedTextColor.AQUA, formatted.color());
        assertTrue(alex.getHeardSounds().isEmpty());

        assertTrue(lineFor(event, bob).startsWith("» "), lineFor(event, bob));
        assertEquals(1, bob.getHeardSounds().size());
    }

    @Test
    void previewShowsFormattedLine() {
        PlayerMock player = addPlayer("Steve");
        PlayerMock admin = addAdmin("Admin");
        ChatListener.sendPreview(admin, user(player));

        List<String> messages = messages(admin);
        assertEquals(2, messages.size(), messages.toString());
        assertTrue(messages.get(0).contains("Steve"));
        assertTrue(messages.get(1).contains("Steve"));
        assertTrue(messages.get(1).endsWith("Hello, this is what my messages look like!"), messages.get(1));
    }

}
