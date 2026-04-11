package com.javamastery.async;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlingTest {

    @Test
    void exceptionallyRecovery() throws Exception {
        String result = CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("fail");
                })
                .exceptionally(ex -> "recovered")
                .get(2, TimeUnit.SECONDS);

        assertEquals("recovered", result);
    }

    @Test
    void handleOnSuccess() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> "ok")
                .handle((data, ex) -> ex != null ? "error" : data.toUpperCase())
                .get(2, TimeUnit.SECONDS);
        assertEquals("OK", result);
    }

    @Test
    void handleOnFailure() throws Exception {
        String result = CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("boom");
                })
                .handle((data, ex) -> ex != null ? "handled" : data)
                .get(2, TimeUnit.SECONDS);
        assertEquals("handled", result);
    }

    @Test
    void whenCompletePreservesException() {
        boolean[] sideEffect = {false};
        CompletableFuture<String> cf = CompletableFuture.<String>supplyAsync(() -> {
                    throw new RuntimeException("fail");
                })
                .whenComplete((result, ex) -> sideEffect[0] = true);

        assertThrows(ExecutionException.class, cf::get);
        assertTrue(sideEffect[0]);
    }

    @Test
    void errorPropagatesThroughChain() throws Exception {
        String result = CompletableFuture.supplyAsync(() -> "start")
                .<String>thenApply(s -> { throw new RuntimeException("mid-chain error"); })
                .thenApply(s -> "never reached")
                .exceptionally(ex -> "caught")
                .get(2, TimeUnit.SECONDS);

        assertEquals("caught", result);
    }
}
