package de.grimlock.nitromc.task;

import de.grimlock.nitromc.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class TaskWrapper {

    private final Main plugin;
    private BukkitTask task;

    public TaskWrapper(Main plugin) {
        this.plugin = plugin;
    }

    public TaskWrapper runSync(Runnable runnable) {
        this.task = Bukkit.getScheduler().runTask(plugin, runnable);
        return this;
    }

    public TaskWrapper runAsync(Runnable runnable) {
        this.task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
        return this;
    }

    public TaskWrapper runLaterSync(Runnable runnable, long delay) {
        this.task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
        return this;
    }

    public TaskWrapper runTimerSync(Runnable runnable, long delay, long period) {
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
        return this;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }

    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }
}
