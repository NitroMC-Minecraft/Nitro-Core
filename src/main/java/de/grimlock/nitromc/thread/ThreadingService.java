package de.grimlock.nitromc.thread;

import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class ThreadingService implements IService {

    private ExecutorService ioExecutor;
    private ExecutorService computeExecutor;
    private ScheduledExecutorService scheduler;

    @Override
    public void onEnable() {
        AtomicInteger ioCounter = new AtomicInteger(0);
        this.ioExecutor = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            r -> {
                Thread t = new Thread(r, "Nitro-IO-" + ioCounter.getAndIncrement());
                t.setPriority(Thread.NORM_PRIORITY - 1);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        int cores = Runtime.getRuntime().availableProcessors();
        int computeThreads = Math.max(2, cores / 2);
        AtomicInteger computeCounter = new AtomicInteger(0);
        this.computeExecutor = new ThreadPoolExecutor(
            computeThreads, computeThreads, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> {
                Thread t = new Thread(r, "Nitro-Compute-" + computeCounter.getAndIncrement());
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.scheduler = new ScheduledThreadPoolExecutor(
            2,
            r -> {
                Thread t = new Thread(r, "Nitro-Scheduler");
                t.setDaemon(true);
                return t;
            }
        );
    }

    @Override
    public void onDisable() {
        shutdown(ioExecutor);
        shutdown(computeExecutor);
        shutdown(scheduler);
    }

    private void shutdown(ExecutorService executor) {
        if (executor == null) return;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }

    public ExecutorService getComputeExecutor() {
        return computeExecutor;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
