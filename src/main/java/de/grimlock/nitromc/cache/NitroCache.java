package de.grimlock.nitromc.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import de.grimlock.nitromc.service.IService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
public class NitroCache implements IService {

    private final Cache<String, Object> storage;
    private final ConcurrentHashMap<String, Long> deadlineNanos;
    private final List<Consumer<Map.Entry<String, Object>>> writeBehindActions;
    private final long defaultTtlNanos;
    private final boolean statsEnabled;

    private final Map<String, Set<Consumer<String>>> channels = new ConcurrentHashMap<>();

    @Inject
    public NitroCache() {
        this(new Builder());
    }

    private NitroCache(Builder builder) {
        this.defaultTtlNanos = builder.defaultTtlNanos;
        this.statsEnabled = builder.stats;
        this.deadlineNanos = new ConcurrentHashMap<>();
        this.writeBehindActions = new CopyOnWriteArrayList<>();

        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
                .maximumSize(builder.maxSize);

        if (statsEnabled) {
            caffeineBuilder.recordStats();
        }

        Expiry<String, Object> expiry = new Expiry<String, Object>() {
            @Override
            public long expireAfterCreate(String key, Object value, long currentTime) {
                long deadline = deadlineNanos.getOrDefault(key, currentTime + defaultTtlNanos);
                return Math.max(1, deadline - currentTime);
            }

            @Override
            public long expireAfterUpdate(String key, Object value, long currentTime, long currentDuration) {
                long deadline = deadlineNanos.getOrDefault(key, currentTime + defaultTtlNanos);
                return Math.max(1, deadline - currentTime);
            }

            @Override
            public long expireAfterRead(String key, Object value, long currentTime, long currentDuration) {
                return currentDuration;
            }
        };

        this.storage = caffeineBuilder.expireAfter(expiry).build();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        storage.invalidateAll();
        deadlineNanos.clear();
        writeBehindActions.clear();
        channels.clear();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long maxSize = 50_000;
        private long defaultTtlNanos = TimeUnit.MINUTES.toNanos(30);
        private boolean stats = false;

        public Builder maxSize(long size) {
            this.maxSize = size;
            return this;
        }

        public Builder defaultTtl(long duration, TimeUnit unit) {
            this.defaultTtlNanos = unit.toNanos(duration);
            return this;
        }

        public Builder recordStats() {
            this.stats = true;
            return this;
        }

        public NitroCache build() {
            return new NitroCache(this);
        }
    }

    public void addWriteBehindAction(Consumer<Map.Entry<String, Object>> action) {
        writeBehindActions.add(action);
    }

    public record CacheUpdate(String key, Object value) {
    }

    public void set(String key, Object value) {
        set(key, value, defaultTtlNanos, TimeUnit.NANOSECONDS);
    }

    public void set(String key, Object value, long ttl, TimeUnit unit) {
        long deadline = System.nanoTime() + unit.toNanos(ttl);
        deadlineNanos.put(key, deadline);
        storage.put(key, value);
        notifyWriteBehind(key, value);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object val = storage.getIfPresent(key);
        return val != null ? clazz.cast(val) : null;
    }

    public <T> T getOrLoad(String key, Class<T> type, Function<String, T> loader) {
        Object val = storage.get(key, k -> loader.apply(k));
        return val != null ? type.cast(val) : null;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> clazz) {
        return get(key, clazz);
    }

    public void delete(String key) {
        storage.invalidate(key);
        deadlineNanos.remove(key);
    }

    public void invalidateAll(Collection<String> keys) {
        storage.invalidateAll(keys);
        keys.forEach(deadlineNanos::remove);
    }

    private void notifyWriteBehind(String key, Object value) {
        if (!writeBehindActions.isEmpty()) {
            Map.Entry<String, Object> entry = Map.entry(key, value);
            writeBehindActions.forEach(action -> action.accept(entry));
        }
    }

    public long incr(String key) {
        Object val = storage.get(key, k -> new AtomicLong(0));
        if (val instanceof AtomicLong) {
            long newVal = ((AtomicLong) val).incrementAndGet();
            notifyWriteBehind(key, newVal);
            return newVal;
        }
        return -1;
    }

    public boolean exists(String key) {
        return storage.getIfPresent(key) != null;
    }

    // --- Pub/Sub System ---

    public void publish(String channel, String message) {
        Set<Consumer<String>> subscribers = channels.get(channel);
        if (subscribers != null) {
            subscribers.forEach(sub -> sub.accept(message));
        }
    }

    public void subscribe(String channel, Consumer<String> callback) {
        channels.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(callback);
    }

    public void unsubscribe(String channel, Consumer<String> subscriber) {
        channels.computeIfPresent(channel, (k, set) -> {
            set.remove(subscriber);
            return set.isEmpty() ? null : set;
        });
    }
}
