package de.grimlock.nitromc.player;

import de.grimlock.nitromc.database.DatabaseManager;
import de.grimlock.nitromc.database.DatabasePriority;
import de.grimlock.nitromc.event.AsyncDataIntegrityCheckEvent;
import de.grimlock.nitromc.event.NitroEventBus;
import de.grimlock.nitromc.event.PreDataLoadEvent;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import de.grimlock.nitromc.service.IService;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class NitroPlayerManager implements IService {

    private final DatabaseManager databaseManager;
    private final LuckPermsService luckPermsService;
    private final NitroEventBus eventBus;
    private final ConcurrentHashMap<UUID, NitroPlayer> players = new ConcurrentHashMap<>();
    private NitroPlayerTable playerTable;

    @Inject
    public NitroPlayerManager(DatabaseManager databaseManager, LuckPermsService luckPermsService, NitroEventBus eventBus) {
        this.databaseManager = databaseManager;
        this.luckPermsService = luckPermsService;
        this.eventBus = eventBus;
    }

    @Override
    public void onEnable() {
        playerTable = databaseManager.get(NitroPlayerTable.class);
    }

    @Override
    public void onDisable() {
        for (NitroPlayer player : players.values()) {
            java.util.Map<String, Object> updates = java.util.Map.of("last_join", System.currentTimeMillis());
            playerTable.updateMultiple("uuid", player.getUniqueId().toString(), updates).join();
        }
        players.clear();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        PreDataLoadEvent event = new PreDataLoadEvent(uuid);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);

        playerTable.query()
            .where("`uuid` = ?", uuid.toString())
            .mapTo(rs -> {
                String name = rs.getString("name");
                long firstJoin = rs.getLong("first_join");
                long lastJoin = rs.getLong("last_join");
                return new NitroPlayer(player, luckPermsService, name, firstJoin, lastJoin);
            })
            .thenAccept(nitroPlayer -> {
                if (nitroPlayer != null) {
                    players.put(uuid, nitroPlayer);
                    AsyncDataIntegrityCheckEvent loadEvent = new AsyncDataIntegrityCheckEvent(uuid.toString(), nitroPlayer);
                    loadEvent.setValid(true);
                    eventBus.post(loadEvent);
                } else {
                    long now = System.currentTimeMillis();
                    NitroPlayer newPlayer = new NitroPlayer(player, luckPermsService, player.getName(), now, now);
                    insertPlayerData(newPlayer);
                }
            })
            .exceptionally(e -> {
                org.bukkit.Bukkit.getLogger().warning("Failed to load player data for " + player.getName() + ": " + e.getMessage());
                return null;
            });
    }

    public void unloadPlayer(UUID uuid) {
        NitroPlayer player = players.remove(uuid);
        if (player != null) {
            java.util.Map<String, Object> updates = java.util.Map.of("last_join", System.currentTimeMillis());
            playerTable.updateMultiple("uuid", uuid.toString(), updates);
        }
    }

    public NitroPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<NitroPlayer> getOnlinePlayers() {
        return players.values();
    }

    private void insertPlayerData(NitroPlayer player) {
        playerTable.save(player)
            .thenAccept(v -> {
                players.put(player.getUniqueId(), player);
                AsyncDataIntegrityCheckEvent event = new AsyncDataIntegrityCheckEvent(player.getUniqueId().toString(), player);
                event.setValid(true);
                eventBus.post(event);
            })
            .exceptionally(e -> {
                org.bukkit.Bukkit.getLogger().warning("Failed to insert player data for " + player.getName() + ": " + e.getMessage());
                return null;
            });
    }
}
