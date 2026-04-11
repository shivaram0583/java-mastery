package com.javamastery.async;

import java.util.concurrent.*;

/**
 * Demonstrates chaining and composing CompletableFutures.
 * thenCompose (flatMap) vs thenApply (map).
 */
public class ChainingDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Chaining CompletableFutures ===\n");

        // --- 1. thenCompose — flatMap (avoids nested futures) ---
        System.out.println("--- thenCompose (flatMap) ---");
        thenComposeDemo();

        // --- 2. Multi-stage pipeline ---
        System.out.println("\n--- Multi-Stage Pipeline ---");
        pipelineDemo();

        // --- 3. thenApply vs thenCompose ---
        System.out.println("\n--- thenApply vs thenCompose ---");
        comparisonDemo();
    }

    // Simulates fetching user ID from username
    static CompletableFuture<Integer> getUserId(String username) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  Looking up user: " + username);
            return username.hashCode() % 1000; // simulated ID
        });
    }

    // Simulates fetching user email from ID
    static CompletableFuture<String> getEmail(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  Fetching email for userId: " + userId);
            return "user" + userId + "@example.com";
        });
    }

    // Simulates sending email
    static CompletableFuture<Boolean> sendEmail(String email, String message) {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("  Sending '" + message + "' to " + email);
            return true;
        });
    }

    static void thenComposeDemo() throws Exception {
        // thenCompose chains async operations that return CompletableFuture
        // Like flatMap — prevents CompletableFuture<CompletableFuture<T>>

        CompletableFuture<Boolean> result = getUserId("alice")
                .thenCompose(userId -> getEmail(userId))              // flatMap
                .thenCompose(email -> sendEmail(email, "Welcome!"));  // flatMap

        System.out.println("  Email sent: " + result.get());
    }

    static void pipelineDemo() throws Exception {
        // Complete async pipeline: fetch → transform → validate → process
        String result = CompletableFuture.supplyAsync(() -> {
                    System.out.println("  Stage 1: Fetch raw data");
                    return "  raw-data-123";
                })
                .thenApply(data -> {
                    System.out.println("  Stage 2: Parse data");
                    return data.trim().toUpperCase();
                })
                .thenApply(parsed -> {
                    System.out.println("  Stage 3: Validate");
                    if (parsed.isEmpty()) throw new RuntimeException("Invalid data");
                    return parsed;
                })
                .thenApply(valid -> {
                    System.out.println("  Stage 4: Enrich");
                    return valid + " [enriched at " + System.currentTimeMillis() + "]";
                })
                .get();

        System.out.println("  Final: " + result);
    }

    static void comparisonDemo() throws Exception {
        // thenApply: for synchronous transformations
        // Returns CompletableFuture<R> when function returns R
        CompletableFuture<String> applied = CompletableFuture.supplyAsync(() -> 42)
                .thenApply(n -> "Value is " + n); // Function<T, R>

        System.out.println("  thenApply result: " + applied.get());

        // thenCompose: for async transformations (function returns CompletableFuture)
        // Returns CompletableFuture<R> (flattened), not CompletableFuture<CompletableFuture<R>>
        CompletableFuture<String> composed = CompletableFuture.supplyAsync(() -> 42)
                .thenCompose(n -> CompletableFuture.supplyAsync(() -> "Async value: " + n));

        System.out.println("  thenCompose result: " + composed.get());

        // WITHOUT thenCompose — nested future (BAD)
        CompletableFuture<CompletableFuture<String>> nested = CompletableFuture.supplyAsync(() -> 42)
                .thenApply(n -> CompletableFuture.supplyAsync(() -> "Nested: " + n));
        System.out.println("  WRONG (nested): " + nested.get().get()); // double get()!
    }
}
