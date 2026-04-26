package de.grimlock.nitromc;

import de.grimlock.nitromc.service.ServiceFactory;
import de.grimlock.nitromc.service.impl.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private ServiceFactory serviceFactory;

    @Override
    public void onEnable() {
        instance = this;
        this.serviceFactory = new ServiceFactory();
        registerServices();

        getLogger().info("NitroCore has been enabled!");
    }

    @Override
    public void onDisable() {
        if (serviceFactory != null) {
            serviceFactory.disableAll();
        }
        getLogger().info("NitroCore has been disabled!");
    }

    private void registerServices() {
        serviceFactory.registerService(MessageService.class, new MessageService());
    }

    public static Main getInstance() {
        return instance;
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }
}
