package com.javamastery.threading;

/**
 * Demonstrates the Thread Lifecycle and state transitions.
 *
 * Thread States: NEW → RUNNABLE → { BLOCKED | WAITING | TIMED_WAITING } → TERMINATED
 */
public class ThreadLifecycleDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Thread Lifecycle Demo ===\n");

        // --- 1. Creating threads ---
        // Method 1: Extend Thread
        Thread t1 = new Thread("Worker-1") {
            @Override
            public void run() {
                System.out.println(getName() + " running (Thread subclass)");
            }
        };

        // Method 2: Runnable lambda (preferred)
        Thread t2 = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " running (Runnable)");
        }, "Worker-2");

        // --- 2. NEW state ---
        System.out.println("t1 state before start: " + t1.getState()); // NEW

        // --- 3. RUNNABLE state ---
        t1.start();
        t2.start();
        System.out.println("t1 state after start: " + t1.getState()); // RUNNABLE (or TERMINATED if fast)

        t1.join();
        t2.join();

        // --- 4. TERMINATED state ---
        System.out.println("t1 state after join: " + t1.getState()); // TERMINATED

        // --- 5. TIMED_WAITING state ---
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Sleeper");
        sleeper.start();
        Thread.sleep(100); // give it time to enter sleep
        System.out.println("\nSleeper state: " + sleeper.getState()); // TIMED_WAITING
        sleeper.join();

        // --- 6. WAITING state ---
        Object lock = new Object();
        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait(); // infinite wait
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Waiter");
        waiter.start();
        Thread.sleep(100);
        System.out.println("Waiter state: " + waiter.getState()); // WAITING

        // Notify to release waiter
        synchronized (lock) {
            lock.notify();
        }
        waiter.join();
        System.out.println("Waiter state after notify: " + waiter.getState()); // TERMINATED

        // --- 7. BLOCKED state ---
        Object mutex = new Object();
        Thread holder = new Thread(() -> {
            synchronized (mutex) {
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            }
        }, "Holder");
        Thread blocked = new Thread(() -> {
            synchronized (mutex) {
                // Will block until holder releases
                System.out.println("  Blocked thread acquired lock");
            }
        }, "Blocked");

        holder.start();
        Thread.sleep(50);
        blocked.start();
        Thread.sleep(50);
        System.out.println("\nBlocked state: " + blocked.getState()); // BLOCKED

        holder.join();
        blocked.join();
    }
}
