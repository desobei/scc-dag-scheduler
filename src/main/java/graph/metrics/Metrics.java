package graph.metrics;

/**
 * Common metrics interface for instrumenting graph algorithms.
 * Tracks operation counts and execution time.
 * 
 * @author Smart City Scheduling Team
 * @version 1.0
 */
public interface Metrics {
    
    /**
     * Start timing the algorithm execution.
     */
    void startTimer();
    
    /**
     * Stop timing and record elapsed time.
     */
    void stopTimer();
    
    /**
     * Get elapsed time in nanoseconds.
     * @return elapsed time in nanoseconds
     */
    long getElapsedNanos();
    
    /**
     * Get elapsed time in milliseconds.
     * @return elapsed time in milliseconds
     */
    double getElapsedMillis();
    
    /**
     * Increment a named counter.
     * @param counterName name of the counter
     */
    void incrementCounter(String counterName);
    
    /**
     * Increment a counter by a specific amount.
     * @param counterName name of the counter
     * @param amount amount to increment
     */
    void incrementCounter(String counterName, int amount);
    
    /**
     * Get the value of a counter.
     * @param counterName name of the counter
     * @return counter value
     */
    long getCounter(String counterName);
    
    /**
     * Reset all metrics.
     */
    void reset();
    
    /**
     * Get a formatted string of all metrics.
     * @return formatted metrics string
     */
    String getReport();
}
