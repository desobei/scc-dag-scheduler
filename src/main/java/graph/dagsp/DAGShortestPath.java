package graph.dagsp;

import com.daa.model.Component;
import com.daa.model.CondensationGraph;
import graph.metrics.DefaultMetrics;
import graph.metrics.Metrics;
import graph.topo.TopologicalSort;

import java.util.*;

// Shortest and longest paths in DAG using DP
// Complexity: O(V + E)
// Process nodes in topological order and relax edges
public class DAGShortestPath {
    private CondensationGraph dag;
    private List<Integer> topologicalOrder;
    private Metrics metrics;
    
    public DAGShortestPath(CondensationGraph dag) {
        this(dag, new DefaultMetrics());
    }
    
    public DAGShortestPath(CondensationGraph dag, Metrics metrics) {
        this.dag = dag;
        this.metrics = metrics;
        // need topological order first
        TopologicalSort topoSort = new TopologicalSort(dag);
        this.topologicalOrder = topoSort.sortComponents();
    }
    
    /**
     * Compute single-source shortest paths from a source component.
     * Uses DP over topological order with edge relaxation.
     * 
     * @param source Source component ID
     * @return Map of component ID to shortest distance from source
     */
    public Map<Integer, Integer> shortestPaths(int source) {
        metrics.reset();
        metrics.startTimer();
        
        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        
        // Step 1: Initialize distances to infinity
        for (Component comp : dag.getComponents()) {
            distance.put(comp.getId(), Integer.MAX_VALUE);
            parent.put(comp.getId(), -1);
        }
        distance.put(source, dag.getComponentDuration(source));
        
        // Step 2: Process vertices in topological order
        for (int u : topologicalOrder) {
            metrics.incrementCounter("vertices_processed");
            
            if (distance.get(u) != Integer.MAX_VALUE) {
                // Step 3: Relax all outgoing edges
                for (int v : dag.getAdjacencyList().get(u)) {
                    metrics.incrementCounter("edges_examined");
                    metrics.incrementCounter("relaxations");
                    
                    int newDist = distance.get(u) + dag.getComponentDuration(v);
                    
                    // check if this path is shorter
                    if (newDist < distance.get(v)) {
                        distance.put(v, newDist);
                        parent.put(v, u);
                        metrics.incrementCounter("distance_updates");
                    }
                }
            }
        }
        
        metrics.stopTimer();
        return distance;
    }
    
    // Find longest path (critical path) using DP
    // Basically same as shortest but use max instead of min
    public PathResult longestPath() {
        metrics.reset();
        metrics.startTimer();
        
        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        
        // start with 0
        for (Component comp : dag.getComponents()) {
            distance.put(comp.getId(), 0);
            parent.put(comp.getId(), -1);
        }
        
        // go through nodes in topological order
        for (int u : topologicalOrder) {
            metrics.incrementCounter("vertices_processed");
            int currentDist = distance.get(u) + dag.getComponentDuration(u);
            
            // relax edges
            for (int v : dag.getAdjacencyList().get(u)) {
                metrics.incrementCounter("edges_examined");
                metrics.incrementCounter("relaxations");
                
                // update if we found longer path
                if (currentDist > distance.get(v)) {
                    distance.put(v, currentDist);
                    parent.put(v, u);
                    metrics.incrementCounter("distance_updates");
                }
            }
        }
        
        // find which node has longest distance
        int maxDist = 0;
        int endNode = -1;
        for (Component comp : dag.getComponents()) {
            int finalDist = distance.get(comp.getId()) + dag.getComponentDuration(comp.getId());
            if (finalDist > maxDist) {
                maxDist = finalDist;
                endNode = comp.getId();
            }
        }
        
        // reconstruct path backwards
        List<Integer> path = new ArrayList<>();
        int current = endNode;
        while (current != -1) {
            path.add(0, current);
            current = parent.get(current);
        }
        
        metrics.stopTimer();
        return new PathResult(path, maxDist);
    }
    
    /**
     * Compute shortest path between two specific nodes.
     * 
     * @param source source component ID
     * @param target target component ID
     * @return PathResult with the shortest path
     */
    public PathResult shortestPath(int source, int target) {
        metrics.reset();
        metrics.startTimer();
        
        Map<Integer, Integer> distance = new HashMap<>();
        Map<Integer, Integer> parent = new HashMap<>();
        
        // Initialize
        for (Component comp : dag.getComponents()) {
            distance.put(comp.getId(), Integer.MAX_VALUE);
            parent.put(comp.getId(), -1);
        }
        distance.put(source, dag.getComponentDuration(source));
        
        // DP over topological order
        boolean foundSource = false;
        for (int u : topologicalOrder) {
            if (u == source) foundSource = true;
            
            if (foundSource && distance.get(u) != Integer.MAX_VALUE) {
                metrics.incrementCounter("vertices_processed");
                
                for (int v : dag.getAdjacencyList().get(u)) {
                    metrics.incrementCounter("edges_examined");
                    metrics.incrementCounter("relaxations");
                    
                    int newDist = distance.get(u) + dag.getComponentDuration(v);
                    if (newDist < distance.get(v)) {
                        distance.put(v, newDist);
                        parent.put(v, u);
                        metrics.incrementCounter("distance_updates");
                    }
                    
                    if (v == target) break;
                }
            }
        }
        
        // Reconstruct path
        List<Integer> path = new ArrayList<>();
        if (distance.get(target) != Integer.MAX_VALUE) {
            int current = target;
            while (current != -1) {
                path.add(0, current);
                current = parent.get(current);
            }
        }
        
        int dist = distance.get(target) == Integer.MAX_VALUE ? -1 : distance.get(target);
        metrics.stopTimer();
        return new PathResult(path, dist);
    }
    
    /**
     * Get the metrics collected during the last execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
    
    /**
     * Print shortest paths from a source with performance metrics.
     * @param source source component ID
     */
    public void printShortestPaths(int source) {
        Map<Integer, Integer> distances = shortestPaths(source);
        System.out.println("\n=== Shortest Paths from Component " + source + " ===");
        for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
            if (entry.getValue() != Integer.MAX_VALUE) {
                System.out.printf("Component %d: distance = %d%n", entry.getKey(), entry.getValue());
            } else {
                System.out.printf("Component %d: unreachable%n", entry.getKey());
            }
        }
        System.out.println("\n" + metrics.getReport());
    }
    
    /**
     * Print the critical path (longest path) with performance metrics.
     */
    public void printCriticalPath() {
        PathResult result = longestPath();
        System.out.println("\n=== Critical Path (Longest Path) ===");
        System.out.println("Length: " + result.length());
        System.out.println("Path (component IDs): " + result.path());
        
        System.out.println("\nDetailed path:");
        for (int compId : result.path()) {
            Component comp = dag.getComponents().get(compId);
            System.out.printf("  Component %d (duration=%d): tasks=%s%n", 
                compId, dag.getComponentDuration(compId), comp.getTaskIds());
        }
        
        System.out.println("\n" + metrics.getReport());
    }
    
    /**
     * Result of a path computation.
     * 
     * @param path list of component IDs in the path
     * @param length total path length
     */
    public record PathResult(List<Integer> path, int length) {
        /**
         * Check if a valid path exists.
         * @return true if path exists
         */
        public boolean exists() {
            return length >= 0 && !path.isEmpty();
        }
    }
}
