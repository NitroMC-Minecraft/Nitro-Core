package de.grimlock.nitromc.service;

import java.util.HashMap;
import java.util.Map;

public class ServiceFactory {

    private final Map<Class<? extends IService>, IService> services = new HashMap<>();

    public <T extends IService> void registerService(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
        implementation.onEnable();
    }

    public <T extends IService> T getService(Class<T> serviceClass) {
        return serviceClass.cast(services.get(serviceClass));
    }

    public void disableAll() {
        services.values().forEach(IService::onDisable);
        services.clear();
    }
}
