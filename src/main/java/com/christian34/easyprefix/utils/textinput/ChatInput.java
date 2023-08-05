package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import org.bukkit.conversations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

class ChatInput extends UserInput {

    @Override
    public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
        user.getPlayer().closeInventory();
        Prompt prompt = new StringPrompt() {
            @NotNull
            @Override
            public String getPromptText(@NotNull ConversationContext conversationContext) {
                return Message.PREFIX_ALT.getText() + " " + title;
            }

            @Nullable
            @Override
            public Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String text) {
                consumer.accept(text);
                return Prompt.END_OF_CONVERSATION;
            }
        };

        ConversationFactory factory = new ConversationFactory(EasyPrefix.getInstance());
        factory.withFirstPrompt(prompt).withLocalEcho(false).withEscapeSequence("quit").withTimeout(180);
        Conversation conversation = factory.buildConversation(user.getPlayer());
        conversation.begin();
    }

}
