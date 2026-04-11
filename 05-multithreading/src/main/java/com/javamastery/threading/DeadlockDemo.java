package com.javamastery.threading;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Demonstrates deadlock, livelock, starvation — and strategies to fix them.
 */
public class DeadlockDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Deadlock / Livelock / Starvation Demo ===\n");

        // --- 1. Deadlock (with detection and fix) ---
        System.out.println("--- Deadlock Reproduction & Fix ---");
        deadlockDemo();

        // --- 2. Livelock ---
        System.out.println("\n--- Livelock ---");
        livelockDemo();

        // --- 3. Starvation ---
        System.out.println("\n--- Starvation ---");
        starvationDemo();

        System.out.println("\n--- Prevention Strategies ---");
        System.out.println("  1. Lock ordering: always acquire locks in the same global order");
        System.out.println("  2. tryLock with timeout: avoid indefinite blocking");
        System.out.println("  3. Avoid nested locks when possible");
        System.out.println("  4. Use higher-level concurrency utilities (ConcurrentHashMap, etc.)");
        System.out.println("  5. Use jstack/JMX to detect deadlocks in production");
    }

    // ========== DEADLOCK ==========
    static void deadlockDemo() throws InterruptedException {
        Object lockA = new Object();
        Object lockB = new Object();

        System.out.println("  [Note] Showing the FIXED version (consistent lock ordering)");
        System.out.println("  Deadlock occurs when Thread-1 holds A, waits for B");
        System.out.println("  while Thread-2 holds B, waits for A");

        // FIXED: Both threads acquire locks in the same order (A then B)
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("  Thread-1 holds lockA");
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockB) {
                    System.out.println("  Thread-1 holds lockA + lockB");
                }
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lockA) { // FIXED: acquire lockA first (same order as Thread-1)
                System.out.println("  Thread-2 holds lockA");
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                synchronized (lockB) {
                    System.out.println("  Thread-2 holds lockA + lockB");
                }
            }
        }, "Thread-2");

        t1.start(); t2.start();
        t1.join(2000); t2.join(2000);

        // Alternative fix: tryLock with timeout
        System.out.println("\n  Alternative fix: tryLock with timeout");
        tryLockDemo();
    }

    static void tryLockDemo() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        Runnable task = () -> {
            try {
                boolean gotLock1 = lock1.tryLock(100, TimeUnit.MILLISECONDS);
                if (gotLock1) {
                    try {
                        boolean gotLock2 = lock2.tryLock(100, TimeUnit.MILLISECONDS);
                        if (gotLock2) {
                            try {
                                System.out.println("  " + Thread.currentThread().getName() + " acquired both locks");
                            } finally {
                                lock2.unlock();
                            }
                        } else {
                            System.out.println("  " + Thread.currentThread().getName() + " couldn't get lock2, backing off");
                        }
                    } finally {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread t1 = new Thread(task, "TryLock-1");
        Thread t2 = new Thread(task, "TryLock-2");
        t1.start(); t2.start();
        t1.join(); t2.join();
    }

    // ========== LIVELOCK ==========
    static void livelockDemo() throws InterruptedException {
        // Two threads keep responding to each other but make no progress
        // Like two people in a hallway who keep stepping aside for each other
        final boolean[] aMovedAside = {false};
        final boolean[] bMovedAside = {false};
        final int[] iterations = {0};

        Thread personA = new Thread(() -> {
            while (iterations[0] < 5) {
                if (bMovedAside[0]) {
                    System.out.println("  PersonA: B moved aside, I'll pass");
                    break;
                }
                System.out.println("  PersonA: I'll move aside for B (iteration " + iterations[0] + ")");
                aMovedAside[0] = true;
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                aMovedAside[0] = false;
                iterations[0]++;
            }
        });

        Thread personB = new Thread(() -> {
            while (iterations[0] < 5) {
                if (aMovedAside[0]) {
                    System.out.println("  PersonB: A moved aside, I'll pass");
                    break;
                }
                System.out.println("  PersonB: I'll move aside for A (iteration " + iterations[0] + ")");
                bMovedAside[0] = true;
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                bMovedAside[0] = false;
            }
        });

        personA.start(); personB.start();
        personA.join(2000); personB.join(2000);
        System.out.println("  Fix: introduce randomized backoff or priority");
    }

    // ========== STARVATION ==========
    static void starvationDemo() throws InterruptedException {
        // High-priority thread monopolizes the lock; low-priority thread starves
        // Using fair lock to solve it
        ReentrantLock unfairLock = new ReentrantLock(false);
        ReentrantLock fairLock = new ReentrantLock(true);

        System.out.println("  Starvation: a thread never gets access to the resource");
        System.out.println("  Fix: use fair locks (ReentrantLock(true)) — FIFO ordering");
        System.out.println("  Trade-off: fair locks have lower throughput");

        // Demonstrate fair lock
        Runnable task = () -> {
            fairLock.lock();
            try {
                System.out.println("  " + Thread.currentThread().getName() + " got fair lock");
            } finally {
                fairLock.unlock();
            }
        };

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task, "Worker-" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) t.join();
    }
}
