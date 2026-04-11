package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Functional Interfaces")
class FunctionalInterfacesTest {

    @Test
    @DisplayName("Predicate tests a condition")
    void predicateTests() {
        Predicate<Integer> isPositive = n -> n > 0;

        assertTrue(isPositive.test(5));
        assertFalse(isPositive.test(-1));
    }

    @Test
    @DisplayName("Predicate and/or/negate composition")
    void predicateComposition() {
        Predicate<Integer> isPositive = n -> n > 0;
        Predicate<Integer> isEven = n -> n % 2 == 0;

        Predicate<Integer> isPositiveAndEven = isPositive.and(isEven);
        Predicate<Integer> isNonPositive = isPositive.negate();

        assertTrue(isPositiveAndEven.test(4));
        assertFalse(isPositiveAndEven.test(3));
        assertTrue(isNonPositive.test(-1));
    }

    @Test
    @DisplayName("Function apply and compose")
    void functionApplyAndCompose() {
        Function<Integer, Integer> doubleIt = n -> n * 2;
        Function<Integer, Integer> addTen = n -> n + 10;

        // andThen: doubleIt first, then addTen
        assertEquals(30, doubleIt.andThen(addTen).apply(10)); // (10*2) + 10

        // compose: addTen first, then doubleIt
        assertEquals(40, doubleIt.compose(addTen).apply(10)); // (10+10) * 2
    }

    @Test
    @DisplayName("Consumer accepts value for side effects")
    void consumerAccepts() {
        StringBuilder sb = new StringBuilder();
        Consumer<String> appender = sb::append;

        appender.accept("Hello");
        appender.accept(" World");

        assertEquals("Hello World", sb.toString());
    }

    @Test
    @DisplayName("Consumer andThen chains consumers")
    void consumerAndThen() {
        StringBuilder sb = new StringBuilder();
        Consumer<String> lower = s -> sb.append(s.toLowerCase());
        Consumer<String> space = s -> sb.append(" ");

        Consumer<String> lowerThenSpace = lower.andThen(space);
        lowerThenSpace.accept("HELLO");
        lowerThenSpace.accept("WORLD");

        assertEquals("hello world ", sb.toString());
    }

    @Test
    @DisplayName("Supplier provides values")
    void supplierProvides() {
        Supplier<String> greeting = () -> "Hello";

        assertEquals("Hello", greeting.get());
    }

    @Test
    @DisplayName("BiFunction takes two arguments")
    void biFunctionTwoArgs() {
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);

        assertEquals("abcabcabc", repeat.apply("abc", 3));
    }

    @Test
    @DisplayName("UnaryOperator is Function<T,T>")
    void unaryOperator() {
        UnaryOperator<String> toUpper = String::toUpperCase;

        assertEquals("HELLO", toUpper.apply("hello"));
    }

    @Test
    @DisplayName("BinaryOperator is BiFunction<T,T,T>")
    void binaryOperator() {
        BinaryOperator<Integer> sum = Integer::sum;

        assertEquals(7, sum.apply(3, 4));
    }
}
