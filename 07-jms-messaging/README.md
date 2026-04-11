# Module 07 — JMS Messaging

## What is JMS?
**Java Message Service (JMS)** is a Java API for sending messages between two or more clients. It provides a standard way to create, send, receive, and read messages using **asynchronous, reliable, loosely coupled** communication.

## Core Concepts

### Messaging Models
| Model | Description | Use Case |
|---|---|---|
| **Point-to-Point (Queue)** | One producer → one consumer. Message consumed once. | Task distribution, order processing |
| **Publish/Subscribe (Topic)** | One producer → many subscribers. Broadcast. | Event notifications, logs |

### JMS Architecture
```
Producer → ConnectionFactory → Connection → Session → MessageProducer → Destination (Queue/Topic)
Consumer → ConnectionFactory → Connection → Session → MessageConsumer → Destination (Queue/Topic)
```

### Message Types
| Type | Content |
|---|---|
| `TextMessage` | String payload |
| `ObjectMessage` | Serializable Java object |
| `MapMessage` | Key-value pairs |
| `BytesMessage` | Raw bytes |
| `StreamMessage` | Stream of primitive types |

### Delivery Guarantees
- **Persistent** (default): message survives broker restart
- **Non-persistent**: faster but may be lost on crash
- **Acknowledgment modes**: AUTO_ACKNOWLEDGE, CLIENT_ACKNOWLEDGE, DUPS_OK_ACKNOWLEDGE
- **Transactions**: group send/receive into atomic units

### Message Selectors
SQL92-like syntax to filter messages:
```java
consumer = session.createConsumer(queue, "priority > 5 AND region = 'US'");
```

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
- JMS decouples producers from consumers (temporal + spatial)
- Queues = work distribution, Topics = event broadcasting
- Use transactions for reliable multi-message workflows
- ActiveMQ Artemis provides a lightweight embedded broker for learning
