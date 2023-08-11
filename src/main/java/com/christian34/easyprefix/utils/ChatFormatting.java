package com.christian34.easyprefix.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public enum ChatFormatting {
    BOLD("l", Message.FORMATTING_BOLD.getText()),
    ITALIC("o", Message.FORMATTING_ITALIC.getText()),
    STRIKETHROUGH("m", Message.FORMATTING_STRIKETHROUGH.getText()),
    UNDERLINE("n", Message.FORMATTING_UNDERLINE.getText()),
    UNDEFINED("@", ""),
    INHERIT("", "");

    private final String code;
    private final String name;

    ChatFormatting(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Nullable
    public static ChatFormatting getByCode(String code) {
        for (ChatFormatting formatting : ChatFormatting.values()) {
            if (formatting.code.equals(code)) return formatting;
        }
        return null;
    }

    @NotNull
    public static ChatFormatting[] getValues() {
        ChatFormatting[] formattings = new ChatFormatting[values().length - 2];
        int i = 0;
        for (ChatFormatting formatting : values()) {
            if (formatting == UNDEFINED || formatting == INHERIT) continue;
            formattings[i] = formatting;
            i++;
        }
        return formattings;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (code != null) {
            return getCode() + getName();
        }
        return "";
    }

    public boolean isBukkit() {
        return true;
    }

    @NotNull
    public String getCode() {
        return code == null ? "" : "§" + code;
    }

}
