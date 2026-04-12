package com.javamastery.java8;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

class MethodReferencesTest {

    @Test
    void staticMethodReference() {
        Function<String, Integer> parser = Integer::parseInt;
        assertEquals(42, parser.apply("42"));
    }

    @Test
    void boundInstanceMethodReference() {
        String prefix = "Hello, ";
        Function<String, String> greeter = prefix::concat;
        assertEquals("Hello, Alice", greeter.apply("Alice"));
    }

    @Test
    void unboundInstanceMethodReference() {
        Function<String, String> toUpper = String::toUpperCase;
        assertEquals("HELLO", toUpper.apply("hello"));
    }

    @Test
    void constructorReference() {
        Function<String, StringBuilder> factory = StringBuilder::new;
        StringBuilder sb = factory.apply("test");
        assertEquals("test", sb.toString());
    }

    @Test
    void arrayConstructorReference() {
        String[] result = Stream.of("a", "b", "c").toArray(String[]::new);
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test
    void methodReferenceInSorting() {
        List<String> names = List.of("Charlie", "Alice", "Bob");
        List<String> sorted = names.stream()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
        assertEquals(List.of("Alice", "Bob", "Charlie"), sorted);
    }

    @Test
    void methodReferenceWithPredicate() {
        Predicate<String> isEmpty = String::isEmpty;
        assertTrue(isEmpty.test(""));
        assertFalse(isEmpty.test("hello"));
    }
}
