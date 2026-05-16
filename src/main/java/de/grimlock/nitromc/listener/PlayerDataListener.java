package de.grimlock.nitromc.listener;

import de.grimlock.nitromc.annotation.AutoListener;
import de.grimlock.nitromc.player.NitroPlayerManager;
import de.grimlock.nitromc.service.IService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@AutoListener
public class PlayerDataListener implements Listener, IService {

    private final NitroPlayerManager playerManager;

    @Inject
    public PlayerDataListener(NitroPlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerManager.loadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerManager.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
