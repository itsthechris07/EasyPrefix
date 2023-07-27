package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import net.wesjd.anvilgui.AnvilGUI;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
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

        this.builder.onClick((slot, stateSnapshot) -> {
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
            }

            completeConsumer.accept(stateSnapshot.getText());
            return List.of(AnvilGUI.ResponseAction.close());
        });

        return this;
    }

}
