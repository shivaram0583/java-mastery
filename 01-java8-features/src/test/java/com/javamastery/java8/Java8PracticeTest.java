package com.javamastery.java8;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class Java8PracticeTest {

    @Test
    void testFilterAndTransform() {
        var input = List.of("Alice", "Bob", "Anna", "Charlie", "Andrew");
        var result = Java8PracticeProblems.filterAndTransform(input);
        assertEquals(List.of("ALICE", "ANNA", "ANDREW"), result);
    }

    @Test
    void testSumOfSquaresOfOdds() {
        assertEquals(35, Java8PracticeProblems.sumOfSquaresOfOdds(List.of(1, 2, 3, 4, 5)));
        assertEquals(0, Java8PracticeProblems.sumOfSquaresOfOdds(List.of(2, 4, 6)));
    }

    @Test
    void testFindLongest() {
        var result = Java8PracticeProblems.findLongest(List.of("hi", "hello", "hey"));
        assertTrue(result.isPresent());
        assertEquals("hello", result.get());
        assertTrue(Java8PracticeProblems.findLongest(Collections.emptyList()).isEmpty());
    }

    @Test
    void testJoinWithCommas() {
        assertEquals("a, b, c", Java8PracticeProblems.joinWithCommas(List.of("a", "b", "c")));
        assertEquals("solo", Java8PracticeProblems.joinWithCommas(List.of("solo")));
    }

    @Test
    void testCountLongStrings() {
        assertEquals(2, Java8PracticeProblems.countLongStrings(
                List.of("hi", "hello", "wonderful", "amazing")));
    }

    @Test
    void testFlattenAndSort() {
        var nested = List.of(List.of(3, 1, 2), List.of(2, 4, 3), List.of(5, 1));
        assertEquals(List.of(1, 2, 3, 4, 5), Java8PracticeProblems.flattenAndSort(nested));
    }

    @Test
    void testWordFrequency() {
        var freq = Java8PracticeProblems.wordFrequency("the quick brown fox jumps over the lazy fox");
        assertEquals(2L, freq.get("the"));
        assertEquals(2L, freq.get("fox"));
        assertEquals(1L, freq.get("quick"));
    }

    @Test
    void testGroupByFirstLetter() {
        var groups = Java8PracticeProblems.groupByFirstLetter(
                List.of("Alice", "Anna", "Bob", "Barry", "Charlie"));
        assertEquals(2, groups.get('A').size());
        assertEquals(2, groups.get('B').size());
        assertEquals(1, groups.get('C').size());
    }

    @Test
    void testFindSecondHighest() {
        assertEquals(OptionalInt.of(7),
                Java8PracticeProblems.findSecondHighest(List.of(5, 3, 8, 1, 8, 7)));
        assertEquals(OptionalInt.empty(),
                Java8PracticeProblems.findSecondHighest(List.of(5)));
    }

    @Test
    void testPartitionEvenOdd() {
        var result = Java8PracticeProblems.partitionEvenOdd(List.of(1, 2, 3, 4, 5));
        assertEquals(List.of(2, 4), result.get(true));   // evens
        assertEquals(List.of(1, 3, 5), result.get(false)); // odds
    }

    @Test
    void testHighestSalaryPerDept() {
        var employees = List.of(
                new Java8PracticeProblems.Employee("Alice", "Engineering", 100000),
                new Java8PracticeProblems.Employee("Bob", "Engineering", 120000),
                new Java8PracticeProblems.Employee("Charlie", "Sales", 80000)
        );
        var result = Java8PracticeProblems.highestSalaryPerDept(employees);
        assertEquals("Bob", result.get("Engineering").get().getName());
        assertEquals("Charlie", result.get("Sales").get().getName());
    }

    @Test
    void testInvertMap() {
        var map = Map.of("a", 1, "b", 2, "c", 1);
        var inverted = Java8PracticeProblems.invertMap(map);
        assertEquals(2, inverted.get(1).size());
        assertEquals(1, inverted.get(2).size());
    }

    @Test
    void testFibonacci() {
        assertEquals(List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L, 13L, 21L, 34L),
                Java8PracticeProblems.fibonacci(10));
        assertEquals(List.of(0L, 1L), Java8PracticeProblems.fibonacci(2));
    }

    @Test
    void testSumOfCubesOfPrimes() {
        assertEquals(160L, // 2^3 + 3^3 + 5^3 = 8 + 27 + 125
                Java8PracticeProblems.sumOfCubesOfPrimes(List.of(1, 2, 3, 4, 5, 6)));
    }

    @Test
    void testIteratorToStream() {
        var iterator = List.of("a", "b", "c").iterator();
        var result = Java8PracticeProblems.iteratorToStream(iterator)
                .map(String::toUpperCase)
                .toList();
        assertEquals(List.of("A", "B", "C"), result);
    }

    @Test
    void testSlidingWindowAverage() {
        var result = Java8PracticeProblems.slidingWindowAverage(
                List.of(1.0, 2.0, 3.0, 4.0, 5.0), 3);
        assertEquals(3, result.size());
        assertEquals(2.0, result.get(0), 0.001);
        assertEquals(3.0, result.get(1), 0.001);
        assertEquals(4.0, result.get(2), 0.001);
    }

    @Test
    void testMapUsingReduce() {
        var result = Java8PracticeProblems.mapUsingReduce(
                List.of("hello", "world"), String::toUpperCase);
        assertEquals(List.of("HELLO", "WORLD"), result);
    }
}
