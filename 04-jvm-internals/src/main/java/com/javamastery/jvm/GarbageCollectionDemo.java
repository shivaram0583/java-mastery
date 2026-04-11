package com.javamastery.jvm;

import java.lang.management.*;
import java.util.List;

/**
 * Demonstrates Garbage Collection concepts.
 *
 * This demo creates objects to trigger GC and shows GC statistics.
 * Run with GC logging for detailed output:
 *   java -Xlog:gc*:file=gc.log -Xms64m -Xmx128m GarbageCollectionDemo
 *
 * Try different collectors:
 *   -XX:+UseSerialGC        Serial (single-threaded, stop-the-world)
 *   -XX:+UseParallelGC      Parallel (multi-threaded, throughput-oriented)
 *   -XX:+UseG1GC            G1 (default since Java 9, region-based)
 *   -XX:+UseZGC             ZGC (ultra-low latency, <1ms pauses)
 *   -XX:+UseShenandoahGC    Shenandoah (concurrent compaction)
 */
public class GarbageCollectionDemo {

    public static void main(String[] args) {
        System.out.println("=== Garbage Collection Demo ===\n");

        // --- 1. Show active GC collectors ---
        System.out.println("--- Active Garbage Collectors ---");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            System.out.printf("  %s: %d collections, %d ms total%n",
                    gc.getName(), gc.getCollectionCount(), gc.getCollectionTime());
        }

        // --- 2. Allocate objects to trigger GC ---
        System.out.println("\n--- Allocating objects to trigger GC ---");
        long[] gcCountsBefore = gcBeans.stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionCount).toArray();

        // Create lots of short-lived objects (will be collected in minor GC)
        for (int i = 0; i < 100_000; i++) {
            // These byte arrays fill up Eden space quickly
            byte[] garbage = new byte[1024];
        }

        // Check GC stats after allocation
        System.out.println("\n--- GC Stats After Allocation ---");
        for (int i = 0; i < gcBeans.size(); i++) {
            GarbageCollectorMXBean gc = gcBeans.get(i);
            long newCollections = gc.getCollectionCount() - gcCountsBefore[i];
            System.out.printf("  %s: +%d collections (total: %d, %d ms)%n",
                    gc.getName(), newCollections, gc.getCollectionCount(), gc.getCollectionTime());
        }

        // --- 3. Request GC (hint only — JVM may ignore) ---
        System.out.println("\n--- Requesting System.gc() ---");
        long beforeGc = Runtime.getRuntime().freeMemory();
        System.gc(); // This is a hint; JVM is not obligated to run GC
        long afterGc = Runtime.getRuntime().freeMemory();
        System.out.printf("  Free memory before: %,d bytes%n", beforeGc);
        System.out.printf("  Free memory after:  %,d bytes%n", afterGc);
        System.out.printf("  Freed: %,d bytes%n", afterGc - beforeGc);

        // --- 4. Key GC Tuning Tips ---
        System.out.println("\n--- GC Tuning Tips ---");
        System.out.println("  • Set -Xms = -Xmx to avoid heap resizing");
        System.out.println("  • G1GC: tune -XX:MaxGCPauseMillis for latency targets");
        System.out.println("  • ZGC: for heaps >4GB needing <1ms pauses");
        System.out.println("  • Monitor with: -Xlog:gc*:file=gc.log:time,level,tags");
        System.out.println("  • Analyze GC logs with GCViewer or GCEasy");
        System.out.println("  • Avoid System.gc() in production (use XX:+DisableExplicitGC)");

        // --- 5. Finalization (deprecated) ---
        System.out.println("\n--- finalize() is DEPRECATED since Java 9 ---");
        System.out.println("  Use try-with-resources or Cleaner instead.");
    }
}
