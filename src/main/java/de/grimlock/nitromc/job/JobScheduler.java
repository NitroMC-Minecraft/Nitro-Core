package de.grimlock.nitromc.job;

import com.google.inject.Injector;
import de.grimlock.nitromc.Main;
import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.thread.ThreadingService;
import org.bukkit.Bukkit;
import org.reflections.Reflections;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class JobScheduler implements IService {

    private final ThreadingService threadingService;
    private final Injector injector;
    private final Main plugin;
    private final List<ScheduledFuture<?>> scheduledJobs = new ArrayList<>();

    @Inject
    public JobScheduler(ThreadingService threadingService, Injector injector, Main plugin) {
        this.threadingService = threadingService;
        this.injector = injector;
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        for (ScheduledFuture<?> future : scheduledJobs) {
            future.cancel(false);
        }
        scheduledJobs.clear();
    }

    public void scheduleJobs(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Job.class);

        for (Class<?> clazz : annotated) {
            if (NitroJob.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends NitroJob> jobClass = (Class<? extends NitroJob>) clazz;
                Job annotation = jobClass.getAnnotation(Job.class);
                NitroJob job = injector.getInstance(jobClass);
                schedule(job, annotation);
            }
        }
    }

    private void schedule(NitroJob job, Job annotation) {
        if (annotation.async()) {
            ScheduledExecutorService scheduler = threadingService.getScheduler();
            ExecutorService ioExecutor = threadingService.getIoExecutor();
            ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> ioExecutor.submit(job::execute),
                0,
                annotation.interval(),
                annotation.unit()
            );
            scheduledJobs.add(future);
        } else {
            long tickDelay = toTicks(annotation.interval(), annotation.unit());
            long tickPeriod = toTicks(annotation.interval(), annotation.unit());
            Bukkit.getScheduler().runTaskTimer(plugin, job::execute, tickDelay, tickPeriod);
        }
    }

    public CompletableFuture<Void> submit(Runnable task) {
        return CompletableFuture.runAsync(task, threadingService.getIoExecutor());
    }

    public CompletableFuture<Void> submitHeavy(Runnable task) {
        return CompletableFuture.runAsync(task, threadingService.getIoExecutor());
    }

    private long toTicks(long duration, TimeUnit unit) {
        return (unit.toMillis(duration) / 50);
    }
}
