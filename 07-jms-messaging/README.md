# Module 07 — JMS Messaging

## What is JMS?

**Java Message Service (JMS)** is a Java API for sending messages between two or more clients. It provides a standard way to create, send, receive, and read messages using **asynchronous, reliable, loosely coupled** communication.

> **Why messaging matters:** In a traditional direct method call, the caller must know exactly which object to call, that object must be available right now, and both must be in the same application. Messaging removes all these constraints — the sender and receiver don’t need to know about each other, don’t need to be online at the same time, and can even be written in different languages.

> **Real-world analogy:** Think of JMS like a postal service. When you mail a letter (send a message), you don’t need the recipient to be home (the consumer doesn’t need to be running). The post office (message broker) holds the letter until the recipient picks it up. You don’t even need to know the recipient’s schedule — the postal service handles delivery. This is **temporal decoupling** (sender and receiver don’t need to be active simultaneously) and **spatial decoupling** (they don’t need to know each other’s address/location).

---

## Core Concepts

### Messaging Models

JMS supports two fundamental messaging patterns:

| Model | Description | Use Case |
|---|---|---|
| **Point-to-Point (Queue)** | One producer sends a message to a queue. **Exactly one** consumer receives and processes it. Once consumed, the message is removed. | Task distribution (e.g., order processing: each order is processed exactly once), work queues |
| **Publish/Subscribe (Topic)** | One producer publishes a message to a topic. **All active subscribers** receive a copy of the message. | Event notifications (e.g., price updates, system alerts), broadcasting logs |

```
  Point-to-Point (Queue):                Publish/Subscribe (Topic):

  Producer ──► Queue ──► Consumer          Producer ──► Topic ─┬─► Subscriber 1
                │                                           ├─► Subscriber 2
                │     Only ONE consumer                     └─► Subscriber 3
                │     gets the message
                │                              ALL subscribers get a copy
  Producer ──► Queue ──► Consumer
            (load balancing:
             messages spread
             across consumers)
```

> **When to use which:** Use **Queues** when each message represents a unit of work that should be processed exactly once (e.g., processing an order, sending an email). Use **Topics** when multiple systems need to react to the same event (e.g., a new user signs up → email service sends welcome email, analytics service records the event, recommendation engine updates).

---

### JMS Architecture

Understanding the JMS component model helps you see how messages flow from sender to receiver:

```
  ┌───────────────────────────────────────────────────────────┐
  │                    JMS Architecture                         │
  │                                                             │
  │  ConnectionFactory   ──►   Connection   ──►   Session         │
  │  (configured with         (TCP link to       (unit of work, │
  │   broker URL,              the broker)        single-        │
  │   credentials)                                threaded)      │
  │                                                    │          │
  │                                              ┌────┴────┐     │
  │                                              │         │     │
  │                                        Producer    Consumer   │
  │                                              │         │     │
  │                                              └────┬────┘     │
  │                                                   │          │
  │                                             Destination      │
  │                                          (Queue or Topic)    │
  └───────────────────────────────────────────────────────────┘
```

**Key components explained:**
- **ConnectionFactory** — A factory object configured with the broker’s connection details (URL, port, credentials). You obtain it from JNDI or create it directly. Think of it as the “address book entry” for your message broker.
- **Connection** — A TCP connection to the message broker. It’s heavyweight (represents a real network socket), so you typically create one per application and reuse it.
- **Session** — A lightweight context for producing and consuming messages. Sessions are **single-threaded** — use one session per thread. Sessions can be transactional or non-transactional.
- **MessageProducer/MessageConsumer** — Objects created from a session that send or receive messages to/from a specific destination.
- **Destination** — The target for messages: either a **Queue** (point-to-point) or a **Topic** (publish/subscribe).

---

### Message Types

JMS supports several message body types to accommodate different data formats:

| Type | Content | Typical Use |
|---|---|---|
| `TextMessage` | String payload | JSON/XML data, simple text |
| `ObjectMessage` | Serializable Java object | Java-to-Java communication (avoid in cross-language systems) |
| `MapMessage` | Key-value pairs (String keys, primitive values) | Structured data without a schema |
| `BytesMessage` | Raw bytes | Binary data, protocol buffers, images |
| `StreamMessage` | Stream of primitive types | Sequential reading/writing of mixed primitives |

