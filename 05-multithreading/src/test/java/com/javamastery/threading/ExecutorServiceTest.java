package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ExecutorServiceTest {

    @Test
    void fixedThreadPoolExecutesTasks() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        int[] counter = {0};
        Object lock = new Object();

        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                synchronized (lock) {
                    counter[0]++;
                }
            });
        }

        pool.shutdown();
        assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(10, counter[0]);
    }

    @Test
    void callableReturnsFuture() throws Exception {
        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<Integer> future = pool.submit(() -> 21 * 2);

        assertEquals(42, future.get(2, TimeUnit.SECONDS));
        pool.shutdown();
    }

    @Test
    void invokeAllReturnsAllResults() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(3);
        List<Callable<Integer>> tasks = List.of(
                () -> 1, () -> 2, () -> 3
        );

        List<Future<Integer>> futures = pool.invokeAll(tasks);
        int sum = futures.stream()
                .mapToInt(f -> {
                    try { return f.get(); }
                    catch (Exception e) { return 0; }
                })
                .sum();

        assertEquals(6, sum);
        pool.shutdown();
    }
}
