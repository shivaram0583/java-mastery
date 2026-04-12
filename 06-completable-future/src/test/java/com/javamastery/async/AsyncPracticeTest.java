package com.javamastery.async;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Async Practice Problems")
class AsyncPracticeTest {

    @Test
    @DisplayName("Greeting pipeline transforms name correctly")
    void greetingPipeline() {
        assertEquals("Hello, ALICE!", AsyncPracticeProblems.greetingPipeline("alice").join());
        assertEquals("Hello, BOB!", AsyncPracticeProblems.greetingPipeline("bob").join());
    }

    @Test
    @DisplayName("Successful fetch returns live data")
    void fetchWithFallbackSuccess() {
        assertEquals("Live data", AsyncPracticeProblems.fetchWithFallback(false).join());
    }

    @Test
    @DisplayName("Failed fetch returns fallback value")
    void fetchWithFallbackFailure() {
        String result = AsyncPracticeProblems.fetchWithFallback(true).join();
        assertTrue(result.startsWith("Default value"));
    }

    @Test
    @DisplayName("Parallel execution is faster than sequential")
    void parallelFasterThanSequential() {
        long sequential = AsyncPracticeProblems.runSequentially();
        long parallel = AsyncPracticeProblems.runInParallel();
        assertTrue(parallel < sequential, "Parallel (%dms) should be faster than sequential (%dms)"
                .formatted(parallel, sequential));
    }

    @Test
    @DisplayName("thenCombine produces combined response")
    void fetchUserData() {
        var response = AsyncPracticeProblems.fetchUserData("42").join();
        assertEquals("User-42", response.profile().name());
        assertEquals("42@example.com", response.profile().email());
        assertEquals(2, response.orders().orders().size());
    }

    @Test
    @DisplayName("thenCompose chain produces permissions")
    void authenticateAndAuthorize() {
        var perms = AsyncPracticeProblems.authenticateAndAuthorize("bob").join();
        assertEquals(List.of("READ", "WRITE"), perms.roles());
        assertNotNull(perms.profile());
        assertEquals("token-bob", perms.profile().authToken().token());
    }

    @Test
    @DisplayName("Parallel batch fetch handles failures with fallbacks")
    void parallelBatchFetch() {
        List<String> results = AsyncPracticeProblems.parallelBatchFetch(
                List.of(1, 2, 3, 4, 5, 6)).join();
        assertEquals(6, results.size());

        // IDs divisible by 3 fail and get fallback
        assertEquals("Data-1", results.get(0));
        assertEquals("Data-2", results.get(1));
        assertEquals("Fallback-3", results.get(2)); // id=3 fails
        assertEquals("Data-4", results.get(3));
        assertEquals("Data-5", results.get(4));
        assertEquals("Fallback-6", results.get(5)); // id=6 fails
    }

    @Test
    @DisplayName("Callback bridge produces result")
    void modernFetch() {
        String result = AsyncPracticeProblems.modernFetch("item-1").join();
        assertEquals("Result for item-1", result);
    }

    @Test
    @DisplayName("Error recovery pipeline continues after stage 3 failure")
    void recoverablePipeline() {
        String result = AsyncPracticeProblems.recoverablePipeline("start").join();
        assertTrue(result.contains("recovered"));
        assertTrue(result.contains("stage4"));
        assertTrue(result.contains("stage5"));
    }

    @Test
    @DisplayName("Retry succeeds on transient failures")
    void retryAsync() {
        int[] attempts = {0};
        var result = AsyncPracticeProblems.retryAsync(() -> {
            attempts[0]++;
            if (attempts[0] < 3) {
                return java.util.concurrent.CompletableFuture.<String>failedFuture(
                        new RuntimeException("attempt " + attempts[0]));
            }
            return java.util.concurrent.CompletableFuture.completedFuture("success");
        }, 3, 10).join();

        assertEquals("success", result);
        assertEquals(3, attempts[0]);
    }

    @Test
    @DisplayName("Retry exhaustion propagates failure")
    void retryExhausted() {
        assertThrows(CompletionException.class, () ->
                AsyncPracticeProblems.retryAsync(
                        () -> java.util.concurrent.CompletableFuture.<String>failedFuture(
                                new RuntimeException("always fails")),
                        2, 10).join());
    }

    @Test
    @DisplayName("fetchWithTimeoutDefault returns default on timeout")
    void timeoutDefault() {
        // Task takes 500ms, timeout at 50ms → should get default
        String result = AsyncPracticeProblems.fetchWithTimeoutDefault(500, 50).join();
        assertEquals("Default (timed out)", result);
    }

    @Test
    @DisplayName("fetchWithTimeoutDefault returns result when fast enough")
    void noTimeout() {
        String result = AsyncPracticeProblems.fetchWithTimeoutDefault(10, 2000).join();
        assertEquals("Completed", result);
    }
}
