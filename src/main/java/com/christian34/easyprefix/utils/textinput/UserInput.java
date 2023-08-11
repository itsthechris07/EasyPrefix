package com.christian34.easyprefix.utils.textinput;

import com.christian34.easyprefix.user.User;
import net.wesjd.anvilgui.version.VersionMatcher;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public abstract class UserInput {

    public static UserInput create() {
        try {
            if (new VersionMatcher().match() == null) return new ChatInput();
            return new AnvilInput();
        } catch (Exception ignored) {
        }
        return new ChatInput();
    }

    public abstract void build(User user, String title, @Nullable String value, Consumer<String> consumer);

}
