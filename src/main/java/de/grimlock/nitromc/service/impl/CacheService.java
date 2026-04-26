package de.grimlock.nitromc.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class CacheService implements IService {

    private Cache<String, Object> generalCache;

    @Override
    public void onEnable() {
        this.generalCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    @Override
    public void onDisable() {
        generalCache.invalidateAll();
    }

    public void put(String key, Object value) {
        generalCache.put(key, value);
    }

    public Object get(String key) {
        return generalCache.getIfPresent(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object val = generalCache.getIfPresent(key);
        if (val == null) return null;
        return clazz.cast(val);
    }

    public void invalidate(String key) {
        generalCache.invalidate(key);
    }
}
