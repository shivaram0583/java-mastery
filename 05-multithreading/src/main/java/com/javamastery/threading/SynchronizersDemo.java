package com.javamastery.threading;

import java.util.concurrent.*;

/**
 * Demonstrates synchronization aids from java.util.concurrent:
 * CountDownLatch, CyclicBarrier, Semaphore, and Phaser.
 */
public class SynchronizersDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Synchronizers Demo ===\n");

        // --- 1. CountDownLatch ---
        System.out.println("--- CountDownLatch ---");
        countDownLatchDemo();

        // --- 2. CyclicBarrier ---
        System.out.println("\n--- CyclicBarrier ---");
        cyclicBarrierDemo();

        // --- 3. Semaphore ---
        System.out.println("\n--- Semaphore ---");
        semaphoreDemo();

        // --- 4. Phaser ---
        System.out.println("\n--- Phaser ---");
        phaserDemo();

        System.out.println("\n--- Comparison ---");
        System.out.println("  CountDownLatch: one-shot, count to zero, await releases all");
        System.out.println("  CyclicBarrier:  reusable, all threads meet, optional barrier action");
        System.out.println("  Semaphore:      permits (rate-limiting), acquire/release");
        System.out.println("  Phaser:         dynamic parties, multiple phases, most flexible");
    }

    static void countDownLatchDemo() throws InterruptedException {
        // One-shot barrier: main thread waits for N tasks to complete
        int taskCount = 3;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 1; i <= taskCount; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("  Service " + id + " initialized");
                latch.countDown(); // decrement count
            }).start();
        }

        latch.await(); // blocks until count reaches 0
        System.out.println("  All services ready — application started!");

        // Note: latch cannot be reset — it's one-shot
    }

    static void cyclicBarrierDemo() throws Exception {
        // All threads wait at barrier, then proceed together
        // Can be reused (cyclic)
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties,
                () -> System.out.println("  >> Barrier tripped — all threads arrived!"));

        for (int i = 1; i <= parties; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("  Worker " + id + " performing phase 1");
                    barrier.await(); // wait for all parties

                    System.out.println("  Worker " + id + " performing phase 2");
                    barrier.await(); // reuse — barrier resets automatically
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Thread.sleep(500); // wait for demo to finish
    }

    static void semaphoreDemo() throws InterruptedException {
        // Limits concurrent access to a resource (e.g., connection pool)
        int maxConcurrent = 2;
        Semaphore semaphore = new Semaphore(maxConcurrent);

        for (int i = 1; i <= 5; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    semaphore.acquire(); // blocks if no permits available
                    System.out.println("  Thread " + id + " acquired permit (available: " + semaphore.availablePermits() + ")");
                    Thread.sleep(100); // simulate work
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    semaphore.release(); // return permit
                    System.out.println("  Thread " + id + " released permit");
                }
            }).start();
        }

        Thread.sleep(800); // wait for all threads
    }

    static void phaserDemo() throws InterruptedException {
        // Dynamic barrier — parties can register/deregister at runtime
        Phaser phaser = new Phaser(1); // register self (main thread)

        for (int i = 1; i <= 3; i++) {
            phaser.register(); // dynamically add party
            final int id = i;
            new Thread(() -> {
                System.out.println("  Worker " + id + " Phase 0 done");
                phaser.arriveAndAwaitAdvance(); // barrier for phase 0

                System.out.println("  Worker " + id + " Phase 1 done");
                phaser.arriveAndDeregister(); // leave after phase 1
            }).start();
        }

        // Main thread coordinates phases
        phaser.arriveAndAwaitAdvance(); // wait for phase 0
        System.out.println("  >> Phase 0 complete");

        phaser.arriveAndDeregister(); // main thread leaves
        System.out.println("  >> Main deregistered, workers finish phase 1");
        Thread.sleep(300);
    }
}
