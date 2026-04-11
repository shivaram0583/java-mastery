package com.javamastery.threading;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ConcurrentCollectionsTest {

    @Test
    void concurrentHashMapHandlesConcurrentWrites() throws InterruptedException {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        Thread[] threads = new Thread[4];
        for (int i = 0; i < threads.length; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    map.put("key-" + id + "-" + j, j);
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        assertEquals(400, map.size());
    }

    @Test
    void concurrentHashMapAtomicCompute() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("count", 0);

        map.compute("count", (k, v) -> v + 1);
        map.merge("count", 10, Integer::sum);

        assertEquals(11, map.get("count"));
    }

    @Test
    void blockingQueueProducerConsumer() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) queue.put(i);
                queue.put(-1); // poison pill
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        int[] sum = {0};
        Thread consumer = new Thread(() -> {
            try {
                while (true) {
                    int val = queue.take();
                    if (val == -1) break;
                    sum[0] += val;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start(); consumer.start();
        producer.join(); consumer.join();
        assertEquals(45, sum[0]); // 0+1+2+...+9
    }

    @Test
    void copyOnWriteArrayListSafeIteration() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");

        // No ConcurrentModificationException
        for (String item : list) {
            list.add(item + "-copy");
        }
        assertEquals(4, list.size());
    }
}
