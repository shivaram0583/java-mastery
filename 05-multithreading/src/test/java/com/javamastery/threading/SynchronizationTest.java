package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SynchronizationTest {

    @Test
    void synchronizedBlockPreventesRaceCondition() throws InterruptedException {
        int[] counter = {0};
        Object lock = new Object();

        Runnable task = () -> {
            for (int i = 0; i < 10_000; i++) {
                synchronized (lock) {
                    counter[0]++;
                }
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        assertEquals(20_000, counter[0]);
    }

    @Test
    void waitNotifyCoordination() throws InterruptedException {
        Object monitor = new Object();
        boolean[] ready = {false};
        int[] value = {0};

        Thread producer = new Thread(() -> {
            synchronized (monitor) {
                value[0] = 42;
                ready[0] = true;
                monitor.notify();
            }
        });

        Thread consumer = new Thread(() -> {
            synchronized (monitor) {
                while (!ready[0]) {
                    try { monitor.wait(); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });

        consumer.start();
        Thread.sleep(50);
        producer.start();
        producer.join(2000);
        consumer.join(2000);

        assertEquals(42, value[0]);
    }
}
