package de.grimlock.nitromc.thread;

import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
public class ThreadingService implements IService {

    private ExecutorService ioExecutor;
    private ExecutorService computeExecutor;

    @Override
    public void onEnable() {
        this.ioExecutor = Executors.newFixedThreadPool(8, r -> {
            Thread t = new Thread(r, "Nitro-IO-Worker");
            t.setPriority(Thread.NORM_PRIORITY - 1);
            return t;
        });

        int cores = Runtime.getRuntime().availableProcessors();
        this.computeExecutor = Executors.newFixedThreadPool(Math.max(2, cores / 2), r -> {
            Thread t = new Thread(r, "Nitro-Compute-Worker");
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });
    }

    @Override
    public void onDisable() {
        shutdown(ioExecutor);
        shutdown(computeExecutor);
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
}
