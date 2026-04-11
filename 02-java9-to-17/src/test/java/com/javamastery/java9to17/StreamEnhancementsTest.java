package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stream Enhancements (Java 9)")
class StreamEnhancementsTest {

    @Test
    @DisplayName("takeWhile stops at first failing predicate")
    void takeWhile() {
        List<Integer> result = List.of(1, 2, 3, 4, 5, 6).stream()
                .takeWhile(n -> n < 4)
                .collect(Collectors.toList());

        assertEquals(List.of(1, 2, 3), result);
    }

    @Test
    @DisplayName("dropWhile skips while predicate is true")
    void dropWhile() {
        List<Integer> result = List.of(1, 2, 3, 4, 5).stream()
                .dropWhile(n -> n < 3)
                .collect(Collectors.toList());

        assertEquals(List.of(3, 4, 5), result);
    }

    @Test
    @DisplayName("Stream.ofNullable returns empty for null")
    void ofNullableNull() {
        assertEquals(0, Stream.ofNullable(null).count());
    }

    @Test
    @DisplayName("Stream.ofNullable returns single element for non-null")
    void ofNullableNonNull() {
        List<String> result = Stream.ofNullable("hello").collect(Collectors.toList());
        assertEquals(List.of("hello"), result);
    }

    @Test
    @DisplayName("Stream.iterate with predicate creates bounded stream")
    void iterateWithPredicate() {
        List<Integer> result = Stream.iterate(1, n -> n <= 16, n -> n * 2)
                .collect(Collectors.toList());

        assertEquals(List.of(1, 2, 4, 8, 16), result);
    }
}
