package com.christian34.easyprefix;

import com.christian34.easyprefix.commands.CommandManager;
import com.christian34.easyprefix.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.MessageTarget;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EasyPrefix 2026.
 * <p>
 * Starts a mocked Paper server with EasyPrefix enabled for every test. Each test gets a fresh plugin data folder.
 *
 * @author Christian34
 */
public abstract class PluginTestBase {
    protected ServerMock server;
    protected EasyPrefix plugin;
    protected TestCommandManager commands;

    @BeforeEach
    void setUpServer() {
        this.server = MockBukkit.mock(new TestServerMock());
        this.server.addSimpleWorld("world");
        beforePluginLoad();
        this.plugin = MockBukkit.load(EasyPrefix.class);
        this.commands = new TestCommandManager();
        this.plugin.setCommandManager(new CommandManager(plugin, commands));
    }

    /**
     * called after the server has been started, before EasyPrefix is enabled (e.g. to add plugins it hooks into)
     */
    protected void beforePluginLoad() {
    }

    @AfterEach
    void tearDownServer() {
        MockBukkit.unmock();
    }

    /**
     * joins a player with the given permissions - they have to be set before the join, as the user is loaded then
     */
    protected PlayerMock addPlayer(String name, String... permissions) {
        PlayerMock player = new PlayerMock(server, name);
        for (String permission : permissions) {
            player.addAttachment(plugin, permission, true);
        }
        server.addPlayer(player);
        return player;
    }

    protected PlayerMock addAdmin(String name) {
        return addPlayer(name, "EasyPrefix.admin");
    }

    protected User user(PlayerMock player) {
        return plugin.getUser(player);
    }

    /**
     * runs the command like a player would type it (without the leading slash) and the tasks it schedules
     *
     * @return the exception the command failed with (the error message has been sent to the sender already)
     */
    @Nullable
    protected Throwable execute(@NotNull CommandSender sender, @NotNull String input) {
        Throwable failure = null;
        try {
            commands.commandExecutor().executeCommand(sender, input).join();
        } catch (CompletionException ex) {
            failure = ex.getCause();
        }
        server.getScheduler().performOneTick();
        return failure;
    }

    /**
     * all messages the sender received since the last call, without colors
     */
    protected List<String> messages(@NotNull MessageTarget target) {
        List<String> messages = new ArrayList<>();
        Component message;
        while ((message = target.nextComponentMessage()) != null) {
            // legacy messages keep their color codes in the text
            messages.add(ChatColor.stripColor(PlainTextComponentSerializer.plainText().serialize(message)));
        }
        return messages;
    }

    protected static void assertContains(List<String> messages, String expected) {
        assertTrue(messages.stream().anyMatch(message -> message.contains(expected)),
                () -> "expected a message containing '" + expected + "' but got " + messages);
    }

    protected static void assertNotContains(List<String> messages, String unexpected) {
        assertTrue(messages.stream().noneMatch(message -> message.contains(unexpected)),
                () -> "expected no message containing '" + unexpected + "' but got " + messages);
    }

}
