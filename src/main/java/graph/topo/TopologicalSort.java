package graph.topo;

import com.daa.model.Component;
import com.daa.model.CondensationGraph;
import graph.metrics.DefaultMetrics;
import graph.metrics.Metrics;

import java.util.*;

// Topological sort using Kahn's algorithm (BFS)
// Complexity: O(V + E)
// Works by removing nodes with no incoming edges
public class TopologicalSort {
    private CondensationGraph condensationGraph;
    private Metrics metrics;
    
    public TopologicalSort(CondensationGraph condensationGraph) {
        this(condensationGraph, new DefaultMetrics());
    }
    
    public TopologicalSort(CondensationGraph condensationGraph, Metrics metrics) {
        this.condensationGraph = condensationGraph;
        this.metrics = metrics;
    }
    
    // Main method - returns topological order or empty list if cycle found
    public List<Integer> sortComponents() {
        metrics.reset();
        metrics.startTimer();
        
        Map<Integer, List<Integer>> adj = condensationGraph.getAdjacencyList();
        Map<Integer, Integer> inDegree = new HashMap<>();
        
        // step 1: set all in-degrees to 0
        for (Component comp : condensationGraph.getComponents()) {
            inDegree.put(comp.getId(), 0);
        }
        
        // step 2: calculate in-degrees
        for (Component comp : condensationGraph.getComponents()) {
            for (int neighbor : adj.get(comp.getId())) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
                metrics.incrementCounter("in_degree_calculations");
            }
        }
        
        // step 3: add all nodes with 0 in-degree to queue
        Queue<Integer> queue = new LinkedList<>();
        for (Component comp : condensationGraph.getComponents()) {
            if (inDegree.get(comp.getId()) == 0) {
                queue.offer(comp.getId());
                metrics.incrementCounter("queue_pushes");
            }
        }
        
        List<Integer> topologicalOrder = new ArrayList<>();
        
        // step 4: process queue
        while (!queue.isEmpty()) {
            int current = queue.poll();
            metrics.incrementCounter("queue_pops");
            metrics.incrementCounter("vertices_processed");
            topologicalOrder.add(current);
            
            // reduce in-degree for neighbors
            for (int neighbor : adj.get(current)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                metrics.incrementCounter("in_degree_updates");
                
                // if in-degree is now 0, add to queue
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                    metrics.incrementCounter("queue_pushes");
                }
            }
        }
        
        metrics.stopTimer();
        
        // if we didn't process all nodes, there's a cycle
        if (topologicalOrder.size() != condensationGraph.size()) {
            System.err.println("ERROR: Cycle detected in condensation graph!");
            return new ArrayList<>();
        }
        
        return topologicalOrder;
    }
    
    /**
     * Get topological order of original tasks based on component order.
     * @return list of task IDs in valid execution order
     */
    public List<String> sortTasks() {
        List<Integer> componentOrder = sortComponents();
        List<String> taskOrder = new ArrayList<>();
        
        for (int compId : componentOrder) {
            Component comp = condensationGraph.getComponents().get(compId);
            taskOrder.addAll(comp.getTaskIds());
        }
        
        return taskOrder;
    }
    
    /**
     * Get the metrics collected during the last execution.
     * @return metrics object
     */
    public Metrics getMetrics() {
        return metrics;
    }
    
    /**
     * Print topological order with performance metrics.
     */
    public void printTopologicalOrder() {
        List<Integer> order = sortComponents();
        System.out.println("\n=== Topological Order (Kahn's Algorithm) ===");
        System.out.println("Component Order: " + order);
        
        System.out.println("\n=== Derived Task Order ===");
        List<String> taskOrder = sortTasks();
        for (int i = 0; i < taskOrder.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, taskOrder.get(i));
        }
        
        System.out.println("\n" + metrics.getReport());
    }
}
