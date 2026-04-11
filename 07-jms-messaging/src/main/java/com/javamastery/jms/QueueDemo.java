package com.javamastery.jms;

import jakarta.jms.*;

/**
 * Demonstrates Point-to-Point messaging with JMS Queues.
 * One producer sends messages; one consumer receives them.
 * Each message is consumed exactly once.
 */
public class QueueDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JMS Queue (Point-to-Point) Demo ===\n");

        EmbeddedBrokerUtil.startBroker();
        try {
            ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

            // --- 1. Send messages ---
            System.out.println("--- Sending Messages ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.queue");

                // Send text messages
                for (int i = 1; i <= 5; i++) {
                    context.createProducer().send(queue, "Message #" + i);
                    System.out.println("  Sent: Message #" + i);
                }
            }

            // --- 2. Receive messages ---
            System.out.println("\n--- Receiving Messages ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.queue");
                JMSConsumer consumer = context.createConsumer(queue);

                for (int i = 0; i < 5; i++) {
                    String msg = consumer.receiveBody(String.class, 2000);
                    System.out.println("  Received: " + msg);
                }
            }

            // --- 3. Message properties ---
            System.out.println("\n--- Message Properties ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.props");

                TextMessage msg = context.createTextMessage("Priority order");
                msg.setIntProperty("priority", 10);
                msg.setStringProperty("region", "US");
                msg.setJMSPriority(9);
                context.createProducer().send(queue, msg);
                System.out.println("  Sent message with properties");

                JMSConsumer consumer = context.createConsumer(queue);
                Message received = consumer.receive(2000);
                System.out.println("  Received — priority=" + received.getIntProperty("priority")
                        + ", region=" + received.getStringProperty("region"));
            }

        } finally {
            EmbeddedBrokerUtil.stopBroker();
        }
    }
}
