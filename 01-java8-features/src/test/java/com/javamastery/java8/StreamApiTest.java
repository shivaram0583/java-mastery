package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stream API")
class StreamApiTest {

    private final List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Test
    @DisplayName("filter selects matching elements")
    void filterSelectsMatching() {
        List<Integer> evens = numbers.stream()
                .filter(n -> n % 2 == 0)
                .collect(Collectors.toList());

        assertEquals(List.of(2, 4, 6, 8, 10), evens);
    }

    @Test
    @DisplayName("map transforms each element")
    void mapTransforms() {
        List<Integer> squared = numbers.stream()
                .map(n -> n * n)
                .collect(Collectors.toList());

        assertEquals(List.of(1, 4, 9, 16, 25, 36, 49, 64, 81, 100), squared);
    }

    @Test
    @DisplayName("reduce accumulates to single value")
    void reduceAccumulates() {
        int sum = numbers.stream()
                .reduce(0, Integer::sum);

        assertEquals(55, sum);
    }

    @Test
    @DisplayName("flatMap flattens nested structures")
    void flatMapFlattens() {
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        List<Integer> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        assertEquals(List.of(1, 2, 3, 4, 5), flat);
    }

    @Test
    @DisplayName("findFirst returns first matching element")
    void findFirstReturns() {
        Optional<Integer> first = numbers.stream()
                .filter(n -> n > 5)
                .findFirst();

        assertTrue(first.isPresent());
        assertEquals(6, first.get());
    }

    @Test
    @DisplayName("sorted returns elements in order")
    void sortedReturns() {
        List<Integer> unsorted = List.of(5, 3, 1, 4, 2);
        List<Integer> sorted = unsorted.stream()
                .sorted()
                .collect(Collectors.toList());

        assertEquals(List.of(1, 2, 3, 4, 5), sorted);
    }

    @Test
    @DisplayName("distinct removes duplicates")
    void distinctRemovesDuplicates() {
        List<Integer> withDupes = List.of(1, 2, 2, 3, 3, 3);
        List<Integer> unique = withDupes.stream()
                .distinct()
                .collect(Collectors.toList());

        assertEquals(List.of(1, 2, 3), unique);
    }

    @Test
    @DisplayName("anyMatch/allMatch/noneMatch predicates")
    void matchPredicates() {
        assertTrue(numbers.stream().anyMatch(n -> n > 9));
        assertTrue(numbers.stream().allMatch(n -> n > 0));
        assertTrue(numbers.stream().noneMatch(n -> n < 0));
    }

    @Test
    @DisplayName("parallel stream produces same results")
    void parallelStreamSameResults() {
        long sequentialCount = numbers.stream()
                .filter(n -> n % 2 == 0)
                .count();

        long parallelCount = numbers.parallelStream()
                .filter(n -> n % 2 == 0)
                .count();

        assertEquals(sequentialCount, parallelCount);
    }

    @Test
    @DisplayName("Stream.of creates stream from values")
    void streamOf() {
        List<String> result = Stream.of("a", "b", "c")
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        assertEquals(List.of("A", "B", "C"), result);
    }

    @Test
    @DisplayName("IntStream range operations")
    void intStreamRange() {
        int sum = IntStream.rangeClosed(1, 10).sum();
        assertEquals(55, sum);

        long count = IntStream.range(0, 5).count();
        assertEquals(5, count);
    }
}
