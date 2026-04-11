package com.javamastery.async;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class CompletableFutureBasicsTest {

    @Test
    void supplyAsyncReturnsValue() throws Exception {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "hello");
        assertEquals("hello", cf.get(2, TimeUnit.SECONDS));
    }

    @Test
    void thenApplyTransformsResult() throws Exception {
        CompletableFuture<Integer> cf = CompletableFuture.supplyAsync(() -> "42")
                .thenApply(Integer::parseInt)
                .thenApply(n -> n * 2);
        assertEquals(84, cf.get(2, TimeUnit.SECONDS));
    }

    @Test
    void thenAcceptConsumesResult() throws Exception {
        String[] consumed = {null};
        CompletableFuture.supplyAsync(() -> "data")
                .thenAccept(s -> consumed[0] = s)
                .get(2, TimeUnit.SECONDS);
        assertEquals("data", consumed[0]);
    }

    @Test
    void completedFutureIsImmediate() throws Exception {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("instant");
        assertTrue(cf.isDone());
        assertEquals("instant", cf.get());
    }
}
