package de.grimlock.nitromc.database;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DatabaseTask<T> implements Comparable<DatabaseTask<?>> {

    private final DatabasePriority priority;
    private final DatabaseAction<T> action;
    private final CompletableFuture<T> future;

    public DatabaseTask(DatabasePriority priority, DatabaseAction<T> action) {
        this.priority = priority;
        this.action = action;
        this.future = new CompletableFuture<>();
    }

    public void execute() {
        try {
            T result = action.run();
            future.complete(result);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    @Override
    public int compareTo(@NotNull DatabaseTask<?> other) {
        return Integer.compare(other.priority.getLevel(), this.priority.getLevel());
    }

    @FunctionalInterface
    public interface DatabaseAction<T> {
        T run() throws Exception;
    }
}
