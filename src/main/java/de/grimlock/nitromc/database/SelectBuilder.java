package de.grimlock.nitromc.database;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SelectBuilder {

    private final DatabaseService databaseService;
    private final String tableName;
    private final List<String> wheres = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private DatabasePriority priority = DatabasePriority.MEDIUM;

    public SelectBuilder(DatabaseService databaseService, String tableName) {
        this.databaseService = databaseService;
        this.tableName = tableName;
    }

    public SelectBuilder where(String condition, Object... params) {
        wheres.add(condition);
        for (Object p : params) {
            this.params.add(p);
        }
        return this;
    }

    public SelectBuilder priority(DatabasePriority priority) {
        this.priority = priority;
        return this;
    }

    public <T> CompletableFuture<T> mapTo(DatabaseService.ResultSetMapper<T> mapper) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        return databaseService.executeQueryAsync(priority, sql.toString(), rs -> {
            try {
                if (rs.next()) {
                    return mapper.map(rs);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error mapping result set", e);
            }
            return null;
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }

    public <T> CompletableFuture<List<T>> mapToList(DatabaseService.ResultSetMapper<T> mapper) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        return databaseService.executeQueryAsync(priority, sql.toString(), rs -> {
            List<T> results = new ArrayList<>();
            try {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error mapping result set", e);
            }
            return results;
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }
}
