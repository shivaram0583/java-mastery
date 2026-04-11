package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.locks.*;
import static org.junit.jupiter.api.Assertions.*;

class LocksTest {

    @Test
    void reentrantLockGuardsSharedState() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        int[] counter = {0};

        Runnable task = () -> {
            for (int i = 0; i < 10_000; i++) {
                lock.lock();
                try { counter[0]++; }
                finally { lock.unlock(); }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        assertEquals(20_000, counter[0]);
    }

    @Test
    void readWriteLockAllowsConcurrentReads() throws InterruptedException {
        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        String[] data = {"hello"};

        // Writer
        rwLock.writeLock().lock();
        try { data[0] = "world"; }
        finally { rwLock.writeLock().unlock(); }

        // Readers
        rwLock.readLock().lock();
        try { assertEquals("world", data[0]); }
        finally { rwLock.readLock().unlock(); }
    }

    @Test
    void stampedLockOptimisticRead() {
        StampedLock lock = new StampedLock();
        double[] coords = {1.0, 2.0};

        long stamp = lock.tryOptimisticRead();
        double x = coords[0], y = coords[1];
        assertTrue(lock.validate(stamp));
        assertEquals(1.0, x);
        assertEquals(2.0, y);
    }
}
