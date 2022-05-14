package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import net.wesjd.anvilgui.AnvilGUI;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public class TextInput {
    private final User user;
    private final AnvilGUI.Builder builder;
    private Consumer<String> completeConsumer;

    public TextInput(User user, String title, @Nullable String value) {
        this.user = user;
        if (value == null) {
            value = " ";
        }
        this.builder = new AnvilGUI.Builder()
                .title(title)
                .text(value)
                .plugin(EasyPrefix.getInstance());
    }

    public void build() {
        this.builder.open(user.getPlayer());
    }

    public TextInput onComplete(Consumer<String> consumer) {
        this.completeConsumer = consumer;

        this.builder.onComplete((player, s) -> {
            completeConsumer.accept(s);
            return AnvilGUI.Response.close();
        });
        return this;
    }

}
