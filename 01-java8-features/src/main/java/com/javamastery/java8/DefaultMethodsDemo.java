package com.javamastery.java8;

/**
 * Demonstrates default and static methods in interfaces (Java 8).
 *
 * Before Java 8, interfaces could only have abstract methods.
 * Default methods allow adding new methods to interfaces without
 * breaking existing implementations. Static methods provide utility
 * functions directly on the interface.
 */
public class DefaultMethodsDemo {

    // Interface with default and static methods
    interface Logger {
        void log(String message);

        // Default method — provides a default implementation
        default void logWithTimestamp(String message) {
            log("[" + java.time.LocalDateTime.now() + "] " + message);
        }

        // Static method — utility that belongs to the interface itself
        static Logger createConsoleLogger() {
            return message -> System.out.println("[CONSOLE] " + message);
        }
    }

    // Demonstrates the diamond problem resolution
    interface Flyable {
        default String capability() {
            return "I can fly!";
        }
    }

    interface Swimmable {
        default String capability() {
            return "I can swim!";
        }
    }

    // Must override because both interfaces define the same default method
    static class Duck implements Flyable, Swimmable {
        @Override
        public String capability() {
            // Explicitly choose which default to use, or provide new logic
            return Flyable.super.capability() + " And " + Swimmable.super.capability();
        }
    }

    // Interface evolution example
    interface Validator<T> {
        boolean validate(T item);

        // Added later without breaking existing implementations
        default Validator<T> and(Validator<T> other) {
            return item -> this.validate(item) && other.validate(item);
        }

        default Validator<T> or(Validator<T> other) {
            return item -> this.validate(item) || other.validate(item);
        }

        default Validator<T> negate() {
            return item -> !this.validate(item);
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Default & Static Methods Demo ===\n");

        // --- 1. Static factory method on interface ---
        Logger logger = Logger.createConsoleLogger();
        logger.log("Direct log message");
        logger.logWithTimestamp("Log with default timestamp method");

        // --- 2. Custom implementation still works ---
        Logger fileLogger = message -> System.out.println("[FILE] " + message);
        fileLogger.log("Simulated file log");
        fileLogger.logWithTimestamp("File log with timestamp");

        // --- 3. Diamond problem resolution ---
        Duck duck = new Duck();
        System.out.println("\nDuck says: " + duck.capability());

        // --- 4. Composable validators via default methods ---
        Validator<String> notEmpty = s -> !s.isEmpty();
        Validator<String> notTooLong = s -> s.length() <= 20;
        Validator<String> startsWithA = s -> s.startsWith("A");

        Validator<String> composed = notEmpty.and(notTooLong).and(startsWithA);

        System.out.println("\nValidate 'Alice': " + composed.validate("Alice"));
        System.out.println("Validate '': " + composed.validate(""));
        System.out.println("Validate 'Bob': " + composed.validate("Bob"));

        Validator<String> notEmptyOrStartsWithA = notEmpty.or(startsWithA);
        System.out.println("Validate '' (or): " + notEmptyOrStartsWithA.validate(""));
    }
}
