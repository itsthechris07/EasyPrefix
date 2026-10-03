package com.christian34.easyprefix;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.scheduler.BukkitSchedulerMock;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * EasyPrefix 2026.
 * <p>
 * The global and async schedulers of MockBukkit can't cancel their tasks, so timers of a stopped synchronizer or
 * display manager would keep running. These run on the {@link BukkitSchedulerMock} as well, but can be cancelled.
 *
 * @author Christian34
 */
public class TestServerMock extends ServerMock {
    private final GlobalRegionScheduler globalScheduler = new GlobalScheduler();
    private final AsyncScheduler asyncScheduler = new Async();

    @Override
    public @NotNull GlobalRegionScheduler getGlobalRegionScheduler() {
        return globalScheduler;
    }

    @Override
    public @NotNull AsyncScheduler getAsyncScheduler() {
        return asyncScheduler;
    }

    private static long toTicks(long time, TimeUnit unit) {
        return Math.max(0, unit.toMillis(time) / 50);
    }

    private ScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> consumer, boolean repeating, Function<Runnable, BukkitTask> scheduler) {
        Task task = new Task(plugin, repeating);
        task.bukkitTask = scheduler.apply(() -> consumer.accept(task));
        return task;
    }

    private static final class Task implements ScheduledTask {
        private final Plugin plugin;
        private final boolean repeating;
        private BukkitTask bukkitTask;

        private Task(Plugin plugin, boolean repeating) {
            this.plugin = plugin;
            this.repeating = repeating;
        }

        @Override
        public @NotNull Plugin getOwningPlugin() {
            return plugin;
        }

        @Override
        public boolean isRepeatingTask() {
            return repeating;
        }

        @Override
        public @NotNull CancelledState cancel() {
            if (bukkitTask.isCancelled()) return CancelledState.CANCELLED_ALREADY;
            bukkitTask.cancel();
            return CancelledState.CANCELLED_BY_CALLER;
        }

        @Override
        public @NotNull ExecutionState getExecutionState() {
            return bukkitTask.isCancelled() ? ExecutionState.CANCELLED : ExecutionState.IDLE;
        }
    }

    private class GlobalScheduler implements GlobalRegionScheduler {

        @Override
        public void execute(@NotNull Plugin plugin, @NotNull Runnable run) {
            getScheduler().runTask(plugin, run);
        }

        @Override
        public @NotNull ScheduledTask run(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
            return schedule(plugin, task, false, runnable -> getScheduler().runTask(plugin, runnable));
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
            return schedule(plugin, task, false, runnable -> getScheduler().runTaskLater(plugin, runnable, delayTicks));
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
            return schedule(plugin, task, true, runnable -> getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks));
        }

        @Override
        public void cancelTasks(@NotNull Plugin plugin) {
            getScheduler().cancelTasks(plugin);
        }
    }

    private class Async implements AsyncScheduler {

        @Override
        public @NotNull ScheduledTask runNow(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
            return schedule(plugin, task, false, runnable -> getScheduler().runTaskAsynchronously(plugin, runnable));
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delay, @NotNull TimeUnit unit) {
            return schedule(plugin, task, false, runnable -> getScheduler().runTaskLaterAsynchronously(plugin, runnable, toTicks(delay, unit)));
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long initialDelay, long period, @NotNull TimeUnit unit) {
            return schedule(plugin, task, true, runnable -> getScheduler().runTaskTimerAsynchronously(plugin, runnable, toTicks(initialDelay, unit), toTicks(period, unit)));
        }

        @Override
        public void cancelTasks(@NotNull Plugin plugin) {
            getScheduler().cancelTasks(plugin);
        }
    }

}
