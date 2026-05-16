package de.grimlock.nitromc.player;

import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NitroPlayer {

    private final Player player;
    private final LuckPermsService luckPermsService;
    private final String name;
    private final long firstJoin;
    private final long lastJoin;

    public NitroPlayer(Player player, LuckPermsService luckPermsService) {
        this(player, luckPermsService, player.getName(), System.currentTimeMillis(), System.currentTimeMillis());
    }

    public NitroPlayer(Player player, LuckPermsService luckPermsService, String name, long firstJoin, long lastJoin) {
        this.player = player;
        this.luckPermsService = luckPermsService;
        this.name = name;
        this.firstJoin = firstJoin;
        this.lastJoin = lastJoin;
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
}
