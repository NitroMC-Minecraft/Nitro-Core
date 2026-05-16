package de.grimlock.nitromc.database;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MigrationRunner {
    private static final Logger logger = Logger.getLogger("NitroCore");
    private static final String MIGRATIONS_TABLE = "schema_migrations";

    private final DatabaseService databaseService;
    private final List<Migration> migrations = new ArrayList<>();

    @Inject
    public MigrationRunner(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public MigrationRunner version(int version, String description, MigrationAction action) {
        if (version <= 0) {
            throw new IllegalArgumentException("Version must be > 0");
        }
        for (Migration m : migrations) {
            if (m.version == version) {
                throw new IllegalArgumentException("Version " + version + " already added");
            }
        }
        migrations.add(new Migration(version, description, action));
        return this;
    }

    public CompletableFuture<Integer> run() {
        return databaseService.submitTask(DatabasePriority.HIGH, () -> {
            try (Connection conn = databaseService.getConnection()) {
                ensureMigrationsTable(conn);

                Set<Integer> appliedVersions = loadAppliedVersions(conn);

                List<Migration> pending = new ArrayList<>();
                for (Migration m : migrations) {
                    if (!appliedVersions.contains(m.version)) {
                        pending.add(m);
                    }
                }

                pending.sort((a, b) -> Integer.compare(a.version, b.version));

                int count = 0;
                for (Migration m : pending) {
                    try {
                        m.action.run(conn);
                        recordMigration(conn, m.version, m.description);
                        count++;
                        logger.log(Level.INFO, "Applied migration " + m.version + ": " + m.description);
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, "Migration " + m.version + " failed", e);
                        throw e;
                    }
                }

                return count;
            }
        });
    }

    private void ensureMigrationsTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS " + MIGRATIONS_TABLE + " (" +
            "version INT NOT NULL PRIMARY KEY, " +
            "description VARCHAR(255) NOT NULL, " +
            "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        }
    }

    private Set<Integer> loadAppliedVersions(Connection conn) throws SQLException {
        Set<Integer> versions = new HashSet<>();
        String sql = "SELECT version FROM " + MIGRATIONS_TABLE;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                versions.add(rs.getInt("version"));
            }
        }

        return versions;
    }

    private void recordMigration(Connection conn, int version, String description) throws SQLException {
        String sql = "INSERT INTO " + MIGRATIONS_TABLE + " (version, description) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, version);
            stmt.setString(2, description);
            stmt.execute();
        }
    }

    public record Migration(int version, String description, MigrationAction action) {
    }

    @FunctionalInterface
    public interface MigrationAction {
        void run(Connection connection) throws SQLException;
    }
}
