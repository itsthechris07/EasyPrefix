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
