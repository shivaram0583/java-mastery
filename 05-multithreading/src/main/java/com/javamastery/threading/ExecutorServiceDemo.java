package com.javamastery.threading;

import java.util.concurrent.*;
import java.util.List;

/**
 * Demonstrates ExecutorService and thread pool management.
 */
public class ExecutorServiceDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== ExecutorService Demo ===\n");

        // --- 1. Fixed thread pool ---
        System.out.println("--- Fixed Thread Pool (4 threads) ---");
        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 8; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    System.out.printf("  Task %d on %s%n", taskId, Thread.currentThread().getName());
                    Thread.sleep(100);
                    return taskId;
                });
            }
        }

        // --- 2. Callable + Future (return values) ---
        System.out.println("\n--- Callable + Future ---");
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Integer> future = executor.submit(() -> {
                Thread.sleep(200);
                return 42;
            });

            System.out.println("  Future done? " + future.isDone());
            Integer result = future.get(5, TimeUnit.SECONDS); // blocks until complete
            System.out.println("  Future result: " + result);

            // invokeAll — submit multiple Callables and get all results
            List<Callable<String>> tasks = List.of(
                    () -> { Thread.sleep(100); return "Task A"; },
                    () -> { Thread.sleep(200); return "Task B"; },
                    () -> { Thread.sleep(50);  return "Task C"; }
            );
            List<Future<String>> futures = executor.invokeAll(tasks);
            for (Future<String> f : futures) {
                System.out.println("  invokeAll result: " + f.get());
            }

            // invokeAny — returns the first completed result
            String fastest = executor.invokeAny(tasks);
            System.out.println("  invokeAny (fastest): " + fastest);
        }

        // --- 3. Scheduled executor ---
        System.out.println("\n--- ScheduledExecutorService ---");
        try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2)) {
            // Schedule with delay
            ScheduledFuture<String> delayed = scheduler.schedule(
                    () -> "Delayed result",
                    500, TimeUnit.MILLISECONDS
            );
            System.out.println("  Scheduled result: " + delayed.get());
        }

        // --- 4. ForkJoinPool ---
        System.out.println("\n--- ForkJoinPool ---");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("  Common pool parallelism: " + pool.getParallelism());

        long sum = pool.invoke(new SumTask(1, 1_000_000));
        System.out.println("  Sum 1..1M (ForkJoin): " + sum);

        // --- 5. Virtual thread executor (Java 21) ---
        System.out.println("\n--- Virtual Thread Executor (Java 21) ---");
        try (ExecutorService vtExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int taskId = i;
                vtExecutor.submit(() -> {
                    System.out.printf("  VT Task %d on %s (virtual=%b)%n",
                            taskId, Thread.currentThread().getName(),
                            Thread.currentThread().isVirtual());
                });
            }
        }
    }

    /** RecursiveTask for ForkJoinPool — sums a range of longs */
    static class SumTask extends RecursiveTask<Long> {
        private final long start, end;
        private static final long THRESHOLD = 10_000;

        SumTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            if (end - start <= THRESHOLD) {
                // Base case: compute directly
                long sum = 0;
                for (long i = start; i <= end; i++) sum += i;
                return sum;
            }
            // Fork: split into subtasks
            long mid = (start + end) / 2;
            SumTask left = new SumTask(start, mid);
            SumTask right = new SumTask(mid + 1, end);
            left.fork();   // run left in background
            long rightResult = right.compute(); // compute right in current thread
            long leftResult = left.join();      // wait for left
            return leftResult + rightResult;
        }
    }
}
