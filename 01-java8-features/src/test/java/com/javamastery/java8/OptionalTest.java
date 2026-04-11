package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Optional")
class OptionalTest {

    @Test
    @DisplayName("Optional.of wraps non-null value")
    void ofWrapsNonNull() {
        Optional<String> opt = Optional.of("hello");

        assertTrue(opt.isPresent());
        assertEquals("hello", opt.get());
    }

    @Test
    @DisplayName("Optional.of throws on null")
    void ofThrowsOnNull() {
        assertThrows(NullPointerException.class, () -> Optional.of(null));
    }

    @Test
    @DisplayName("Optional.ofNullable handles null gracefully")
    void ofNullableHandlesNull() {
        Optional<String> opt = Optional.ofNullable(null);

        assertTrue(opt.isEmpty());
    }

    @Test
    @DisplayName("orElse returns fallback for empty")
    void orElseReturnsFallback() {
        String result = Optional.<String>empty().orElse("fallback");

        assertEquals("fallback", result);
    }

    @Test
    @DisplayName("orElseGet lazily provides fallback")
    void orElseGetIsLazy() {
        // orElseGet's Supplier is only called when Optional is empty
        String result = Optional.<String>empty().orElseGet(() -> "lazy fallback");

        assertEquals("lazy fallback", result);
    }

    @Test
    @DisplayName("orElseThrow throws for empty")
    void orElseThrowThrows() {
        assertThrows(IllegalStateException.class,
                () -> Optional.empty().orElseThrow(() -> new IllegalStateException("empty!")));
    }

    @Test
    @DisplayName("map transforms value when present")
    void mapTransforms() {
        Optional<Integer> length = Optional.of("hello").map(String::length);

        assertEquals(5, length.get());
    }

    @Test
    @DisplayName("map returns empty when Optional is empty")
    void mapOnEmpty() {
        Optional<Integer> length = Optional.<String>empty().map(String::length);

        assertTrue(length.isEmpty());
    }

    @Test
    @DisplayName("flatMap avoids nested Optionals")
    void flatMapAvoidsNesting() {
        // If mapping function returns Optional, use flatMap to avoid Optional<Optional<T>>
        Optional<String> result = Optional.of("hello")
                .flatMap(s -> Optional.of(s.toUpperCase()));

        assertEquals("HELLO", result.get());
    }

    @Test
    @DisplayName("filter keeps value matching predicate")
    void filterKeepsMatching() {
        Optional<String> result = Optional.of("hello")
                .filter(s -> s.length() > 3);

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("filter removes value not matching predicate")
    void filterRemovesNonMatching() {
        Optional<String> result = Optional.of("hi")
                .filter(s -> s.length() > 3);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Chaining operations for real-world use")
    void chainingOperations() {
        String result = Optional.of("  Hello World  ")
                .map(String::trim)
                .filter(s -> s.length() > 5)
                .map(String::toUpperCase)
                .orElse("default");

        assertEquals("HELLO WORLD", result);
    }
}
