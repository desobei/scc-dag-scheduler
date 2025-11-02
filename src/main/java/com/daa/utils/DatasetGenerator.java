package com.daa.utils;

import com.daa.model.Task;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility for generating test datasets with various graph structures.
 * Generates graphs of different sizes, densities, and cycle characteristics.
 * 
 * <p>Dataset categories:
 * <ul>
 *   <li>Small: 6-10 nodes, simple cases with 1-2 cycles or pure DAG</li>
 *   <li>Medium: 10-20 nodes, mixed structures with several SCCs</li>
 *   <li>Large: 20-50 nodes, performance and timing tests</li>
 * </ul>
 * 
 * @author Smart City Scheduling Team
 * @version 2.0
 */
public class DatasetGenerator {
    
    private static final Random RANDOM = new Random(42); // Fixed seed for reproducibility
    private static final String[] TASK_TYPES = {
        "Street Cleaning", "Traffic Light Repair", "Camera Installation",
        "Sensor Calibration", "Network Config", "Data Analytics",
        "System Test", "Bug Fixes", "Security Audit", "Performance Tuning",
        "Database Migration", "UI Update", "API Integration", "Load Testing",
        "Documentation", "Code Review", "Deployment", "Monitoring Setup"
    };
    
    /**
     * Generate all 9 datasets (3 small, 3 medium, 3 large).
     * @param outputDir directory to save datasets
     * @throws IOException if file writing fails
     */
    public static void generateAllDatasets(String outputDir) throws IOException {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        System.out.println("Generating datasets in: " + outputDir);
        
        // Small datasets (6-10 nodes)
        generateDataset(outputDir + "/small_dag_sparse.json", 
            6, 0.3, false, "Small sparse DAG");
        generateDataset(outputDir + "/small_cyclic_medium.json", 
            8, 0.4, true, "Small graph with 1-2 cycles");
        generateDataset(outputDir + "/small_dag_dense.json", 
            10, 0.5, false, "Small dense DAG");
        
        // Medium datasets (10-20 nodes)
        generateDataset(outputDir + "/medium_mixed_sparse.json", 
            12, 0.25, true, "Medium sparse with multiple SCCs");
        generateDataset(outputDir + "/medium_dag.json", 
            15, 0.35, false, "Medium DAG");
        generateDataset(outputDir + "/medium_cyclic_dense.json", 
            18, 0.45, true, "Medium dense with several cycles");
        
        // Large datasets (20-50 nodes)
        generateDataset(outputDir + "/large_sparse_dag.json", 
            25, 0.15, false, "Large sparse DAG for performance testing");
        generateDataset(outputDir + "/large_mixed.json", 
            35, 0.25, true, "Large mixed structure with multiple SCCs");
        generateDataset(outputDir + "/large_dense.json", 
            45, 0.30, false, "Large dense DAG for timing tests");
        
        System.out.println("✅ Generated 9 datasets successfully!");
    }
    
    /**
     * Generate a single dataset with specified characteristics.
     * 
     * @param filename output file path
     * @param numNodes number of vertices
     * @param density edge density (0.0 to 1.0)
     * @param allowCycles whether to allow cycles
     * @param description dataset description
     * @throws IOException if file writing fails
     */
    public static void generateDataset(String filename, int numNodes, 
                                       double density, boolean allowCycles, 
                                       String description) throws IOException {
        List<Task> tasks = new ArrayList<>();
        
        // Generate tasks
        for (int i = 0; i < numNodes; i++) {
            String id = "T" + (i + 1);
            String name = TASK_TYPES[i % TASK_TYPES.length] + " #" + (i / TASK_TYPES.length + 1);
            int duration = 2 + RANDOM.nextInt(8); // 2-9 time units
            tasks.add(new Task(id, name, duration));
        }
        
        // Generate edges based on density
        int maxEdges = numNodes * (numNodes - 1) / 2;
        int targetEdges = (int) (maxEdges * density);
        int edgesAdded = 0;
        
        if (allowCycles) {
            // Allow cycles: add edges randomly
            edgesAdded = addRandomEdges(tasks, targetEdges);
        } else {
            // Enforce DAG: only add edges from lower to higher indices
            edgesAdded = addDAGEdges(tasks, targetEdges);
        }
        
        // Save to file
        TaskJsonParser.saveToJson(tasks, filename);
        
        // Print statistics
        System.out.printf("Generated: %s%n", new File(filename).getName());
        System.out.printf("  Description: %s%n", description);
        System.out.printf("  Vertices: %d, Edges: %d, Density: %.2f%n", 
            numNodes, edgesAdded, density);
        System.out.printf("  Cyclic: %s%n%n", allowCycles ? "Yes" : "No (DAG)");
    }
    
