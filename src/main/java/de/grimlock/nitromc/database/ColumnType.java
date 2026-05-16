package de.grimlock.nitromc.database;

public enum ColumnType {
    VARCHAR, TEXT, INT, BIGINT, DOUBLE, BOOLEAN, BLOB, TIMESTAMP, DATETIME;

    public String toSql(int length) {
        return this == VARCHAR ? "VARCHAR(" + length + ")" : name();
    }
}
