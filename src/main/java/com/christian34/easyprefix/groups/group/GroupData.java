package com.christian34.easyprefix.groups.group;

import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public abstract class GroupData {

    @Nullable
    public abstract String getPrefix();

    public abstract void setPrefix(String prefix);

    @Nullable
    public abstract String getSuffix();

    public abstract void setSuffix(String suffix);

    @Nullable
    public abstract Character getColor();

    public abstract void setColor(Character color);

    @Nullable
    public abstract Character getFormatting();

    public abstract void setFormatting(Character formatting);

    @Nullable
    public abstract String getJoinMessage();

    public abstract void setJoinMessage(String message);

    @Nullable
    public abstract String getQuitMessage();

    public abstract void setQuitMessage(String message);

    public abstract void delete();

}
