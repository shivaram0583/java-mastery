package com.javamastery.threading;

import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates explicit Lock implementations from java.util.concurrent.locks:
 * ReentrantLock, ReadWriteLock, and StampedLock.
 */
public class LocksDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Locks Demo ===\n");

        // --- 1. ReentrantLock ---
        System.out.println("--- ReentrantLock ---");
        reentrantLockDemo();

        // --- 2. ReadWriteLock ---
        System.out.println("\n--- ReadWriteLock ---");
        readWriteLockDemo();

        // --- 3. StampedLock ---
        System.out.println("\n--- StampedLock ---");
        stampedLockDemo();

        System.out.println("\n--- Comparison ---");
        System.out.println("  synchronized: simple, no tryLock, no fairness, auto-release");
        System.out.println("  ReentrantLock: tryLock, timed, interruptible, fairness option");
        System.out.println("  ReadWriteLock: concurrent reads, exclusive writes");
        System.out.println("  StampedLock: optimistic reads (best performance), non-reentrant");
    }

    static void reentrantLockDemo() throws InterruptedException {
        ReentrantLock lock = new ReentrantLock(true); // fair = true
        int[] counter = {0};

        Runnable task = () -> {
            for (int i = 0; i < 10_000; i++) {
                lock.lock();
                try {
                    counter[0]++;
                } finally {
                    lock.unlock(); // ALWAYS unlock in finally
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println("  Counter: " + counter[0] + " (expected 20000)");

        // tryLock — non-blocking attempt
        boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
        System.out.println("  tryLock acquired: " + acquired);
        if (acquired) lock.unlock();
    }

    static void readWriteLockDemo() throws InterruptedException {
        ReadWriteLock rwLock = new ReentrantReadWriteLock();
        String[] data = {"initial"};

        // Readers can run concurrently
        Runnable reader = () -> {
            rwLock.readLock().lock();
            try {
                System.out.println("  [READ] " + Thread.currentThread().getName() + ": " + data[0]);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
            }
        };

        // Writer gets exclusive access
        Runnable writer = () -> {
            rwLock.writeLock().lock();
            try {
                data[0] = "updated by " + Thread.currentThread().getName();
                System.out.println("  [WRITE] " + Thread.currentThread().getName() + ": " + data[0]);
            } finally {
                rwLock.writeLock().unlock();
            }
        };

        Thread r1 = new Thread(reader, "Reader-1");
        Thread r2 = new Thread(reader, "Reader-2");
        Thread w1 = new Thread(writer, "Writer-1");

        r1.start(); r2.start();
        Thread.sleep(10);
        w1.start();
        r1.join(); r2.join(); w1.join();
    }

    static void stampedLockDemo() {
        StampedLock lock = new StampedLock();
        double[] coords = {0.0, 0.0};

        // Optimistic read — does NOT acquire lock, very fast
        long stamp = lock.tryOptimisticRead();
        double x = coords[0], y = coords[1];

        // Validate that no write happened during our read
        if (lock.validate(stamp)) {
            System.out.println("  Optimistic read succeeded: (" + x + ", " + y + ")");
        } else {
            // Fallback to pessimistic read lock
            stamp = lock.readLock();
            try {
                x = coords[0];
                y = coords[1];
                System.out.println("  Pessimistic read: (" + x + ", " + y + ")");
            } finally {
                lock.unlockRead(stamp);
            }
        }

        // Write lock
        stamp = lock.writeLock();
        try {
            coords[0] = 3.0;
            coords[1] = 4.0;
            System.out.println("  Write: (" + coords[0] + ", " + coords[1] + ")");
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
