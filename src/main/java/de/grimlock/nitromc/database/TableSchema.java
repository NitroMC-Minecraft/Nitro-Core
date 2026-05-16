package de.grimlock.nitromc.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableSchema {
    private final String tableName;
    private final List<ColumnDef> columns = new ArrayList<>();
    private final List<String[]> foreignKeys = new ArrayList<>();

    private TableSchema(String tableName) {
        this.tableName = tableName;
    }

    public static TableSchema of(String tableName) {
        return new TableSchema(tableName);
    }

    public TableSchema column(ColumnDef def) {
        columns.add(def);
        return this;
    }

    public TableSchema foreignKey(String column, String referencedTable, String referencedColumn) {
        foreignKeys.add(new String[]{column, referencedTable, referencedColumn});
        return this;
    }

    public String toCreateDdl() {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(tableName).append("` (\n");

        String columnDdls = columns.stream()
            .map(ColumnDef::toDdl)
            .collect(Collectors.joining(",\n  "));
        sb.append("  ").append(columnDdls);

        for (String[] fk : foreignKeys) {
            sb.append(",\n  FOREIGN KEY (`").append(fk[0]).append("`) REFERENCES `")
              .append(fk[1]).append("`(`").append(fk[2]).append("`)");
        }

        sb.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        return sb.toString();
    }

    public String getTableName() {
        return tableName;
    }

    public List<ColumnDef> getColumns() {
        return new ArrayList<>(columns);
    }

    public List<String> getColumnNames() {
        return columns.stream().map(ColumnDef::getName).collect(Collectors.toList());
    }

    public String getPrimaryKeyColumn() {
        return columns.stream()
            .filter(ColumnDef::isPrimaryKey)
            .map(ColumnDef::getName)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No primary key defined for table " + tableName));
    }
}
