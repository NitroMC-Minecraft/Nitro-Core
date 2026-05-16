package de.grimlock.nitromc.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class CooldownManager implements IService {

    private final Cache<String, Long> cooldowns = Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .build();

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        cooldowns.invalidateAll();
    }

    public void setCooldown(UUID uuid, String key, long durationMs) {
        cooldowns.put(uuid.toString() + ":" + key, System.currentTimeMillis() + durationMs);
    }

    public boolean hasCooldown(UUID uuid, String key) {
        String fullKey = uuid.toString() + ":" + key;
        Long expiry = cooldowns.getIfPresent(fullKey);
        if (expiry == null) return false;
        if (expiry <= System.currentTimeMillis()) {
            cooldowns.invalidate(fullKey);
            return false;
        }
        return true;
    }

    public long getRemaining(UUID uuid, String key) {
        String fullKey = uuid.toString() + ":" + key;
        Long expiry = cooldowns.getIfPresent(fullKey);
        if (expiry == null) return 0;
        return Math.max(0, expiry - System.currentTimeMillis());
    }
}
