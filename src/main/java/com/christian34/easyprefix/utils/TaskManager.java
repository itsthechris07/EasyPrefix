package com.christian34.easyprefix.utils;

import com.christian34.easyprefix.EasyPrefix;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * EasyPrefix 2026.
 * <p>
 * Runs tasks with the region schedulers of Paper, so the plugin runs on Paper and Folia with the same code: on Paper
 * the global and the entity scheduler run on the main thread, on Folia the global region or the region of the entity
 * ticks them. The BukkitScheduler throws on Folia and must not be used.
 *
 * @author Christian34
 */
public class TaskManager {
    private static final boolean FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

    /**
     * @return true if the server is Folia - only needed for apis that Folia does not support (e.g. scoreboards)
     */
    public static boolean isFolia() {
        return FOLIA;
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * runs the task on the global region (the main thread on Paper), e.g. for reloads or disabling the plugin
     */
    public static void global(@NotNull Runnable runnable) {
        Bukkit.getGlobalRegionScheduler().execute(EasyPrefix.getInstance(), runnable);
    }

    @NotNull
    public static ScheduledTask globalLater(@NotNull Runnable runnable, long delayTicks) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(EasyPrefix.getInstance(), task -> runnable.run(), delayTicks);
    }

    @NotNull
    public static ScheduledTask globalTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(EasyPrefix.getInstance(), task -> runnable.run(), delayTicks, periodTicks);
    }

    /**
     * runs the task on the thread that owns the entity (the main thread on Paper), e.g. to open an inventory - the task
     * is dropped if the entity has been removed (the player has left)
     */
    public static void run(@NotNull Entity entity, @NotNull Runnable runnable) {
        entity.getScheduler().run(EasyPrefix.getInstance(), task -> runnable.run(), null);
    }

    /**
     * like {@link #run(Entity, Runnable)}, but runs the task at once if the current thread owns the entity already
     */
    public static void runNowOrLater(@NotNull Entity entity, @NotNull Runnable runnable) {
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            runnable.run();
        } else {
            run(entity, runnable);
        }
    }

    public static void runLater(@NotNull Entity entity, @NotNull Runnable runnable, long delayTicks) {
        entity.getScheduler().runDelayed(EasyPrefix.getInstance(), task -> runnable.run(), null, delayTicks);
    }

    @NotNull
    public static ScheduledTask async(@NotNull Runnable runnable) {
        return Bukkit.getAsyncScheduler().runNow(EasyPrefix.getInstance(), task -> runnable.run());
    }

    @NotNull
    public static ScheduledTask asyncLater(@NotNull Runnable runnable, long delayTicks) {
        return Bukkit.getAsyncScheduler().runDelayed(EasyPrefix.getInstance(), task -> runnable.run(), toMillis(delayTicks), TimeUnit.MILLISECONDS);
    }

    @NotNull
    public static ScheduledTask asyncTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(EasyPrefix.getInstance(), task -> runnable.run(),
                toMillis(delayTicks), toMillis(periodTicks), TimeUnit.MILLISECONDS);
    }

    private static long toMillis(long ticks) {
        return ticks * 50;
    }

}
