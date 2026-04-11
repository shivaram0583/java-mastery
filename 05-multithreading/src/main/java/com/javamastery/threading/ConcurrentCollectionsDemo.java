package com.javamastery.threading;

import java.util.concurrent.*;
import java.util.*;

/**
 * Demonstrates thread-safe concurrent collections from java.util.concurrent.
 * These replace synchronized wrappers (Collections.synchronizedXxx) with
 * better performance through lock striping, copy-on-write, and lock-free algorithms.
 */
public class ConcurrentCollectionsDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Concurrent Collections Demo ===\n");

        // --- 1. ConcurrentHashMap ---
        System.out.println("--- ConcurrentHashMap ---");
        concurrentHashMapDemo();

        // --- 2. CopyOnWriteArrayList ---
        System.out.println("\n--- CopyOnWriteArrayList ---");
        copyOnWriteDemo();

        // --- 3. BlockingQueue ---
        System.out.println("\n--- BlockingQueue (Producer-Consumer) ---");
        blockingQueueDemo();

        // --- 4. ConcurrentLinkedQueue ---
        System.out.println("\n--- ConcurrentLinkedQueue ---");
        concurrentLinkedQueueDemo();
    }

    static void concurrentHashMapDemo() throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // Multiple threads can write simultaneously — lock striping
        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    String key = "key-" + (threadId * 1000 + j);
                    map.put(key, j);
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();
        System.out.println("  Size: " + map.size() + " (expected 4000)");

        // Atomic compute operations (no external synchronization needed)
        map.put("counter", 0);
        map.compute("counter", (k, v) -> v + 1);
        map.merge("counter", 10, Integer::sum);
        System.out.println("  Atomic compute => counter: " + map.get("counter"));

        // Bulk operations (parallel with threshold)
        long sum = map.reduceValues(1000, // parallelism threshold
                (a, b) -> a + b);
        System.out.println("  reduceValues sum: " + sum);

        // forEach in parallel
        map.forEach(2000, (k, v) -> {}); // threshold controls parallelism
        System.out.println("  forEach completed (parallel when size > threshold)");
    }

    static void copyOnWriteDemo() {
        // CopyOnWriteArrayList — ideal for read-heavy, write-rare scenarios
        // Every write creates a new internal array copy
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        // Safe to iterate while modifying — iterator sees snapshot
        for (String item : list) {
            list.add(item + "-copy"); // No ConcurrentModificationException!
        }
        System.out.println("  After modification during iteration: " + list);
        System.out.println("  Use when: read >> write (e.g., listener lists, config)");
        System.out.println("  Avoid when: frequent writes (each write copies entire array)");
    }

    static void blockingQueueDemo() throws InterruptedException {
        // Producer-Consumer pattern with bounded queue
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(5);

        // Producer
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    String item = "Item-" + i;
                    queue.put(item); // blocks if queue is full
                    System.out.println("  [PRODUCED] " + item + " (queue size: " + queue.size() + ")");
                }
                queue.put("DONE"); // poison pill
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        // Consumer
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    String item = queue.take(); // blocks if queue is empty
                    if ("DONE".equals(item)) break;
                    System.out.println("  [CONSUMED] " + item);
                    Thread.sleep(50); // simulate processing
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        System.out.println("  Queue types: ArrayBlockingQueue (bounded), LinkedBlockingQueue,");
        System.out.println("    PriorityBlockingQueue, SynchronousQueue (handoff), DelayQueue");
    }

    static void concurrentLinkedQueueDemo() throws InterruptedException {
        // Non-blocking, lock-free queue using CAS operations
        ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

        Thread[] producers = new Thread[3];
        for (int i = 0; i < producers.length; i++) {
            final int id = i;
            producers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    queue.offer(id * 100 + j);
                }
            });
            producers[i].start();
        }
        for (Thread t : producers) t.join();

        System.out.println("  Total elements: " + queue.size() + " (expected 300)");
        System.out.println("  Use when: unbounded, non-blocking, high-throughput needed");
    }
}
