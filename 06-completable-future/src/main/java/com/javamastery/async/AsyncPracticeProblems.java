package com.javamastery.async;

import java.util.List;
import java.util.concurrent.*;

/**
 * Practice problems for CompletableFuture and async programming.
 */
public class AsyncPracticeProblems {

    // ==================== Problem 1: Basic Pipeline ====================

    /** Pipeline: get name → uppercase → greeting */
    public static CompletableFuture<String> greetingPipeline(String name) {
        return CompletableFuture.supplyAsync(() -> name)
                .thenApply(String::toUpperCase)
                .thenApply(upper -> "Hello, " + upper + "!");
    }

    // ==================== Problem 2: Fallback on Error ====================

    /** Simulates an API call that fails, with a fallback */
    public static CompletableFuture<String> fetchWithFallback(boolean shouldFail) {
        return CompletableFuture.supplyAsync(() -> {
            if (shouldFail) {
                throw new RuntimeException("API is down");
            }
            return "Live data";
        }).exceptionally(ex -> "Default value (error: " + ex.getMessage() + ")");
    }

    // ==================== Problem 3: Parallel vs Sequential Timing ====================

    /** Simulate an API call that takes delayMs milliseconds */
    static CompletableFuture<String> simulateApiCall(String name, long delayMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return name + " result";
        });
    }

    /** Run 3 API calls sequentially, return total elapsed ms */
    public static long runSequentially() {
        long start = System.nanoTime();
        simulateApiCall("API-1", 100).join();
        simulateApiCall("API-2", 100).join();
        simulateApiCall("API-3", 100).join();
        return (System.nanoTime() - start) / 1_000_000;
    }

    /** Run 3 API calls in parallel, return total elapsed ms */
    public static long runInParallel() {
        long start = System.nanoTime();
        var f1 = simulateApiCall("API-1", 100);
        var f2 = simulateApiCall("API-2", 100);
        var f3 = simulateApiCall("API-3", 100);
        CompletableFuture.allOf(f1, f2, f3).join();
        return (System.nanoTime() - start) / 1_000_000;
    }

    // ==================== Problem 4: thenCombine ====================

    record UserProfile(String name, String email) {}
    record UserOrders(List<String> orders) {}
    record UserResponse(UserProfile profile, UserOrders orders) {}

    /** Fetch profile and orders in parallel, combine into single response */
    public static CompletableFuture<UserResponse> fetchUserData(String userId) {
        CompletableFuture<UserProfile> profileFuture = CompletableFuture.supplyAsync(
                () -> new UserProfile("User-" + userId, userId + "@example.com"));
        CompletableFuture<UserOrders> ordersFuture = CompletableFuture.supplyAsync(
                () -> new UserOrders(List.of("Order-1", "Order-2")));

        return profileFuture.thenCombine(ordersFuture, UserResponse::new);
    }

    // ==================== Problem 5: thenCompose Chain ====================

    record AuthToken(String token) {}
    record Profile(String name, AuthToken authToken) {}
    record Permissions(List<String> roles, Profile profile) {}

    /** Authenticate → fetch profile → fetch permissions (each depends on previous) */
    public static CompletableFuture<Permissions> authenticateAndAuthorize(String user) {
        return authenticate(user)
                .thenCompose(token -> fetchProfile(token))
                .thenCompose(profile -> fetchPermissions(profile));
    }

    static CompletableFuture<AuthToken> authenticate(String user) {
        return CompletableFuture.supplyAsync(() -> new AuthToken("token-" + user));
    }

    static CompletableFuture<Profile> fetchProfile(AuthToken token) {
        return CompletableFuture.supplyAsync(() -> new Profile("User for " + token.token(), token));
    }

    static CompletableFuture<Permissions> fetchPermissions(Profile profile) {
        return CompletableFuture.supplyAsync(() -> new Permissions(List.of("READ", "WRITE"), profile));
    }

    // ==================== Problem 6: Retry with Backoff ====================

    /** Retry an async operation up to maxRetries times with delayMs between retries */
    public static <T> CompletableFuture<T> retryAsync(
            java.util.function.Supplier<CompletableFuture<T>> operation,
            int maxRetries, long delayMs) {
        return operation.get().handle((result, ex) -> {
            if (ex == null) {
                return CompletableFuture.completedFuture(result);
            }
            if (maxRetries <= 0) {
                return CompletableFuture.<T>failedFuture(ex);
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CompletableFuture.<T>failedFuture(e);
            }
            return retryAsync(operation, maxRetries - 1, delayMs);
        }).thenCompose(f -> f);
    }

    // ==================== Problem 7: Timeout Handling ====================

    /** Async operation with timeout */
    public static CompletableFuture<String> fetchWithTimeout(long taskDelayMs, long timeoutMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(taskDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Completed";
        }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }

    /** Async operation with timeout and default value */
    public static CompletableFuture<String> fetchWithTimeoutDefault(long taskDelayMs, long timeoutMs) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(taskDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Completed";
        }).completeOnTimeout("Default (timed out)", timeoutMs, TimeUnit.MILLISECONDS);
    }

    // ==================== Problem 8: Parallel Batch Processing ====================

    /** Fetch data for a list of IDs in parallel, with fallbacks for individual failures */
    public static CompletableFuture<List<String>> parallelBatchFetch(List<Integer> ids) {
        List<CompletableFuture<String>> futures = ids.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> {
                    if (id % 3 == 0) { // simulate some failures
                        throw new RuntimeException("Failed for ID " + id);
                    }
                    return "Data-" + id;
                }).exceptionally(ex -> "Fallback-" + id))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    // ==================== Problem 9: Callback-based API Bridge ====================

    /** Simulates a callback-based SDK */
    interface Callback<T> {
        void onSuccess(T result);
        void onFailure(Exception error);
    }

    /** Old-style callback API */
    static void legacyFetch(String id, Callback<String> callback) {
        new Thread(() -> {
            try {
                Thread.sleep(50);
                callback.onSuccess("Result for " + id);
            } catch (InterruptedException e) {
                callback.onFailure(e);
            }
        }).start();
    }

    /** Bridge: wrap callback-based API in CompletableFuture */
    public static CompletableFuture<String> modernFetch(String id) {
        CompletableFuture<String> future = new CompletableFuture<>();
        legacyFetch(id, new Callback<>() {
            @Override
            public void onSuccess(String result) { future.complete(result); }
            @Override
            public void onFailure(Exception error) { future.completeExceptionally(error); }
        });
        return future;
    }

    // ==================== Problem 10: Pipeline with Error Recovery ====================

    /** 5-stage pipeline where stage 3 fails, recovered by handle() */
    public static CompletableFuture<String> recoverablePipeline(String input) {
        return CompletableFuture.supplyAsync(() -> input)                           // stage 1
                .thenApply(s -> s + " → stage2")                                    // stage 2
                .thenApply(s -> { throw new RuntimeException("Stage 3 failure"); })  // stage 3 (fails)
                .handle((result, ex) -> {                                            // recovery
                    if (ex != null) return input + " → stage2 → recovered";
                    return (String) result;
                })
                .thenApply(s -> s + " → stage4")                                    // stage 4
                .thenApply(s -> s + " → stage5");                                   // stage 5
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Async Practice Problems ===\n");

        // Problem 1
        System.out.println("--- Problem 1: Greeting Pipeline ---");
        System.out.println(greetingPipeline("alice").join());

        // Problem 2
        System.out.println("\n--- Problem 2: Fallback ---");
        System.out.println("Success: " + fetchWithFallback(false).join());
        System.out.println("Failure: " + fetchWithFallback(true).join());

        // Problem 3
        System.out.println("\n--- Problem 3: Sequential vs Parallel ---");
        System.out.println("Sequential: " + runSequentially() + "ms");
        System.out.println("Parallel: " + runInParallel() + "ms");

        // Problem 4
        System.out.println("\n--- Problem 4: thenCombine ---");
        System.out.println(fetchUserData("42").join());

        // Problem 5
        System.out.println("\n--- Problem 5: thenCompose Chain ---");
        System.out.println(authenticateAndAuthorize("bob").join());

        // Problem 8
        System.out.println("\n--- Problem 8: Parallel Batch ---");
        System.out.println(parallelBatchFetch(List.of(1, 2, 3, 4, 5, 6)).join());

        // Problem 9
        System.out.println("\n--- Problem 9: Callback Bridge ---");
        System.out.println(modernFetch("item-1").join());

        // Problem 10
        System.out.println("\n--- Problem 10: Error Recovery Pipeline ---");
        System.out.println(recoverablePipeline("start").join());
    }
}
