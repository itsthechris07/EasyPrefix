package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.files.ConfigData;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.TextUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * EasyPrefix 2026.
 * <p>
 * Asks a player for a text, e.g. a new prefix. By default in a dialog with a text field that contains the current
 * value, the chat ({@code config.text-input: chat}) is the fallback for clients that can't show dialogs.
 *
 * @author Christian34
 */
public abstract class UserInput {
    protected Function<String, Component> preview = text -> TextUtils.getLegacySerializer().deserialize(TextUtils.colorize(text));
    protected Function<String, @Nullable String> validator = text -> null;
    private static @Nullable Supplier<UserInput> factory;

    /**
     * replaces the inputs that are created - MockBukkit can't show dialogs or conversations
     */
    @VisibleForTesting
    public static void setFactory(@Nullable Supplier<UserInput> factory) {
        UserInput.factory = factory;
    }

    public static UserInput create() {
        if (factory != null) return factory.get();
        String type = EasyPrefix.getInstance().getConfigData().getString(ConfigData.Keys.TEXT_INPUT, "dialog");
        if (!"chat".equalsIgnoreCase(type)) return new DialogTextInput();
        return TaskManager.isFolia() ? new FoliaChatInput() : new ChatInput();
    }

    /**
     * @param preview how the input looks, e.g. the whole chat line with a new prefix
     */
    public UserInput preview(Function<String, Component> preview) {
        this.preview = preview;
        return this;
    }

    /**
     * @param validator returns the error message for an invalid input (the input is not accepted), otherwise null
     */
    public UserInput validate(Function<String, @Nullable String> validator) {
        this.validator = validator;
        return this;
    }

    /**
     * @return true if the player already saw the preview before saving, so no extra confirmation is needed
     */
    public abstract boolean showsPreview();

    /**
     * @param value the current value, which the player can edit (if the input supports it)
     */
    public abstract void build(User user, String title, @Nullable String value, Consumer<String> consumer);

    /**
     * @return the prompt of a chat input, with the hint how to cancel it
     */
    protected static String chatPrompt(String title) {
        return Message.PREFIX_ALT.getText() + " " + title + " " + Message.CHAT_INPUT_QUIT_HINT.getText();
    }

    /**
     * shows the current value in the chat - clicking it writes it into the chat box, so it can be edited
     */
    protected static void sendCurrentValue(Player player, @Nullable String value) {
        if (value == null || value.isBlank()) return;
        Component text = TextUtils.getLegacySerializer().deserialize(Message.CHAT_INPUT_CURRENT.getText()
                .replace("%content%", value.replace("§", "&")));
        player.sendMessage(text.clickEvent(ClickEvent.suggestCommand(value.replace("§", "&"))));
    }

    /**
     * passes a chat input to the consumer, unless it is invalid
     */
    protected void accept(User user, String text, Consumer<String> consumer) {
        String error = validator.apply(text);
        if (error != null) {
            user.getPlayer().sendMessage(error);
            return;
        }
        consumer.accept(text);
    }

}
