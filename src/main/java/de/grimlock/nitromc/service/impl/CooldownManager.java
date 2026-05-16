package de.grimlock.nitromc.service.impl;

import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class CooldownManager implements IService {

    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        cooldowns.clear();
    }

    public void setCooldown(UUID uuid, String key, long durationMs) {
        cooldowns.put(uuid.toString() + ":" + key, System.currentTimeMillis() + durationMs);
    }

    public boolean hasCooldown(UUID uuid, String key) {
        String fullKey = uuid.toString() + ":" + key;
        if (!cooldowns.containsKey(fullKey)) return false;
        if (cooldowns.get(fullKey) <= System.currentTimeMillis()) {
            cooldowns.remove(fullKey);
            return false;
        }
        return true;
    }

    public long getRemaining(UUID uuid, String key) {
        String fullKey = uuid.toString() + ":" + key;
        return Math.max(0, cooldowns.getOrDefault(fullKey, 0L) - System.currentTimeMillis());
    }
}
