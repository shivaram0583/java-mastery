package com.javamastery.jms;

import jakarta.jms.*;

/**
 * Demonstrates JMS transacted sessions.
 * Messages are only visible to consumers after commit.
 * Rollback discards all messages sent in the transaction.
 */
public class TransactionalMessagingDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== JMS Transactional Messaging Demo ===\n");

        EmbeddedBrokerUtil.startBroker();
        try {
            ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

            // --- 1. Committed transaction ---
            System.out.println("--- Committed Transaction ---");
            committedTransaction(cf);

            // --- 2. Rolled back transaction ---
            System.out.println("\n--- Rolled Back Transaction ---");
            rolledBackTransaction(cf);

            // --- 3. Transacted receive ---
            System.out.println("\n--- Transacted Receive ---");
            transactedReceive(cf);

        } finally {
            EmbeddedBrokerUtil.stopBroker();
        }
    }

    static void committedTransaction(ConnectionFactory cf) throws Exception {
        // Transacted session: Session.SESSION_TRANSACTED
        try (JMSContext context = cf.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = context.createQueue("demo.tx.commit");

            context.createProducer().send(queue, "TX Message 1");
            context.createProducer().send(queue, "TX Message 2");
            System.out.println("  Sent 2 messages (not yet committed)");

            context.commit(); // Messages are now visible to consumers
            System.out.println("  Committed — messages now visible");
        }

        // Verify consumer receives them
        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("demo.tx.commit");
            JMSConsumer consumer = context.createConsumer(queue);

            String m1 = consumer.receiveBody(String.class, 2000);
            String m2 = consumer.receiveBody(String.class, 2000);
            System.out.println("  Consumer received: " + m1 + ", " + m2);
        }
    }

    static void rolledBackTransaction(ConnectionFactory cf) throws Exception {
        try (JMSContext context = cf.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = context.createQueue("demo.tx.rollback");

            context.createProducer().send(queue, "Will be rolled back 1");
            context.createProducer().send(queue, "Will be rolled back 2");
            System.out.println("  Sent 2 messages");

            context.rollback(); // Discard all messages in this transaction
            System.out.println("  Rolled back — messages discarded");
        }

        // Verify consumer sees nothing
        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("demo.tx.rollback");
            JMSConsumer consumer = context.createConsumer(queue);

            String msg = consumer.receiveBody(String.class, 1000);
            System.out.println("  Consumer received: " + msg + " (null = no messages)");
        }
    }

    static void transactedReceive(ConnectionFactory cf) throws Exception {
        // First, send some messages
        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("demo.tx.receive");
            for (int i = 1; i <= 3; i++) {
                context.createProducer().send(queue, "Order #" + i);
            }
            System.out.println("  Sent 3 orders");
        }

        // Transacted receive — rollback redelivers messages
        try (JMSContext context = cf.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = context.createQueue("demo.tx.receive");
            JMSConsumer consumer = context.createConsumer(queue);

            // Receive but don't commit
            String msg = consumer.receiveBody(String.class, 2000);
            System.out.println("  Received (1st attempt): " + msg);
            context.rollback(); // Message goes back to queue
            System.out.println("  Rolled back — message redelivered");

            // Receive again and commit
            msg = consumer.receiveBody(String.class, 2000);
            System.out.println("  Received (2nd attempt): " + msg);
            context.commit(); // Message is now acknowledged
            System.out.println("  Committed — message consumed");
        }
    }
}
