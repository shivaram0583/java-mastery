package com.javamastery.threading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Producer-Consumer")
class ProducerConsumerTest {

    @Test
    @DisplayName("Consumer receives all produced items and correct sum")
    void producerConsumerCorrectSum() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        int count = 20;
        int expectedSum = count * (count + 1) / 2;

        int[] result = new int[1];
        Thread producer = new Thread(() -> ProducerConsumerDemo.produce(queue, count));
        Thread consumer = new Thread(() -> result[0] = ProducerConsumerDemo.consume(queue));

        consumer.start();
        producer.start();

        producer.join(5000);
        consumer.join(5000);

        assertEquals(expectedSum, result[0]);
    }

    @Test
    @DisplayName("Single item produced and consumed")
    void singleItem() throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(2);

        int[] result = new int[1];
        Thread producer = new Thread(() -> ProducerConsumerDemo.produce(queue, 1));
        Thread consumer = new Thread(() -> result[0] = ProducerConsumerDemo.consume(queue));

        consumer.start();
        producer.start();

        producer.join(5000);
        consumer.join(5000);

        assertEquals(1, result[0]);
    }
}
