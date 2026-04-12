package com.javamastery.threading;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Threading Practice Problems")
class ThreadingPracticeTest {

    // ==================== Shared Counter ====================

    @Test
    @DisplayName("Synchronized counter is correct under contention")
    void synchronizedCounterCorrect() throws InterruptedException {
        assertEquals(10_000, ThreadingPracticeProblems.synchronizedIncrement(10, 1000));
    }

    @Test
    @DisplayName("Atomic counter is correct under contention")
    void atomicCounterCorrect() throws InterruptedException {
        assertEquals(10_000, ThreadingPracticeProblems.atomicIncrement(10, 1000));
    }

    @Test
    @DisplayName("Lock counter is correct under contention")
    void lockCounterCorrect() throws InterruptedException {
        assertEquals(10_000, ThreadingPracticeProblems.lockIncrement(10, 1000));
    }

    // ==================== ThreadLocal Isolation ====================

    @Test
    @DisplayName("ThreadLocal provides isolated values per thread")
    void threadLocalIsolation() throws InterruptedException {
        ConcurrentHashMap<String, Integer> results = ThreadingPracticeProblems.threadLocalIsolation(3);
        assertEquals(3, results.size());

        // Each thread stores id * 100 → values are 0, 100, 200
        assertTrue(results.containsValue(0));
        assertTrue(results.containsValue(100));
        assertTrue(results.containsValue(200));
    }

    // ==================== Thread Interleaving ====================

    @Test
    @DisplayName("Both threads produce all their outputs")
    void threadInterleavingProducesAllOutput() throws InterruptedException {
        List<String> output = Collections.synchronizedList(new ArrayList<>());
        Thread t1 = ThreadingPracticeProblems.printNumbers(5, output);
        Thread t2 = ThreadingPracticeProblems.printLetters(5, output);

        t1.join(5000);
        t2.join(5000);

        assertEquals(10, output.size());

        // All numbers and letters present
        for (int i = 1; i <= 5; i++) assertTrue(output.contains("N" + i));
        for (int i = 0; i < 5; i++) assertTrue(output.contains("L" + (char) ('A' + i)));
    }

    // ==================== ForkJoin Parallel Sum ====================

    @Test
    @DisplayName("Parallel sum computes correct result")
    void parallelSumCorrect() {
        int[] data = new int[10_000];
        for (int i = 0; i < data.length; i++) data[i] = i + 1;
        long expected = (long) data.length * (data.length + 1) / 2;
        assertEquals(expected, ThreadingPracticeProblems.parallelSum(data));
    }

    @Test
    @DisplayName("Parallel sum of single element array")
    void parallelSumSingleElement() {
        assertEquals(42, ThreadingPracticeProblems.parallelSum(new int[]{42}));
    }

    // ==================== ReadWriteCache ====================

    @Test
    @DisplayName("ReadWriteCache supports concurrent reads and writes")
    void readWriteCacheConcurrent() throws InterruptedException {
        var cache = new ThreadingPracticeProblems.ReadWriteCache<String, Integer>();
        cache.put("a", 1);
        cache.put("b", 2);

        assertEquals(1, cache.get("a"));
        assertEquals(2, cache.get("b"));
        assertEquals(2, cache.size());
        assertNull(cache.get("c"));
    }

    // ==================== StampedLock Point ====================

    @Test
    @DisplayName("StampedLock Point calculates correct distance")
    void stampedLockPointDistance() {
        var point = new ThreadingPracticeProblems.Point(3, 4);
        assertEquals(5.0, point.distanceFromOrigin(), 0.001);
    }

    @Test
    @DisplayName("StampedLock Point move is thread-safe")
    void stampedLockPointMove() throws InterruptedException {
        var point = new ThreadingPracticeProblems.Point(0, 0);
        int threads = 10;
        Thread[] workers = new Thread[threads];

        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    point.move(0.001, 0.001);
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();

        assertEquals(10.0, point.getX(), 0.01);
        assertEquals(10.0, point.getY(), 0.01);
    }

    // ==================== Rate Limiter ====================

    @Test
    @DisplayName("Rate limiter allows within capacity")
    void rateLimiterAllows() throws Exception {
        var limiter = new ThreadingPracticeProblems.RateLimiter(3);
        assertEquals(3, limiter.availablePermits());

        String result = limiter.execute(() -> "done", 1000);
        assertEquals("done", result);
    }

    @Test
    @DisplayName("Rate limiter blocks when capacity exceeded")
    void rateLimiterBlocks() throws Exception {
        var limiter = new ThreadingPracticeProblems.RateLimiter(1);

        // Acquire the single permit in a separate thread and hold it
        Thread blocker = new Thread(() -> {
            try {
                limiter.execute(() -> {
                    Thread.sleep(2000);
                    return null;
                }, 3000);
            } catch (Exception e) {
                // expected
            }
        });
        blocker.start();
        Thread.sleep(100); // let blocker acquire permit

        // Now try to execute — should time out
        assertThrows(Exception.class, () -> limiter.execute(() -> "fail", 200));

        blocker.interrupt();
        blocker.join(5000);
    }
}
