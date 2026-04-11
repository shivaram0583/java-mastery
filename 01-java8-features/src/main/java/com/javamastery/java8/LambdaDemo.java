package com.javamastery.java8;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Demonstrates Lambda Expressions in Java 8.
 *
 * Lambdas provide a concise way to represent anonymous functions (instances
 * of functional interfaces). They eliminate the verbosity of anonymous classes.
 *
 * Syntax forms:
 *   (params) -> expression
 *   (params) -> { statements; }
 *   methodReference  e.g. String::toUpperCase
 */
public class LambdaDemo {

    // A functional interface has exactly one abstract method
    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);
    }

    // Functional interface for greeting
    @FunctionalInterface
    interface Greeting {
        String greet(String name);
    }

    public static void main(String[] args) {
        System.out.println("=== Lambda Expressions Demo ===\n");

        // --- 1. Basic lambda syntax ---
        MathOperation addition = (a, b) -> a + b;
        MathOperation subtraction = (a, b) -> a - b;
        // Block body lambda when logic is more complex
        MathOperation multiplication = (a, b) -> {
            int result = a * b;
            return result;
        };

        System.out.println("10 + 5 = " + addition.operate(10, 5));
        System.out.println("10 - 5 = " + subtraction.operate(10, 5));
        System.out.println("10 * 5 = " + multiplication.operate(10, 5));

        // --- 2. Lambdas replace anonymous inner classes ---
        // Old style:
        Comparator<String> oldComparator = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        };
        // Lambda style:
        Comparator<String> lambdaComparator = (s1, s2) -> s1.compareTo(s2);

        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        names.sort(lambdaComparator);
        System.out.println("\nSorted names: " + names);

        // --- 3. Method references ---
        // Static method reference
        Function<String, Integer> parseInt = Integer::parseInt;
        System.out.println("\nParsed '42': " + parseInt.apply("42"));

        // Instance method reference (arbitrary object)
        Function<String, String> toUpper = String::toUpperCase;
        System.out.println("Upper 'hello': " + toUpper.apply("hello"));

        // Instance method reference (specific object)
        String prefix = "Hello, ";
        Function<String, String> greeter = prefix::concat;
        System.out.println(greeter.apply("World!"));

        // --- 4. Lambdas with effectively final variables ---
        // The variable captured must be effectively final (not reassigned)
        String greeting = "Hi";
        Greeting greetFn = name -> greeting + ", " + name + "!";
        System.out.println("\n" + greetFn.greet("Alice"));

        // --- 5. Comparator composition ---
        List<String> words = Arrays.asList("banana", "apple", "cherry", "date");
        // Sort by length, then alphabetically
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("\nSorted by length then alpha: " + words);

        // --- 6. BinaryOperator (specialization of BiFunction) ---
        BinaryOperator<Integer> max = Integer::max;
        System.out.println("\nMax of 3, 7: " + max.apply(3, 7));
    }
}
