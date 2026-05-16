package de.grimlock.nitromc.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class QueryBuilder {
    private final DatabaseService databaseService;
    private final String tableName;
    private final List<String> andWheres = new ArrayList<>();
    private final List<String> orWheres = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private final List<String> joins = new ArrayList<>();
    private final List<String> orderBys = new ArrayList<>();
    private String[] projection = null;
    private Integer limitVal = null;
    private Integer offsetVal = null;
    private DatabasePriority priority = DatabasePriority.MEDIUM;

    public QueryBuilder(DatabaseService databaseService, String tableName) {
        this.databaseService = databaseService;
        this.tableName = tableName;
    }

    public QueryBuilder where(String condition, Object... params) {
        andWheres.add(condition);
        for (Object param : params) {
            this.params.add(param);
        }
        return this;
    }

    public QueryBuilder orWhere(String condition, Object... params) {
        orWheres.add(condition);
        for (Object param : params) {
            this.params.add(param);
        }
        return this;
    }

    public QueryBuilder join(String table, String onCondition) {
        joins.add("INNER JOIN `" + table + "` ON " + onCondition);
        return this;
    }

    public QueryBuilder leftJoin(String table, String onCondition) {
        joins.add("LEFT JOIN `" + table + "` ON " + onCondition);
        return this;
    }

    public QueryBuilder orderBy(String column) {
        orderBys.add("`" + column + "` ASC");
        return this;
    }

    public QueryBuilder orderByDesc(String column) {
        orderBys.add("`" + column + "` DESC");
        return this;
    }

    public QueryBuilder limit(int n) {
        this.limitVal = n;
        return this;
    }

    public QueryBuilder offset(int n) {
        this.offsetVal = n;
        return this;
    }

    public QueryBuilder select(String... columns) {
        this.projection = columns;
        return this;
    }

    public QueryBuilder priority(DatabasePriority priority) {
        this.priority = priority;
        return this;
    }

    public <T> CompletableFuture<T> mapTo(DatabaseService.ResultSetMapper<T> mapper) {
        String selectClause = projection != null
            ? String.join(", ", projection)
            : "*";

        String sql = buildSql(selectClause);
        if (limitVal == null) {
            sql += " LIMIT 1";
        }

        return databaseService.executeQueryAsync(priority, sql, rs -> {
            try {
                return rs.next() ? mapper.map(rs) : null;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }

    public <T> CompletableFuture<List<T>> mapToList(DatabaseService.ResultSetMapper<T> mapper) {
        String selectClause = projection != null
            ? String.join(", ", projection)
            : "*";

        String sql = buildSql(selectClause);

        return databaseService.executeQueryAsync(priority, sql, rs -> {
            List<T> results = new ArrayList<>();
            try {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return results;
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Long> count() {
        String sql = buildSql("COUNT(*)");
        sql += " LIMIT 1";

        return databaseService.executeQueryAsync(priority, sql, rs -> {
            try {
                return rs.next() ? rs.getLong(1) : 0L;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }

    public CompletableFuture<Boolean> exists() {
        String sql = buildSql("1");
        sql += " LIMIT 1";

        return databaseService.executeQueryAsync(priority, sql, rs -> {
            try {
                return rs.next();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }, params.toArray()).orTimeout(10, TimeUnit.SECONDS);
    }

    private String buildSql(String selectClause) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(selectClause).append(" FROM `").append(tableName).append("`");

        for (String join : joins) {
            sb.append(" ").append(join);
        }

        if (!andWheres.isEmpty() || !orWheres.isEmpty()) {
            sb.append(" WHERE ");

            if (!andWheres.isEmpty()) {
                String andClause = andWheres.stream()
                    .collect(Collectors.joining(" AND "));
                sb.append("(").append(andClause).append(")");
            }

            for (String orWhere : orWheres) {
                if (!andWheres.isEmpty() || orWheres.indexOf(orWhere) > 0) {
                    sb.append(" OR ");
                }
                sb.append(orWhere);
            }
        }

        if (!orderBys.isEmpty()) {
            sb.append(" ORDER BY ").append(String.join(", ", orderBys));
        }

        if (limitVal != null) {
            sb.append(" LIMIT ").append(limitVal);
            if (offsetVal != null) {
                sb.append(" OFFSET ").append(offsetVal);
            }
        }

        return sb.toString();
    }
}
