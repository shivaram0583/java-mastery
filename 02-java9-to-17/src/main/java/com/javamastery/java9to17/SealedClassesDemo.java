package com.javamastery.java9to17;

import java.util.List;

/**
 * Demonstrates Sealed Classes (Java 17, finalized).
 *
 * Sealed classes restrict which classes can extend them using the 'permits' clause.
 * This enables exhaustive pattern matching and controlled type hierarchies.
 *
 * Subclasses of a sealed class must be:
 *   - final    — no further subclassing
 *   - sealed   — can further restrict subclasses
 *   - non-sealed — reopens for arbitrary subclassing
 */
public class SealedClassesDemo {

    // --- 1. Sealed interface with permitted implementations ---
    sealed interface Expression permits Literal, Add, Multiply, Negate {}

    record Literal(double value) implements Expression {}
    record Add(Expression left, Expression right) implements Expression {}
    record Multiply(Expression left, Expression right) implements Expression {}
    record Negate(Expression operand) implements Expression {}

    // --- 2. Sealed class hierarchy with different modifiers ---
    sealed abstract class Vehicle permits Car, Truck, Bicycle {}

    static final class Car extends Vehicle {
        private final int seats;
        Car(int seats) { this.seats = seats; }
        @Override public String toString() { return "Car(" + seats + " seats)"; }
    }

    // non-sealed: allows arbitrary subclassing
    static non-sealed class Truck extends Vehicle {
        @Override public String toString() { return "Truck"; }
    }

    static final class Bicycle extends Vehicle {
        @Override public String toString() { return "Bicycle"; }
    }

    // Because Truck is non-sealed, it can be extended
    static class ElectricTruck extends Truck {
        @Override public String toString() { return "ElectricTruck"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Sealed Classes Demo (Java 17) ===\n");

        // --- Evaluating expressions ---
        // (3 + 4) * -(2)
        Expression expr = new Multiply(
                new Add(new Literal(3), new Literal(4)),
                new Negate(new Literal(2))
        );

        System.out.println("Expression: (3 + 4) * -(2)");
        System.out.println("Result: " + evaluate(expr));

        // --- Sealed hierarchy demo ---
        List<Vehicle> vehicles = List.of(
                new Car(4), new Truck(), new Bicycle(), new ElectricTruck()
        );

        System.out.println("\nVehicles:");
        for (Vehicle v : vehicles) {
            System.out.println("  " + v + " -> " + describeVehicle(v));
        }

        // --- Checking sealed class info via reflection ---
        System.out.println("\nSealed info for Expression:");
        System.out.println("  isSealed: " + Expression.class.isSealed());
        Class<?>[] permitted = Expression.class.getPermittedSubclasses();
        for (Class<?> c : permitted) {
            System.out.println("  permitted: " + c.getSimpleName());
        }
    }

    /** Evaluates an expression tree. Sealed types enable exhaustive matching. */
    static double evaluate(Expression expr) {
        return switch (expr) {
            case Literal l   -> l.value();
            case Add a       -> evaluate(a.left()) + evaluate(a.right());
            case Multiply m  -> evaluate(m.left()) * evaluate(m.right());
            case Negate n    -> -evaluate(n.operand());
            // No default needed — compiler knows all subtypes
        };
    }

    static String describeVehicle(Vehicle v) {
        // Truck and its subclasses (non-sealed) need default or explicit handling
        if (v instanceof Car c) return "car with " + c.seats + " seats";
        if (v instanceof Truck) return "truck (or subtype)";
        if (v instanceof Bicycle) return "bicycle";
        return "unknown";
    }
}
