package com.daa.model;

import java.util.*;

/**
 * Represents a directed graph of tasks with their dependencies.
 */
public class TaskGraph {
    private Map<String, Task> tasks;
    private Map<String, List<String>> adjacencyList; // task id -> list of dependent task ids
    private Map<String, List<String>> reverseAdjacencyList; // for transpose graph
    
    public TaskGraph() {
        this.tasks = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.reverseAdjacencyList = new HashMap<>();
    }
    
    public void addTask(Task task) {
        tasks.put(task.getId(), task);
        adjacencyList.putIfAbsent(task.getId(), new ArrayList<>());
        reverseAdjacencyList.putIfAbsent(task.getId(), new ArrayList<>());
    }
    
    public void addEdge(String from, String to) {
        adjacencyList.get(from).add(to);
        reverseAdjacencyList.putIfAbsent(to, new ArrayList<>());
        reverseAdjacencyList.get(to).add(from);
    }
    
    public Map<String, Task> getTasks() {
        return tasks;
    }
    
    public Task getTask(String id) {
        return tasks.get(id);
    }
    
    public Map<String, List<String>> getAdjacencyList() {
        return adjacencyList;
    }
    
    public Map<String, List<String>> getReverseAdjacencyList() {
        return reverseAdjacencyList;
    }
    
    public Set<String> getVertices() {
        return tasks.keySet();
    }
    
    public int size() {
        return tasks.size();
    }
    
    /**
     * Build the graph from task dependencies.
     */
    public void buildFromTasks() {
        for (Task task : tasks.values()) {
            for (String depId : task.getDependencies()) {
                if (tasks.containsKey(depId)) {
                    addEdge(depId, task.getId()); // dependency points to task
                }
            }
        }
    }
}
