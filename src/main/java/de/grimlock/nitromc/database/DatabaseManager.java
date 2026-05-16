package de.grimlock.nitromc.database;

import de.grimlock.nitromc.service.IService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class DatabaseManager implements IService {

    private final DatabaseService databaseService;
    private final Map<Class<? extends ManagedTable<?>>, ManagedTable<?>> registry
        = new LinkedHashMap<>();

    @Inject
    public DatabaseManager(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public <T> void register(Class<? extends ManagedTable<T>> tableClass, ManagedTable<T> instance) {
        registry.put(tableClass, instance);
    }

    @SuppressWarnings("unchecked")
    public <T, R extends ManagedTable<T>> R get(Class<R> tableClass) {
        ManagedTable<?> table = registry.get(tableClass);
        if (table == null) {
            throw new IllegalStateException("Table " + tableClass.getSimpleName() + " not registered");
        }
        return (R) table;
    }

    @Override
    public void onEnable() {
        CompletableFuture<Void>[] futures = registry.values().stream()
            .map(ManagedTable::createTableIfNotExists)
            .toArray(CompletableFuture[]::new);

        if (futures.length > 0) {
            CompletableFuture.allOf(futures)
                .orTimeout(30, TimeUnit.SECONDS)
                .join();
        }
    }

    @Override
    public void onDisable() {
        registry.clear();
    }
}
