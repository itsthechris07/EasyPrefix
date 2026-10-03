package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * EasyPrefix 2026.
 * <p>
 * Asks the player for a text in the chat. Typing "quit" or not answering within 3 minutes cancels the input.
 * <p>
 * The conversation api is marked for removal, but it is the only api that takes the input before the chat events
 * (a chat listener would let other plugins, e.g. discord bridges, see the input first).
 *
 * @author Christian34
 */
@SuppressWarnings("removal")
class ChatInput extends UserInput {

    @Override
    public boolean showsPreview() {
        return false;
    }

    @Override
    public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
        Player player = user.getPlayer();
        player.closeInventory();
        sendCurrentValue(player, value);
        Prompt prompt = new StringPrompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext conversationContext) {
                return chatPrompt(title);
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String text) {
                if (text != null) TaskManager.run(player, () -> accept(user, text, consumer));
                return Prompt.END_OF_CONVERSATION;
            }
        };

        ConversationFactory factory = new ConversationFactory(EasyPrefix.getInstance());
        factory.withFirstPrompt(prompt).withLocalEcho(false).withEscapeSequence("quit").withTimeout(180)
                .addConversationAbandonedListener(event -> {
                    if (event.gracefulExit()) return;
                    // the input has been cancelled by "quit" or by the timeout
                    player.sendMessage(event.getCanceller() instanceof InactivityConversationCanceller
                            ? Message.CHAT_INPUT_TIMEOUT.getText() : Message.CHAT_INPUT_CANCELLED.getText());
                });
        Conversation conversation = factory.buildConversation(player);
        conversation.begin();
    }

}
