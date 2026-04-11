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

## Key Takeaways
- JMS decouples producers from consumers both **temporally** (they don’t need to be online at the same time) and **spatially** (they don’t need to know each other’s location)
- **Queues** = work distribution (each message processed once), **Topics** = event broadcasting (each message delivered to all subscribers)
- Use **transactions** for reliable multi-message workflows where all operations must succeed or fail together
- **Message selectors** enable routing and filtering at the broker level, reducing unnecessary message delivery
- ActiveMQ Artemis provides a lightweight embedded broker for learning and testing without external infrastructure
