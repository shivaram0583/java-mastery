# Java Mastery — A Structured Java Learning Repository

A comprehensive, hands-on Java learning path covering **Java 8 through Java 24**, JVM internals, multithreading, async programming, messaging, and advanced topics.

---

## Prerequisites

| Requirement      | Minimum Version |
|-----------------|----------------|
| **Java (JDK)**  | 21 (LTS)       |
| **Maven**       | 3.8+           |
| **Git**         | 2.x            |

Verify your setup:

```bash
java --version    # should print 21+
mvn --version     # should print 3.8+
```

---

## Learning Roadmap (Suggested Order)

```
┌─────────────────────────────────────────────────────────────────┐
│  START HERE                                                     │
│                                                                 │
│  01 ─► Java 8 Features (lambdas, streams, Optional, Date API)  │
│           │                                                     │
│  02 ─► Java 9–17 (modules, var, records, sealed classes)       │
│           │                                                     │
│  03 ─► Java 18–24 (virtual threads, pattern matching)          │
│           │                                                     │
│  04 ─► JVM Internals (memory, GC, JIT, classloading)          │
│           │                                                     │
│  05 ─► Multithreading (threads, locks, concurrent collections) │
│           │                                                     │
│  06 ─► CompletableFuture (async pipelines, structured concur.) │
│           │                                                     │
│  07 ─► JMS Messaging (queues, topics, ActiveMQ Artemis)        │
│           │                                                     │
│  08 ─► Advanced Concepts (reflection, generics, patterns)      │
│                                                                 │
│  MASTERY ACHIEVED 🎓                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## How to Run

### Run all tests from root

```bash
mvn clean test
```

### Run tests for a single module

```bash
mvn clean test -pl 01-java8-features
```

### Run a specific demo class

```bash
cd 01-java8-features
mvn compile exec:java -Dexec.mainClass="com.javamastery.java8.LambdaDemo"
```

Or compile and run directly:

```bash
cd 01-java8-features
mvn compile
java -cp target/classes com.javamastery.java8.LambdaDemo
```

For preview features (modules 03, parts of 02):

```bash
java --enable-preview -cp target/classes com.javamastery.java21.VirtualThreadDemo
```

---

## Concept Index

| Concept | Module | Key Classes |
|---|---|---|
| Lambda Expressions | 01-java8-features | `LambdaDemo`, `LambdaTest` |
| Stream API | 01-java8-features | `StreamApiDemo`, `StreamApiTest` |
| Optional | 01-java8-features | `OptionalDemo`, `OptionalTest` |
| Default/Static Interface Methods | 01-java8-features | `DefaultMethodsDemo`, `DefaultMethodsTest` |
| Date/Time API | 01-java8-features | `DateTimeApiDemo`, `DateTimeApiTest` |
| Collectors | 01-java8-features | `CollectorsDemo`, `CollectorsTest` |
| Functional Interfaces | 01-java8-features | `FunctionalInterfacesDemo`, `FunctionalInterfacesTest` |
| Module System (JPMS) | 02-java9-to-17 | `ModuleSystemDemo` |
| Local Variable Type Inference | 02-java9-to-17 | `VarDemo`, `VarTest` |
| New String Methods | 02-java9-to-17 | `StringMethodsDemo`, `StringMethodsTest` |
| HttpClient | 02-java9-to-17 | `HttpClientDemo` |
| Switch Expressions | 02-java9-to-17 | `SwitchExpressionsDemo`, `SwitchExpressionsTest` |
| Records | 02-java9-to-17 | `RecordsDemo`, `RecordsTest` |
| Sealed Classes | 02-java9-to-17 | `SealedClassesDemo`, `SealedClassesTest` |
| Pattern Matching instanceof | 02-java9-to-17 | `PatternMatchingDemo`, `PatternMatchingTest` |
| Virtual Threads | 03-java18-to-24 | `VirtualThreadDemo`, `VirtualThreadTest` |
| Record Patterns | 03-java18-to-24 | `RecordPatternsDemo`, `RecordPatternsTest` |
| Pattern Matching Switch | 03-java18-to-24 | `PatternMatchingSwitchDemo`, `PatternMatchingSwitchTest` |
| Sequenced Collections | 03-java18-to-24 | `SequencedCollectionsDemo`, `SequencedCollectionsTest` |
| Structured Concurrency | 03-java18-to-24 | `StructuredConcurrencyDemo` |
| JVM Architecture | 04-jvm-internals | `ClassLoaderDemo`, `MemoryAreasDemo` |
| Garbage Collection | 04-jvm-internals | `GarbageCollectionDemo` |
| JIT Compilation | 04-jvm-internals | `JitDemo` |
| Bytecode | 04-jvm-internals | `BytecodeDemo` |
| Thread Lifecycle | 05-multithreading | `ThreadLifecycleDemo`, `ThreadLifecycleTest` |
| Synchronization | 05-multithreading | `SynchronizationDemo`, `SynchronizationTest` |
| Volatile | 05-multithreading | `VolatileDemo` |
| Executors | 05-multithreading | `ExecutorServiceDemo`, `ExecutorServiceTest` |
| Locks | 05-multithreading | `LocksDemo`, `LocksTest` |
| Atomic Classes | 05-multithreading | `AtomicDemo`, `AtomicTest` |
| Concurrent Collections | 05-multithreading | `ConcurrentCollectionsDemo`, `ConcurrentCollectionsTest` |
| Synchronizers | 05-multithreading | `SynchronizersDemo`, `SynchronizersTest` |
| ThreadLocal | 05-multithreading | `ThreadLocalDemo` |
| Deadlock/Livelock | 05-multithreading | `DeadlockDemo`, `DeadlockTest` |
| CompletableFuture Basics | 06-completable-future | `CompletableFutureBasicsDemo`, `CompletableFutureBasicsTest` |
| Chaining Futures | 06-completable-future | `ChainingDemo`, `ChainingTest` |
| Error Handling | 06-completable-future | `ErrorHandlingDemo`, `ErrorHandlingTest` |
| Combining Futures | 06-completable-future | `CombiningFuturesDemo`, `CombiningFuturesTest` |
| Real-World Patterns | 06-completable-future | `ParallelApiCallsDemo` |
| Structured Concurrency | 06-completable-future | `StructuredConcurrencyDemo` |
| JMS Concepts | 07-jms-messaging | `QueueDemo`, `TopicDemo` |
| Message Selectors | 07-jms-messaging | `MessageSelectorDemo` |
| Transactional Messaging | 07-jms-messaging | `TransactionalMessagingDemo` |
| Reflection API | 08-advanced-concepts | `ReflectionDemo`, `ReflectionTest` |
| Custom Annotations | 08-advanced-concepts | `AnnotationsDemo`, `AnnotationsTest` |
| Generics Deep Dive | 08-advanced-concepts | `GenericsDemo`, `GenericsTest` |
| Design Patterns | 08-advanced-concepts | `DesignPatternsDemo`, `DesignPatternsTest` |
| Memory Leaks | 08-advanced-concepts | `MemoryLeaksDemo` |
| Serialization | 08-advanced-concepts | `SerializationDemo`, `SerializationTest` |

---

## Project Structure

```
java-mastery/
├── README.md
├── pom.xml
├── 01-java8-features/
├── 02-java9-to-17/
├── 03-java18-to-24/
├── 04-jvm-internals/
├── 05-multithreading/
├── 06-completable-future/
├── 07-jms-messaging/
└── 08-advanced-concepts/
```

Each module follows the standard Maven layout:

```
module/
├── README.md
├── pom.xml
├── src/
│   ├── main/java/com/javamastery/<module>/
│   └── test/java/com/javamastery/<module>/
```

---

## License

This repository is for educational purposes. Feel free to fork and extend.
