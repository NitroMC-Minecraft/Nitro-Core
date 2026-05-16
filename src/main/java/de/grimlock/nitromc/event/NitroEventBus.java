package de.grimlock.nitromc.event;

import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.thread.ThreadingService;
import org.bukkit.Bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Singleton
public class NitroEventBus implements IService {

    private final Map<Class<?>, Set<Consumer<Object>>> listeners = new ConcurrentHashMap<>();
    private final ThreadingService threadingService;

    @Inject
    public NitroEventBus(ThreadingService threadingService) {
        this.threadingService = threadingService;
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        listeners.clear();
    }

    public <T> void subscribe(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> ConcurrentHashMap.newKeySet()).add(obj -> listener.accept(eventClass.cast(obj)));
    }

    public void post(Object event) {
        Set<Consumer<Object>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            eventListeners.forEach(listener -> threadingService.getComputeExecutor().submit(() -> {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Error in NitroEventBus listener: " + e.getMessage());
                    e.printStackTrace();
                }
            }));
        }
    }
}
