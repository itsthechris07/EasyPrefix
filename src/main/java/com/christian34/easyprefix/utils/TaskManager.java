package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

/**
 * EasyPrefix 2023.
 *
 * @author Christian34
 */
public class TaskManager {

    @NotNull
    public static BukkitTask run(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTask(EasyPrefix.getInstance(), runnable);
    }

    @NotNull
    public static BukkitTask runLater(@NotNull Runnable runnable, long delay) {
        return Bukkit.getScheduler().runTaskLater(EasyPrefix.getInstance(), runnable, delay);
    }

    @NotNull
    public static BukkitTask async(@NotNull Runnable runnable) {
        return Bukkit.getScheduler().runTaskAsynchronously(EasyPrefix.getInstance(), runnable);
    }

}
