package com.javamastery.async;

import java.util.concurrent.*;
import java.util.List;

/**
 * Real-world patterns with CompletableFuture:
 * timeout, retry, parallel API calls, and virtual threads integration.
 */
public class RealWorldPatternsDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Real-World CompletableFuture Patterns ===\n");

        // --- 1. Timeout ---
        System.out.println("--- Timeout (Java 9+) ---");
        timeoutDemo();

        // --- 2. Retry Pattern ---
        System.out.println("\n--- Retry Pattern ---");
        retryDemo();

        // --- 3. Parallel API Calls ---
        System.out.println("\n--- Parallel API Calls ---");
        parallelApiCallsDemo();

        // --- 4. Virtual Threads (Java 21) ---
        System.out.println("\n--- Virtual Thread Executor ---");
        virtualThreadDemo();
    }

    static void timeoutDemo() throws Exception {
        // orTimeout — fails with TimeoutException
        try {
            CompletableFuture.supplyAsync(() -> {
                        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        return "slow result";
                    })
                    .orTimeout(100, TimeUnit.MILLISECONDS) // Java 9
                    .get();
        } catch (ExecutionException e) {
            System.out.println("  orTimeout: " + e.getCause().getClass().getSimpleName());
        }

        // completeOnTimeout — provides fallback value on timeout
        String result = CompletableFuture.supplyAsync(() -> {
                    try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "slow result";
                })
                .completeOnTimeout("default-value", 100, TimeUnit.MILLISECONDS) // Java 9
                .get();
        System.out.println("  completeOnTimeout: " + result);
    }

    static void retryDemo() throws Exception {
        int[] attempt = {0};

        CompletableFuture<String> result = retryAsync(() -> {
            attempt[0]++;
            if (attempt[0] < 3) {
                throw new RuntimeException("Attempt " + attempt[0] + " failed");
            }
            return "Success on attempt " + attempt[0];
        }, 3);

        System.out.println("  " + result.get());
    }

    /**
     * Generic retry utility for CompletableFuture.
     */
    static <T> CompletableFuture<T> retryAsync(java.util.function.Supplier<T> supplier, int maxRetries) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);

        for (int i = 1; i < maxRetries; i++) {
            future = future.exceptionallyCompose(ex -> {
                System.out.println("  Retrying after: " + ex.getMessage());
                return CompletableFuture.supplyAsync(supplier);
            });
        }
        return future;
    }

    static void parallelApiCallsDemo() throws Exception {
        // Simulate calling multiple services in parallel
        long start = System.currentTimeMillis();

        CompletableFuture<String> userService = simulateApiCall("UserService", 150);
        CompletableFuture<String> orderService = simulateApiCall("OrderService", 200);
        CompletableFuture<String> inventoryService = simulateApiCall("InventoryService", 100);

        // Wait for all, then aggregate
        CompletableFuture<String> aggregated = CompletableFuture
                .allOf(userService, orderService, inventoryService)
                .thenApply(v -> String.join(" + ",
                        userService.join(), orderService.join(), inventoryService.join()));

        System.out.println("  Result: " + aggregated.get());
        System.out.println("  Elapsed: ~" + (System.currentTimeMillis() - start) + "ms (parallel, not 450ms)");
    }

    static CompletableFuture<String> simulateApiCall(String name, int delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(delayMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return name + ":OK";
        });
    }

    static void virtualThreadDemo() throws Exception {
        // Use virtual threads as executor for CompletableFuture
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<String>> futures = java.util.stream.IntStream.range(0, 10)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                        return "VT-" + i + " on " + Thread.currentThread();
                    }, executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get();
            System.out.println("  Completed " + futures.size() + " tasks on virtual threads");
            System.out.println("  Example: " + futures.getFirst().get());
        }
    }
}
