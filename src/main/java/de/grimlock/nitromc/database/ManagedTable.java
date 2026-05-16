package de.grimlock.nitromc.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class ManagedTable<T> extends Table {

    private final TableSchema schema;

    protected ManagedTable(DatabaseService databaseService) {
        super(databaseService, "");
        this.schema = defineSchema();
        this.tableName = schema.getTableName();
    }

    public abstract TableSchema defineSchema();

    public abstract T mapRow(ResultSet rs) throws SQLException;

    public CompletableFuture<Void> createTableIfNotExists() {
        String ddl = schema.toCreateDdl();
        return databaseService.executeUpdateAsync(DatabasePriority.HIGH, ddl);
    }

    public CompletableFuture<Optional<T>> findById(Object id) {
        String pkCol = schema.getPrimaryKeyColumn();
        return query()
            .where("`" + pkCol + "` = ?", id)
            .limit(1)
            .mapTo(this::mapRow)
            .thenApply(Optional::ofNullable);
    }

    public CompletableFuture<List<T>> findAll() {
        return query().mapToList(this::mapRow);
    }

    public CompletableFuture<List<T>> findWhere(String column, Object value) {
        return query()
            .where("`" + column + "` = ?", value)
            .mapToList(this::mapRow);
    }

    public CompletableFuture<Void> save(T entity) {
        Saveable saveable = castSaveable(entity);
        Map<String, Object> row = saveable.toRow();
        String pkCol = saveable.getPrimaryKeyColumn();

        String keys = String.join(", ", row.keySet().stream()
            .map(k -> "`" + k + "`")
            .collect(Collectors.toList()));

        String placeholders = row.keySet().stream()
            .map(k -> "?")
            .collect(Collectors.joining(", "));

        String updateCols = row.keySet().stream()
            .filter(k -> !k.equals(pkCol))
            .map(k -> "`" + k + "`=VALUES(`" + k + "`)")
            .collect(Collectors.joining(", "));

        String sql = String.format(
            "INSERT INTO `%s` (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
            schema.getTableName(), keys, placeholders, updateCols
        );

        Object[] values = row.values().toArray();
        return databaseService.executeUpdateAsync(DatabasePriority.MEDIUM, sql, values)
            .orTimeout(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> updateMultiple(String whereCol, Object whereVal, Map<String, Object> updates) {
        String setCols = updates.keySet().stream()
            .map(k -> "`" + k + "` = ?")
            .collect(Collectors.joining(", "));

        String sql = String.format(
            "UPDATE `%s` SET %s WHERE `%s` = ?",
            schema.getTableName(), setCols, whereCol
        );

        List<Object> params = new ArrayList<>(updates.values());
        params.add(whereVal);

        return databaseService.executeUpdateAsync(
            DatabasePriority.MEDIUM,
            sql,
            params.toArray()
        ).orTimeout(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public CompletableFuture<Void> deleteById(Object id) {
        String pkCol = schema.getPrimaryKeyColumn();
        return delete(pkCol, id);
    }

    public CompletableFuture<Void> deleteWhere(String column, Object value) {
        return delete(column, value);
    }

    public CompletableFuture<Long> count() {
        return query().count();
    }

    public CompletableFuture<Boolean> exists(String column, Object value) {
        return query()
            .where("`" + column + "` = ?", value)
            .exists();
    }

    public CompletableFuture<Void> batchSave(List<T> entities) {
        if (entities.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Saveable firstSaveable = castSaveable(entities.get(0));
        String pkCol = firstSaveable.getPrimaryKeyColumn();

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (T entity : entities) {
            dataList.add(castSaveable(entity).toRow());
        }

        Map<String, Object> firstRow = dataList.get(0);
        String keys = String.join(", ", firstRow.keySet().stream()
            .map(k -> "`" + k + "`")
            .collect(Collectors.toList()));

        String placeholders = firstRow.keySet().stream()
            .map(k -> "?")
            .collect(Collectors.joining(", "));

        String updateCols = firstRow.keySet().stream()
            .filter(k -> !k.equals(pkCol))
            .map(k -> "`" + k + "`=VALUES(`" + k + "`)")
            .collect(Collectors.joining(", "));

        String sql = String.format(
            "INSERT INTO `%s` (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
            schema.getTableName(), keys, placeholders, updateCols
        );

        return databaseService.submitTask(DatabasePriority.MEDIUM, () -> {
            long start = System.currentTimeMillis();
            try (var conn = databaseService.getConnection();
                 var stmt = conn.prepareStatement(sql)) {

                conn.setAutoCommit(false);
                try {
                    for (Map<String, Object> row : dataList) {
                        Object[] values = row.values().toArray();
                        for (int i = 0; i < values.length; i++) {
                            stmt.setObject(i + 1, values[i]);
                        }
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                    conn.commit();
                    databaseService.getPerformanceMonitor().record(sql, System.currentTimeMillis() - start);
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
            return null;
        }).<Void>thenApply(v -> null).orTimeout(10, java.util.concurrent.TimeUnit.SECONDS);
    }

    public QueryBuilder query() {
        return new QueryBuilder(databaseService, schema.getTableName());
    }

    public TableSchema getSchema() {
        return schema;
    }

    private Saveable castSaveable(T entity) {
        if (!(entity instanceof Saveable)) {
            throw new IllegalArgumentException(
                entity.getClass().getSimpleName() + " must implement Saveable to use save()/batchSave()");
        }
        return (Saveable) entity;
    }
}
