package com.daa.utils;

import com.daa.model.Task;
import com.daa.model.TaskGraph;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Utility class for loading and saving task graphs from/to JSON files.
 */
public class TaskJsonParser {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Load tasks from a JSON file.
     * Expected format: Array of Task objects with id, name, duration, and dependencies.
     * 
     * Example:
     * [
     *   {
     *     "id": "T1",
     *     "name": "Street Cleaning",
     *     "duration": 5,
     *     "dependencies": []
     *   },
     *   {
     *     "id": "T2",
     *     "name": "Sensor Maintenance",
     *     "duration": 3,
     *     "dependencies": ["T1"]
     *   }
     * ]
     */
    public static TaskGraph loadFromJson(String filePath) throws IOException {
        TaskGraph graph = new TaskGraph();
        
        try (FileReader reader = new FileReader(filePath)) {
            Type taskListType = new TypeToken<List<Task>>(){}.getType();
            List<Task> tasks = gson.fromJson(reader, taskListType);
            
            if (tasks == null || tasks.isEmpty()) {
                throw new IOException("No tasks found in JSON file");
            }
            
            // Add all tasks to the graph
            for (Task task : tasks) {
                graph.addTask(task);
            }
            
            // Build edges from dependencies
            graph.buildFromTasks();
            
            System.out.println("Loaded " + tasks.size() + " tasks from " + filePath);
            return graph;
        }
    }
    
    /**
     * Save tasks to a JSON file.
     */
    public static void saveToJson(List<Task> tasks, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(tasks, writer);
            System.out.println("Saved " + tasks.size() + " tasks to " + filePath);
        }
    }
    
    /**
     * Create a sample tasks.json file for testing.
     */
    public static void createSampleFile(String filePath) throws IOException {
        List<Task> sampleTasks = List.of(
            createTask("T1", "Street Cleaning Zone A", 5, List.of()),
            createTask("T2", "Street Cleaning Zone B", 4, List.of()),
            createTask("T3", "Repair Traffic Light 1", 6, List.of("T1")),
            createTask("T4", "Repair Traffic Light 2", 5, List.of("T2")),
            createTask("T5", "Camera Installation", 8, List.of("T3", "T4")),
            createTask("T6", "Sensor Calibration", 3, List.of("T5")),
            createTask("T7", "Data Analytics Setup", 7, List.of("T6")),
            createTask("T8", "Network Configuration", 4, List.of("T5")),
            // Create a cycle for demonstration
            createTask("T9", "System Test Phase 1", 6, List.of("T7", "T8")),
            createTask("T10", "System Test Phase 2", 5, List.of("T9")),
            createTask("T11", "Bug Fixes", 4, List.of("T10")),
            createTask("T12", "Regression Testing", 3, List.of("T11", "T9")) // Creates cycle T9->T10->T11->T12->T9
        );
        
        saveToJson(sampleTasks, filePath);
    }
    
    private static Task createTask(String id, String name, int duration, List<String> dependencies) {
        Task task = new Task(id, name, duration);
        task.setDependencies(dependencies);
        return task;
    }
}
