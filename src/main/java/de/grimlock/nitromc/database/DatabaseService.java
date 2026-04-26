package de.grimlock.nitromc.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
public class DatabaseService implements IService {

    private HikariDataSource dataSource;
    private ExecutorService databaseExecutor;

    @Override
    public void onEnable() {
        // Initial setup - usually these would come from a config file
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://localhost:3306/nitrocore");
        config.setUsername("root");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(config);
        this.databaseExecutor = Executors.newFixedThreadPool(4);
    }

    @Override
    public void onDisable() {
        if (dataSource != null) {
            dataSource.close();
        }
        if (databaseExecutor != null) {
            databaseExecutor.shutdown();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void executeUpdateAsync(String sql, Object... params) {
        databaseExecutor.submit(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public <T> CompletableFuture<T> executeQueryAsync(String sql, Function<ResultSet, T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    return mapper.apply(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, databaseExecutor);
    }
}
