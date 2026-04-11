package com.javamastery.threading;

/**
 * Demonstrates synchronization in Java.
 *
 * Without synchronization, concurrent writes to shared state cause data races.
 * Java provides intrinsic locks (synchronized) and explicit Locks.
 */
public class SynchronizationDemo {

    // Shared mutable state — NOT thread-safe without synchronization
    private int unsafeCounter = 0;
    private int safeCounter = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Synchronization Demo ===\n");

        SynchronizationDemo demo = new SynchronizationDemo();

        // --- 1. Race condition (no synchronization) ---
        System.out.println("--- Without synchronization (race condition) ---");
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) demo.unsafeCounter++;
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) demo.unsafeCounter++;
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("Expected: 200000, Got: " + demo.unsafeCounter);

        // --- 2. Synchronized method ---
        System.out.println("\n--- With synchronized method ---");
        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) demo.incrementSafe();
        });
        Thread t4 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) demo.incrementSafe();
        });
        t3.start(); t4.start();
        t3.join(); t4.join();
        System.out.println("Expected: 200000, Got: " + demo.safeCounter);

        // --- 3. Synchronized block with specific lock object ---
        System.out.println("\n--- Synchronized block ---");
        int[] blockCounter = {0};
        Object lock = new Object();

        Thread t5 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) {
                synchronized (lock) {
                    blockCounter[0]++;
                }
            }
        });
        Thread t6 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) {
                synchronized (lock) {
                    blockCounter[0]++;
                }
            }
        });
        t5.start(); t6.start();
        t5.join(); t6.join();
        System.out.println("Expected: 200000, Got: " + blockCounter[0]);

        // --- 4. Key points ---
        System.out.println("\n--- Key Points ---");
        System.out.println("• synchronized is reentrant (same thread can re-acquire)");
        System.out.println("• synchronized on instance method locks 'this'");
        System.out.println("• synchronized on static method locks the Class object");
        System.out.println("• Prefer synchronized blocks over methods (finer granularity)");
        System.out.println("• Every Java object has an intrinsic lock (monitor)");
    }

    /** Synchronized method — lock = 'this' object */
    synchronized void incrementSafe() {
        safeCounter++;
    }
}
