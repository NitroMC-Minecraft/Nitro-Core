package de.grimlock.nitromc;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.grimlock.nitromc.cache.NitroCache;
import de.grimlock.nitromc.config.ConfigService;
import de.grimlock.nitromc.database.DatabaseManager;
import de.grimlock.nitromc.database.DatabasePerformanceMonitor;
import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.event.NitroEventBus;
import de.grimlock.nitromc.handler.CommandHandler;
import de.grimlock.nitromc.handler.ListenerHandler;
import de.grimlock.nitromc.integration.luckperms.LuckPermsService;
import de.grimlock.nitromc.listener.PlayerDataListener;
import de.grimlock.nitromc.player.NitroPlayerManager;
import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.service.impl.CooldownManager;
import de.grimlock.nitromc.service.impl.DiagnosticsService;
import de.grimlock.nitromc.service.impl.EffectService;
import de.grimlock.nitromc.service.impl.MessageService;
import de.grimlock.nitromc.thread.ThreadingService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {

    private static Main instance;
    private Injector injector;
    private final List<IService> services = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        // Initialize Guice
        this.injector = Guice.createInjector(new CoreModule(this));

        // Start core services in specific order
        startService(ThreadingService.class);
        startService(NitroEventBus.class);
        startService(ConfigService.class);
        startService(NitroCache.class);
        startService(DatabasePerformanceMonitor.class);
        startService(DatabaseService.class);
        DatabaseManager databaseManager = injector.getInstance(DatabaseManager.class);
        startService(DatabaseManager.class);
        databaseManager.scanAndRegister("de.grimlock.nitromc");
        startService(DiagnosticsService.class);
        startService(CooldownManager.class);
        startService(EffectService.class);
        startService(MessageService.class);
        startService(LuckPermsService.class);
        startService(NitroPlayerManager.class);
        startService(PlayerDataListener.class);

        // Register Commands and Listeners via Reflection
        injector.getInstance(CommandHandler.class).registerCommands("de.grimlock.nitromc");
        injector.getInstance(ListenerHandler.class).registerListeners("de.grimlock.nitromc");

        getLogger().info("NitroCore 'God Mode' Architecture enabled!");
    }

    @Override
    public void onDisable() {
        // Disable in reverse
        for (int i = services.size() - 1; i >= 0; i--) {
            services.get(i).onDisable();
        }
        getLogger().info("NitroCore disabled!");
    }

    private <T extends IService> void startService(Class<T> serviceClass) {
        T service = injector.getInstance(serviceClass);
        service.onEnable();
        services.add(service);
    }

    public static Main getInstance() {
        return instance;
    }

    public Injector getInjector() {
        return injector;
    }
}
