package com.javamastery.java18to24;

import java.util.Map;

/**
 * Demonstrates Unnamed Variables and Patterns (Java 22 — JEP 456).
 *
 * Use _ (underscore) when a variable or pattern is unused.
 * This clarifies intent and avoids compiler warnings.
 *
 * NOTE: Requires --enable-preview on Java 22+.
 */
public class UnnamedVariablesDemo {

    sealed interface Shape permits Circ, Rect {}
    record Circ(double radius) implements Shape {}
    record Rect(double w, double h) implements Shape {}

    record Pair<A, B>(A first, B second) {}

    public static void main(String[] args) {
        System.out.println("=== Unnamed Variables Demo (Java 22) ===\n");

        // --- 1. Unnamed variable in catch block ---
        try {
            int result = Integer.parseInt("not-a-number");
        } catch (NumberFormatException _) {
            // We don't need the exception object, just handling the case
            System.out.println("Failed to parse number (exception variable unused)");
        }

        // --- 2. Unnamed variable in enhanced for loop ---
        var items = java.util.List.of("a", "b", "c", "d", "e");
        int count = 0;
        for (var _ : items) {
            // We only care about how many items, not their values
            count++;
        }
        System.out.println("Count (unused loop var): " + count);

        // --- 3. Unnamed variable in try-with-resources ---
        // When you need the resource for its side-effects only
        // try (var _ = ScopedContext.open()) { ... }

        // --- 4. Unnamed patterns in switch ---
        Shape shape = new Circ(5.0);
        String type = switch (shape) {
            case Circ(_) -> "circle";   // don't need radius
            case Rect(_, _) -> "rectangle"; // don't need dimensions
        };
        System.out.println("Shape type: " + type);

        // --- 5. Partial destructuring with unnamed ---
        var pair = new Pair<>("key", 42);
        if (pair instanceof Pair<String, Integer>(var key, _)) {
            System.out.println("Key only: " + key);
        }

        // --- 6. Unnamed in map iteration ---
        var map = Map.of("a", 1, "b", 2, "c", 3);
        int total = 0;
        for (var entry : map.entrySet()) {
            // If we only need values
            total += entry.getValue();
        }
        System.out.println("Total values: " + total);
    }
}
