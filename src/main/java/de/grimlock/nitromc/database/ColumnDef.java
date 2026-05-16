package de.grimlock.nitromc.database;

public class ColumnDef {
    private final String name;
    private final ColumnType type;
    private final int length;
    private boolean primaryKey = false;
    private boolean notNull = false;
    private boolean nullable = true;
    private boolean autoIncrement = false;
    private Object defaultValue = null;
    private boolean hasDefault = false;

    private ColumnDef(String name, ColumnType type, int length) {
        this.name = name;
        this.type = type;
        this.length = length;
    }

    public static ColumnDef of(String name, ColumnType type, int length) {
        return new ColumnDef(name, type, length);
    }

    public static ColumnDef of(String name, ColumnType type) {
        return new ColumnDef(name, type, 0);
    }

    public ColumnDef primaryKey() {
        this.primaryKey = true;
        this.notNull = true;
        this.nullable = false;
        return this;
    }

    public ColumnDef notNull() {
        this.notNull = true;
        this.nullable = false;
        return this;
    }

    public ColumnDef nullable() {
        this.nullable = true;
        this.notNull = false;
        return this;
    }

    public ColumnDef autoIncrement() {
        this.autoIncrement = true;
        return this;
    }

    public ColumnDef defaultValue(Object value) {
        this.defaultValue = value;
        this.hasDefault = true;
        return this;
    }

    public String toDdl() {
        StringBuilder sb = new StringBuilder();
        sb.append("`").append(name).append("` ").append(type.toSql(length));

        if (notNull || primaryKey) {
            sb.append(" NOT NULL");
        }

        if (autoIncrement) {
            sb.append(" AUTO_INCREMENT");
        }

        if (hasDefault) {
            sb.append(" DEFAULT ");
            if (defaultValue == null) {
                sb.append("NULL");
            } else if (defaultValue instanceof String) {
                sb.append("'").append(defaultValue).append("'");
            } else if (defaultValue instanceof Boolean) {
                sb.append(((Boolean) defaultValue) ? 1 : 0);
            } else {
                sb.append(defaultValue);
            }
        }

        if (primaryKey) {
            sb.append(" PRIMARY KEY");
        }

        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
