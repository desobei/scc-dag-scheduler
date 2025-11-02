package com.daa;

import com.daa.model.Component;
import com.daa.model.CondensationGraph;
import com.daa.model.TaskGraph;
import com.daa.utils.TaskJsonParser;
import graph.dagsp.DAGShortestPath;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Main application for Assignment 4
// Does SCC detection, topological sort, and finds critical path
public class App {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  Smart City DAG Scheduler - Java 21");
        System.out.println("  SCC Detection & Path Analysis");
        System.out.println("==============================================\n");
        
        try {
            // figure out which file to load
            String filePath;
            if (args.length > 0) {
                filePath = args[0];
                System.out.println("Loading tasks from: " + filePath);
            } else {
                filePath = "tasks.json";
                System.out.println("No input file provided. Creating sample file: " + filePath);
                TaskJsonParser.createSampleFile(filePath);
            }
            
            // Load task graph
            TaskGraph graph = TaskJsonParser.loadFromJson(filePath);
            System.out.println("Task graph loaded with " + graph.size() + " tasks\n");
            
            // Step 1: Find Strongly Connected Components (SCC)
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 1: STRONGLY CONNECTED COMPONENTS (Tarjan)");
            System.out.println("=".repeat(50));
            
            TarjanSCC tarjan = new TarjanSCC(graph);
            List<Component> sccs = tarjan.findSCCs();
            tarjan.printSCCs();
            
            
            // Step 2: Build condensation graph (DAG from SCCs)
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 2: CONDENSATION GRAPH (DAG)");
            System.out.println("=".repeat(50));
            
            Map<String, Integer> taskToComponent = tarjan.getTaskToComponentMap();
            CondensationGraph condensationDAG = new CondensationGraph(sccs, graph);
            condensationDAG.build(taskToComponent);
            condensationDAG.printGraph();
            
            // Step 3: Do topological sort on the DAG
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 3: TOPOLOGICAL ORDERING");
            System.out.println("=".repeat(50));
            
            TopologicalSort topoSort = new TopologicalSort(condensationDAG);
            topoSort.printTopologicalOrder();
            
            // Step 4: Find shortest and longest paths
            System.out.println("\n" + "=".repeat(50));
            System.out.println("STEP 4: PATH ANALYSIS ON DAG");
            System.out.println("=".repeat(50));
            
            DAGShortestPath pathFinder = new DAGShortestPath(condensationDAG);
            
            // critical path is the longest path
            pathFinder.printCriticalPath();
            
            // also show shortest paths from first component
            if (!sccs.isEmpty()) {
                int sourceComponent = sccs.get(0).getId();
                pathFinder.printShortestPaths(sourceComponent);
            }
            
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Analysis Complete!");
            System.out.println("=".repeat(50));
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.err.println("\nUsage: java com.daa.App [path/to/tasks.json]");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
