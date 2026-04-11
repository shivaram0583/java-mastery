package com.javamastery.java18to24;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Demonstrates Virtual Threads (Java 21, finalized — JEP 444).
 *
 * Virtual threads are lightweight threads managed by the JVM runtime.
 * They are ideal for I/O-bound tasks and can scale to millions of concurrent threads,
 * unlike platform (OS) threads which are limited to thousands.
 */
public class VirtualThreadDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Threads Demo (Java 21) ===\n");

        // --- 1. Creating a virtual thread ---
        Thread vThread = Thread.ofVirtual()
                .name("my-virtual-thread")
                .start(() -> {
                    System.out.println("Hello from virtual thread: " + Thread.currentThread());
                    System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
                });
        vThread.join();

        // --- 2. Platform thread for comparison ---
        Thread pThread = Thread.ofPlatform()
                .name("my-platform-thread")
                .start(() -> {
                    System.out.println("\nHello from platform thread: " + Thread.currentThread());
                    System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
                });
        pThread.join();

        // --- 3. Virtual thread executor (recommended approach) ---
        System.out.println("\n--- Using newVirtualThreadPerTaskExecutor ---");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.println("Task " + taskId + " on " + Thread.currentThread());
                    return taskId;
                });
            }
        } // auto-shutdown and await

        // --- 4. Throughput benchmark: Virtual vs Platform threads ---
        System.out.println("\n--- Throughput Benchmark ---");

        int taskCount = 10_000;

        // Virtual threads benchmark
        Instant vStart = Instant.now();
        AtomicInteger vCompleted = new AtomicInteger(0);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i ->
                executor.submit(() -> {
                    // Simulate I/O-bound work
                    Thread.sleep(Duration.ofMillis(10));
                    vCompleted.incrementAndGet();
                    return null;
                })
            );
        }
        Duration vDuration = Duration.between(vStart, Instant.now());
        System.out.printf("Virtual threads:  %d tasks in %d ms%n", vCompleted.get(), vDuration.toMillis());

        // Platform threads benchmark (limited pool to avoid OS limits)
        Instant pStart = Instant.now();
        AtomicInteger pCompleted = new AtomicInteger(0);
        try (ExecutorService executor = Executors.newFixedThreadPool(200)) {
            IntStream.range(0, taskCount).forEach(i ->
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(10));
                    pCompleted.incrementAndGet();
                    return null;
                })
            );
        }
        Duration pDuration = Duration.between(pStart, Instant.now());
        System.out.printf("Platform threads: %d tasks in %d ms (200-thread pool)%n",
                pCompleted.get(), pDuration.toMillis());

        System.out.printf("%nVirtual threads are ~%.1fx faster for I/O-bound tasks%n",
                (double) pDuration.toMillis() / vDuration.toMillis());

        // --- 5. Key points ---
        System.out.println("\n--- Key Points ---");
        System.out.println("• Virtual threads are cheap (~1KB stack vs ~1MB for platform threads)");
        System.out.println("• Best for I/O-bound tasks (HTTP calls, DB queries, file I/O)");
        System.out.println("• Avoid synchronized blocks — they pin virtual threads to carriers");
        System.out.println("• Use ReentrantLock instead of synchronized for virtual threads");
        System.out.println("• Don't pool virtual threads — create a new one per task");
    }
}
