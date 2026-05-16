package de.grimlock.nitromc.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class NitroUtils {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();

    private NitroUtils() {
    }

    // Text

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return text;
        Component component = LEGACY_SERIALIZER.deserialize(text);
        return LEGACY_SERIALIZER.serialize(component);
    }

    public static Component parseComponent(String miniMessageText) {
        if (miniMessageText == null || miniMessageText.isEmpty()) return Component.empty();
        return MINI_MESSAGE.deserialize(miniMessageText);
    }

    public static String stripColor(String text) {
        if (text == null || text.isEmpty()) return text;
        Component component = LEGACY_SERIALIZER.deserialize(text);
        return PLAIN_SERIALIZER.serialize(component);
    }

    // Formatting

    public static String formatDuration(long millis) {
        if (millis < 1000) return "0s";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    // Collections

    public static <T> List<T> paginate(List<T> list, int page, int pageSize) {
        if (page < 1 || pageSize < 1) return List.of();
        int fromIndex = (page - 1) * pageSize;
        if (fromIndex >= list.size()) return List.of();
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return list.subList(fromIndex, toIndex);
    }

    // Math

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int randomBetween(int min, int max) {
        if (min > max) return min;
        if (min == max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    // UUID

    public static boolean isUUID(String input) {
        try {
            UUID.fromString(input);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static Optional<UUID> toUUID(String input) {
        try {
            return Optional.of(UUID.fromString(input));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // Scheduling

    public static void runSync(Plugin plugin, Runnable task) {
        plugin.getServer().getScheduler().runTask(plugin, task);
    }

    public static void runAsync(Plugin plugin, Runnable task) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void countdown(Plugin plugin, int seconds, java.util.function.Consumer<Integer> tick, Runnable onFinish) {
        new BukkitRunnable() {
            private int remaining = seconds;

            @Override
            public void run() {
                if (remaining < 0) {
                    cancel();
                    onFinish.run();
                    return;
                }
                tick.accept(remaining);
                remaining--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}
