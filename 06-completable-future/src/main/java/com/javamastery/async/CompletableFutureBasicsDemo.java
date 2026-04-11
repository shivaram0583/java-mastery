package com.javamastery.async;

import java.util.concurrent.*;

/**
 * Demonstrates CompletableFuture basics:
 * creation, supplyAsync, runAsync, thenApply, thenAccept, thenRun.
 */
public class CompletableFutureBasicsDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== CompletableFuture Basics ===\n");

        // --- 1. supplyAsync — async task with return value ---
        System.out.println("--- supplyAsync ---");
        CompletableFuture<String> cf1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Running on: " + Thread.currentThread().getName());
            return "Hello from supplyAsync";
        });
        System.out.println("  Result: " + cf1.get());

        // --- 2. runAsync — async task without return value ---
        System.out.println("\n--- runAsync ---");
        CompletableFuture<Void> cf2 = CompletableFuture.runAsync(() -> {
            System.out.println("  runAsync on: " + Thread.currentThread().getName());
        });
        cf2.get(); // wait for completion

        // --- 3. thenApply — transform result (like map) ---
        System.out.println("\n--- thenApply (transform) ---");
        CompletableFuture<Integer> cf3 = CompletableFuture.supplyAsync(() -> "42")
                .thenApply(s -> {
                    System.out.println("  Parsing on: " + Thread.currentThread().getName());
                    return Integer.parseInt(s);
                })
                .thenApply(n -> n * 2);
        System.out.println("  Result: " + cf3.get());

        // --- 4. thenAccept — consume result (no return) ---
        System.out.println("\n--- thenAccept (consume) ---");
        CompletableFuture.supplyAsync(() -> "Result to consume")
                .thenAccept(s -> System.out.println("  Consumed: " + s))
                .get();

        // --- 5. thenRun — run after completion (ignores result) ---
        System.out.println("\n--- thenRun (fire-and-forget) ---");
        CompletableFuture.supplyAsync(() -> "ignored")
                .thenRun(() -> System.out.println("  thenRun: previous stage done"))
                .get();

        // --- 6. completedFuture — pre-completed value ---
        System.out.println("\n--- completedFuture ---");
        CompletableFuture<String> preCompleted = CompletableFuture.completedFuture("already done");
        System.out.println("  Value: " + preCompleted.get());

        // --- 7. Custom executor ---
        System.out.println("\n--- Custom Executor ---");
        ExecutorService myPool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "MyPool-thread");
            t.setDaemon(true);
            return t;
        });

        CompletableFuture.supplyAsync(() -> {
            System.out.println("  Custom pool: " + Thread.currentThread().getName());
            return "custom";
        }, myPool).thenAcceptAsync(s -> {
            System.out.println("  Async callback: " + Thread.currentThread().getName());
        }, myPool).get();

        myPool.shutdown();

        System.out.println("\n--- Key Takeaways ---");
        System.out.println("  supplyAsync  → returns CompletableFuture<T>");
        System.out.println("  runAsync     → returns CompletableFuture<Void>");
        System.out.println("  thenApply    → transform (like Stream.map)");
        System.out.println("  thenAccept   → consume (like Stream.forEach)");
        System.out.println("  thenRun      → run action after completion");
        System.out.println("  *Async       → runs on different thread");
    }
}
