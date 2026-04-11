package com.javamastery.java8;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Collectors")
class CollectorsTest {

    record Item(String name, String category, double price) {}

    private final List<Item> items = List.of(
        new Item("A", "X", 10.0),
        new Item("B", "X", 20.0),
        new Item("C", "Y", 30.0),
        new Item("D", "Y", 40.0),
        new Item("E", "Z", 50.0)
    );

    @Test
    @DisplayName("groupingBy groups elements by classifier")
    void groupingByClassifier() {
        Map<String, List<Item>> grouped = items.stream()
                .collect(Collectors.groupingBy(Item::category));

        assertEquals(3, grouped.size());
        assertEquals(2, grouped.get("X").size());
        assertEquals(2, grouped.get("Y").size());
        assertEquals(1, grouped.get("Z").size());
    }

    @Test
    @DisplayName("groupingBy with counting downstream")
    void groupingByWithCounting() {
        Map<String, Long> counts = items.stream()
                .collect(Collectors.groupingBy(Item::category, Collectors.counting()));

        assertEquals(2L, counts.get("X"));
        assertEquals(2L, counts.get("Y"));
        assertEquals(1L, counts.get("Z"));
    }

    @Test
    @DisplayName("partitioningBy splits into true/false groups")
    void partitioningBy() {
        Map<Boolean, List<Item>> partitioned = items.stream()
                .collect(Collectors.partitioningBy(i -> i.price() > 25));

        assertEquals(2, partitioned.get(false).size()); // A(10), B(20)
        assertEquals(3, partitioned.get(true).size());  // C(30), D(40), E(50)
    }

    @Test
    @DisplayName("joining concatenates strings with delimiter")
    void joiningWithDelimiter() {
        String result = items.stream()
                .map(Item::name)
                .collect(Collectors.joining(", ", "[", "]"));

        assertEquals("[A, B, C, D, E]", result);
    }

    @Test
    @DisplayName("toMap creates map from stream")
    void toMapCreates() {
        Map<String, Double> priceMap = items.stream()
                .collect(Collectors.toMap(Item::name, Item::price));

        assertEquals(10.0, priceMap.get("A"));
        assertEquals(50.0, priceMap.get("E"));
    }

    @Test
    @DisplayName("toMap with merge function resolves duplicates")
    void toMapWithMerge() {
        // Sum prices by category
        Map<String, Double> totals = items.stream()
                .collect(Collectors.toMap(Item::category, Item::price, Double::sum));

        assertEquals(30.0, totals.get("X")); // 10 + 20
        assertEquals(70.0, totals.get("Y")); // 30 + 40
        assertEquals(50.0, totals.get("Z"));
    }

    @Test
    @DisplayName("summarizingDouble provides statistics")
    void summarizingDoubleStats() {
        DoubleSummaryStatistics stats = items.stream()
                .collect(Collectors.summarizingDouble(Item::price));

        assertEquals(5, stats.getCount());
        assertEquals(10.0, stats.getMin());
        assertEquals(50.0, stats.getMax());
        assertEquals(150.0, stats.getSum());
        assertEquals(30.0, stats.getAverage());
    }

    @Test
    @DisplayName("averagingDouble calculates average")
    void averagingDouble() {
        double avg = items.stream()
                .collect(Collectors.averagingDouble(Item::price));

        assertEquals(30.0, avg);
    }
}
