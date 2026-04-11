package com.javamastery.threading;

import java.util.concurrent.*;

/**
 * Demonstrates ThreadLocal for per-thread isolated storage.
 * Covers usage patterns, InheritableThreadLocal, and pitfalls with thread pools.
 */
public class ThreadLocalDemo {

    // Each thread gets its own independent copy
    private static final ThreadLocal<String> userContext = new ThreadLocal<>();

    // WithInitial — provides default value
    private static final ThreadLocal<Integer> requestCount = ThreadLocal.withInitial(() -> 0);

    // InheritableThreadLocal — child threads inherit parent's value
    private static final InheritableThreadLocal<String> inheritableCtx = new InheritableThreadLocal<>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== ThreadLocal Demo ===\n");

        // --- 1. Basic ThreadLocal ---
        System.out.println("--- Basic ThreadLocal ---");
        basicDemo();

        // --- 2. InheritableThreadLocal ---
        System.out.println("\n--- InheritableThreadLocal ---");
        inheritableDemo();

        // --- 3. Thread Pool Pitfall ---
        System.out.println("\n--- Thread Pool Pitfall ---");
        threadPoolPitfall();

        System.out.println("\n--- Best Practices ---");
        System.out.println("  1. ALWAYS call remove() when done (especially with pools)");
        System.out.println("  2. Use withInitial() for default values");
        System.out.println("  3. Prefer Scoped Values (JEP 429, preview) over ThreadLocal in Java 21+");
        System.out.println("  4. Common uses: user sessions, DB connections, date formatters");
    }

    static void basicDemo() throws InterruptedException {
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            userContext.set("User-" + name);
            requestCount.set(requestCount.get() + 1);

            System.out.println("  " + name + ": context=" + userContext.get()
                    + ", count=" + requestCount.get());

            userContext.remove(); // IMPORTANT: prevent memory leaks
            requestCount.remove();
        };

        Thread t1 = new Thread(task, "Thread-A");
        Thread t2 = new Thread(task, "Thread-B");
        t1.start(); t2.start();
        t1.join(); t2.join();

        // Main thread has its own copy (null since we didn't set it)
        System.out.println("  Main: context=" + userContext.get() + " (null — isolated)");
    }

    static void inheritableDemo() throws InterruptedException {
        inheritableCtx.set("ParentValue");

        Thread child = new Thread(() -> {
            // Child inherits parent's value at creation time
            System.out.println("  Child sees: " + inheritableCtx.get());
            inheritableCtx.set("ChildOverride");
            System.out.println("  Child changed to: " + inheritableCtx.get());
        });
        child.start();
        child.join();

        // Parent's value unchanged
        System.out.println("  Parent still has: " + inheritableCtx.get());
        inheritableCtx.remove();
    }

    static void threadPoolPitfall() throws InterruptedException {
        // DANGER: Thread pool threads are reused, so ThreadLocal values leak!
        ExecutorService pool = Executors.newFixedThreadPool(1); // single thread

        ThreadLocal<String> leaky = new ThreadLocal<>();

        // Task 1 sets a value
        pool.submit(() -> {
            leaky.set("Task1-Secret-Data");
            System.out.println("  Task 1 set: " + leaky.get());
            // BUG: forgot to call leaky.remove()!
        }).get();

        // Task 2 runs on the SAME thread and can see stale data!
        pool.submit(() -> {
            System.out.println("  Task 2 sees LEAKED data: " + leaky.get() + " (should be null!)");
            leaky.remove(); // Clean up
        }).get();

        // CORRECT pattern: always use try-finally
        pool.submit(() -> {
            leaky.set("ProperValue");
            try {
                System.out.println("  Task 3 (proper): " + leaky.get());
            } finally {
                leaky.remove(); // ALWAYS clean up in pools
            }
        }).get();

        pool.submit(() -> {
            System.out.println("  Task 4 after cleanup: " + leaky.get() + " (null — correct!)");
        }).get();

        pool.shutdown();
    }
}