> **Best practice:** `TextMessage` with JSON is the most common choice in modern systems because it’s human-readable, language-independent, and easy to debug.

---

### Delivery Guarantees

JMS provides several mechanisms to ensure messages are delivered reliably:

- **Persistent** (default): The broker saves messages to disk. If the broker crashes and restarts, messages are not lost. Slower but safe.
- **Non-persistent**: Messages are kept only in memory. Faster but may be lost if the broker crashes.

**Acknowledgment modes** determine when the broker considers a message “successfully delivered”:
- **`AUTO_ACKNOWLEDGE`** — The session automatically acknowledges receipt after the consumer’s `receive()` or `onMessage()` returns. Simplest but least control.
- **`CLIENT_ACKNOWLEDGE`** — The consumer explicitly calls `message.acknowledge()`. Gives you control over when to confirm processing.
- **`DUPS_OK_ACKNOWLEDGE`** — The session lazily acknowledges in batches. Better performance but the consumer may receive duplicates after a failure.

**Transactions:** Group multiple send/receive operations into an atomic unit. Either all operations in the transaction succeed (commit), or all are rolled back. Essential for scenarios like “dequeue a message and insert into a database” where both must succeed or both must fail.

```
  Transactional flow:
  ┌────────────────────────────────────┐
  │  session.receive(message)        │
  │  processMessage(message)         │
  │  session.send(responseMessage)   │
  │                                  │
  │  session.commit()   → all saved  │
  │       OR                         │
  │  session.rollback() → all undone │
  └────────────────────────────────────┘
```

---

### Message Selectors

Message selectors let consumers **filter** which messages they receive using SQL92-like expressions on message headers and properties. The filtering happens at the broker, so unmatched messages are never delivered to the consumer.

```java
// Only receive messages where priority > 5 AND region is 'US'
consumer = session.createConsumer(queue, "priority > 5 AND region = 'US'");
```

This is useful when multiple consumers on the same queue need to handle different types of messages (e.g., regional processing, priority-based routing).

---

## Demos in This Module
| Class | Topics |
|---|---|
| `EmbeddedBrokerUtil` | Start/stop embedded ActiveMQ Artemis broker |
| `QueueDemo` | Point-to-point messaging |
| `TopicDemo` | Publish/subscribe pattern |
| `MessageSelectorDemo` | Filtering messages with selectors |
| `TransactionalMessagingDemo` | Transacted sessions, commit/rollback |

## How to Run
```bash
# Run any demo (embedded broker starts automatically)
mvn -pl 07-jms-messaging exec:java -Dexec.mainClass="com.javamastery.jms.QueueDemo"

# Run tests
mvn -pl 07-jms-messaging test
```

---

## Interview Questions

