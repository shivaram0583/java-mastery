# Java Mastery — A Structured Java Learning Repository

A comprehensive, hands-on Java learning path covering **Java 8 through Java 24**, JVM internals, multithreading, async programming, messaging, and advanced topics. Each module contains detailed explanations, runnable code examples, practice problems, and curated interview questions to prepare you for real-world Java roles.

---

## Who Is This For?

- **Beginner to Intermediate Java developers** looking to deepen their understanding of modern Java
- **Senior developers** preparing for architecture/design interviews
- **Anyone transitioning** from Java 8 to modern Java (17/21/24)
- **Interview candidates** preparing for Java positions at top tech companies

---

## Prerequisites

| Requirement      | Minimum Version | Notes |
|-----------------|----------------|-------|
| **Java (JDK)**  | 21 (LTS)       | Required for virtual threads, record patterns, sequenced collections |
| **Maven**       | 3.8+           | For building and running all modules |
| **Git**         | 2.x            | For cloning the repository |
| **IDE**         | IntelliJ IDEA / VS Code | Optional but recommended for debugging |

Verify your setup:

```bash
java --version    # should print 21+
mvn --version     # should print 3.8+
```

> **Tip:** If you're on Windows and don't have Java 21, download from [Adoptium](https://adoptium.net/) or use `winget install EclipseAdoptium.Temurin.21.JDK`. On macOS, use `brew install openjdk@21`.

---

## Learning Roadmap (Suggested Order)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  START HERE                                                                 │
│                                                                             │
│  01 ─► Java 8 Features                     ← Foundation: lambdas, streams, │
│        (lambdas, streams, Optional,           Optional, functional style.   │
│         Date API, Collectors)                 Everything else builds on this│
│           │                                                                 │
│  02 ─► Java 9–17 Features                  ← Language evolution: modules,  │
│        (modules, var, records,                records, sealed classes,      │
│         sealed classes, switch expr.)         pattern matching              │
│           │                                                                 │
│  03 ─► Java 18–24 Features                 ← Latest: virtual threads,      │
│        (virtual threads, pattern matching,    structured concurrency,       │
│         sequenced collections)                pattern matching for switch   │
│           │                                                                 │
│  04 ─► JVM Internals                       ← Deep dive: memory model, GC, │
│        (memory, GC, JIT, classloading,        JIT — critical for perf      │
│         bytecode)                             tuning & debugging            │
│           │                                                                 │
│  05 ─► Multithreading                      ← Core concurrency: threads,    │
│        (threads, locks, atomic ops,           synchronization, executors,   │
│         concurrent collections)               deadlock prevention           │
│           │                                                                 │
│  06 ─► CompletableFuture                   ← Async mastery: non-blocking   │
│        (async pipelines, combining,           pipelines, error handling,    │
│         error handling)                       composition patterns          │
│           │                                                                 │
│  07 ─► JMS Messaging                       ← Enterprise messaging: queues, │
│        (queues, topics, selectors,            topics, transactions,         │
│         transactions)                         ActiveMQ Artemis              │
│           │                                                                 │
│  08 ─► Advanced Concepts                   ← Expert level: reflection,     │
│        (reflection, generics, patterns,       generics, design patterns,   │
│         serialization)                        memory leaks                  │
│                                                                             │
│  MASTERY ACHIEVED 🎓                                                        │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Module Summary

| # | Module | Key Topics | Practice Problems | Interview Qs |
|---|--------|-----------|-------------------|-------------|
| 01 | [Java 8 Features](01-java8-features/) | Lambdas, Streams, Optional, Date/Time, Collectors, Functional Interfaces | 25+ | 40+ |
| 02 | [Java 9–17](02-java9-to-17/) | Modules, var, Records, Sealed Classes, Pattern Matching, Switch Expressions | 20+ | 35+ |
| 03 | [Java 18–24](03-java18-to-24/) | Virtual Threads, Record Patterns, Pattern Matching Switch, Sequenced Collections | 15+ | 30+ |
| 04 | [JVM Internals](04-jvm-internals/) | Class Loading, Memory Model, GC, JIT, Bytecode | 15+ | 40+ |
| 05 | [Multithreading](05-multithreading/) | Thread Lifecycle, Synchronization, Locks, Executors, Concurrent Collections | 25+ | 50+ |
| 06 | [CompletableFuture](06-completable-future/) | Async Pipelines, Combining Futures, Error Handling, Real-World Patterns | 15+ | 25+ |
| 07 | [JMS Messaging](07-jms-messaging/) | P2P Queues, Pub/Sub Topics, Message Selectors, Transactions | 10+ | 20+ |
| 08 | [Advanced Concepts](08-advanced-concepts/) | Reflection, Annotations, Generics, Design Patterns, Serialization | 20+ | 35+ |

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

### Run a specific test

```bash
mvn test -pl 01-java8-features -Dtest=LambdaTest
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
| Method References | 01-java8-features | `MethodReferencesDemo`, `MethodReferencesTest` |
| Practice Problems (Java 8) | 01-java8-features | `Java8PracticeProblems`, `Java8PracticeTest` |
| Module System (JPMS) | 02-java9-to-17 | `ModuleSystemDemo` |
| Local Variable Type Inference | 02-java9-to-17 | `VarDemo`, `VarTest` |
| New String Methods | 02-java9-to-17 | `StringMethodsDemo`, `StringMethodsTest` |
| HttpClient | 02-java9-to-17 | `HttpClientDemo` |
| Switch Expressions | 02-java9-to-17 | `SwitchExpressionsDemo`, `SwitchExpressionsTest` |
| Records | 02-java9-to-17 | `RecordsDemo`, `RecordsTest` |
| Sealed Classes | 02-java9-to-17 | `SealedClassesDemo`, `SealedClassesTest` |
| Pattern Matching instanceof | 02-java9-to-17 | `PatternMatchingDemo`, `PatternMatchingTest` |
| Text Blocks | 02-java9-to-17 | `TextBlocksDemo`, `TextBlocksTest` |
| Practice Problems (9-17) | 02-java9-to-17 | `Java9To17PracticeProblems`, `Java9To17PracticeTest` |
| Virtual Threads | 03-java18-to-24 | `VirtualThreadDemo`, `VirtualThreadTest` |
| Record Patterns | 03-java18-to-24 | `RecordPatternsDemo`, `RecordPatternsTest` |
| Pattern Matching Switch | 03-java18-to-24 | `PatternMatchingSwitchDemo`, `PatternMatchingSwitchTest` |
| Sequenced Collections | 03-java18-to-24 | `SequencedCollectionsDemo`, `SequencedCollectionsTest` |
| Structured Concurrency | 03-java18-to-24 | `StructuredConcurrencyDemo` |
| Practice Problems (18-24) | 03-java18-to-24 | `Java18To24PracticeProblems`, `Java18To24PracticeTest` |
| JVM Architecture | 04-jvm-internals | `ClassLoaderDemo`, `MemoryAreasDemo` |
| Garbage Collection | 04-jvm-internals | `GarbageCollectionDemo` |
| JIT Compilation | 04-jvm-internals | `JitDemo` |
| Bytecode | 04-jvm-internals | `BytecodeDemo` |
| Memory Tuning | 04-jvm-internals | `MemoryTuningDemo` |
| Practice Problems (JVM) | 04-jvm-internals | `JvmPracticeProblems` |
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
| Producer-Consumer | 05-multithreading | `ProducerConsumerDemo`, `ProducerConsumerTest` |
| Practice Problems (Threading) | 05-multithreading | `ThreadingPracticeProblems`, `ThreadingPracticeTest` |
| CompletableFuture Basics | 06-completable-future | `CompletableFutureBasicsDemo`, `CompletableFutureBasicsTest` |
| Chaining Futures | 06-completable-future | `ChainingDemo`, `ChainingTest` |
| Error Handling | 06-completable-future | `ErrorHandlingDemo`, `ErrorHandlingTest` |
| Combining Futures | 06-completable-future | `CombiningFuturesDemo`, `CombiningFuturesTest` |
| Real-World Patterns | 06-completable-future | `RealWorldPatternsDemo` |
| Practice Problems (Async) | 06-completable-future | `AsyncPracticeProblems`, `AsyncPracticeTest` |
| JMS Concepts | 07-jms-messaging | `QueueDemo`, `TopicDemo` |
| Message Selectors | 07-jms-messaging | `MessageSelectorDemo` |
| Transactional Messaging | 07-jms-messaging | `TransactionalMessagingDemo` |
| Dead Letter Queue | 07-jms-messaging | `DeadLetterQueueDemo` |
| Practice Problems (JMS) | 07-jms-messaging | `JmsPracticeProblems` |
| Reflection API | 08-advanced-concepts | `ReflectionDemo`, `ReflectionTest` |
| Custom Annotations | 08-advanced-concepts | `AnnotationsDemo`, `AnnotationsTest` |
| Generics Deep Dive | 08-advanced-concepts | `GenericsDemo`, `GenericsTest` |
| Design Patterns | 08-advanced-concepts | `DesignPatternsDemo`, `DesignPatternsTest` |
| Memory Leaks | 08-advanced-concepts | `MemoryLeaksDemo` |
| Serialization | 08-advanced-concepts | `SerializationDemo`, `SerializationTest` |
| Immutability | 08-advanced-concepts | `ImmutabilityDemo`, `ImmutabilityTest` |
| Practice Problems (Advanced) | 08-advanced-concepts | `AdvancedPracticeProblems`, `AdvancedPracticeTest` |

---

## Java Version History Quick Reference

Understanding the Java release timeline helps you know which features are available in your target runtime:

| Version | Release Date | Type | Key Features |
|---------|-------------|------|-------------|
| Java 8 | March 2014 | **LTS** | Lambdas, Streams, Optional, Date/Time API, default methods |
| Java 9 | September 2017 | Feature | Module System (JPMS), JShell, private interface methods |
| Java 10 | March 2018 | Feature | `var` (local variable type inference) |
| Java 11 | September 2018 | **LTS** | New String methods, HttpClient, single-file source programs |
| Java 12 | March 2019 | Feature | Switch expressions (preview) |
| Java 13 | September 2019 | Feature | Text blocks (preview) |
| Java 14 | March 2020 | Feature | Records (preview), pattern matching instanceof (preview), helpful NPE |
| Java 15 | September 2020 | Feature | Sealed classes (preview), text blocks (final) |
| Java 16 | March 2021 | Feature | Records (final), pattern matching instanceof (final) |
| Java 17 | September 2021 | **LTS** | Sealed classes (final), pattern matching instanceof (final) |
| Java 18 | March 2022 | Feature | UTF-8 by default, simple web server |
| Java 19 | September 2022 | Feature | Virtual threads (preview), structured concurrency (preview) |
| Java 20 | March 2023 | Feature | Scoped values (preview) |
| Java 21 | September 2023 | **LTS** | Virtual threads (final), record patterns, pattern matching switch, sequenced collections |
| Java 22 | March 2024 | Feature | Unnamed variables, unnamed classes |
| Java 23 | September 2024 | Feature | Primitive types in patterns (preview) |
| Java 24 | March 2025 | Feature | Structured concurrency (final), stream gatherers |

> **Production tip:** Most enterprises run Java 11, 17, or 21 (LTS versions). If you're starting a new project, choose Java 21.

---

## Top 20 Cross-Cutting Interview Questions

These questions span multiple modules and test how well you connect Java concepts:

1. **How do lambdas, functional interfaces, and streams work together in Java 8?** — Lambdas implement functional interfaces, streams use them (Predicate for filter, Function for map, etc.)
2. **What is the difference between `HashMap` and `ConcurrentHashMap`?** — Threading, lock striping, null handling, fail-fast vs weakly-consistent iterators
3. **Explain the Java memory model and happens-before relationship.** — Visibility guarantees, volatile, synchronized, final fields
4. **How do virtual threads differ from platform threads? When would you use each?** — OS vs JVM managed, memory footprint, I/O vs CPU bound, pinning issues
5. **What is type erasure and how does it affect generics at runtime?** — Compile-time type info removed, bridge methods, no `new T()`, `instanceof` limitations
6. **Explain the difference between `synchronized`, `ReentrantLock`, and `StampedLock`.** — Features, fairness, tryLock, read/write separation, optimistic reads
7. **How does garbage collection work? Compare G1, ZGC, and Shenandoah.** — Region-based, concurrent marking, compaction strategies, pause time goals
8. **What are sealed classes and how do they enable exhaustive pattern matching?** — Restricted hierarchies, compiler-verified exhaustiveness, algebraic data types
9. **Explain CompletableFuture's error handling model.** — Exception propagation, exceptionally, handle, whenComplete, comparison with try-catch
10. **What causes memory leaks in Java? How do you detect and fix them?** — Static collections, unclosed resources, inner classes, ThreadLocal, heap dumps
11. **How does the JIT compiler optimize Java code?** — Tiered compilation, inlining, escape analysis, OSR, scalar replacement
12. **What are records in Java? How do they differ from regular classes?** — Immutable data carriers, generated methods, restrictions (no extending classes, no mutable fields)
13. **Explain the producer-consumer pattern and which Java classes support it.** — BlockingQueue, wait/notify, Lock/Condition, backpressure
14. **What is the difference between `Comparable` and `Comparator`?** — Natural ordering vs external ordering, single vs multiple strategies, functional interface
15. **How does class loading work? What is the parent delegation model?** — Bootstrap → Platform → Application, class identity, custom classloaders
16. **What are the differences between `Optional.orElse()` and `Optional.orElseGet()`?** — Eager vs lazy evaluation, performance implications
17. **Explain deadlock, livelock, and starvation. How do you prevent each?** — Lock ordering, timeout, randomized backoff, fair locks
18. **What is structured concurrency and why is it important?** — Task scope lifetime, automatic cancellation, parent-child thread relationship
19. **How does serialization work in Java? What are its security concerns?** — Serializable, transient, serialVersionUID, deserialization attacks, JSON alternatives
20. **What is the module system (JPMS) and what problems does it solve?** — Classpath hell, strong encapsulation, explicit dependencies, jlink

---

## Project Structure

```
java-mastery/
├── README.md                    ← You are here
├── pom.xml                      ← Parent POM (module aggregator)
├── 01-java8-features/           ← Lambdas, Streams, Optional, Date/Time
│   ├── README.md               ← Detailed explanations + interview Qs
│   ├── pom.xml
│   └── src/main/java & test/java
├── 02-java9-to-17/              ← var, Records, Sealed Classes, Modules
├── 03-java18-to-24/             ← Virtual Threads, Pattern Matching
├── 04-jvm-internals/            ← Memory, GC, JIT, ClassLoading
├── 05-multithreading/           ← Threads, Locks, Executors, Synchronizers
├── 06-completable-future/       ← Async Programming Patterns
├── 07-jms-messaging/            ← JMS Queues, Topics, Transactions
└── 08-advanced-concepts/        ← Reflection, Generics, Design Patterns
```

Each module follows the standard Maven layout:

```
module/
├── README.md                    ← Theory, diagrams, interview Qs, practice problems
├── pom.xml
├── src/
│   ├── main/java/com/javamastery/<module>/
│   │   ├── *Demo.java          ← Runnable demonstrations
│   │   └── *PracticeProblems.java  ← Hands-on coding exercises
│   └── test/java/com/javamastery/<module>/
│       ├── *Test.java          ← Unit tests for demos
│       └── *PracticeTest.java  ← Tests for practice problems
```

---

## Recommended Study Strategy

1. **Read the module README** — understand the theory, diagrams, and "why" behind each concept
2. **Run the demo classes** — see the concepts in action with real output
3. **Read the test files** — understand how each feature is validated
4. **Solve the practice problems** — attempt them without looking at solutions first
5. **Review the interview questions** — practice explaining concepts out loud
6. **Connect the dots** — revisit the cross-cutting questions above to see how concepts interrelate

---

## License

This repository is for educational purposes. Feel free to fork and extend.
