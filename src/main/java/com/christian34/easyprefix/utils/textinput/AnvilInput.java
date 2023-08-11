package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.EasyPrefix;
import com.christian34.easyprefix.user.User;
import net.wesjd.anvilgui.AnvilGUI;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
class AnvilInput extends UserInput {

    @Override
    public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
        if (value == null) {
            value = " ";
        }
        AnvilGUI.Builder builder = new AnvilGUI.Builder()
                .title(title)
                .text(value)
                .plugin(EasyPrefix.getInstance());

        builder.onClick((slot, stateSnapshot) -> {
            if (slot != AnvilGUI.Slot.OUTPUT) {
                return Collections.emptyList();
            }

            consumer.accept(stateSnapshot.getText());
            return List.of(AnvilGUI.ResponseAction.close());
        });

        builder.open(user.getPlayer());
    }

}
