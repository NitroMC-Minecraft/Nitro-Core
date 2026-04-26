package de.grimlock.nitromc;

import com.google.inject.AbstractModule;
import de.grimlock.nitromc.config.ConfigService;
import de.grimlock.nitromc.database.DatabasePerformanceMonitor;
import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import de.grimlock.nitromc.service.impl.CacheService;
import de.grimlock.nitromc.service.impl.CooldownManager;
import de.grimlock.nitromc.service.impl.EffectService;
import de.grimlock.nitromc.service.impl.MessageService;

public class CoreModule extends AbstractModule {

    private final Main plugin;

    public CoreModule(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Main.class).toInstance(plugin);
        bind(ConfigService.class).asEagerSingleton();
        bind(DatabasePerformanceMonitor.class).asEagerSingleton();
        bind(DatabaseService.class).asEagerSingleton();
        bind(CacheService.class).asEagerSingleton();
        bind(CooldownManager.class).asEagerSingleton();
        bind(EffectService.class).asEagerSingleton();
        bind(MessageService.class).asEagerSingleton();
        bind(LuckPermsService.class).asEagerSingleton();
    }
}
