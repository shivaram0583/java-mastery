package com.javamastery.jms;

import jakarta.jms.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates Publish/Subscribe messaging with JMS Topics.
 * One publisher sends; ALL subscribers receive a copy.
 */
public class TopicDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JMS Topic (Publish/Subscribe) Demo ===\n");

        EmbeddedBrokerUtil.startBroker();
        try {
            ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();
            CountDownLatch latch = new CountDownLatch(6); // 3 messages × 2 subscribers

            // --- 1. Create subscribers FIRST (they must be active to receive) ---
            System.out.println("--- Setting up Subscribers ---");

            // Subscriber 1
            Thread sub1 = new Thread(() -> {
                try (JMSContext context = cf.createContext()) {
                    Topic topic = context.createTopic("demo.topic");
                    JMSConsumer consumer = context.createConsumer(topic);

                    consumer.setMessageListener(msg -> {
                        try {
                            System.out.println("  [Sub-1] Received: " + msg.getBody(String.class));
                            latch.countDown();
                        } catch (JMSException e) { e.printStackTrace(); }
                    });

                    latch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Subscriber-1");

            // Subscriber 2
            Thread sub2 = new Thread(() -> {
                try (JMSContext context = cf.createContext()) {
                    Topic topic = context.createTopic("demo.topic");
                    JMSConsumer consumer = context.createConsumer(topic);

                    consumer.setMessageListener(msg -> {
                        try {
                            System.out.println("  [Sub-2] Received: " + msg.getBody(String.class));
                            latch.countDown();
                        } catch (JMSException e) { e.printStackTrace(); }
                    });

                    latch.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Subscriber-2");

            sub1.start();
            sub2.start();
            Thread.sleep(500); // let subscribers register

            // --- 2. Publish messages ---
            System.out.println("--- Publishing Messages ---");
            try (JMSContext context = cf.createContext()) {
                Topic topic = context.createTopic("demo.topic");

                for (int i = 1; i <= 3; i++) {
                    context.createProducer().send(topic, "Event #" + i);
                    System.out.println("  Published: Event #" + i);
                }
            }

            latch.await(5, TimeUnit.SECONDS);
            sub1.join(2000);
            sub2.join(2000);

            System.out.println("\n  Key difference from Queue:");
            System.out.println("    Queue → each message consumed by ONE consumer");
            System.out.println("    Topic → each message delivered to ALL subscribers");

        } finally {
            EmbeddedBrokerUtil.stopBroker();
        }
    }
}