**Q1: What is JMS and why is it used?**
JMS (Java Message Service) is a Java API standard for asynchronous message-oriented communication. It decouples producers and consumers both temporally (they don't need to be online simultaneously) and spatially (they don't need to know each other's address). Use cases: order processing, event-driven architectures, microservice communication, log aggregation, batch processing triggers.

**Q2: What is the difference between a Queue and a Topic in JMS?**
A **Queue** implements point-to-point messaging: each message is delivered to exactly one consumer (load balanced). A **Topic** implements publish/subscribe: each message is broadcast to all active subscribers. Use queues for work distribution; topics for event notification.

**Q3: Explain the JMS message delivery guarantee modes.**
- **Persistent** (default): broker writes message to disk — survives broker restarts. Slower.
- **Non-persistent**: kept in memory only — faster but may be lost on crash.
Also, acknowledgment modes control when the broker marks a message as delivered: `AUTO_ACKNOWLEDGE` (auto after receive/onMessage), `CLIENT_ACKNOWLEDGE` (consumer calls `message.acknowledge()`), `DUPS_OK_ACKNOWLEDGE` (lazy batch ack, may cause duplicates).

**Q4: What is the difference between synchronous and asynchronous message consumption?**
**Synchronous**: consumer calls `consumer.receive()` or `receive(timeout)`, which blocks the thread until a message arrives. **Asynchronous**: consumer registers a `MessageListener` with an `onMessage(Message)` callback — the JMS provider invokes it on its own thread when a message arrives. Async is preferred for event-driven architectures; sync is simpler for batch processing.

**Q5: What is a Dead Letter Queue (DLQ)?**
A DLQ is a special queue where the broker places messages that cannot be delivered after a configured number of retry attempts (e.g., consumer throws an exception every time, message expires, message is rejected). Monitoring the DLQ helps detect processing failures. Typically you set up alerts on DLQ depth and periodically inspect/reprocess failed messages.

**Q6: What are JMS transactions? When would you use them?**
JMS transactions group multiple send/receive operations into an atomic unit. `session.commit()` makes all operations permanent; `session.rollback()` undoes them all. Use case: "dequeue order message → insert into database → send confirmation". If the DB insert fails, you roll back, and the message goes back to the queue for reprocessing.

**Q7: What is a message selector? How does it work?**
A message selector is a SQL92-like expression evaluated at the broker that filters messages delivered to a consumer based on message headers and properties. Example: `"priority > 5 AND region = 'US'"`. Filtering happens at the broker, so unwanted messages are never transmitted to the consumer. This enables routing without multiple queues.

**Q8: What is the difference between durable and non-durable subscriptions?**
A **non-durable** subscriber only receives messages published while it is actively connected. If it disconnects and reconnects, messages published during the disconnection are lost. A **durable** subscriber asks the broker to hold messages for it while disconnected. On reconnection, the buffered messages are delivered. Durable subscriptions require a unique client ID and subscription name.

**Q9: Explain the difference between JMS 1.1 and JMS 2.0 (Jakarta).**
JMS 2.0 (Jakarta Messaging 3.0+): (1) Simplified API — `JMSContext` replaces the verbose `ConnectionFactory → Connection → Session → Producer` chain. (2) `try-with-resources` on `JMSContext`. (3) Shared subscriptions on topics (multiple consumers share the load). (4) Delivery delay. (5) `CompletionListener` for async send.

**Q10: What is the role of a `ConnectionFactory` in JMS?**
`ConnectionFactory` is the entry point for JMS. It encapsulates the broker's connection details (URL, port, credentials). In Java EE/Jakarta EE, it's typically looked up via JNDI. In standalone applications, you create it directly from the broker's specific factory class (e.g., `ActiveMQConnectionFactory`).

**Q11: How does JMS handle message redelivery?**
When a message is not acknowledged (consumer crashes, throws an exception, or rolls back a transaction), the broker redelivers it. Redelivered messages have the `JMSRedelivered` header set to `true` and an incrementing delivery count. After exceeding the max redelivery count (broker-configured), the message is moved to the DLQ.

**Q12: What is message ordering in JMS? Is it guaranteed?**
JMS guarantees ordering for messages sent by a **single producer to a single destination in a single session**. If you use multiple producers, multiple sessions, or the consumer uses concurrent processing, ordering is NOT guaranteed. Some brokers offer message groups to maintain order within a group.

**Q13: What is a `MessageListener` and how is it used?**
`MessageListener` is an interface with a single `onMessage(Message)` method. You register it on a `MessageConsumer`, and the JMS provider invokes it asynchronously when a message arrives. The listener runs on the provider's thread, so long processing can stall message delivery — offload heavy work to a separate thread pool.

**Q14: Compare JMS with Kafka. When would you choose each?**
| Feature | JMS (ActiveMQ, RabbitMQ) | Apache Kafka |
|---|---|---|
| Model | Queue (P2P) + Topic (pub/sub) | Distributed log with consumer groups |
| Ordering | Per-queue/session | Per-partition only |
| Retention | Removed after consumption | Retained for configurable duration |
| Consumer offset | Broker tracks | Consumer tracks (replayable) |
| Throughput | Moderate (~10K-50K msg/sec) | Very high (~100K-1M msg/sec) |
| Best for | Enterprise integration, transactions | Stream processing, event sourcing |

**Q15: How do you ensure exactly-once processing in JMS?**
JMS provides at-most-once (non-persistent, auto-ack) or at-least-once (persistent, client-ack). **Exactly-once** requires application-level idempotency: assign a unique message ID, track processed IDs in a database, and skip duplicates. This is often combined with JMS + database transactions via JTA (XA transactions).

---

## Practice Problems

### Easy
1. **Basic Queue:** Send 5 text messages to a queue and consume them all, printing each.
2. **Message Properties:** Send messages with custom properties (priority, region). Consume and print both the body and properties.
3. **Topic Subscriber:** Publish 3 messages to a topic with two subscribers. Verify each subscriber receives all 3.

### Medium
4. **Message Selector:** Send 10 messages with a `category` property (alternating "A" and "B"). Create two consumers with selectors: one for category=A, one for category=B. Verify correct routing.
5. **Request-Reply:** Implement request-reply: producer sends a message with `JMSReplyTo` set, consumer reads it, processes it, and sends a response to the reply queue.
6. **Transactional Batch:** Send 5 messages in a transaction using `session.commit()`. Then send 5 more and call `session.rollback()`. Verify only the first 5 are in the queue.

### Hard
7. **Dead Letter Queue:** Configure a maximum redelivery count. Create a consumer that always throws an exception. Verify the message ends up in the DLQ.
8. **Priority Queue:** Send messages with different JMS priorities (0-9). Verify they are consumed in priority order.
9. **Competing Consumers:** Set up 3 consumers on the same queue. Send 30 messages. Verify each consumer processes approximately 10 messages (load balancing).

### Challenge
10. **Saga Pattern:** Implement a simplified saga: Order Service sends "create order" → Inventory Service dequeues and reserves stock → Payment Service charges → Order Service marks order as confirmed. Use JMS queues for each step.

---

## Common Mistakes Cheat Sheet

| Mistake | Why It's Wrong | Fix |
|---|---|---|
| Not closing resources | Connection/session leaks exhaust broker connections | Use `try-with-resources` |
| Creating connection per message | Connections are heavyweight (TCP socket) | Reuse connections, create session per thread |
| Using `ObjectMessage` across services | Requires same class on classpath, tight coupling | Use `TextMessage` with JSON |
| `AUTO_ACKNOWLEDGE` + heavy processing | If consumer crashes mid-processing, message is lost | Use `CLIENT_ACKNOWLEDGE` or transactions |
| Not handling `JMSRedelivered` | Infinite reprocessing loop on persistent errors | Check redelivery count, send to DLQ |
| Non-durable topic subscription | Messages lost when subscriber is offline | Use durable subscriptions |
| Blocking `onMessage()` with long work | Stalls message delivery for that consumer | Offload to thread pool |
| Forgetting `connection.start()` | Required to begin receiving messages | Always call `start()` before consuming |

## Key Takeaways
- JMS decouples producers from consumers both **temporally** (they don't need to be online at the same time) and **spatially** (they don't need to know each other's location)
- **Queues** = work distribution (each message processed once), **Topics** = event broadcasting (each message delivered to all subscribers)
- Use **transactions** for reliable multi-message workflows where all operations must succeed or fail together
- **Message selectors** enable routing and filtering at the broker level, reducing unnecessary message delivery
- ActiveMQ Artemis provides a lightweight embedded broker for learning and testing without external infrastructure
- JMS decouples producers from consumers both **temporally** (they don’t need to be online at the same time) and **spatially** (they don’t need to know each other’s location)
- **Queues** = work distribution (each message processed once), **Topics** = event broadcasting (each message delivered to all subscribers)
- Use **transactions** for reliable multi-message workflows where all operations must succeed or fail together
- **Message selectors** enable routing and filtering at the broker level, reducing unnecessary message delivery
- ActiveMQ Artemis provides a lightweight embedded broker for learning and testing without external infrastructure
