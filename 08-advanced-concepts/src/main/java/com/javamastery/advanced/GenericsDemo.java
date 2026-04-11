package com.javamastery.advanced;

import java.util.*;

/**
 * Demonstrates advanced Generics concepts:
 * bounded types, wildcards, PECS, type erasure, generic methods.
 */
public class GenericsDemo {

    // --- 1. Generic class ---
    static class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A first, B second) { this.first = first; this.second = second; }
        A first() { return first; }
        B second() { return second; }

        @Override
        public String toString() { return "(" + first + ", " + second + ")"; }
    }

    // --- 2. Bounded type parameter ---
    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    // Multiple bounds
    static <T extends Comparable<T> & java.io.Serializable> T bounded(T val) {
        return val;
    }

    // --- 3. Generic method ---
    static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }

    // --- 4. Wildcards and PECS ---
    // PECS: Producer Extends, Consumer Super

    // Producer — reads from collection (use ? extends T)
    static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number n : numbers) {
            total += n.doubleValue();
        }
        return total;
    }

    // Consumer — writes to collection (use ? super T)
    static void fill(List<? super Integer> list, int count) {
        for (int i = 0; i < count; i++) {
            list.add(i);
        }
    }

    // --- 5. Type-safe container using Class<T> as key ---
    static class TypeSafeMap {
        private final Map<Class<?>, Object> map = new HashMap<>();

        <T> void put(Class<T> type, T instance) {
            map.put(type, instance);
        }

        <T> T get(Class<T> type) {
            return type.cast(map.get(type));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Generics Demo ===\n");

        // --- Generic class ---
        System.out.println("--- Generic Class ---");
        Pair<String, Integer> pair = new Pair<>("age", 30);
        System.out.println("  Pair: " + pair);

        // --- Bounded type parameter ---
        System.out.println("\n--- Bounded Type Parameter ---");
        System.out.println("  max(3, 7): " + max(3, 7));
        System.out.println("  max(\"apple\", \"banana\"): " + max("apple", "banana"));

        // --- Wildcards / PECS ---
        System.out.println("\n--- PECS: Producer Extends, Consumer Super ---");

        // ? extends Number — can read Numbers, cannot add
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.5, 2.5);
        System.out.println("  sum(ints): " + sum(ints));
        System.out.println("  sum(doubles): " + sum(doubles));

        // ? super Integer — can add Integers, reading gives Object
        List<Number> numbers = new ArrayList<>();
        fill(numbers, 5);
        System.out.println("  fill result: " + numbers);

        // --- Type-safe heterogeneous container ---
        System.out.println("\n--- Type-Safe Container ---");
        TypeSafeMap container = new TypeSafeMap();
        container.put(String.class, "hello");
        container.put(Integer.class, 42);
        container.put(Double.class, 3.14);

        String s = container.get(String.class);    // no cast needed
        Integer i = container.get(Integer.class);
        System.out.println("  String: " + s + ", Integer: " + i);

        // --- Type Erasure ---
        System.out.println("\n--- Type Erasure ---");
        List<String> strList = new ArrayList<>();
        List<Integer> intList = new ArrayList<>();
        // At runtime, both are just ArrayList — generics are erased
        System.out.println("  strList.getClass() == intList.getClass(): "
                + (strList.getClass() == intList.getClass()));
        System.out.println("  Actual runtime class: " + strList.getClass().getName());
        System.out.println("  This is why you cannot do: new T(), or instanceof List<String>");

        // --- Generic method ---
        System.out.println("\n--- Generic Method ---");
        List<String> list = listOf("a", "b", "c");
        System.out.println("  listOf: " + list);

        System.out.println("\n--- PECS Cheat Sheet ---");
        System.out.println("  Use <? extends T> when you only READ (produce) from collection");
        System.out.println("  Use <? super T>   when you only WRITE (consume) into collection");
        System.out.println("  Use <T>           when you both READ and WRITE");
    }
}
