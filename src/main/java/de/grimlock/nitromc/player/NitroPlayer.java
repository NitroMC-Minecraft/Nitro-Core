package de.grimlock.nitromc.player;

import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NitroPlayer {

    private final Player player;
    private final LuckPermsService luckPermsService;

    public NitroPlayer(Player player, LuckPermsService luckPermsService) {
        this.player = player;
        this.luckPermsService = luckPermsService;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    public String getName() {
        return player.getName();
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
}
