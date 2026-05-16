package de.grimlock.nitromc.integration.essentials;

import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.thread.ThreadingService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Singleton
public class EssentialsService implements IService {

    private Object essentials;
    private final ThreadingService threadingService;

    @Inject
    public EssentialsService(ThreadingService threadingService) {
        this.threadingService = threadingService;
    }

    @Override
    public void onEnable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (plugin != null && plugin.getClass().getName().equals("com.earth2me.essentials.Essentials")) {
            this.essentials = plugin;
            Bukkit.getLogger().info("EssentialsX integration enabled");
        }
    }

    @Override
    public void onDisable() {
        essentials = null;
    }

    public boolean isAvailable() {
        return essentials != null;
    }

    public CompletableFuture<Double> getMoney(UUID uuid) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(-1.0);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object user = essentials.getClass().getMethod("getUser", UUID.class).invoke(essentials, uuid);
                if (user == null) return -1.0;
                Object money = user.getClass().getMethod("getMoney").invoke(user);
                return money != null ? ((Number) money).doubleValue() : -1.0;
            } catch (Exception e) {
                return -1.0;
            }
        }, threadingService.getIoExecutor());
    }

    public CompletableFuture<Long> getPlaytimeTicks(UUID uuid) {
        if (!isAvailable()) {
            return CompletableFuture.completedFuture(0L);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object user = essentials.getClass().getMethod("getUser", UUID.class).invoke(essentials, uuid);
                if (user == null) return 0L;
                Object playtime = user.getClass().getMethod("getStatistic", String.class).invoke(user, "ticks_played");
                if (playtime instanceof Number) {
                    return ((Number) playtime).longValue();
                }
                return 0L;
            } catch (Exception e) {
                return 0L;
            }
        }, threadingService.getIoExecutor());
    }

    public String formatPlaytime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
