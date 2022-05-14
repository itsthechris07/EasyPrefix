package com.christian34.easyprefix.commands;

import com.christian34.easyprefix.user.UserPermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public interface Subcommand extends EasyCommand {

    @Nullable
    UserPermission getPermission();

    @Nullable
    String getDescription();

    @NotNull
    String getCommandUsage();

}
