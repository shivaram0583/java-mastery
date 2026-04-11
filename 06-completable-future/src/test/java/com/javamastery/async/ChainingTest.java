package com.javamastery.async;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ChainingTest {

    @Test
    void thenComposeFlattens() throws Exception {
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> 42)
                .thenCompose(n -> CompletableFuture.supplyAsync(() -> "Value: " + n));

        assertEquals("Value: 42", result.get(2, TimeUnit.SECONDS));
    }

    @Test
    void multiStagePipeline() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> "  hello  ")
                .thenApply(String::trim)
                .thenApply(String::toUpperCase)
                .thenApply(s -> s + "!")
                .get(2, TimeUnit.SECONDS);

        assertEquals("HELLO!", result);
    }

    @Test
    void thenApplyVsThenCompose() throws Exception {
        // thenApply wraps, thenCompose flattens
        CompletableFuture<CompletableFuture<String>> nested =
                CompletableFuture.supplyAsync(() -> 1)
                        .thenApply(n -> CompletableFuture.supplyAsync(() -> "nested " + n));

        CompletableFuture<String> flat =
                CompletableFuture.supplyAsync(() -> 1)
                        .thenCompose(n -> CompletableFuture.supplyAsync(() -> "flat " + n));

        assertEquals("nested 1", nested.get().get());
        assertEquals("flat 1", flat.get());
    }
}
