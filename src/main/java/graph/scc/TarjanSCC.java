package graph.scc;

import com.daa.model.Component;
import com.daa.model.TaskGraph;
import graph.metrics.Metrics;
import graph.metrics.DefaultMetrics;

import java.util.*;

// Tarjan's algorithm for finding SCCs
// Complexity: O(V + E)
// Uses DFS with discovery time and low-link values
public class TarjanSCC {
    private TaskGraph graph;
    private Map<String, Integer> discoveryTime;  // when we first visit each node
    private Map<String, Integer> lowLink;        // lowest reachable node
    private Set<String> onStack;                 // track what's on stack
    private Deque<String> stack;                 // stack for SCC detection
    private List<Component> components;          // found SCCs
    private int time;                            // global time counter
    private int componentId;                     // SCC id counter
    private Metrics metrics;                     // for tracking performance
    
    public TarjanSCC(TaskGraph graph) {
        this(graph, new DefaultMetrics());
    }
    
    public TarjanSCC(TaskGraph graph, Metrics metrics) {
        this.graph = graph;
        this.discoveryTime = new HashMap<>();
        this.lowLink = new HashMap<>();
        this.onStack = new HashSet<>();
        this.stack = new ArrayDeque<>();
        this.components = new ArrayList<>();
        this.time = 0;
        this.componentId = 0;
        this.metrics = metrics;
    }
    
    // Main method to find all SCCs
    public List<Component> findSCCs() {
        metrics.reset();
        metrics.startTimer();
        
        // visit all nodes that haven't been visited yet
        for (String taskId : graph.getVertices()) {
            if (!discoveryTime.containsKey(taskId)) {
                dfs(taskId);
            }
        }
        
        metrics.stopTimer();
        return components;
    }
    
    /**
     * DFS traversal to identify SCCs.
     * Tracks discovery time and low-link values.
     * 
     * @param u current vertex
     */
    private void dfs(String u) {
        metrics.incrementCounter("dfs_calls");
        metrics.incrementCounter("vertices_visited");
        
        // Initialize discovery time and low link value
        discoveryTime.put(u, time);
        lowLink.put(u, time);
        time++;
        
        // Push onto stack
        stack.push(u);
        onStack.add(u);
        metrics.incrementCounter("stack_operations"); // push
        
        // check all neighbors
        Map<String, List<String>> adj = graph.getAdjacencyList();
        if (adj.containsKey(u)) {
            for (String v : adj.get(u)) {
                metrics.incrementCounter("edges_explored");
                
                if (!discoveryTime.containsKey(v)) {
                    // not visited yet, so do DFS
                    dfs(v);
                    // update low-link value
                    lowLink.put(u, Math.min(lowLink.get(u), lowLink.get(v)));
                } else if (onStack.contains(v)) {
                    // already on stack means it's part of current SCC
                    lowLink.put(u, Math.min(lowLink.get(u), discoveryTime.get(v)));
                }
            }
        }
        
        // if u is root of SCC, pop everything from stack to form the component
        if (lowLink.get(u).equals(discoveryTime.get(u))) {
            Component component = new Component(componentId++);
            String v;
            do {
                v = stack.pop();
                onStack.remove(v);
                metrics.incrementCounter("stack_operations");
                component.addTask(v);
            } while (!v.equals(u));
            
            components.add(component);
        }
    }
    
    // helper method to get task to component mapping
    public Map<String, Integer> getTaskToComponentMap() {
        Map<String, Integer> taskToComponent = new HashMap<>();
        for (Component component : components) {
            for (String taskId : component.getTaskIds()) {
                taskToComponent.put(taskId, component.getId());
            }
        }
        return taskToComponent;
    }
    
    public Metrics getMetrics() {
        return metrics;
    }
    
    /**
     * Prints all SCCs with their sizes and performance metrics.
     */
    public void printSCCs() {
        System.out.println("\n=== Strongly Connected Components (Tarjan's Algorithm) ===");
        System.out.println("Total SCCs found: " + components.size());
        for (Component component : components) {
            System.out.println(component);
        }
        System.out.println("\n" + metrics.getReport());
    }
}
