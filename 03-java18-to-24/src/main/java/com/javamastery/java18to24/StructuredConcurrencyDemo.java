package com.javamastery.java18to24;

import java.util.concurrent.*;
import java.time.Duration;
import java.time.Instant;

/**
 * Demonstrates Structured Concurrency (Preview in Java 21, finalized in Java 24).
 *
 * Structured concurrency treats groups of related tasks as a single unit of work.
 * When any subtask fails, the others are automatically cancelled.
 *
 * NOTE: This uses preview features. Compile and run with --enable-preview.
 */
public class StructuredConcurrencyDemo {

    record User(String name) {}
    record Order(String orderId, double amount) {}
    record UserDashboard(User user, Order latestOrder) {}

    public static void main(String[] args) throws Exception {
        System.out.println("=== Structured Concurrency Demo (Java 21/24) ===\n");

        // --- 1. ShutdownOnFailure: all-or-nothing ---
        // If any subtask fails, all others are cancelled and the scope throws
        System.out.println("--- ShutdownOnFailure (all-or-nothing) ---");
        try {
            UserDashboard dashboard = fetchDashboard("user-123");
            System.out.println("Dashboard: " + dashboard);
        } catch (Exception e) {
            System.out.println("Failed to fetch dashboard: " + e.getMessage());
        }

        // --- 2. ShutdownOnSuccess: first-result-wins ---
        System.out.println("\n--- ShutdownOnSuccess (first success wins) ---");
        try {
            String price = fetchPriceFromFastestSource("AAPL");
            System.out.println("First price: " + price);
        } catch (Exception e) {
            System.out.println("All sources failed: " + e.getMessage());
        }

        // --- 3. Comparison with CompletableFuture ---
        System.out.println("\n--- Key differences from CompletableFuture ---");
        System.out.println("• Structured: child tasks are scoped to parent lifetime");
        System.out.println("• On failure: remaining subtasks are automatically cancelled");
        System.out.println("• Thread-safety: no shared mutable state needed");
        System.out.println("• Observability: thread dumps show parent-child relationships");
    }

    /**
     * Fetches user data and latest order concurrently.
     * If either fails, the other is cancelled.
     */
    static UserDashboard fetchDashboard(String userId) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork subtasks — they run concurrently
            StructuredTaskScope.Subtask<User> userTask = scope.fork(() -> fetchUser(userId));
            StructuredTaskScope.Subtask<Order> orderTask = scope.fork(() -> fetchLatestOrder(userId));

            // Wait for all tasks to complete (or one to fail)
            scope.join();

            // Propagate any exception from subtasks
            scope.throwIfFailed();

            // Both succeeded — combine results
            return new UserDashboard(userTask.get(), orderTask.get());
        }
    }

    /**
     * Races multiple data sources — returns the first successful result.
     */
    static String fetchPriceFromFastestSource(String symbol) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            // Fork competing subtasks
            scope.fork(() -> fetchFromSourceA(symbol));
            scope.fork(() -> fetchFromSourceB(symbol));
            scope.fork(() -> fetchFromSourceC(symbol));

            scope.join();

            // Returns the result of the first successful subtask
            return scope.result();
        }
    }

    // --- Simulated services ---

    static User fetchUser(String userId) throws InterruptedException {
        Thread.sleep(100); // simulate network call
        return new User("Alice");
    }

    static Order fetchLatestOrder(String userId) throws InterruptedException {
        Thread.sleep(150); // simulate network call
        return new Order("ORD-001", 99.99);
    }

    static String fetchFromSourceA(String symbol) throws InterruptedException {
        Thread.sleep(200);
        return symbol + " = $150.00 (Source A)";
    }

    static String fetchFromSourceB(String symbol) throws InterruptedException {
        Thread.sleep(100);
        return symbol + " = $149.95 (Source B)";
    }

    static String fetchFromSourceC(String symbol) throws InterruptedException {
        Thread.sleep(300);
        return symbol + " = $150.10 (Source C)";
    }
}
