package com.javamastery.async;

import java.util.concurrent.*;
import java.util.List;
import java.util.stream.*;

/**
 * Demonstrates combining multiple CompletableFutures:
 * thenCombine, allOf, anyOf.
 */
public class CombiningFuturesDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Combining CompletableFutures ===\n");

        // --- 1. thenCombine — combine two futures ---
        System.out.println("--- thenCombine ---");
        thenCombineDemo();

        // --- 2. allOf — wait for all ---
        System.out.println("\n--- allOf ---");
        allOfDemo();

        // --- 3. anyOf — first to complete ---
        System.out.println("\n--- anyOf ---");
        anyOfDemo();

        // --- 4. Collecting results from multiple futures ---
        System.out.println("\n--- Collecting Results Pattern ---");
        collectResultsDemo();
    }

    static CompletableFuture<Double> fetchPrice(String product) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return switch (product) {
                case "laptop" -> 999.99;
                case "phone" -> 699.99;
                case "tablet" -> 449.99;
                default -> 0.0;
            };
        });
    }

    static CompletableFuture<Double> fetchDiscount(String product) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(80); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return 0.10; // 10% discount
        });
    }

    static void thenCombineDemo() throws Exception {
        // thenCombine: waits for both futures, combines results with BiFunction
        CompletableFuture<Double> finalPrice = fetchPrice("laptop")
                .thenCombine(fetchDiscount("laptop"), (price, discount) -> {
                    double discounted = price * (1 - discount);
                    System.out.println("  Price: $" + price + ", Discount: " + (discount * 100) + "%");
                    return discounted;
                });

        System.out.println("  Final price: $" + String.format("%.2f", finalPrice.get()));

        // Chain multiple thenCombine
        CompletableFuture<String> combined = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(() -> " World"), String::concat)
                .thenCombine(CompletableFuture.supplyAsync(() -> "!"), String::concat);
        System.out.println("  Combined: " + combined.get());
    }

    static void allOfDemo() throws Exception {
        // allOf: returns CompletableFuture<Void> — completes when ALL complete
        long start = System.currentTimeMillis();

        CompletableFuture<Double> laptopPrice = fetchPrice("laptop");
        CompletableFuture<Double> phonePrice = fetchPrice("phone");
        CompletableFuture<Double> tabletPrice = fetchPrice("tablet");

        // ALL three run in parallel
        CompletableFuture.allOf(laptopPrice, phonePrice, tabletPrice).get();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("  All prices fetched in ~" + elapsed + "ms (parallel!)");
        System.out.println("  Laptop: $" + laptopPrice.get());
        System.out.println("  Phone: $" + phonePrice.get());
        System.out.println("  Tablet: $" + tabletPrice.get());

        double total = laptopPrice.get() + phonePrice.get() + tabletPrice.get();
        System.out.println("  Total: $" + String.format("%.2f", total));
    }

    static void anyOfDemo() throws Exception {
        // anyOf: returns the first completed future
        CompletableFuture<Object> fastest = CompletableFuture.anyOf(
                CompletableFuture.supplyAsync(() -> {
                    try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "Server A (slow)";
                }),
                CompletableFuture.supplyAsync(() -> {
                    try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "Server B (fast)";
                }),
                CompletableFuture.supplyAsync(() -> {
                    try { Thread.sleep(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    return "Server C (medium)";
                })
        );

        System.out.println("  First response: " + fastest.get());
    }

    static void collectResultsDemo() throws Exception {
        // Pattern: convert List<CompletableFuture<T>> → CompletableFuture<List<T>>
        List<String> products = List.of("laptop", "phone", "tablet");

        List<CompletableFuture<Double>> futures = products.stream()
                .map(CombiningFuturesDemo::fetchPrice)
                .toList();

        CompletableFuture<List<Double>> allPrices = CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());

        List<Double> prices = allPrices.get();
        System.out.println("  All prices: " + prices);
        System.out.println("  Average: $" + String.format("%.2f",
                prices.stream().mapToDouble(d -> d).average().orElse(0)));
    }
}
