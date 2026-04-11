package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class AtomicTest {

    @Test
    void atomicIntegerIsThreadSafe() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        Runnable task = () -> {
            for (int i = 0; i < 10_000; i++) {
                counter.incrementAndGet();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        assertEquals(20_000, counter.get());
    }

    @Test
    void compareAndSetWorks() {
        AtomicInteger value = new AtomicInteger(10);
        assertTrue(value.compareAndSet(10, 20));
        assertEquals(20, value.get());
        assertFalse(value.compareAndSet(10, 30)); // old value doesn't match
        assertEquals(20, value.get());
    }

    @Test
    void longAdderHandlesHighContention() throws InterruptedException {
        LongAdder adder = new LongAdder();

        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50_000; j++) {
                    adder.increment();
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        assertEquals(200_000, adder.sum());
    }

    @Test
    void atomicStampedReferenceDetectsABA() {
        AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

        ref.compareAndSet("A", "B", 0, 1);
        ref.compareAndSet("B", "A", 1, 2);

        // Value is "A" again, but stamp=0 is stale
        assertFalse(ref.compareAndSet("A", "C", 0, 3));
        assertEquals(2, ref.getStamp());
    }
}
