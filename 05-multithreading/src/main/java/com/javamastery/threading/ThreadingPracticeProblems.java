package com.javamastery.threading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Practice problems for multithreading concepts.
 */
public class ThreadingPracticeProblems {

    // ==================== Problem 1: Thread Interleaving ====================

    /** Print numbers 1..n in a new thread, returning the thread for joining */
    public static Thread printNumbers(int n, List<String> output) {
        Thread t = new Thread(() -> {
            for (int i = 1; i <= n; i++) {
                output.add("N" + i);
            }
        });
        t.start();
        return t;
    }

    /** Print letters A..J in a new thread, returning the thread for joining */
    public static Thread printLetters(int n, List<String> output) {
        Thread t = new Thread(() -> {
            for (int i = 0; i < n; i++) {
                output.add("L" + (char) ('A' + i));
            }
        });
        t.start();
        return t;
    }

    // ==================== Problem 2: Shared Counter ====================

    /** Unsafe counter — demonstrates race condition */
    public static int unsafeIncrement(int threads, int incrementsPerThread) throws InterruptedException {
        int[] counter = {0};
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter[0]++;
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        return counter[0]; // likely < threads * incrementsPerThread
    }

    /** Fix 1: synchronized */
    public static int synchronizedIncrement(int threads, int incrementsPerThread) throws InterruptedException {
        int[] counter = {0};
        Object lock = new Object();
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    synchronized (lock) {
                        counter[0]++;
                    }
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        return counter[0];
    }

