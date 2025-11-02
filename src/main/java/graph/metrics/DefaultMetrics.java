package graph.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the Metrics interface.
 * Thread-safe implementation for tracking algorithm performance.
 * 
 * @author Smart City Scheduling Team
 * @version 1.0
 */
public class DefaultMetrics implements Metrics {
    
    private long startTime;
    private long endTime;
    private final Map<String, Long> counters;
    
    public DefaultMetrics() {
        this.counters = new HashMap<>();
        this.startTime = 0;
        this.endTime = 0;
    }
    
    @Override
    public void startTimer() {
        this.startTime = System.nanoTime();
    }
    
    @Override
    public void stopTimer() {
        this.endTime = System.nanoTime();
    }
    
    @Override
    public long getElapsedNanos() {
        return endTime - startTime;
    }
    
    @Override
    public double getElapsedMillis() {
        return getElapsedNanos() / 1_000_000.0;
    }
    
    @Override
    public synchronized void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }
    
    @Override
    public synchronized void incrementCounter(String counterName, int amount) {
        counters.put(counterName, counters.getOrDefault(counterName, 0L) + amount);
    }
    
    @Override
    public synchronized long getCounter(String counterName) {
        return counters.getOrDefault(counterName, 0L);
    }
    
    @Override
    public synchronized void reset() {
        startTime = 0;
        endTime = 0;
        counters.clear();
    }
    
    @Override
    public String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Metrics Report ===\n");
        sb.append(String.format("Elapsed Time: %.3f ms (%.0f ns)\n", 
            getElapsedMillis(), (double) getElapsedNanos()));
        sb.append("\nOperation Counters:\n");
        
        counters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> 
                sb.append(String.format("  %s: %,d\n", entry.getKey(), entry.getValue()))
            );
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getReport();
    }
}
