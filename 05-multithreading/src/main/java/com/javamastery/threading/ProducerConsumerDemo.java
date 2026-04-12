package com.javamastery.threading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Classic Producer-Consumer pattern using BlockingQueue.
 *
 * The BlockingQueue handles all synchronization:
 * - put() blocks when the queue is full (backpressure)
 * - take() blocks when the queue is empty
 *
 * A special "poison pill" value (-1) signals the consumer to stop.
 */
public class ProducerConsumerDemo {

    static final int POISON_PILL = -1;

    /** Produces numbers 1..count into the queue, then sends a poison pill. */
    public static void produce(BlockingQueue<Integer> queue, int count) {
        try {
            for (int i = 1; i <= count; i++) {
                queue.put(i);
                System.out.printf("  Produced: %d (queue size: %d)%n", i, queue.size());
            }
            queue.put(POISON_PILL);
            System.out.println("  Producer finished (poison pill sent).");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Consumes from the queue until a poison pill is received. Returns the sum. */
    public static int consume(BlockingQueue<Integer> queue) {
        int sum = 0;
        try {
            while (true) {
                int value = queue.take();
                if (value == POISON_PILL) {
                    System.out.println("  Consumer received poison pill. Stopping.");
                    break;
                }
                sum += value;
                System.out.printf("  Consumed: %d (running sum: %d)%n", value, sum);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return sum;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Producer-Consumer Demo ===\n");

        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        int count = 10;

        Thread producer = new Thread(() -> produce(queue, count));
        Thread consumer = new Thread(() -> {
            int sum = consume(queue);
            System.out.println("  Final sum: " + sum + " (expected " + (count * (count + 1) / 2) + ")");
        });

        consumer.start();
        producer.start();

        producer.join();
        consumer.join();

        System.out.println("Done.");
    }
}
