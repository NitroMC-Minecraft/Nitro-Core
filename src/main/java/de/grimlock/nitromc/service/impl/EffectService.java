package de.grimlock.nitromc.service.impl;

import de.grimlock.nitromc.Main;
import de.grimlock.nitromc.service.IService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EffectService implements IService {

    private final Main plugin;

    @Inject
    public EffectService(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public void playSoundAsync(Location location, Sound sound, float volume, float pitch) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            location.getWorld().playSound(location, sound, volume, pitch);
        });
    }

    public void spawnParticleAsync(Location location, Particle particle, int count) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            location.getWorld().spawnParticle(particle, location, count);
        });
    }
}
