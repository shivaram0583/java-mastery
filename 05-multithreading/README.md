# Module 05 — Multithreading

Comprehensive coverage of Java concurrency, from basic thread management to advanced synchronizers and virtual threads.

---

## Topics

### 1. Thread Lifecycle
States: `NEW → RUNNABLE → { BLOCKED | WAITING | TIMED_WAITING } → TERMINATED`

**Gotchas:** `RUNNABLE` means ready to run OR actually running. There's no separate "RUNNING" state in Java.

### 2. Creating Threads
- `extends Thread` — simple but inflexible (single inheritance)
- `implements Runnable` — preferred, separates task from execution
- `implements Callable<V>` + `Future<V>` — can return values and throw checked exceptions

### 3. Synchronization
- `synchronized` keyword (intrinsic locks) — method-level or block-level
- Visibility problem: without synchronization, threads may see stale values

**Gotchas:** Every object in Java has an intrinsic lock (monitor). `synchronized` is reentrant.

### 4. Volatile
- Guarantees visibility (reads/writes go directly to main memory)
- Does NOT guarantee atomicity (e.g., `volatile int counter; counter++` is NOT thread-safe)
- Establishes happens-before relationships

### 5. java.util.concurrent Executors
- `ExecutorService` — manage a pool of threads
- `ThreadPoolExecutor` — configurable core/max pool size, queue, rejection policy
- `ScheduledExecutorService` — schedule tasks with delay or periodic execution
- `ForkJoinPool` — work-stealing for recursive/parallel tasks

### 6. Locks
| Lock | Features |
|---|---|
| `ReentrantLock` | Explicit lock/unlock, tryLock, interruptible, fairness option |
| `ReadWriteLock` | Multiple concurrent readers OR one exclusive writer |
| `StampedLock` | Optimistic reads + read/write locks (best performance) |

### 7. Atomic Classes
- `AtomicInteger`, `AtomicLong`, `AtomicReference` — CAS-based thread-safe operations
- `LongAdder`, `LongAccumulator` — high-contention counters (better than AtomicLong)

### 8. Concurrent Collections
| Collection | Thread-Safety Approach |
|---|---|
| `ConcurrentHashMap` | Lock striping (segment-level locking) |
| `CopyOnWriteArrayList` | Copy-on-write (reads are lock-free) |
| `LinkedBlockingQueue` | Lock-based blocking operations |
| `ArrayBlockingQueue` | Bounded, lock-based |

### 9. Synchronizers
- `CountDownLatch` — one-time barrier; wait for N events
- `CyclicBarrier` — reusable barrier; N threads wait for each other
- `Semaphore` — control access to N resources
- `Phaser` — flexible, reusable barrier with phases

### 10. ThreadLocal
- Per-thread storage; each thread sees its own copy
- **Pitfall:** memory leaks with thread pools (values persist across task executions)

### 11. Deadlock, Livelock, Starvation
- **Deadlock:** Two threads each holding a lock the other needs
- **Livelock:** Threads actively trying to resolve conflict but making no progress
- **Starvation:** A thread can never acquire a lock because others always get it first

### 12. Virtual Threads (Java 21)
See module 03 for full virtual thread coverage. This module contrasts with platform threads.

---

## File Overview

| Topic | Demo Class | Test Class |
|---|---|---|
| Thread Lifecycle | `ThreadLifecycleDemo.java` | `ThreadLifecycleTest.java` |
| Synchronization | `SynchronizationDemo.java` | `SynchronizationTest.java` |
| Volatile | `VolatileDemo.java` | — |
| ExecutorService | `ExecutorServiceDemo.java` | `ExecutorServiceTest.java` |
| Locks | `LocksDemo.java` | `LocksTest.java` |
| Atomic Classes | `AtomicDemo.java` | `AtomicTest.java` |
| Concurrent Collections | `ConcurrentCollectionsDemo.java` | `ConcurrentCollectionsTest.java` |
| Synchronizers | `SynchronizersDemo.java` | `SynchronizersTest.java` |
| ThreadLocal | `ThreadLocalDemo.java` | — |
| Deadlock | `DeadlockDemo.java` | `DeadlockTest.java` |
