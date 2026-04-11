package com.javamastery.jms;

import jakarta.jms.*;

/**
 * Demonstrates JMS Message Selectors — SQL92-like filtering on message properties.
 * Only messages matching the selector expression are delivered to the consumer.
 */
public class MessageSelectorDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JMS Message Selector Demo ===\n");

        EmbeddedBrokerUtil.startBroker();
        try {
            ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

            // --- 1. Send messages with different properties ---
            System.out.println("--- Sending Messages with Properties ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.selector");

                // Message 1: US, priority 5
                TextMessage m1 = context.createTextMessage("US standard order");
                m1.setStringProperty("region", "US");
                m1.setIntProperty("priority", 5);
                context.createProducer().send(queue, m1);

                // Message 2: EU, priority 8
                TextMessage m2 = context.createTextMessage("EU urgent order");
                m2.setStringProperty("region", "EU");
                m2.setIntProperty("priority", 8);
                context.createProducer().send(queue, m2);

                // Message 3: US, priority 9
                TextMessage m3 = context.createTextMessage("US urgent order");
                m3.setStringProperty("region", "US");
                m3.setIntProperty("priority", 9);
                context.createProducer().send(queue, m3);

                // Message 4: APAC, priority 3
                TextMessage m4 = context.createTextMessage("APAC low-priority");
                m4.setStringProperty("region", "APAC");
                m4.setIntProperty("priority", 3);
                context.createProducer().send(queue, m4);

                System.out.println("  Sent 4 messages with region and priority properties");
            }

            // --- 2. Consume with selector: high priority US orders ---
            System.out.println("\n--- Selector: region = 'US' AND priority > 7 ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.selector");

                // SQL92-like selector expression
                JMSConsumer consumer = context.createConsumer(queue,
                        "region = 'US' AND priority > 7");

                Message msg;
                while ((msg = consumer.receive(1000)) != null) {
                    System.out.println("  Matched: " + msg.getBody(String.class)
                            + " (region=" + msg.getStringProperty("region")
                            + ", priority=" + msg.getIntProperty("priority") + ")");
                }
            }

            // --- 3. Consume remaining (no selector) ---
            System.out.println("\n--- Remaining messages (no selector) ---");
            try (JMSContext context = cf.createContext()) {
                Queue queue = context.createQueue("demo.selector");
                JMSConsumer consumer = context.createConsumer(queue);

                Message msg;
                while ((msg = consumer.receive(1000)) != null) {
                    System.out.println("  Remaining: " + msg.getBody(String.class));
                }
            }

            System.out.println("\n--- Selector Syntax ---");
            System.out.println("  Comparison: =, <>, <, >, <=, >=");
            System.out.println("  Logical: AND, OR, NOT");
            System.out.println("  Range: BETWEEN x AND y");
            System.out.println("  List: IN ('a', 'b', 'c')");
            System.out.println("  Pattern: LIKE 'order%'");
            System.out.println("  Null: IS NULL, IS NOT NULL");

        } finally {
            EmbeddedBrokerUtil.stopBroker();
        }
    }
}
