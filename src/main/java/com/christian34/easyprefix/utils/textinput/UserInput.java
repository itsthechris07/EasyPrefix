package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.user.User;
import com.christian34.easyprefix.utils.TaskManager;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * EasyPrefix 2026.
 * <p>
 * Asks a player for a text, e.g. a new prefix.
 *
 * @author Christian34
 */
public abstract class UserInput {

    public static UserInput create() {
        return TaskManager.isFolia() ? new FoliaChatInput() : new ChatInput();
    }

    public abstract void build(User user, String title, @Nullable String value, Consumer<String> consumer);

}
