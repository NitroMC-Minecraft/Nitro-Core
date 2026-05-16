package de.grimlock.nitromc.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Table {

    private final DatabaseService databaseService;
    private final String tableName;

    public Table(DatabaseService databaseService, String tableName) {
        this.databaseService = databaseService;
        this.tableName = tableName;
    }

    public SelectBuilder select() {
        return new SelectBuilder(databaseService, tableName);
    }

    public CompletableFuture<Void> update(String identifierKey, Object identifierValue, String targetKey, Object targetValue) {
        return update(identifierKey, identifierValue, targetKey, targetValue, DatabasePriority.MEDIUM);
    }

    public CompletableFuture<Void> update(String identifierKey, Object identifierValue, String targetKey, Object targetValue, DatabasePriority priority) {
        String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, targetKey, identifierKey);
        return databaseService.executeUpdateAsync(priority, sql, targetValue, identifierValue)
                .orTimeout(5, TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> insert(Map<String, Object> data) {
        return insert(data, DatabasePriority.MEDIUM);
    }

    public CompletableFuture<Void> insert(Map<String, Object> data, DatabasePriority priority) {
        String keys = String.join(", ", data.keySet());
        String placeholders = data.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, keys, placeholders);
        
        return databaseService.executeUpdateAsync(priority, sql, data.values().toArray())
                .orTimeout(5, TimeUnit.SECONDS);
    }

    public <T> CompletableFuture<List<T>> select(String key, Object value, DatabaseService.ResultSetMapper<T> mapper) {
        return select(key, value, mapper, DatabasePriority.MEDIUM);
    }

    public <T> CompletableFuture<List<T>> select(String key, Object value, DatabaseService.ResultSetMapper<T> mapper, DatabasePriority priority) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, key);
        return databaseService.executeQueryAsync(priority, sql, rs -> {
            List<T> results = new ArrayList<>();
            try {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error mapping result set", e);
            }
            return results;
        }, value).orTimeout(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> delete(String key, Object value) {
        return delete(key, value, DatabasePriority.MEDIUM);
    }

    public CompletableFuture<Void> delete(String key, Object value, DatabasePriority priority) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, key);
        return databaseService.executeUpdateAsync(priority, sql, value)
                .orTimeout(5, TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> batchInsert(List<Map<String, Object>> dataList) {
        return batchInsert(dataList, DatabasePriority.MEDIUM);
    }

    public CompletableFuture<Void> batchInsert(List<Map<String, Object>> dataList, DatabasePriority priority) {
        if (dataList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Object> result = databaseService.submitTask(priority, () -> {
            long start = System.currentTimeMillis();
            Map<String, Object> firstRow = dataList.get(0);
            String keys = String.join(", ", firstRow.keySet());
            String placeholders = firstRow.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));
            String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, keys, placeholders);

            try (Connection conn = databaseService.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (Map<String, Object> row : dataList) {
                    Object[] values = row.values().toArray();
                    for (int i = 0; i < values.length; i++) {
                        stmt.setObject(i + 1, values[i]);
                    }
                    stmt.addBatch();
                }

                stmt.executeBatch();
                databaseService.getPerformanceMonitor().record(sql, System.currentTimeMillis() - start);
            }
            return null;
        });
        return result.<Void>thenApply(v -> null).orTimeout(10, TimeUnit.SECONDS);
    }
}
