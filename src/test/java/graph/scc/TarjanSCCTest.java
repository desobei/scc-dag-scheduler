package graph.scc;

import com.daa.model.Component;
import com.daa.model.Task;
import com.daa.model.TaskGraph;
import graph.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for Tarjan's SCC algorithm.
 * Tests include edge cases, deterministic small cases, and metrics validation.
 * 
 * @author Smart City Scheduling Team
 */
class TarjanSCCTest {
    
    @Test
    void testSimpleDAG_NoSCC() {
        // Given: Simple DAG with 4 nodes
        TaskGraph graph = createLinearDAG(4);
        
        // When: Find SCCs
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        // Then: Each node is its own SCC
        assertEquals(4, sccs.size());
        for (Component scc : sccs) {
            assertEquals(1, scc.size());
        }
    }
    
    @Test
    void testSingleCycle() {
        // Given: Graph with one cycle T1->T2->T3->T1
        TaskGraph graph = new TaskGraph();
        Task t1 = new Task("T1", "Task 1", 5);
        Task t2 = new Task("T2", "Task 2", 3);
        Task t3 = new Task("T3", "Task 3", 4);
        
        t2.setDependencies(List.of("T1"));
        t3.setDependencies(List.of("T2"));
        t1.getDependencies().add("T3"); // Creates cycle
        
        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);
        graph.buildFromTasks();
        
        // When: Find SCCs
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        // Then: One SCC with all 3 nodes
        assertEquals(1, sccs.size());
        assertEquals(3, sccs.get(0).size());
    }
    
    @Test
    void testMultipleSCCs() {
        // Given: Graph with 2 separate cycles
        TaskGraph graph = new TaskGraph();
        
        // Cycle 1: T1->T2->T1
        Task t1 = new Task("T1", "Task 1", 5);
        Task t2 = new Task("T2", "Task 2", 3);
        t2.getDependencies().add("T1");
        t1.getDependencies().add("T2");
        
        // Cycle 2: T3->T4->T3
        Task t3 = new Task("T3", "Task 3", 4);
        Task t4 = new Task("T4", "Task 4", 2);
        t4.getDependencies().add("T3");
        t3.getDependencies().add("T4");
        
        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);
        graph.addTask(t4);
        graph.buildFromTasks();
        
        // When: Find SCCs
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        // Then: 2 SCCs, each with 2 nodes
        assertEquals(2, sccs.size());
        assertTrue(sccs.stream().allMatch(scc -> scc.size() == 2));
    }
    
    @Test
    void testSingleNode() {
        // Given: Graph with single node
        TaskGraph graph = new TaskGraph();
        graph.addTask(new Task("T1", "Task 1", 5));
        graph.buildFromTasks();
        
        // When: Find SCCs
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        // Then: One SCC with one node
        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
    }
    
    @Test
    void testSelfLoop() {
        // Given: Node with self-loop
        TaskGraph graph = new TaskGraph();
        Task t1 = new Task("T1", "Task 1", 5);
        t1.getDependencies().add("T1"); // Self-loop
        graph.addTask(t1);
        graph.buildFromTasks();
        
        // When: Find SCCs
        TarjanSCC tarjan = new TarjanSCC(graph);
        List<Component> sccs = tarjan.findSCCs();
        
        // Then: One SCC with the node
        assertEquals(1, sccs.size());
        assertEquals(1, sccs.get(0).size());
    }
    
    @Test
    void testMetricsTracking() {
        // Given: Graph with known structure
        TaskGraph graph = createLinearDAG(5);
        
        // When: Find SCCs with metrics
        TarjanSCC tarjan = new TarjanSCC(graph);
        tarjan.findSCCs();
        Metrics metrics = tarjan.getMetrics();
        
        // Then: Metrics are tracked correctly
        assertTrue(metrics.getElapsedNanos() > 0);
        assertEquals(5, metrics.getCounter("dfs_calls"));
        assertEquals(5, metrics.getCounter("vertices_visited"));
        assertEquals(4, metrics.getCounter("edges_explored")); // Linear chain has n-1 edges
        assertEquals(10, metrics.getCounter("stack_operations")); // 5 pushes + 5 pops
    }
    
    @Test
    void testTaskToComponentMapping() {
        // Given: Graph with 2 SCCs
        TaskGraph graph = new TaskGraph();
        Task t1 = new Task("T1", "Task 1", 5);
        Task t2 = new Task("T2", "Task 2", 3);
        Task t3 = new Task("T3", "Task 3", 4);
        
        t2.getDependencies().add("T1");
        t1.getDependencies().add("T2"); // Cycle: T1<->T2
        t3.getDependencies().add("T1");
        
        graph.addTask(t1);
        graph.addTask(t2);
        graph.addTask(t3);
        graph.buildFromTasks();
        
        // When: Get task-to-component mapping
        TarjanSCC tarjan = new TarjanSCC(graph);
        tarjan.findSCCs();
        var mapping = tarjan.getTaskToComponentMap();
        
        // Then: T1 and T2 in same component, T3 in different component
        assertEquals(mapping.get("T1"), mapping.get("T2"));
        assertNotEquals(mapping.get("T1"), mapping.get("T3"));
    }
    
    // Helper method
    private TaskGraph createLinearDAG(int n) {
        TaskGraph graph = new TaskGraph();
        for (int i = 0; i < n; i++) {
            Task task = new Task("T" + (i + 1), "Task " + (i + 1), i + 2);
            if (i > 0) {
                task.getDependencies().add("T" + i);
            }
            graph.addTask(task);
        }
        graph.buildFromTasks();
        return graph;
    }
}
