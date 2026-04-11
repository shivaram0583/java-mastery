package com.javamastery.java9to17;

import java.util.List;

/**
 * Demonstrates Pattern Matching for instanceof (Java 16, finalized).
 *
 * Traditional instanceof requires a separate cast after the check.
 * Pattern matching combines the check and cast into a single expression.
 */
public class PatternMatchingDemo {

    // Hierarchy for demonstration
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    public static void main(String[] args) {
        System.out.println("=== Pattern Matching for instanceof Demo (Java 16) ===\n");

        // --- 1. Old style vs new style ---
        Object obj = "Hello, World!";

        // Old way: check then cast
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("Old style - length: " + s.length());
        }

        // New way: pattern variable 's' is automatically cast and scoped
        if (obj instanceof String s) {
            System.out.println("New style - length: " + s.length());
        }

        // --- 2. Pattern variable scoping ---
        Object value = 42;
        // Pattern variable available in the 'true' branch
        if (value instanceof Integer n && n > 10) {
            System.out.println("\nInteger greater than 10: " + n);
        }

        // Pattern variable available after negated check (in else/after)
        if (!(value instanceof String s)) {
            System.out.println("Not a string");
        }

        // --- 3. Practical use: polymorphic behavior without visitor pattern ---
        List<Shape> shapes = List.of(
            new Circle(5),
            new Rectangle(4, 6),
            new Triangle(3, 8)
        );

        System.out.println("\nAreas:");
        for (Shape shape : shapes) {
            double area = calculateArea(shape);
            System.out.printf("  %s -> area = %.2f%n", shape, area);
        }

        // --- 4. Using in expressions ---
        Object mixed = 3.14;
        String description = describe(mixed);
        System.out.println("\nDescription: " + description);
    }

    static double calculateArea(Shape shape) {
        // Pattern matching for instanceof — cleaner than visitor pattern
        if (shape instanceof Circle c) {
            return Math.PI * c.radius() * c.radius();
        } else if (shape instanceof Rectangle r) {
            return r.width() * r.height();
        } else if (shape instanceof Triangle t) {
            return 0.5 * t.base() * t.height();
        }
        throw new IllegalArgumentException("Unknown shape: " + shape);
    }

    static String describe(Object obj) {
        if (obj instanceof Integer i) {
            return "Integer: " + i;
        } else if (obj instanceof Double d) {
            return "Double: " + d;
        } else if (obj instanceof String s && !s.isEmpty()) {
            return "Non-empty String: " + s;
        } else {
            return "Other: " + obj;
        }
    }
}
