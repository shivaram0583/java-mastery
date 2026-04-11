package com.javamastery.java9to17;

import java.util.List;

/**
 * Demonstrates Records (Java 16, finalized).
 *
 * Records are compact, immutable data carriers. The compiler auto-generates:
 *   - private final fields
 *   - canonical constructor
 *   - accessor methods (name(), not getName())
 *   - equals(), hashCode(), toString()
 */
public class RecordsDemo {

    // --- 1. Basic record declaration ---
    // This single line replaces ~40 lines of boilerplate
    record Point(int x, int y) {}

    // --- 2. Record with custom compact constructor (validation) ---
    record Range(int min, int max) {
        // Compact constructor — no parameter list, validates and assigns
        Range {
            if (min > max) {
                throw new IllegalArgumentException("min (" + min + ") must be <= max (" + max + ")");
            }
        }
    }

    // --- 3. Record with custom methods ---
    record Person(String name, int age) {
        // Records can have instance methods
        boolean isAdult() {
            return age >= 18;
        }

        // Static methods too
        static Person unknown() {
            return new Person("Unknown", 0);
        }

        // Can override toString but NOT add mutable state (no non-final instance fields)
    }

    // --- 4. Record implementing an interface ---
    interface HasName {
        String name();
    }

    record Employee(String name, String department) implements HasName {
        // name() accessor already matches the interface method
    }

    // --- 5. Generic record ---
    record Pair<A, B>(A first, B second) {}

    public static void main(String[] args) {
        System.out.println("=== Records Demo (Java 16) ===\n");

        // Basic usage
        Point p = new Point(3, 4);
        System.out.println("Point: " + p);                    // Point[x=3, y=4]
        System.out.println("x: " + p.x() + ", y: " + p.y()); // accessor methods

        // Equals is structural (value-based)
        Point p2 = new Point(3, 4);
        System.out.println("p.equals(p2): " + p.equals(p2)); // true

        // Validation via compact constructor
        Range r = new Range(1, 10);
        System.out.println("\nRange: " + r);
        try {
            new Range(10, 1); // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Custom methods
        Person alice = new Person("Alice", 30);
        System.out.println("\n" + alice.name() + " is adult: " + alice.isAdult());
        System.out.println("Unknown: " + Person.unknown());

        // Generic record
        Pair<String, Integer> pair = new Pair<>("age", 25);
        System.out.println("\nPair: " + pair);

        // Records in collections
        List<Person> people = List.of(
            new Person("Alice", 30),
            new Person("Bob", 17),
            new Person("Charlie", 22)
        );
        List<String> adults = people.stream()
                .filter(Person::isAdult)
                .map(Person::name)
                .toList();
        System.out.println("\nAdults: " + adults);
    }
}
