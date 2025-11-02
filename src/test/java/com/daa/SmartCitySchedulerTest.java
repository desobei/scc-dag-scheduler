package com.daa;

import graph.dagsp.DAGShortestPath;
import graph.scc.TarjanSCC;
import graph.topo.TopologicalSort;
import com.daa.model.Component;
import com.daa.model.CondensationGraph;
import com.daa.model.Task;
import com.daa.model.TaskGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Tests for the scheduler
class SmartCitySchedulerTest {
    
    private TaskGraph createSimpleDAG() {
        TaskGraph graph = new TaskGraph();
        
        Task t1 = new Task("T1", "Task 1", 5);
        Task t2 = new Task("T2", "Task 2", 3);
        Task t3 = new Task("T3", "Task 3", 4);
        Task t4 = new Task("T4", "Task 4", 2);
        
        t2.setDependencies(List.of("T1"));
        t3.setDependencies(List.of("T1"));
        t4.setDependencies(List.of("T2", "T3"));
        
        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);
        graph.addTask(t4);
        graph.buildFromTasks();
        
        return graph;
    }
    
    private TaskGraph createGraphWithCycle() {
        TaskGraph graph = new TaskGraph();
        
        Task t1 = new Task("T1", "Task 1", 5);
        Task t2 = new Task("T2", "Task 2", 3);
        Task t3 = new Task("T3", "Task 3", 4);
        Task t4 = new Task("T4", "Task 4", 2);
        Task t5 = new Task("T5", "Task 5", 6);
        
        // Create a cycle: T2 -> T3 -> T4 -> T2
        t2.setDependencies(new java.util.ArrayList<>(List.of("T1")));
        t3.setDependencies(List.of("T2"));
        t4.setDependencies(List.of("T3"));
        t5.setDependencies(List.of("T4"));
        
        // Add cycle
        t2.getDependencies().add("T4");
        
        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);
        graph.addTask(t4);
        graph.addTask(t5);
        graph.buildFromTasks();
        
        return graph;
    }
    
    @Test
    void testSimpleDAG_NoSCC() {
        TaskGraph graph = createSimpleDAG();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        assertEquals(4, sccs.size());
        
        for (Component scc : sccs) {
            assertEquals(1, scc.size());
        }
    }
    
    @Test
    void testGraphWithCycle_DetectsSCC() {
        TaskGraph graph = createGraphWithCycle();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        assertTrue(sccs.size() < 5);
        
        boolean hasLargeSCC = sccs.stream().anyMatch(scc -> scc.size() > 1);
        assertTrue(hasLargeSCC, "Should detect at least one SCC with multiple nodes");
    }
    
    @Test
    void testTopologicalSort() {
        TaskGraph graph = createSimpleDAG();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        Map<String, Integer> taskToComp = tarjan.getTaskToComponentMap();
        
        CondensationGraph dag = new CondensationGraph(sccs, graph);
        dag.build(taskToComp);
        
        TopologicalSort topoSort = new TopologicalSort(dag);
        List<Integer> order = topoSort.sortComponents();
        
        assertEquals(sccs.size(), order.size());
        
        Map<Integer, List<Integer>> adj = dag.getAdjacencyList();
        Map<Integer, Integer> position = new java.util.HashMap<>();
        for (int i = 0; i < order.size(); i++) {
            position.put(order.get(i), i);
        }
        
        for (int u : order) {
            for (int v : adj.get(u)) {
                assertTrue(position.get(u) < position.get(v), 
                    "Topological order violated: " + u + " -> " + v);
            }
        }
    }
    
    @Test
    void testLongestPath() {
        TaskGraph graph = createSimpleDAG();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        Map<String, Integer> taskToComp = tarjan.getTaskToComponentMap();
        
        CondensationGraph dag = new CondensationGraph(sccs, graph);
        dag.build(taskToComp);
        
        DAGShortestPath pathFinder = new DAGShortestPath(dag);
        DAGShortestPath.PathResult criticalPath = pathFinder.longestPath();
        
        assertNotNull(criticalPath);
        assertTrue(criticalPath.exists());
        assertTrue(criticalPath.length() > 0);
        assertFalse(criticalPath.path().isEmpty());
        
        System.out.println("Critical path length: " + criticalPath.length());
        System.out.println("Critical path: " + criticalPath.path());
    }
    
    @Test
    void testShortestPaths() {
        TaskGraph graph = createSimpleDAG();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        Map<String, Integer> taskToComp = tarjan.getTaskToComponentMap();
        
        CondensationGraph dag = new CondensationGraph(sccs, graph);
        dag.build(taskToComp);
        
        DAGShortestPath pathFinder = new DAGShortestPath(dag);
        
        if (!sccs.isEmpty()) {
            int source = sccs.get(0).getId();
            Map<Integer, Integer> distances = pathFinder.shortestPaths(source);
            
            assertNotNull(distances);
            assertFalse(distances.isEmpty());
            
            assertEquals(dag.getComponentDuration(source), distances.get(source));
        }
    }
    
    @Test
    void testCondensationGraph() {
        TaskGraph graph = createGraphWithCycle();
        
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        Map<String, Integer> taskToComp = tarjan.getTaskToComponentMap();
        
        CondensationGraph dag = new CondensationGraph(sccs, graph);
        dag.build(taskToComp);
        
        TopologicalSort topoSort = new TopologicalSort(dag);
        List<Integer> order = topoSort.sortComponents();
        
        assertEquals(sccs.size(), order.size(), 
            "Condensation graph should be a valid DAG");
    }
    
    @Test
    void testTaskModel() {
        Task task = new Task("T1", "Test Task", 10);
        task.setDependencies(List.of("T2", "T3"));
        
        assertEquals("T1", task.getId());
        assertEquals("Test Task", task.getName());
        assertEquals(10, task.getDuration());
        assertEquals(2, task.getDependencies().size());
    }
    
    @Test
    void testComponentModel() {
        Component comp = new Component(1);
        comp.addTask("T1");
        comp.addTask("T2");
        comp.addTask("T3");
        
        assertEquals(1, comp.getId());
        assertEquals(3, comp.size());
        assertTrue(comp.getTaskIds().contains("T1"));
    }
}
