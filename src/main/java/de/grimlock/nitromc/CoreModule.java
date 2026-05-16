package de.grimlock.nitromc;

import com.google.inject.AbstractModule;
import de.grimlock.nitromc.cache.NitroCache;
import de.grimlock.nitromc.config.ConfigService;
import de.grimlock.nitromc.database.DatabaseManager;
import de.grimlock.nitromc.database.DatabasePerformanceMonitor;
import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.event.NitroEventBus;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import de.grimlock.nitromc.listener.PlayerDataListener;
import de.grimlock.nitromc.player.NitroPlayerManager;
import de.grimlock.nitromc.service.impl.CooldownManager;
import de.grimlock.nitromc.service.impl.DiagnosticsService;
import de.grimlock.nitromc.service.impl.EffectService;
import de.grimlock.nitromc.service.impl.MessageService;
import de.grimlock.nitromc.thread.ThreadingService;

public class CoreModule extends AbstractModule {

    private final Main plugin;

    public CoreModule(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    protected void configure() {
        bind(Main.class).toInstance(plugin);
        bind(ThreadingService.class).asEagerSingleton();
        bind(NitroEventBus.class).asEagerSingleton();
        bind(ConfigService.class).asEagerSingleton();
        bind(NitroCache.class).asEagerSingleton();
        bind(DatabasePerformanceMonitor.class).asEagerSingleton();
        bind(DatabaseService.class).asEagerSingleton();
        bind(DatabaseManager.class).asEagerSingleton();
        bind(DiagnosticsService.class).asEagerSingleton();
        bind(CooldownManager.class).asEagerSingleton();
        bind(EffectService.class).asEagerSingleton();
        bind(MessageService.class).asEagerSingleton();
        bind(LuckPermsService.class).asEagerSingleton();
        bind(NitroPlayerManager.class).asEagerSingleton();
        bind(PlayerDataListener.class).asEagerSingleton();
    }
}
