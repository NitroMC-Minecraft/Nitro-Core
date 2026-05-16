package de.grimlock.nitromc.database;

import java.util.Map;

public interface Saveable {
    Map<String, Object> toRow();

    String getPrimaryKeyColumn();

    Object getPrimaryKeyValue();
}
