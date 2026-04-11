package com.javamastery.jvm;

import java.lang.management.*;
import java.util.List;

/**
 * Demonstrates JVM Memory Areas using MXBeans.
 *
 * Shows runtime information about heap, non-heap memory pools,
 * and how to query memory usage programmatically.
 */
public class MemoryAreasDemo {

    public static void main(String[] args) {
        System.out.println("=== JVM Memory Areas Demo ===\n");

        // --- 1. Overall memory usage ---
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        System.out.println("--- Heap Memory ---");
        System.out.printf("  Init:      %,d bytes (%.1f MB)%n", heapUsage.getInit(), toMB(heapUsage.getInit()));
        System.out.printf("  Used:      %,d bytes (%.1f MB)%n", heapUsage.getUsed(), toMB(heapUsage.getUsed()));
        System.out.printf("  Committed: %,d bytes (%.1f MB)%n", heapUsage.getCommitted(), toMB(heapUsage.getCommitted()));
        System.out.printf("  Max:       %,d bytes (%.1f MB)%n", heapUsage.getMax(), toMB(heapUsage.getMax()));

        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        System.out.println("\n--- Non-Heap Memory (Metaspace, Code Cache) ---");
        System.out.printf("  Used:      %,d bytes (%.1f MB)%n", nonHeapUsage.getUsed(), toMB(nonHeapUsage.getUsed()));
        System.out.printf("  Committed: %,d bytes (%.1f MB)%n", nonHeapUsage.getCommitted(), toMB(nonHeapUsage.getCommitted()));

        // --- 2. Individual memory pools ---
        System.out.println("\n--- Memory Pools ---");
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            MemoryUsage usage = pool.getUsage();
            System.out.printf("  %-30s [%s] Used: %,d bytes (%.1f MB)%n",
                    pool.getName(), pool.getType(), usage.getUsed(), toMB(usage.getUsed()));
        }

        // --- 3. Runtime info ---
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        System.out.println("\n--- Runtime Info ---");
        System.out.println("  VM Name: " + runtime.getVmName());
        System.out.println("  VM Version: " + runtime.getVmVersion());
        System.out.println("  Spec Version: " + runtime.getSpecVersion());
        System.out.println("  Input Args: " + runtime.getInputArguments());

        // --- 4. Key concepts summary ---
        System.out.println("\n--- Key Memory Concepts ---");
        System.out.println("  HEAP: Where objects live. Divided into Young (Eden + Survivors) and Old generations.");
        System.out.println("  STACK: Per-thread. Stores method frames (local variables, operand stack, return address).");
        System.out.println("  METHOD AREA (Metaspace): Class metadata, constant pools, static variables. Off-heap since Java 8.");
        System.out.println("  PC REGISTER: Per-thread. Points to current bytecode instruction.");
        System.out.println("  NATIVE METHOD STACK: For JNI calls to native code.");
        System.out.println("  TLAB: Thread-Local Allocation Buffer — fast, lock-free object allocation in Eden.");
    }

    private static double toMB(long bytes) {
        return bytes / (1024.0 * 1024.0);
    }
}
