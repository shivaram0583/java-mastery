package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class SynchronizersTest {

    @Test
    void countDownLatchReleasesWaiters() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            new Thread(latch::countDown).start();
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(0, latch.getCount());
    }

    @Test
    void cyclicBarrierSynchronizesThreads() throws Exception {
        int parties = 3;
        int[] arrivals = {0};
        CyclicBarrier barrier = new CyclicBarrier(parties, () -> arrivals[0]++);

        Thread[] threads = new Thread[parties];
        for (int i = 0; i < parties; i++) {
            threads[i] = new Thread(() -> {
                try { barrier.await(2, TimeUnit.SECONDS); }
                catch (Exception e) { fail(e); }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join(3000);

        assertEquals(1, arrivals[0]);
    }

    @Test
    void semaphoreLimitsConcurrency() throws InterruptedException {
        Semaphore semaphore = new Semaphore(2);
        int[] maxConcurrent = {0};
        int[] current = {0};
        Object lock = new Object();

        Runnable task = () -> {
            try {
                semaphore.acquire();
                synchronized (lock) {
                    current[0]++;
                    maxConcurrent[0] = Math.max(maxConcurrent[0], current[0]);
                }
                Thread.sleep(50);
                synchronized (lock) { current[0]--; }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        };

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(task);
            threads[i].start();
        }
        for (Thread t : threads) t.join(5000);

        assertTrue(maxConcurrent[0] <= 2, "Max concurrent should be <= 2");
    }
}
