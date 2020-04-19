package com.christian34.easyprefix.groups;

import com.christian34.easyprefix.user.Gender;
import com.christian34.easyprefix.utils.ChatFormatting;
import com.christian34.easyprefix.utils.Color;
import com.sun.istack.internal.Nullable;
import org.bukkit.ChatColor;

public abstract class EasyGroup {

    public abstract String getName();

    public abstract String getRawPrefix();

    public abstract String getPrefix();

    public abstract void setPrefix(String prefix);

    public abstract String getPrefix(Gender gender);

    public abstract String getRawSuffix();

    public abstract String getSuffix();

    public abstract void setSuffix(String suffix);

    public abstract String getSuffix(Gender gender);

    public abstract ChatColor getGroupColor();

    @Nullable
    public abstract Color getChatColor();

    public abstract void setChatColor(Color color);

    @Nullable
    public abstract ChatFormatting getChatFormatting();

    public abstract void setChatFormatting(ChatFormatting chatFormatting);

    public abstract String getFilePath();

    public abstract void delete();

}
