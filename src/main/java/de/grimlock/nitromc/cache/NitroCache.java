package de.grimlock.nitromc.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Singleton
public class NitroCache implements IService {

    private final Cache<String, Object> storage = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(50_000)
            .build();

    private final Map<String, Long> customExpiry = new ConcurrentHashMap<>();

    private final Map<String, Set<Consumer<String>>> channels = new ConcurrentHashMap<>();
    private Consumer<CacheUpdate> writeBehindAction;

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        storage.invalidateAll();
        customExpiry.clear();
        channels.clear();
    }

    public void setWriteBehindAction(Consumer<CacheUpdate> action) {
        this.writeBehindAction = action;
    }

    public record CacheUpdate(String key, Object value) {}

    public void set(String key, Object value) {
        storage.put(key, value);
        if (writeBehindAction != null) {
            writeBehindAction.accept(new CacheUpdate(key, value));
        }
    }

    public void set(String key, Object value, long ttl, TimeUnit unit) {
        customExpiry.put(key, System.currentTimeMillis() + unit.toMillis(ttl));
        storage.put(key, value);
        if (writeBehindAction != null) {
            writeBehindAction.accept(new CacheUpdate(key, value));
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        Long expiry = customExpiry.get(key);
        if (expiry != null && System.currentTimeMillis() > expiry) {
            delete(key);
            return null;
        }
        Object val = storage.getIfPresent(key);
        return val != null ? clazz.cast(val) : null;
    }

    public void delete(String key) {
        storage.invalidate(key);
        customExpiry.remove(key);
    }

    public long incr(String key) {
        Object val = storage.get(key, k -> new AtomicLong(0));
        if (val instanceof AtomicLong) {
            long newVal = ((AtomicLong) val).incrementAndGet();
            if (writeBehindAction != null) {
                writeBehindAction.accept(new CacheUpdate(key, newVal));
            }
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
}
