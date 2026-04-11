package com.javamastery.jms;

import jakarta.jms.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MessageSelectorTest {

    @BeforeAll
    static void startBroker() throws Exception {
        EmbeddedBrokerUtil.startBroker();
    }

    @AfterAll
    static void stopBroker() throws Exception {
        EmbeddedBrokerUtil.stopBroker();
    }

    @Test
    void selectorFiltersMessages() throws JMSException {
        ConnectionFactory cf = EmbeddedBrokerUtil.createConnectionFactory();

        try (JMSContext context = cf.createContext()) {
            Queue queue = context.createQueue("test.selector");

            // Send message that matches selector
            TextMessage match = context.createTextMessage("matching");
            match.setIntProperty("priority", 10);
            context.createProducer().send(queue, match);

            // Send message that doesn't match
            TextMessage noMatch = context.createTextMessage("not matching");
            noMatch.setIntProperty("priority", 2);
            context.createProducer().send(queue, noMatch);

            // Consumer with selector
            JMSConsumer consumer = context.createConsumer(queue, "priority > 5");
            Message received = consumer.receive(2000);

            assertNotNull(received);
            assertEquals("matching", received.getBody(String.class));

            // Filtered message still on queue but not delivered to this consumer
            Message next = consumer.receive(500);
            assertNull(next); // no more matching messages
        }
    }
}
