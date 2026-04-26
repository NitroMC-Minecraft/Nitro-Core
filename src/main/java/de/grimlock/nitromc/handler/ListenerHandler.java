package de.grimlock.nitromc.handler;

import com.google.inject.Injector;
import de.grimlock.nitromc.Main;
import de.grimlock.nitromc.annotation.AutoListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.util.Set;

public class ListenerHandler {

    private final Injector injector;
    private final Main plugin;

    @Inject
    public ListenerHandler(Injector injector, Main plugin) {
        this.injector = injector;
        this.plugin = plugin;
    }

    public void registerListeners(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(AutoListener.class);

        for (Class<?> clazz : annotated) {
            if (Listener.class.isAssignableFrom(clazz)) {
                AutoListener annotation = clazz.getAnnotation(AutoListener.class);
                Listener listener = (Listener) injector.getInstance(clazz);
                Bukkit.getPluginManager().registerEvents(listener, plugin);
                // Note: Standard Bukkit doesn't support dynamic EventPriority per class easily
                // but we use the annotation's priority here if we were using a custom event system.
                // For now, we just register it.
            }
        }
    }
}
