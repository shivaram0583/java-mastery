package com.javamastery.java8;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates Collectors utility class in Java 8.
 *
 * Collectors provides predefined implementations for common mutable reduction
 * operations like grouping, partitioning, joining, and accumulating into collections.
 */
public class CollectorsDemo {

    record Product(String name, String category, double price) {}

    public static void main(String[] args) {
        System.out.println("=== Collectors Demo ===\n");

        List<Product> products = List.of(
            new Product("Laptop", "Electronics", 999.99),
            new Product("Phone", "Electronics", 699.99),
            new Product("Tablet", "Electronics", 449.99),
            new Product("Desk", "Furniture", 249.99),
            new Product("Chair", "Furniture", 199.99),
            new Product("Lamp", "Furniture", 59.99),
            new Product("Notebook", "Stationery", 4.99),
            new Product("Pen", "Stationery", 1.99)
        );

        // --- 1. toList, toSet, toUnmodifiableList ---
        List<String> nameList = products.stream()
                .map(Product::name)
                .collect(Collectors.toList());
        System.out.println("Names (list): " + nameList);

        Set<String> categorySet = products.stream()
                .map(Product::category)
                .collect(Collectors.toSet());
        System.out.println("Categories (set): " + categorySet);

        // --- 2. toMap ---
        // Key: product name, Value: price
        // Third arg (merge function) resolves duplicate keys
        Map<String, Double> priceMap = products.stream()
                .collect(Collectors.toMap(Product::name, Product::price, (v1, v2) -> v1));
        System.out.println("\nPrice map: " + priceMap);

        // --- 3. joining ---
        String joined = products.stream()
                .map(Product::name)
                .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("\nJoined: " + joined);

        // --- 4. groupingBy ---
        Map<String, List<Product>> byCategory = products.stream()
                .collect(Collectors.groupingBy(Product::category));
        System.out.println("\nGrouped by category:");
        byCategory.forEach((cat, prods) ->
                System.out.println("  " + cat + ": " + prods.stream()
                        .map(Product::name).collect(Collectors.joining(", "))));

        // --- 5. groupingBy with downstream collector ---
        // Count products per category
        Map<String, Long> countByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::category, Collectors.counting()));
        System.out.println("\nCount by category: " + countByCategory);

        // Average price per category
        Map<String, Double> avgPriceByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::category,
                        Collectors.averagingDouble(Product::price)));
        System.out.println("Avg price by category: " + avgPriceByCategory);

        // Max price product per category
        Map<String, Optional<Product>> maxByCategory = products.stream()
                .collect(Collectors.groupingBy(Product::category,
                        Collectors.maxBy(Comparator.comparingDouble(Product::price))));
        System.out.println("Most expensive per category:");
        maxByCategory.forEach((cat, prod) ->
                prod.ifPresent(p -> System.out.println("  " + cat + ": " + p.name() + " ($" + p.price() + ")")));

        // --- 6. partitioningBy --- (splits into true/false groups)
        Map<Boolean, List<Product>> partitioned = products.stream()
                .collect(Collectors.partitioningBy(p -> p.price() > 100));
        System.out.println("\nExpensive (>$100): " + partitioned.get(true).stream()
                .map(Product::name).collect(Collectors.joining(", ")));
        System.out.println("Cheap (≤$100): " + partitioned.get(false).stream()
                .map(Product::name).collect(Collectors.joining(", ")));

        // --- 7. summarizingDouble ---
        DoubleSummaryStatistics stats = products.stream()
                .collect(Collectors.summarizingDouble(Product::price));
        System.out.println("\nPrice statistics:");
        System.out.println("  Count: " + stats.getCount());
        System.out.println("  Sum: " + stats.getSum());
        System.out.println("  Min: " + stats.getMin());
        System.out.println("  Max: " + stats.getMax());
        System.out.println("  Average: " + stats.getAverage());

        // --- 8. collectingAndThen --- (collect then transform)
        int productCount = products.stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(), List::size));
        System.out.println("\nTotal products: " + productCount);
    }
}
