package de.grimlock.nitromc.database;

public enum DatabasePriority {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    MONITOR(3);

    private final int level;

    DatabasePriority(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
