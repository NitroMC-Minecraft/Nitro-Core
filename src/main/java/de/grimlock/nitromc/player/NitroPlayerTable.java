package de.grimlock.nitromc.player;

import de.grimlock.nitromc.database.AutoTable;
import de.grimlock.nitromc.database.ColumnDef;
import de.grimlock.nitromc.database.ColumnType;
import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.database.ManagedTable;
import de.grimlock.nitromc.database.TableSchema;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@AutoTable
public class NitroPlayerTable extends ManagedTable<NitroPlayer> {

    @Inject
    public NitroPlayerTable(DatabaseService databaseService) {
        super(databaseService);
    }

    @Override
    public TableSchema defineSchema() {
        return TableSchema.of("nitro_players")
            .column(ColumnDef.of("uuid", ColumnType.VARCHAR, 36).primaryKey().notNull())
            .column(ColumnDef.of("name", ColumnType.VARCHAR, 16).notNull())
            .column(ColumnDef.of("first_join", ColumnType.BIGINT).notNull())
            .column(ColumnDef.of("last_join", ColumnType.BIGINT).notNull());
    }

    @Override
    public NitroPlayer mapRow(ResultSet rs) throws SQLException {
        String uuid = rs.getString("uuid");
        String name = rs.getString("name");
        long firstJoin = rs.getLong("first_join");
        long lastJoin = rs.getLong("last_join");
        org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(UUID.fromString(uuid));
        if (bukkitPlayer != null) {
            return new NitroPlayer(bukkitPlayer, null, name, firstJoin, lastJoin);
        }
        return null;
    }

    @Override
    protected Map<String, Object> toRow(NitroPlayer entity) {
        return Map.of(
            "uuid", entity.getUniqueId().toString(),
            "name", entity.getName(),
            "first_join", entity.getFirstJoin(),
            "last_join", entity.getLastJoin()
        );
    }
}
