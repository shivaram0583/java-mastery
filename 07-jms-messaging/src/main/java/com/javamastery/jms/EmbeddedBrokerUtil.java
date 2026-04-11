package com.javamastery.jms;

import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import jakarta.jms.ConnectionFactory;

/**
 * Utility to start/stop an embedded ActiveMQ Artemis broker.
 * No external broker installation needed — everything runs in-process.
 */
public class EmbeddedBrokerUtil {

    private static EmbeddedActiveMQ server;

    /**
     * Starts an embedded ActiveMQ Artemis broker on vm://0 (in-VM transport).
     */
    public static synchronized void startBroker() throws Exception {
        if (server != null) return; // already running

        var config = new ConfigurationImpl()
                .setPersistenceEnabled(false)               // in-memory only
                .setJournalDirectory("target/data/journal")
                .setBindingsDirectory("target/data/bindings")
                .setLargeMessagesDirectory("target/data/large")
                .setSecurityEnabled(false)                  // no auth for demos
                .addAcceptorConfiguration("in-vm", "vm://0")
                .addAcceptorConfiguration("tcp", "tcp://localhost:61616");

        server = new EmbeddedActiveMQ();
        server.setConfiguration(config);
        server.start();
        System.out.println("[Broker] Embedded ActiveMQ Artemis started");
    }

    /**
     * Stops the embedded broker.
     */
    public static synchronized void stopBroker() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
            System.out.println("[Broker] Embedded ActiveMQ Artemis stopped");
        }
    }

    /**
     * Creates a JMS ConnectionFactory for the embedded broker.
     */
    public static ConnectionFactory createConnectionFactory() {
        return new ActiveMQConnectionFactory("vm://0");
    }
}
