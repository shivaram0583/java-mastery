package com.javamastery.async;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CombiningFuturesTest {

    @Test
    void thenCombineMergesResults() throws Exception {
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> "Hello")
                .thenCombine(CompletableFuture.supplyAsync(() -> " World"), String::concat);

        assertEquals("Hello World", result.get(2, TimeUnit.SECONDS));
    }

    @Test
    void allOfWaitsForAll() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 2);
        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> 3);

        CompletableFuture.allOf(f1, f2, f3).get(2, TimeUnit.SECONDS);

        assertEquals(1, f1.get());
        assertEquals(2, f2.get());
        assertEquals(3, f3.get());
    }

    @Test
    void anyOfReturnsFirst() throws Exception {
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "slow";
        });
        CompletableFuture<String> fast = CompletableFuture.completedFuture("fast");

        Object result = CompletableFuture.anyOf(slow, fast).get(2, TimeUnit.SECONDS);
        assertEquals("fast", result);
    }

    @Test
    void collectFutureResults() throws Exception {
        List<CompletableFuture<Integer>> futures = List.of(
                CompletableFuture.supplyAsync(() -> 10),
                CompletableFuture.supplyAsync(() -> 20),
                CompletableFuture.supplyAsync(() -> 30)
        );

        CompletableFuture<List<Integer>> all = CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());

        List<Integer> results = all.get(2, TimeUnit.SECONDS);
        assertEquals(List.of(10, 20, 30), results);
    }
}
