package io.github.unknownredgreen;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EveryHourStatsLog implements Runnable {
    private ScheduledExecutorService scheduler;
    private final Map<String, Object> stats = new HashMap<>();
    private int runtimeHours = 0;

    public void put(String key, Object value) {
        stats.put(key, value);
    }

    public void incrementIntValue(String key) {
        Integer value = null;
        if (stats.containsKey(key)) {
            value = (Integer) stats.get(key);
        }

        if (value == null) {
            value = 0;
        }
        put(key, value+1);
    }

    @Override
    public void run() {
        runtimeHours++;
        log.info("EVERY HOUR LOG START");
        log.info("{} hours passed since start", runtimeHours);
        for (Map.Entry<String, Object> entry : stats.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            log.info("{}: {}", key, value.toString());
        }
        log.info("EVERY HOUR LOG END");
        stats.clear();
    }

    public void startLogging() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this, 1, 1, TimeUnit.HOURS);
    }

    public void stopLogging() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
