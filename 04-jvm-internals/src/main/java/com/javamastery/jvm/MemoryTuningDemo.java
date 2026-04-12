package com.javamastery.jvm;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

/**
 * Demonstrates JVM memory tuning concepts and runtime memory inspection.
 *
 * Run with different flags to observe behavior:
 *   -Xms256m -Xmx512m -XX:+UseG1GC -verbose:gc
 *   -Xms64m  -Xmx64m  -XX:+UseZGC  -verbose:gc
 */
public class MemoryTuningDemo {

    // ==================== Memory Info ====================

    /** Print current JVM memory configuration */
    public static Map<String, Long> getMemoryInfo() {
        Runtime rt = Runtime.getRuntime();
        Map<String, Long> info = new LinkedHashMap<>();
        info.put("maxMemory", rt.maxMemory());
        info.put("totalMemory", rt.totalMemory());
        info.put("freeMemory", rt.freeMemory());
        info.put("usedMemory", rt.totalMemory() - rt.freeMemory());
        return info;
    }

    /** Print detailed memory pool info using MXBeans */
    public static void printMemoryPoolInfo() {
        System.out.println("=== Memory Pools ===");
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        System.out.println("Heap: " + memBean.getHeapMemoryUsage());
        System.out.println("Non-Heap: " + memBean.getNonHeapMemoryUsage());

        System.out.println("\n=== Individual Memory Pools ===");
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            System.out.printf("  %-30s type=%-8s usage=%s%n",
                    pool.getName(), pool.getType(), pool.getUsage());
        }
    }

    /** Print GC info */
    public static void printGCInfo() {
        System.out.println("\n=== Garbage Collectors ===");
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("  %-30s collections=%d  time=%dms%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }
    }

    // ==================== Memory Pressure Simulation ====================

    /** Simulate gradual memory pressure to observe GC behavior */
    public static void simulateMemoryPressure(int chunkSizeMB, int chunks) {
        System.out.println("\n=== Simulating Memory Pressure ===");
        System.out.printf("Allocating %d chunks of %dMB each%n", chunks, chunkSizeMB);

        List<byte[]> allocations = new ArrayList<>();
        for (int i = 0; i < chunks; i++) {
            try {
                allocations.add(new byte[chunkSizeMB * 1024 * 1024]);
                Runtime rt = Runtime.getRuntime();
                long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
                long maxMB = rt.maxMemory() / (1024 * 1024);
                System.out.printf("  Chunk %d: used=%dMB / max=%dMB (%.1f%%)%n",
                        i + 1, usedMB, maxMB, (double) usedMB / maxMB * 100);
            } catch (OutOfMemoryError e) {
                System.out.println("  OutOfMemoryError at chunk " + (i + 1));
                break;
            }
        }
        allocations.clear();
        System.gc();
        System.out.println("Cleared allocations and requested GC.");
    }

    // ==================== Object Promotion Demo ====================

    /**
     * Demonstrate how objects are promoted from Young to Old generation.
     * Run with: -verbose:gc -XX:+PrintGCDetails to see GC activity
     */
    public static void demonstrateObjectPromotion() {
        System.out.println("\n=== Object Promotion Demo ===");

        // Long-survived objects get promoted to Old/Tenured Generation
        List<byte[]> survivors = new ArrayList<>();

        for (int round = 0; round < 20; round++) {
            // Create short-lived garbage
            for (int i = 0; i < 1000; i++) {
                byte[] garbage = new byte[256];
            }

            // Keep some objects alive — they survive multiple GCs
            survivors.add(new byte[1024]);

            if (round % 5 == 0) {
                System.gc();
                System.out.printf("  Round %d: survivors=%d, used=%dKB%n",
                        round, survivors.size(),
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024);
            }
        }
        survivors.clear();
    }

    // ==================== Finalizer Demo ====================

    /** Demonstrate why finalize() is deprecated and unreliable */
    public static void demonstrateFinalization() {
        System.out.println("\n=== Finalization Demo (deprecated since Java 9) ===");

        for (int i = 0; i < 5; i++) {
            new FinalizableResource("Resource-" + i);
        }

        System.gc();
        System.out.println("GC requested — finalize() calls are NOT guaranteed to run immediately.");
    }

    @SuppressWarnings("deprecation")
    static class FinalizableResource {
        private final String name;

        FinalizableResource(String name) {
            this.name = name;
        }

        @Override
        protected void finalize() {
            // DON'T DO THIS — use try-with-resources or Cleaner instead
            System.out.println("  finalize() called for: " + name);
        }
    }

    // ==================== JVM Arguments Inspector ====================

    /** Print JVM startup arguments */
    public static List<String> getJvmArguments() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments();
    }

    public static void main(String[] args) {
        System.out.println("JVM: " + System.getProperty("java.vm.name") +
                " " + System.getProperty("java.vm.version"));

        System.out.println("\nJVM Arguments: " + getJvmArguments());

        Map<String, Long> memory = getMemoryInfo();
        System.out.println("\n=== Runtime Memory ===");
        memory.forEach((k, v) -> System.out.printf("  %-15s: %d MB%n", k, v / (1024 * 1024)));

        printMemoryPoolInfo();
        printGCInfo();

        demonstrateObjectPromotion();
        demonstrateFinalization();
    }
}
