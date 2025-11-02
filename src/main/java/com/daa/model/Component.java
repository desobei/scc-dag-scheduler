package com.daa.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a strongly connected component (SCC) in the task graph.
 */
public class Component {
    private int id;
    private List<String> taskIds;
    
    public Component(int id) {
        this.id = id;
        this.taskIds = new ArrayList<>();
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public List<String> getTaskIds() {
        return taskIds;
    }
    
    public void addTask(String taskId) {
        this.taskIds.add(taskId);
    }
    
    public int size() {
        return taskIds.size();
    }
    
    @Override
    public String toString() {
        return String.format("Component{id=%d, size=%d, tasks=%s}", id, size(), taskIds);
    }
}
