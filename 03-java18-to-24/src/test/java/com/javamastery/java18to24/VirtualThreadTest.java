package com.javamastery.java18to24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Virtual Threads (Java 21)")
class VirtualThreadTest {

    @Test
    @DisplayName("Virtual thread reports isVirtual() as true")
    void isVirtual() throws InterruptedException {
        var result = new boolean[1];
        Thread vThread = Thread.ofVirtual().start(() -> {
            result[0] = Thread.currentThread().isVirtual();
        });
        vThread.join();

        assertTrue(result[0]);
    }

    @Test
    @DisplayName("Platform thread reports isVirtual() as false")
    void platformIsNotVirtual() throws InterruptedException {
        var result = new boolean[]{true}; // default true, should become false
        Thread pThread = Thread.ofPlatform().start(() -> {
            result[0] = Thread.currentThread().isVirtual();
        });
        pThread.join();

        assertFalse(result[0]);
    }

    @Test
    @DisplayName("Virtual thread executor runs all tasks")
    void executorRunsAllTasks() {
        AtomicInteger counter = new AtomicInteger(0);
        int taskCount = 1000;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, taskCount).forEach(i ->
                    executor.submit(counter::incrementAndGet));
        }

        assertEquals(taskCount, counter.get());
    }

    @Test
    @DisplayName("Many virtual threads can be created cheaply")
    void manyVirtualThreads() throws InterruptedException {
        int threadCount = 10_000;
        AtomicInteger completed = new AtomicInteger(0);
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = Thread.ofVirtual().start(() -> {
                // Minimal work
                completed.incrementAndGet();
            });
        }

        for (Thread t : threads) {
            t.join();
        }

        assertEquals(threadCount, completed.get());
    }
}
