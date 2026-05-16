package de.grimlock.nitromc.player;

import de.grimlock.nitromc.database.DatabasePriority;
import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.event.AsyncDataIntegrityCheckEvent;
import de.grimlock.nitromc.event.NitroEventBus;
import de.grimlock.nitromc.event.PreDataLoadEvent;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import de.grimlock.nitromc.service.IService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class NitroPlayerManager implements IService {

    private final DatabaseService databaseService;
    private final LuckPermsService luckPermsService;
    private final NitroEventBus eventBus;
    private final ConcurrentHashMap<UUID, NitroPlayer> players = new ConcurrentHashMap<>();

    @Inject
    public NitroPlayerManager(DatabaseService databaseService, LuckPermsService luckPermsService, NitroEventBus eventBus) {
        this.databaseService = databaseService;
        this.luckPermsService = luckPermsService;
        this.eventBus = eventBus;
    }

    @Override
    public void onEnable() {
        databaseService.executeUpdateAsync(DatabasePriority.HIGH,
            "CREATE TABLE IF NOT EXISTS nitro_players (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "name VARCHAR(16) NOT NULL, " +
            "first_join BIGINT NOT NULL, " +
            "last_join BIGINT NOT NULL" +
            ")"
        ).thenAccept(v -> Bukkit.getLogger().info("NitroPlayer table initialized"))
            .exceptionally(e -> {
                Bukkit.getLogger().warning("Failed to initialize NitroPlayer table: " + e.getMessage());
                return null;
            });
    }

    @Override
    public void onDisable() {
        players.clear();
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        PreDataLoadEvent event = new PreDataLoadEvent(uuid);
        Bukkit.getPluginManager().callEvent(event);

        databaseService.table("nitro_players")
            .select()
            .where("uuid = ?", uuid.toString())
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
                    insertPlayerData(uuid, newPlayer);
                }
            })
            .exceptionally(e -> {
                Bukkit.getLogger().warning("Failed to load player data for " + player.getName() + ": " + e.getMessage());
                return null;
            });
    }

    public void unloadPlayer(UUID uuid) {
        NitroPlayer player = players.remove(uuid);
        if (player != null) {
            long now = System.currentTimeMillis();
            databaseService.executeUpdateAsync(DatabasePriority.MEDIUM,
                "UPDATE nitro_players SET last_join = ? WHERE uuid = ?",
                now, uuid.toString()
            );
        }
    }

    public NitroPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public Collection<NitroPlayer> getOnlinePlayers() {
        return players.values();
    }

    private void insertPlayerData(UUID uuid, NitroPlayer player) {
        databaseService.executeUpdateAsync(DatabasePriority.MEDIUM,
            "INSERT INTO nitro_players (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)",
            uuid.toString(), player.getName(), player.getFirstJoin(), player.getLastJoin()
        ).thenAccept(v -> {
            players.put(uuid, player);
            AsyncDataIntegrityCheckEvent event = new AsyncDataIntegrityCheckEvent(uuid.toString(), player);
            event.setValid(true);
            eventBus.post(event);
        })
        .exceptionally(e -> {
            Bukkit.getLogger().warning("Failed to insert player data for " + player.getName() + ": " + e.getMessage());
            return null;
        });
    }
}
