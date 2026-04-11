package com.javamastery.threading;

/**
 * Demonstrates the volatile keyword and visibility guarantees.
 *
 * volatile ensures:
 *   1. Visibility: writes to the variable are immediately visible to all threads
 *   2. Happens-before: a write to volatile has happens-before relationship with subsequent reads
 *
 * volatile does NOT guarantee atomicity for compound operations (e.g., counter++).
 */
public class VolatileDemo {

    // Without volatile, the reader thread may never see the update
    private static volatile boolean running = true;

    // Volatile does NOT make increment atomic
    private static volatile int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Volatile Demo ===\n");

        // --- 1. Visibility problem solved by volatile ---
        System.out.println("--- Visibility with volatile ---");

        Thread worker = new Thread(() -> {
            int count = 0;
            // Without volatile on 'running', JIT might optimize this to while(true)
            while (running) {
                count++;
            }
            System.out.println("  Worker stopped after " + count + " iterations");
        });

        worker.start();
        Thread.sleep(100); // let worker run
        running = false;   // volatile write: immediately visible to worker
        worker.join();
        System.out.println("  Worker terminated successfully (volatile ensured visibility)");

        // --- 2. Volatile does NOT guarantee atomicity ---
        System.out.println("\n--- Volatile is NOT atomic for compound operations ---");
        counter = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) counter++; // NOT atomic
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100_000; i++) counter++; // NOT atomic
        });
        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("  Expected: 200000, Got: " + counter + " (likely less due to race)");
        System.out.println("  Use AtomicInteger for atomic increment operations");

        // --- 3. When to use volatile ---
        System.out.println("\n--- When to use volatile ---");
        System.out.println("  ✓ Flags (boolean) read by one thread, written by another");
        System.out.println("  ✓ One-time publication of an immutable object");
        System.out.println("  ✓ Double-checked locking (with volatile instance field)");
        System.out.println("  ✗ Don't use for compound operations (read-modify-write)");
        System.out.println("  ✗ Don't use for operations needing multiple variables to be consistent");
    }
}
