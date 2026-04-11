package com.javamastery.java9to17;

import java.util.Optional;

/**
 * Demonstrates Optional enhancements from Java 9+:
 * - ifPresentOrElse (Java 9)
 * - or (Java 9)
 * - stream (Java 9)
 */
public class OptionalEnhancementsDemo {

    public static void main(String[] args) {
        System.out.println("=== Optional Enhancements Demo (Java 9+) ===\n");

        Optional<String> present = Optional.of("Hello");
        Optional<String> empty = Optional.empty();

        // --- ifPresentOrElse: handle both present and empty cases ---
        System.out.print("present ifPresentOrElse: ");
        present.ifPresentOrElse(
                val -> System.out.println("Got: " + val),
                () -> System.out.println("Empty!")
        );

        System.out.print("empty ifPresentOrElse: ");
        empty.ifPresentOrElse(
                val -> System.out.println("Got: " + val),
                () -> System.out.println("Empty!")
        );

        // --- or: provide alternative Optional when empty ---
        Optional<String> result = empty.or(() -> Optional.of("Fallback"));
        System.out.println("\nempty.or(fallback): " + result);

        Optional<String> result2 = present.or(() -> Optional.of("Fallback"));
        System.out.println("present.or(fallback): " + result2);

        // --- stream: convert Optional to Stream (0 or 1 element) ---
        long count = present.stream().count();
        System.out.println("\npresent.stream().count(): " + count);

        long emptyCount = empty.stream().count();
        System.out.println("empty.stream().count(): " + emptyCount);
    }
}
