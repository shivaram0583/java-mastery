package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.locks.*;
import static org.junit.jupiter.api.Assertions.*;

class DeadlockTest {

    @Test
    void consistentLockOrderingPreventsDeadlock() throws InterruptedException {
        Object lockA = new Object();
        Object lockB = new Object();
        boolean[] completed = {false, false};

        // Both threads acquire locks in the same order: A then B
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                synchronized (lockB) {
                    completed[0] = true;
                }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lockA) {
                synchronized (lockB) {
                    completed[1] = true;
                }
            }
        });

        t1.start(); t2.start();
        t1.join(2000); t2.join(2000);

        assertTrue(completed[0], "Thread 1 should complete");
        assertTrue(completed[1], "Thread 2 should complete");
    }

    @Test
    void tryLockPreventsDeadlock() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();
        boolean[] acquired = {false};

        Thread t = new Thread(() -> {
            try {
                if (lock1.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    try {
                        if (lock2.tryLock(100, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                            try {
                                acquired[0] = true;
                            } finally {
                                lock2.unlock();
                            }
                        }
                    } finally {
                        lock1.unlock();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        t.start();
        t.join(2000);
        assertTrue(acquired[0]);
    }
}
