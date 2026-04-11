package com.javamastery.java9to17;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates Stream enhancements from Java 9:
 * - takeWhile: takes elements while predicate is true
 * - dropWhile: drops elements while predicate is true
 * - Stream.ofNullable: handles null gracefully
 * - Stream.iterate with hasNext predicate (bounded iterate)
 */
public class StreamEnhancementsDemo {

    public static void main(String[] args) {
        System.out.println("=== Stream Enhancements Demo (Java 9) ===\n");

        List<Integer> sorted = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // --- takeWhile: stop once predicate fails (works best on ordered streams) ---
        List<Integer> taken = sorted.stream()
                .takeWhile(n -> n < 5)
                .collect(Collectors.toList());
        System.out.println("takeWhile(< 5): " + taken); // [1, 2, 3, 4]

        // --- dropWhile: skip while predicate is true, take the rest ---
        List<Integer> dropped = sorted.stream()
                .dropWhile(n -> n < 5)
                .collect(Collectors.toList());
        System.out.println("dropWhile(< 5): " + dropped); // [5, 6, 7, 8, 9, 10]

        // --- Stream.ofNullable: returns empty stream for null ---
        String value = null;
        long count = Stream.ofNullable(value).count();
        System.out.println("\nStream.ofNullable(null).count(): " + count); // 0

        long countNonNull = Stream.ofNullable("hello").count();
        System.out.println("Stream.ofNullable(\"hello\").count(): " + countNonNull); // 1

        // --- Stream.iterate with predicate (bounded iterate) ---
        // Java 8 iterate was infinite: Stream.iterate(seed, unaryOperator)
        // Java 9 adds: Stream.iterate(seed, hasNext, next)
        List<Integer> powers = Stream.iterate(1, n -> n < 1000, n -> n * 2)
                .collect(Collectors.toList());
        System.out.println("\nPowers of 2 (< 1000): " + powers);
    }
}
