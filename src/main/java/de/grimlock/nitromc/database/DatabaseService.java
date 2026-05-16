package de.grimlock.nitromc.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.grimlock.nitromc.config.ConfigService;
import de.grimlock.nitromc.event.NitroEventBus;
import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.thread.ThreadingService;
import org.bukkit.configuration.file.FileConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Singleton
public class DatabaseService implements IService {

    private final ConfigService configService;
    private final DatabasePerformanceMonitor performanceMonitor;
    private final ThreadingService threadingService;
    private final NitroEventBus eventBus;

    private HikariDataSource dataSource;
    private PriorityBlockingQueue<DatabaseTask<?>> taskQueue;
    private boolean running = true;

    // Circuit Breaker State
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicBoolean circuitOpen = new AtomicBoolean(false);
    private static final int FAILURE_THRESHOLD = 5;

    @Inject
    public DatabaseService(ConfigService configService, DatabasePerformanceMonitor performanceMonitor, 
                           ThreadingService threadingService, NitroEventBus eventBus) {
        this.configService = configService;
        this.performanceMonitor = performanceMonitor;
        this.threadingService = threadingService;
        this.eventBus = eventBus;
    }

    @Override
    public void onEnable() {
        FileConfiguration config = configService.getConfig("mysql").get();
        config.addDefault("host", "localhost");
        config.addDefault("port", 3306);
        config.addDefault("database", "nitrocore");
        config.addDefault("username", "root");
        config.addDefault("password", "");
        configService.getConfig("mysql").save();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getString("host") + ":" + config.getInt("port") + "/" + config.getString("database"));
        hikariConfig.setUsername(config.getString("username"));
        hikariConfig.setPassword(config.getString("password"));
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setIdleTimeout(600_000);
        hikariConfig.setMaxLifetime(1_800_000);
        hikariConfig.setKeepaliveTime(60_000);

        this.dataSource = new HikariDataSource(hikariConfig);
        this.taskQueue = new PriorityBlockingQueue<>();

        startWorker();
    }

    private void startWorker() {
        int workers = Math.min(4, 10);
        for (int i = 0; i < workers; i++) {
            threadingService.getIoExecutor().submit(() -> {
                while (running) {
                    try {
                        DatabaseTask<?> task = taskQueue.poll(500, TimeUnit.MILLISECONDS);
                        if (task != null) {
                            if (circuitOpen.get()) {
                                task.getFuture().completeExceptionally(new RuntimeException("Database Circuit is OPEN"));
                                continue;
                            }
                            task.execute();
                            failureCount.set(0);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        handleFailure();
                    }
                }
            });
        }
    }

    private void handleFailure() {
        if (failureCount.incrementAndGet() >= FAILURE_THRESHOLD) {
            this.circuitOpen.set(true);
            eventBus.post(new DatabaseFailureEvent(true));
            threadingService.getScheduler().schedule(
                () -> {
                    this.circuitOpen.set(false);
                    this.failureCount.set(0);
                    eventBus.post(new DatabaseFailureEvent(false));
                },
                30, TimeUnit.SECONDS
            );
        }
    }

    @Override
    public void onDisable() {
        running = false;
        if (dataSource != null) dataSource.close();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public <T> CompletableFuture<T> submitTask(DatabasePriority priority, DatabaseTask.DatabaseAction<T> action) {
        DatabaseTask<T> task = new DatabaseTask<>(priority, action);
        taskQueue.add(task);
        return task.getFuture();
    }

    public Table table(String name) {
        return new Table(this, name);
    }

    public DatabasePerformanceMonitor getPerformanceMonitor() {
        return performanceMonitor;
    }

    public CompletableFuture<Void> executeUpdateAsync(DatabasePriority priority, String sql, Object... params) {
        return submitTask(priority, () -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
            } finally {
                performanceMonitor.record(sql, System.currentTimeMillis() - start);
            }
            return null;
        });
    }

    public <T> CompletableFuture<T> executeQueryAsync(DatabasePriority priority, String sql, Function<ResultSet, T> mapper, Object... params) {
        return submitTask(priority, () -> {
            long start = System.currentTimeMillis();
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    T result = mapper.apply(rs);
                    performanceMonitor.record(sql, System.currentTimeMillis() - start);
                    return result;
                }
            }
        });
    }

    public static class DatabaseFailureEvent {
        private final boolean opened;
        public DatabaseFailureEvent(boolean opened) { this.opened = opened; }
        public boolean isOpened() { return opened; }
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
