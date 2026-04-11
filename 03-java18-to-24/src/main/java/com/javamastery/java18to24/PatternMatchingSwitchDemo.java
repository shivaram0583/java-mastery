package com.javamastery.java18to24;

import java.util.List;

/**
 * Demonstrates Pattern Matching for switch (Java 21, finalized — JEP 441).
 *
 * Enhanced switch can now match against type patterns, guarded patterns,
 * null values, and record patterns — making it a powerful dispatching mechanism.
 */
public class PatternMatchingSwitchDemo {

    sealed interface Animal permits Dog, Cat, Fish {}
    record Dog(String name, String breed) implements Animal {}
    record Cat(String name, boolean indoor) implements Animal {}
    record Fish(String species) implements Animal {}

    public static void main(String[] args) {
        System.out.println("=== Pattern Matching for switch Demo (Java 21) ===\n");

        // --- 1. Type patterns in switch ---
        printInfo("Hello");
        printInfo(42);
        printInfo(3.14);
        printInfo(List.of(1, 2, 3));
        printInfo(null);

        // --- 2. Guarded patterns (when clause) ---
        System.out.println("\n--- Guarded Patterns ---");
        categorizeAge(5);
        categorizeAge(15);
        categorizeAge(25);
        categorizeAge(70);

        // --- 3. Sealed type exhaustive matching ---
        System.out.println("\n--- Sealed Type Matching ---");
        List<Animal> animals = List.of(
                new Dog("Rex", "German Shepherd"),
                new Cat("Whiskers", true),
                new Fish("Goldfish"),
                new Dog("Buddy", "Labrador"),
                new Cat("Shadow", false)
        );

        for (Animal animal : animals) {
            System.out.println("  " + describeAnimal(animal));
        }

        // --- 4. Null handling in switch ---
        System.out.println("\n--- Null Handling ---");
        System.out.println(formatValue(null));
        System.out.println(formatValue("test"));
        System.out.println(formatValue(42));
    }

    static void printInfo(Object obj) {
        // switch with type patterns and null handling
        String info = switch (obj) {
            case null         -> "null value";
            case Integer i    -> "Integer: " + i;
            case String s     -> "String: \"" + s + "\" (length " + s.length() + ")";
            case Double d     -> "Double: " + d;
            default           -> "Other: " + obj.getClass().getSimpleName();
        };
        System.out.println(info);
    }

    static void categorizeAge(int age) {
        // Guarded patterns with 'when' clause
        String category = switch (age) {
            case int a when a < 0  -> "Invalid";
            case int a when a < 13 -> "Child";
            case int a when a < 18 -> "Teenager";
            case int a when a < 65 -> "Adult";
            default                -> "Senior";
        };
        System.out.println("  Age " + age + " -> " + category);
    }

    static String describeAnimal(Animal animal) {
        // Exhaustive matching over sealed hierarchy + record deconstruction
        return switch (animal) {
            case Dog(String name, String breed) -> name + " is a " + breed + " dog";
            case Cat(String name, boolean indoor) -> name + " is an " + (indoor ? "indoor" : "outdoor") + " cat";
            case Fish(String species)            -> "A " + species + " fish";
        };
    }

    static String formatValue(Object value) {
        return switch (value) {
            case null      -> "<null>";
            case String s  -> "string:" + s;
            case Integer i -> "int:" + i;
            default        -> "unknown:" + value;
        };
    }
}
