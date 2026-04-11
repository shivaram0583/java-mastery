package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Lambda Expressions")
class LambdaTest {

    @FunctionalInterface
    interface MathOperation {
        int operate(int a, int b);
    }

    @Test
    @DisplayName("Lambda implements functional interface")
    void lambdaImplementsFunctionalInterface() {
        MathOperation add = (a, b) -> a + b;
        MathOperation multiply = (a, b) -> a * b;

        assertEquals(15, add.operate(10, 5));
        assertEquals(50, multiply.operate(10, 5));
    }

    @Test
    @DisplayName("Method reference equivalent to lambda")
    void methodReferenceEquivalent() {
        Function<String, Integer> lambdaVersion = s -> Integer.parseInt(s);
        Function<String, Integer> methodRef = Integer::parseInt;

        assertEquals(lambdaVersion.apply("42"), methodRef.apply("42"));
    }

    @Test
    @DisplayName("Lambda captures effectively final variables")
    void capturesEffectivelyFinal() {
        String prefix = "Hello"; // effectively final — never reassigned
        Function<String, String> greeter = name -> prefix + ", " + name;

        assertEquals("Hello, World", greeter.apply("World"));
    }

    @Test
    @DisplayName("Comparator with lambda sorts correctly")
    void comparatorWithLambda() {
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
        names.sort((a, b) -> a.compareTo(b));

        assertEquals(List.of("Alice", "Bob", "Charlie"), names);
    }

    @Test
    @DisplayName("Comparator composition works")
    void comparatorComposition() {
        List<String> words = Arrays.asList("bb", "a", "ccc", "dd");
        // Sort by length, then alphabetically
        words.sort(Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder()));

        assertEquals(List.of("a", "bb", "dd", "ccc"), words);
    }

    @Test
    @DisplayName("Instance method reference on specific object")
    void instanceMethodReference() {
        String prefix = "Hello, ";
        Function<String, String> greeter = prefix::concat;

        assertEquals("Hello, World!", greeter.apply("World!"));
    }
}
