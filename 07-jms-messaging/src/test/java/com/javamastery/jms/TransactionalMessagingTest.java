package com.javamastery.jms;

import jakarta.jms.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionalMessagingTest {

    @BeforeAll
    static void startBroker() throws Exception {
        EmbeddedBrokerUtil.startBroker();
    }

    @AfterAll
    static void stopBroker() throws Exception {
        EmbeddedBrokerUtil.stopBroker();
    }

    @Test
    void committedMessagesAreVisible() {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext txContext = cf.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = txContext.createQueue("test.tx.commit");
            txContext.createProducer().send(queue, "committed msg");
            txContext.commit();
        }

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.tx.commit");
            String msg = context.createConsumer(queue).receiveBody(String.class, 2000);
            assertEquals("committed msg", msg);
        }
    }

    @Test
    void rolledBackMessagesAreDiscarded() {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext txContext = cf.createContext(JMSContext.SESSION_TRANSACTED)) {
            Queue queue = txContext.createQueue("test.tx.rollback");
            txContext.createProducer().send(queue, "will be rolled back");
            txContext.rollback();
        }

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.tx.rollback");
            String msg = context.createConsumer(queue).receiveBody(String.class, 1000);
            assertNull(msg);
        }
    }
}
