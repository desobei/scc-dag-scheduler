package com.daa.model;

import java.util.*;

/**
 * Represents the condensation graph - a DAG where each node is an SCC.
 */
public class CondensationGraph {
    private List<Component> components;
    private Map<Integer, List<Integer>> adjacencyList; // component id -> list of dependent component ids
    private Map<Integer, Integer> componentDuration; // component id -> max duration in component
    private TaskGraph originalGraph;
    
    public CondensationGraph(List<Component> components, TaskGraph originalGraph) {
        this.components = components;
        this.originalGraph = originalGraph;
        this.adjacencyList = new HashMap<>();
        this.componentDuration = new HashMap<>();
        
        // Initialize adjacency list
        for (Component component : components) {
            adjacencyList.put(component.getId(), new ArrayList<>());
        }
    }
    
    /**
     * Build the condensation graph from the original graph and SCCs.
     */
    public void build(Map<String, Integer> taskToComponent) {
        Set<String> addedEdges = new HashSet<>();
        
        // For each task in the original graph
        for (String taskId : originalGraph.getVertices()) {
            int fromComp = taskToComponent.get(taskId);
            
            // Check all outgoing edges
            List<String> neighbors = originalGraph.getAdjacencyList().get(taskId);
            if (neighbors != null) {
                for (String neighborId : neighbors) {
                    int toComp = taskToComponent.get(neighborId);
                    
                    // Only add edge if it's between different components
                    if (fromComp != toComp) {
                        String edgeKey = fromComp + "->" + toComp;
                        if (!addedEdges.contains(edgeKey)) {
                            adjacencyList.get(fromComp).add(toComp);
                            addedEdges.add(edgeKey);
                        }
                    }
                }
            }
        }
        
        // Calculate component durations (max duration of tasks in component)
        for (Component component : components) {
            int maxDuration = 0;
            for (String taskId : component.getTaskIds()) {
                int duration = originalGraph.getTask(taskId).getDuration();
                maxDuration = Math.max(maxDuration, duration);
            }
            componentDuration.put(component.getId(), maxDuration);
        }
    }
    
    public List<Component> getComponents() {
        return components;
    }
    
    public Map<Integer, List<Integer>> getAdjacencyList() {
        return adjacencyList;
    }
    
    public int getComponentDuration(int componentId) {
        return componentDuration.getOrDefault(componentId, 0);
    }
    
    public int size() {
        return components.size();
    }
    
    public void printGraph() {
        System.out.println("\n=== Condensation DAG ===");
        System.out.println("Number of components: " + components.size());
        for (Component comp : components) {
            List<Integer> deps = adjacencyList.get(comp.getId());
            System.out.printf("Component %d (size=%d, duration=%d) -> %s%n", 
                comp.getId(), comp.size(), getComponentDuration(comp.getId()), deps);
        }
    }
}
