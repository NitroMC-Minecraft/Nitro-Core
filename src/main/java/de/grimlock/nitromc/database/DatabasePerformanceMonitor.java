package de.grimlock.nitromc.database;

import de.grimlock.nitromc.service.IService;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Singleton
public class DatabasePerformanceMonitor implements IService {

    private final Map<String, QueryMetric> metrics = new ConcurrentHashMap<>();
    private static final long SLOW_QUERY_THRESHOLD = 100; // ms

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {
        printSummary();
    }

    public void record(String sql, long durationMs) {
        metrics.computeIfAbsent(sql, k -> new QueryMetric()).add(durationMs);
        if (durationMs > SLOW_QUERY_THRESHOLD) {
            Logger.getLogger("NitroCore").warning(String.format("Slow Database Query (%dms): %s", durationMs, sql));
        }
    }

    private void printSummary() {
        Logger logger = Logger.getLogger("NitroCore");
        logger.info("--- Database Performance Summary ---");
        metrics.forEach((sql, metric) -> {
            logger.info(String.format("Avg: %dms, Count: %d | SQL: %s", 
                    metric.average(), metric.count.get(), sql));
        });
    }

    private static class QueryMetric {
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong count = new AtomicLong(0);

        void add(long time) {
            totalTime.addAndGet(time);
            count.incrementAndGet();
        }

        long average() {
            return count.get() == 0 ? 0 : totalTime.get() / count.get();
        }
    }
}
