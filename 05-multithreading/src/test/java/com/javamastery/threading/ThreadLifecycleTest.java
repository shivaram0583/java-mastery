package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ThreadLifecycleTest {

    @Test
    void threadStartsAndCompletes() throws InterruptedException {
        boolean[] ran = {false};
        Thread t = new Thread(() -> ran[0] = true);
        assertEquals(Thread.State.NEW, t.getState());

        t.start();
        t.join(2000);
        assertTrue(ran[0]);
        assertEquals(Thread.State.TERMINATED, t.getState());
    }

    @Test
    void threadCanBeInterrupted() throws InterruptedException {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                // busy work
            }
        });
        t.start();
        Thread.sleep(50);
        t.interrupt();
        t.join(2000);
        assertTrue(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    void runnableAndThreadBothWork() throws InterruptedException {
        int[] result = {0};
        Runnable runnable = () -> result[0] = 42;

        Thread t = new Thread(runnable);
        t.start();
        t.join();
        assertEquals(42, result[0]);
    }
}
