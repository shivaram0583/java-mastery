package com.javamastery.jms;

import jakarta.jms.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class QueueDemoTest {

    @BeforeAll
    static void startBroker() throws Exception {
        EmbeddedBrokerUtil.startBroker();
    }

    @AfterAll
    static void stopBroker() throws Exception {
        EmbeddedBrokerUtil.stopBroker();
    }

    @Test
    void sendAndReceiveFromQueue() {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.queue.basic");

            context.createProducer().send(queue, "Hello JMS");
            JMSConsumer consumer = context.createConsumer(queue);
            String received = consumer.receiveBody(String.class, 2000);

            assertEquals("Hello JMS", received);
        }
    }

    @Test
    void multipleMessagesConsumedInOrder() {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.queue.order");

            for (int i = 1; i <= 3; i++) {
                context.createProducer().send(queue, "Msg-" + i);
            }

            JMSConsumer consumer = context.createConsumer(queue);
            assertEquals("Msg-1", consumer.receiveBody(String.class, 2000));
            assertEquals("Msg-2", consumer.receiveBody(String.class, 2000));
            assertEquals("Msg-3", consumer.receiveBody(String.class, 2000));
        }
    }

    @Test
    void messagePropertiesPreserved() throws JMSException {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.queue.props");

            TextMessage msg = context.createTextMessage("with props");
            msg.setStringProperty("color", "blue");
            msg.setIntProperty("count", 42);
            context.createProducer().send(queue, msg);

            JMSConsumer consumer = context.createConsumer(queue);
            Message received = consumer.receive(2000);

            assertNotNull(received);
            assertEquals("blue", received.getStringProperty("color"));
            assertEquals(42, received.getIntProperty("count"));
        }
    }
}