    /**
     * Add edges to maintain DAG property (no cycles).
     */
    private static int addDAGEdges(List<Task> tasks, int targetEdges) {
        int edgesAdded = 0;
        Set<String> addedEdges = new HashSet<>();
        
        // Add edges only from lower index to higher index (ensures DAG)
        for (int i = 0; i < tasks.size() && edgesAdded < targetEdges; i++) {
            for (int j = i + 1; j < tasks.size() && edgesAdded < targetEdges; j++) {
                if (RANDOM.nextDouble() < 0.4) { // 40% chance for each potential edge
                    String from = tasks.get(i).getId();
                    String to = tasks.get(j).getId();
                    String edgeKey = from + "->" + to;
                    
                    if (!addedEdges.contains(edgeKey)) {
                        tasks.get(j).getDependencies().add(from);
                        addedEdges.add(edgeKey);
                        edgesAdded++;
                    }
                }
            }
        }
        
        return edgesAdded;
    }
    
    /**
     * Add edges randomly, potentially creating cycles.
     */
    private static int addRandomEdges(List<Task> tasks, int targetEdges) {
        int edgesAdded = 0;
        Set<String> addedEdges = new HashSet<>();
        
        // First, add some DAG edges for structure
        int dagEdges = targetEdges * 2 / 3;
        for (int i = 0; i < tasks.size() && edgesAdded < dagEdges; i++) {
            for (int j = i + 1; j < tasks.size() && edgesAdded < dagEdges; j++) {
                if (RANDOM.nextDouble() < 0.3) {
                    String from = tasks.get(i).getId();
                    String to = tasks.get(j).getId();
                    String edgeKey = from + "->" + to;
                    
                    if (!addedEdges.contains(edgeKey)) {
                        tasks.get(j).getDependencies().add(from);
                        addedEdges.add(edgeKey);
                        edgesAdded++;
                    }
                }
            }
        }
        
        // Add some back edges to create cycles (1-3 cycles)
        int cyclesToAdd = 1 + RANDOM.nextInt(3);
        for (int c = 0; c < cyclesToAdd && edgesAdded < targetEdges; c++) {
            int i = RANDOM.nextInt(tasks.size());
            int j = RANDOM.nextInt(i); // j < i, creates back edge
            
            if (j < i) {
                String from = tasks.get(i).getId();
                String to = tasks.get(j).getId();
                String edgeKey = from + "->" + to;
                
                if (!addedEdges.contains(edgeKey)) {
                    tasks.get(j).getDependencies().add(from);
                    addedEdges.add(edgeKey);
                    edgesAdded++;
                }
            }
        }
        
        return edgesAdded;
    }
    
    /**
     * Main method to generate all datasets.
     */
    public static void main(String[] args) {
        try {
            String outputDir = args.length > 0 ? args[0] : "data";
            generateAllDatasets(outputDir);
            
            System.out.println("Dataset generation complete!");
            System.out.println("Use these files to test the algorithms with different graph structures.");
            
        } catch (IOException e) {
            System.err.println("Error generating datasets: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
