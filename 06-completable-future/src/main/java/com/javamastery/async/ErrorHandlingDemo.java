package com.javamastery.async;

import java.util.concurrent.*;

/**
 * Demonstrates error handling in CompletableFuture:
 * exceptionally, handle, whenComplete.
 */
public class ErrorHandlingDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Error Handling in CompletableFuture ===\n");

        // --- 1. exceptionally — recover from exceptions ---
        System.out.println("--- exceptionally ---");
        exceptionallyDemo();

        // --- 2. handle — process both result and exception ---
        System.out.println("\n--- handle ---");
        handleDemo();

        // --- 3. whenComplete — side effects without changing result ---
        System.out.println("\n--- whenComplete ---");
        whenCompleteDemo();

        // --- 4. Composition error propagation ---
        System.out.println("\n--- Error Propagation in Chains ---");
        propagationDemo();

        System.out.println("\n--- Summary ---");
        System.out.println("  exceptionally(fn)   → recover with fallback value (only on error)");
        System.out.println("  handle(bifn)        → process BOTH success and error, transform result");
        System.out.println("  whenComplete(biCon)  → side-effect (logging), does NOT change result");
    }

    static void exceptionallyDemo() throws Exception {
        // exceptionally provides a fallback value when exception occurs
        String result = CompletableFuture.supplyAsync(() -> {
                    if (true) throw new RuntimeException("Service unavailable");
                    return "data";
                })
                .exceptionally(ex -> {
                    System.out.println("  Exception caught: " + ex.getMessage());
                    return "fallback-data"; // recovery value
                })
                .get();

        System.out.println("  Result: " + result);

        // No exception case — exceptionally is skipped
        String ok = CompletableFuture.supplyAsync(() -> "success")
                .exceptionally(ex -> "fallback") // never called
                .get();
        System.out.println("  No error: " + ok);
    }

    static void handleDemo() throws Exception {
        // handle receives BOTH result and exception (one will be null)
        // Can transform the result regardless of success/failure

        String result = CompletableFuture.supplyAsync(() -> {
                    if (true) throw new RuntimeException("Oops");
                    return "data";
                })
                .handle((data, ex) -> {
                    if (ex != null) {
                        System.out.println("  handle (error): " + ex.getMessage());
                        return "handled-fallback";
                    }
                    return data.toUpperCase();
                })
                .get();

        System.out.println("  Result: " + result);

        // Success case
        String ok = CompletableFuture.supplyAsync(() -> "hello")
                .handle((data, ex) -> {
                    if (ex != null) return "error";
                    return data.toUpperCase(); // transform on success
                })
                .get();
        System.out.println("  Success handled: " + ok);
    }

    static void whenCompleteDemo() throws Exception {
        // whenComplete is for side effects — logging, metrics
        // Does NOT change the result (unlike handle)

        try {
            CompletableFuture.supplyAsync(() -> {
                        if (true) throw new RuntimeException("Boom!");
                        return "data";
                    })
                    .whenComplete((result, ex) -> {
                        // Side effect — logging
                        if (ex != null) {
                            System.out.println("  whenComplete: logged error - " + ex.getMessage());
                        } else {
                            System.out.println("  whenComplete: success - " + result);
                        }
                    })
                    .get(); // This will still throw because whenComplete doesn't swallow exceptions
        } catch (ExecutionException e) {
            System.out.println("  Exception still propagated: " + e.getCause().getMessage());
        }

        // Success case
        String ok = CompletableFuture.supplyAsync(() -> "data")
                .whenComplete((result, ex) -> System.out.println("  whenComplete side-effect: " + result))
                .get();
        System.out.println("  Original result preserved: " + ok);
    }

    static void propagationDemo() throws Exception {
        // Exceptions propagate through the chain until handled
        String result = CompletableFuture.supplyAsync(() -> "start")
                .thenApply(s -> {
                    throw new RuntimeException("Error in stage 2");
                })
                .thenApply(s -> {
                    System.out.println("  Stage 3 — SKIPPED (previous error)");
                    return "never reached";
                })
                .thenApply(s -> {
                    System.out.println("  Stage 4 — SKIPPED");
                    return "never reached";
                })
                .exceptionally(ex -> {
                    System.out.println("  Caught at end: " + ex.getMessage());
                    return "recovered";
                })
                .get();

        System.out.println("  Final: " + result);
    }
}
