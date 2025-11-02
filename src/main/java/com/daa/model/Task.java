package com.daa.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a task in the Smart City scheduling system.
 */
public class Task {
    private String id;
    private String name;
    private int duration; // Node duration for path calculations
    private List<String> dependencies; // IDs of tasks this task depends on
    
    public Task() {
        this.dependencies = new ArrayList<>();
    }
    
    public Task(String id, String name, int duration) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.dependencies = new ArrayList<>();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    @Override
    public String toString() {
        return String.format("Task{id='%s', name='%s', duration=%d, dependencies=%s}", 
            id, name, duration, dependencies);
    }
}
