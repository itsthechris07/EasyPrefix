package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.Message;
import com.christian34.easyprefix.utils.TaskManager;
import com.christian34.easyprefix.utils.TextUtils;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * EasyPrefix 2026.
 * <p>
 * Asks the player for a text in a dialog: the text field contains the current value, "Preview" shows how the text
 * looks and keeps it in the field, an invalid input opens the dialog again with the error and the text. Unlike the
 * chat input, other plugins never see the text.
 *
 * @author Christian34
 */
@SuppressWarnings("UnstableApiUsage")
class DialogTextInput extends UserInput {
    private static final String KEY = "text";
    private static final int MAX_LENGTH = 256;
    private static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(1).lifetime(Duration.ofMinutes(10)).build();

    @Override
    public boolean showsPreview() {
        return true;
    }

    @Override
    public void build(User user, String title, @Nullable String value, Consumer<String> consumer) {
        Player player = user.getPlayer();
        player.closeInventory();
        show(user, title, value == null ? "" : value, null, consumer);
    }

    /**
     * @param error shown above the preview, e.g. if the last input contained a blocked word
     */
    private void show(User user, String title, String text, @Nullable String error, Consumer<String> consumer) {
        Player player = user.getPlayer();
        List<DialogBody> body = new ArrayList<>();
        if (error != null) body.add(DialogBody.plainMessage(legacy(error), 400));
        body.add(DialogBody.plainMessage(legacy(Message.DIALOG_PREVIEW.getText()).append(preview.apply(text)), 400));

        ActionButton save = button(Message.DIALOG_BTN_SAVE, player, input -> {
            String invalid = validator.apply(input);
            if (invalid != null) {
                show(user, title, input, invalid, consumer);
            } else {
                consumer.accept(input);
            }
        });
        ActionButton previewButton = button(Message.DIALOG_BTN_PREVIEW, player,
                input -> show(user, title, input, null, consumer));
        // a button without action just closes the dialog
        ActionButton cancel = ActionButton.builder(legacy(Message.DIALOG_BTN_CANCEL.getText())).build();

        DialogBase base = DialogBase.builder(legacy(title))
                .canCloseWithEscape(true)
                .body(body)
                .inputs(List.of(DialogInput.text(KEY, Component.empty())
                        .labelVisible(false).width(400).maxLength(MAX_LENGTH).initial(text).build()))
                .build();
        Dialog dialog = Dialog.create(factory -> factory.empty()
                .base(base)
                .type(DialogType.multiAction(List.of(save, previewButton)).exitAction(cancel).columns(2).build()));
        player.showDialog(dialog);
    }

    private static ActionButton button(Message label, Player player, Consumer<String> onClick) {
        DialogAction action = DialogAction.customClick((response, audience) -> {
            String input = response.getText(KEY);
            if (input == null || !(audience instanceof Player clicker) || !clicker.getUniqueId().equals(player.getUniqueId())) {
                return;
            }
            TaskManager.run(player, () -> onClick.accept(input));
        }, CALLBACK_OPTIONS);
        return ActionButton.builder(legacy(label.getText())).action(action).build();
    }

    private static Component legacy(String text) {
        return TextUtils.getLegacySerializer().deserialize(text);
    }

}
