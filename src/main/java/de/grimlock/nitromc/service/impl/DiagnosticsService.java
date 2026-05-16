package de.grimlock.nitromc.service.impl;

import de.grimlock.nitromc.database.DatabaseService;
import de.grimlock.nitromc.service.IService;
import de.grimlock.nitromc.thread.ThreadingService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Singleton
public class DiagnosticsService implements IService {

    private final ThreadingService threadingService;
    private final DatabaseService databaseService;

    @Inject
    public DiagnosticsService(ThreadingService threadingService, DatabaseService databaseService) {
        this.threadingService = threadingService;
        this.databaseService = databaseService;
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    public CompletableFuture<DiagnosticReport> runDiagnostics() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> results = new ArrayList<>();
            boolean success = true;

            // Check Thread Pools
            if (threadingService.getIoExecutor() instanceof ThreadPoolExecutor io) {
                results.add("IO-Threads: " + io.getActiveCount() + "/" + io.getMaximumPoolSize());
            }
            if (threadingService.getComputeExecutor() instanceof ThreadPoolExecutor compute) {
                results.add("Compute-Threads: " + compute.getActiveCount() + "/" + compute.getMaximumPoolSize());
            }

            // Check Database Connection
            try {
                var conn = databaseService.getConnection();
                try {
                    if (conn.isValid(1)) {
                        results.add("Database: Connected (Ping OK)");
                    } else {
                        results.add("Database: Connection Invalid");
                        success = false;
                    }
                } finally {
                    conn.close();
                }
            } catch (Exception e) {
                results.add("Database: Connection Failed (" + e.getMessage() + ")");
                success = false;
            }

            return new DiagnosticReport(success, results);
        }, threadingService.getComputeExecutor());
    }

    public record DiagnosticReport(boolean success, List<String> details) {}
}