    /** Fix 2: AtomicInteger */
    public static int atomicIncrement(int threads, int incrementsPerThread) throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    counter.incrementAndGet();
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        return counter.get();
    }

    /** Fix 3: ReentrantLock */
    public static int lockIncrement(int threads, int incrementsPerThread) throws InterruptedException {
        int[] counter = {0};
        ReentrantLock lock = new ReentrantLock();
        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    lock.lock();
                    try {
                        counter[0]++;
                    } finally {
                        lock.unlock();
                    }
                }
            });
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        return counter[0];
    }

    // ==================== Problem 3: ThreadLocal Isolation ====================

    private static final ThreadLocal<Integer> threadId = new ThreadLocal<>();

    /** Each thread sets its own value in ThreadLocal and reads it back */
    public static ConcurrentHashMap<String, Integer> threadLocalIsolation(int numThreads) throws InterruptedException {
        ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
        Thread[] workers = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            final int id = i;
            workers[i] = new Thread(() -> {
                try {
                    threadId.set(id * 100);
                    results.put(Thread.currentThread().getName(), threadId.get());
                } finally {
                    threadId.remove(); // always clean up in thread pools!
                }
            }, "Worker-" + i);
            workers[i].start();
        }
        for (Thread w : workers) w.join();
        return results;
    }

    // ==================== Problem 5: CountDownLatch Service Startup ====================

    /** Simulate service startup with N subsystems using CountDownLatch */
    public static long simulateServiceStartup(int numSubsystems) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(numSubsystems);
        long start = System.nanoTime();

        for (int i = 0; i < numSubsystems; i++) {
            final int sysId = i;
            new Thread(() -> {
                // Simulate subsystem initialization (varying times)
                try {
                    Thread.sleep(50 + sysId * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.printf("  Subsystem %d ready%n", sysId);
                latch.countDown();
            }).start();
        }

        latch.await(); // blocks until all subsystems are ready
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  All subsystems ready in " + elapsed + "ms");
        return elapsed;
    }

    // ==================== Problem 6: ForkJoin Parallel Sum ====================

    /** Compute sum of array using ForkJoinPool and RecursiveTask */
    public static long parallelSum(int[] array) {
        ForkJoinPool pool = new ForkJoinPool();
        try {
            return pool.invoke(new SumTask(array, 0, array.length));
        } finally {
            pool.shutdown();
        }
    }

    static class SumTask extends RecursiveTask<Long> {
        private static final int THRESHOLD = 1000;
        private final int[] array;
        private final int start;
        private final int end;

        SumTask(int[] array, int start, int end) {
            this.array = array;
            this.start = start;
            this.end = end;
        }

        @Override
        protected Long compute() {
            int length = end - start;
            if (length <= THRESHOLD) {
                long sum = 0;
                for (int i = start; i < end; i++) {
                    sum += array[i];
                }
                return sum;
            }
            int mid = start + length / 2;
            SumTask left = new SumTask(array, start, mid);
            SumTask right = new SumTask(array, mid, end);
            left.fork();
            long rightResult = right.compute();
            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }

    // ==================== Problem 7: Thread-Safe Cache with ReadWriteLock ====================

    public static class ReadWriteCache<K, V> {
        private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
        private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

        public V get(K key) {
            rwLock.readLock().lock();
            try {
                return map.get(key);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        public void put(K key, V value) {
            rwLock.writeLock().lock();
            try {
                map.put(key, value);
            } finally {
                rwLock.writeLock().unlock();
            }
        }

        public int size() {
            rwLock.readLock().lock();
            try {
                return map.size();
            } finally {
                rwLock.readLock().unlock();
            }
        }
    }

    // ==================== Problem 8: StampedLock Point ====================

    /** Thread-safe 2D Point using StampedLock with optimistic reads */
    public static class Point {
        private double x, y;
        private final StampedLock lock = new StampedLock();

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void move(double deltaX, double deltaY) {
            long stamp = lock.writeLock();
            try {
                x += deltaX;
                y += deltaY;
            } finally {
                lock.unlockWrite(stamp);
            }
        }

        /** Optimistic read — no actual lock acquired unless validation fails */
        public double distanceFromOrigin() {
            long stamp = lock.tryOptimisticRead();
            double currentX = x, currentY = y;
            if (!lock.validate(stamp)) {
                // A write occurred — fall back to pessimistic read
                stamp = lock.readLock();
                try {
                    currentX = x;
                    currentY = y;
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return Math.sqrt(currentX * currentX + currentY * currentY);
        }

        public double getX() {
            long stamp = lock.tryOptimisticRead();
            double val = x;
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try { val = x; } finally { lock.unlockRead(stamp); }
            }
            return val;
        }

        public double getY() {
            long stamp = lock.tryOptimisticRead();
            double val = y;
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try { val = y; } finally { lock.unlockRead(stamp); }
            }
            return val;
        }
    }

    // ==================== Problem 9: Rate Limiter ====================

    /** Simple rate limiter using Semaphore */
    public static class RateLimiter {
        private final Semaphore semaphore;

        public RateLimiter(int maxConcurrent) {
            this.semaphore = new Semaphore(maxConcurrent);
        }

        /** Execute a task if a permit is available within the timeout */
        public <T> T execute(Callable<T> task, long timeoutMs) throws Exception {
            if (semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
                try {
                    return task.call();
                } finally {
                    semaphore.release();
                }
            }
            throw new TimeoutException("Rate limit exceeded");
        }

        public int availablePermits() {
            return semaphore.availablePermits();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Threading Practice Problems ===\n");

        // Problem 2: Shared counter
        System.out.println("--- Problem 2: Shared Counter ---");
        int unsafe = unsafeIncrement(10, 1000);
        System.out.println("Unsafe: " + unsafe + " (expected 10000, likely less)");
        System.out.println("Synchronized: " + synchronizedIncrement(10, 1000));
        System.out.println("Atomic: " + atomicIncrement(10, 1000));
        System.out.println("Lock: " + lockIncrement(10, 1000));

        // Problem 3: ThreadLocal
        System.out.println("\n--- Problem 3: ThreadLocal Isolation ---");
        System.out.println("Thread-local values: " + threadLocalIsolation(3));

        // Problem 5: CountDownLatch
        System.out.println("\n--- Problem 5: CountDownLatch Service Startup ---");
        simulateServiceStartup(3);

        // Problem 6: ForkJoin parallel sum
        System.out.println("\n--- Problem 6: Parallel Sum ---");
        int[] data = new int[10_000];
        for (int i = 0; i < data.length; i++) data[i] = i + 1;
        long expected = (long) data.length * (data.length + 1) / 2;
        System.out.println("Parallel sum: " + parallelSum(data) + " (expected " + expected + ")");

        // Problem 8: StampedLock Point
        System.out.println("\n--- Problem 8: StampedLock Point ---");
        Point p = new Point(3, 4);
        System.out.println("Distance from origin: " + p.distanceFromOrigin() + " (expected 5.0)");
        p.move(1, 1);
        System.out.println("After move(1,1): (" + p.getX() + ", " + p.getY() + ")");

        // Problem 9: Rate Limiter
        System.out.println("\n--- Problem 9: Rate Limiter ---");
        RateLimiter limiter = new RateLimiter(2);
        System.out.println("Available permits: " + limiter.availablePermits());
        String result = limiter.execute(() -> "Task completed", 1000);
        System.out.println("Result: " + result);
    }
}
