package de.grimlock.nitromc.player;

import de.grimlock.nitromc.integration.essentials.EssentialsService;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NitroPlayer {

    private final Player player;
    private final LuckPermsService luckPermsService;
    private final EssentialsService essentialsService;
    private final String name;
    private final long firstJoin;
    private final long lastJoin;
    private long loginCount;

    public NitroPlayer(Player player, LuckPermsService luckPermsService, EssentialsService essentialsService) {
        this(player, luckPermsService, essentialsService, player.getName(), System.currentTimeMillis(), System.currentTimeMillis(), 1);
    }

    public NitroPlayer(Player player, LuckPermsService luckPermsService, EssentialsService essentialsService, String name, long firstJoin, long lastJoin, long loginCount) {
        this.player = player;
        this.luckPermsService = luckPermsService;
        this.essentialsService = essentialsService;
        this.name = name;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
        this.loginCount = loginCount;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public String getName() {
        return name;
    }

    public String getPrimaryGroup() {
        return luckPermsService.getPrimaryGroup(player);
    }

    public boolean hasPermission(String permission) {
        return luckPermsService.hasPermission(player, permission);
    }

    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    public long getFirstJoin() {
        return firstJoin;
    }

    public long getLastJoin() {
        return lastJoin;
    }

    public long getLoginCount() {
        return loginCount;
    }

    public void incrementLoginCount() {
        this.loginCount++;
    }

    public CompletableFuture<Double> getCoins() {
        return essentialsService.getMoney(player.getUniqueId());
    }

    public CompletableFuture<Long> getPlaytimeTicks() {
        return essentialsService.getPlaytimeTicks(player.getUniqueId());
    }

    public CompletableFuture<String> getPlaytimeFormatted() {
        return getPlaytimeTicks().thenApply(essentialsService::formatPlaytime);
    }
}
