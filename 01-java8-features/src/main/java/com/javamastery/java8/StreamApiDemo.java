package com.javamastery.java8;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates the Stream API introduced in Java 8.
 *
 * Streams provide a declarative way to process collections of data.
 * They support lazy evaluation and can be parallelized easily.
 */
public class StreamApiDemo {

    record Employee(String name, String department, double salary) {}

    public static void main(String[] args) {
        System.out.println("=== Stream API Demo ===\n");

        List<Employee> employees = List.of(
            new Employee("Alice", "Engineering", 95000),
            new Employee("Bob", "Engineering", 110000),
            new Employee("Charlie", "Marketing", 70000),
            new Employee("Diana", "Marketing", 75000),
            new Employee("Eve", "Engineering", 120000),
            new Employee("Frank", "Sales", 65000),
            new Employee("Grace", "Sales", 68000)
        );

        // --- 1. filter: select elements matching a predicate ---
        List<Employee> highEarners = employees.stream()
                .filter(e -> e.salary() > 90000)
                .collect(Collectors.toList());
        System.out.println("High earners (>90k): " + highEarners);

        // --- 2. map: transform each element ---
        List<String> names = employees.stream()
                .map(Employee::name)
                .collect(Collectors.toList());
        System.out.println("All names: " + names);

        // --- 3. reduce: combine elements into a single result ---
        double totalSalary = employees.stream()
                .map(Employee::salary)
                .reduce(0.0, Double::sum);
        System.out.println("Total salary: " + totalSalary);

        // --- 4. flatMap: flatten nested structures ---
        List<List<Integer>> nested = List.of(
            List.of(1, 2, 3),
            List.of(4, 5),
            List.of(6, 7, 8, 9)
        );
        List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)  // flatten List<List<Integer>> -> Stream<Integer>
                .collect(Collectors.toList());
        System.out.println("\nFlattened: " + flat);

        // --- 5. sorted + distinct ---
        List<Integer> numbers = List.of(5, 3, 1, 4, 1, 5, 9, 2, 6, 5);
        List<Integer> sortedDistinct = numbers.stream()
                .distinct()   // remove duplicates
                .sorted()     // natural order
                .collect(Collectors.toList());
        System.out.println("Sorted distinct: " + sortedDistinct);

        // --- 6. findFirst + findAny ---
        Optional<Employee> firstEngineer = employees.stream()
                .filter(e -> e.department().equals("Engineering"))
                .findFirst();
        firstEngineer.ifPresent(e -> System.out.println("\nFirst engineer: " + e.name()));

        // --- 7. anyMatch, allMatch, noneMatch ---
        boolean hasMarketing = employees.stream()
                .anyMatch(e -> e.department().equals("Marketing"));
        System.out.println("Has marketing employees: " + hasMarketing);

        boolean allAbove50k = employees.stream()
                .allMatch(e -> e.salary() > 50000);
        System.out.println("All earn above 50k: " + allAbove50k);

        // --- 8. peek: for debugging (intermediate operation) ---
        List<String> upperNames = employees.stream()
                .filter(e -> e.department().equals("Sales"))
                .peek(e -> System.out.println("  [peek] Processing: " + e.name()))
                .map(e -> e.name().toUpperCase())
                .collect(Collectors.toList());
        System.out.println("Sales team (upper): " + upperNames);

        // --- 9. Parallel streams ---
        // Parallel streams split work across ForkJoinPool.commonPool()
        // Use only for CPU-bound operations on large datasets
        long count = LongStream.rangeClosed(1, 1_000_000)
                .parallel()
                .filter(n -> n % 2 == 0)
                .count();
        System.out.println("\nEven numbers 1..1M: " + count);

        // --- 10. Stream.of and IntStream ---
        int sum = IntStream.rangeClosed(1, 10).sum();
        System.out.println("Sum 1..10: " + sum);
    }
}
