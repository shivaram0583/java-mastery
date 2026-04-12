# Module 05 — Multithreading

Comprehensive coverage of Java concurrency, from basic thread management to advanced synchronizers and virtual threads.

**Why multithreading matters:** Modern CPUs have multiple cores, and multithreading allows your program to perform several tasks at the same time — for example, downloading a file while simultaneously updating the user interface. Without multithreading, a program can only do one thing at a time, wasting valuable CPU resources. In server applications, multithreading is essential: a web server must handle thousands of requests concurrently, not one after another.

> **Real-world analogy:** Think of a restaurant kitchen. A single-threaded kitchen has one chef who takes an order, cooks it, plates it, and then takes the next order. A multithreaded kitchen has multiple chefs working simultaneously — one grills, one prepares salads, one handles desserts. The restaurant serves customers much faster, but the chefs need to coordinate so they don't collide at the stove (synchronization) or use the same knife at the same time (locking).

---

## Topics

### 1. Thread Lifecycle

Every thread in Java goes through a well-defined set of states during its lifetime. Understanding these states is crucial for debugging concurrency issues (e.g., finding out why your application is hanging).

```
                    ┌─────────────────────────────────────────────┐
                    │                                             │
        start()     │    ┌──────────┐  waiting for lock           │
 NEW ──────────► RUNNABLE │  BLOCKED  │◄──── (another thread      │
                    │    │          │   holds the monitor)        │
                    │    └────┬─────┘                             │
                    │         │ lock acquired                     │
                    │         ▼                                   │
                    │    ┌──────────┐                             │
                    │    │ WAITING  │◄──── wait(), join(),        │
                    │    │          │      LockSupport.park()     │
                    │    └────┬─────┘                             │
                    │         │ notify()/notifyAll()/unpark()     │
                    │         ▼                                   │
                    │    ┌──────────────┐                         │
                    │    │TIMED_WAITING │◄── sleep(ms),           │
                    │    │              │    wait(ms), join(ms)   │
                    │    └──────┬───────┘                         │
                    │           │ timeout / notify                │
                    └───────────┼─────────────────────────────────┘
                                │
                                ▼
                           TERMINATED  ◄── run() completes or
                                           throws exception
```

- **NEW:** The thread object has been created (`new Thread(...)`) but `start()` has not been called yet.
- **RUNNABLE:** The thread is eligible to run. Note that this means either actively executing on a CPU core **or** waiting for the OS scheduler to give it CPU time — Java does not distinguish between these two situations.
- **BLOCKED:** The thread is waiting to enter a `synchronized` block/method because another thread currently holds that monitor lock.
- **WAITING:** The thread is waiting indefinitely for another thread to perform a specific action (e.g., calling `notify()` or `join()` completion).
- **TIMED_WAITING:** Like WAITING, but with a timeout — the thread will resume automatically after the specified time even if not explicitly notified.
- **TERMINATED:** The thread has finished execution, either by returning from `run()` or by throwing an uncaught exception.

**Gotchas:** `RUNNABLE` means ready to run OR actually running. There's no separate "RUNNING" state in Java.

---

### 2. Creating Threads

Java provides several ways to create and run concurrent tasks. Each approach has trade-offs:

- **`extends Thread`** — The simplest approach: create a subclass of `Thread` and override the `run()` method. However, since Java does not support multiple inheritance, this approach is inflexible — your class cannot extend any other class.
- **`implements Runnable`** — The preferred approach. You implement the `Runnable` interface (which has a single `run()` method), separating the **task definition** from the **thread execution mechanism**. This lets you pass the same task to a thread pool, a single thread, or any executor.
- **`implements Callable<V>` + `Future<V>`** — Use this when your task needs to **return a result** or **throw a checked exception**. You submit a `Callable` to an `ExecutorService` and get a `Future` back, which you can poll or block on to get the result.

```
  Approach 1: extends Thread          Approach 2: implements Runnable
  ┌──────────────────────┐            ┌──────────────────────┐
  │  class MyTask         │            │  class MyTask         │
  │    extends Thread     │            │    implements Runnable│
  │  {                    │            │  {                    │
  │    void run() {..}    │            │    void run() {..}    │
  │  }                    │            │  }                    │
  │  new MyTask().start() │            │  new Thread(task)     │
  └──────────────────────┘            │     .start()          │
                                      └──────────────────────┘

  Approach 3: Callable + Future
  ┌──────────────────────────────────────┐
  │  Callable<Integer> task = () -> 42;  │
  │  Future<Integer> f =                 │
  │      executor.submit(task);          │
  │  int result = f.get(); // blocks     │
  └──────────────────────────────────────┘
```

> **When to use which:** Use `Runnable` (or lambda) for fire-and-forget tasks. Use `Callable` when you need a return value. Avoid `extends Thread` in production code — it's only suitable for quick prototypes.

---

### 3. Synchronization

When multiple threads access shared data concurrently, you can run into **race conditions** — situations where the final result depends on the unpredictable order in which threads execute. Synchronization is the mechanism that ensures only one thread at a time can access a critical section of code.

> **Real-world analogy:** Imagine a shared bathroom with a lock on the door. Only one person can use it at a time. When someone enters, they lock the door (acquire the lock); others must wait outside (BLOCKED state). When the person leaves, they unlock the door (release the lock), and the next person in line enters.

```
  Without synchronization:              With synchronization:
  Thread A: read counter (0)            Thread A: lock → read(0) → write(1) → unlock
  Thread B: read counter (0)            Thread B: lock → read(1) → write(2) → unlock
  Thread A: write counter (1)           Final value: 2 ✓
  Thread B: write counter (1)
  Final value: 1 ✗ (lost update!)
```

- **`synchronized` keyword (intrinsic locks)** — Can be applied at method-level (locks on `this` or the `Class` object for static methods) or block-level (locks on any specified object). When a thread enters a `synchronized` block, it acquires the monitor lock; other threads trying to enter any `synchronized` block on the same object are blocked until the lock is released.
- **Visibility problem:** Without synchronization, a thread may cache variables in CPU registers or local cache and never see updates made by another thread. The `synchronized` keyword establishes a **happens-before** relationship, guaranteeing that changes made before releasing a lock are visible to the thread that next acquires the same lock.

**Gotchas:** Every object in Java has an intrinsic lock (monitor). `synchronized` is reentrant — if a thread already holds a lock, it can re-enter other `synchronized` blocks on the same object without deadlocking itself.

---

### 4. Volatile

The `volatile` keyword is a lighter-weight synchronization mechanism that deals specifically with **visibility** between threads, without the mutual exclusion provided by `synchronized`.

When a variable is declared `volatile`:
- **Every read** of that variable goes directly to main memory (not a CPU cache).
- **Every write** to that variable is immediately flushed to main memory.
- It establishes a **happens-before** relationship: everything that happened before a volatile write is visible to any thread that subsequently reads that volatile variable.

```
  Without volatile:                     With volatile:
  ┌──────────┐    ┌──────────┐         ┌──────────┐    ┌──────────┐
  │ Thread A │    │ Thread B │         │ Thread A │    │ Thread B │
  │ flag=true│    │ if(!flag)│         │ flag=true│───►│ if(!flag)│
  │ (cached) │    │ (cached) │         │ (main    │    │ (main    │
  │          │    │ loop     │         │  memory) │    │  memory) │
  │          │    │ forever! │         │          │    │ sees it!)│
  └──────────┘    └──────────┘         └──────────┘    └──────────┘
```

**Important limitations:**
- `volatile` does **NOT** guarantee atomicity. For example, `volatile int counter; counter++;` is NOT thread-safe because `counter++` is actually three operations: read, increment, write. Two threads can still interleave these steps.
- Use `volatile` for simple flags (e.g., a `boolean` stop signal) or for variables that are written by one thread and read by others. For anything involving compound operations (read-modify-write), use `AtomicInteger` or `synchronized`.

---

### 5. java.util.concurrent Executors

Creating a new `Thread` for every task is expensive: each thread consumes ~1MB of stack memory and requires OS-level scheduling. The **Executor framework** (introduced in Java 5) solves this by providing **thread pools** — a group of reusable threads that pick up tasks from a queue.

```
  ┌────────────────────────────────────────────────────────┐
  │                    ExecutorService                      │
  │                                                        │
  │   Task Queue                  Thread Pool              │
  │  ┌───┬───┬───┬───┐    ┌────────┐ ┌────────┐          │
  │  │ T1│ T2│ T3│...│───►│Thread-1│ │Thread-2│ ...      │
  │  └───┴───┴───┴───┘    └────────┘ └────────┘          │
  │                                                        │
  │  submit(task) ──► queue ──► idle thread picks it up    │
  └────────────────────────────────────────────────────────┘
```

- **`ExecutorService`** — The main interface for managing a pool of threads. You submit `Runnable` or `Callable` tasks, and the pool manages thread creation, reuse, and shutdown.
- **`ThreadPoolExecutor`** — The most configurable implementation. You can set the core pool size (threads kept alive even when idle), maximum pool size, keep-alive time, work queue type, and a rejection policy for when the queue is full.
- **`ScheduledExecutorService`** — Extends `ExecutorService` with the ability to schedule tasks to run after a delay or at fixed intervals. Replaces the old `Timer`/`TimerTask` classes.
- **`ForkJoinPool`** — A specialized pool designed for recursive, divide-and-conquer algorithms. Uses a **work-stealing** algorithm: idle threads steal tasks from busy threads' queues, maximizing CPU utilization. This is also the pool used by parallel streams and `CompletableFuture` by default.

> **Best practice:** Always shut down your executor when done (`executor.shutdown()`), or use try-with-resources (Java 19+). An un-shut-down executor will keep your application alive even after `main()` finishes.

---

### 6. Locks

While `synchronized` is convenient, the `java.util.concurrent.locks` package provides more flexible locking mechanisms for advanced scenarios.

| Lock | Features | Best For |
|---|---|---|
| `ReentrantLock` | Explicit lock/unlock, `tryLock()` with timeout, interruptible, optional fairness | General-purpose when you need more control than `synchronized` |
| `ReadWriteLock` | Multiple concurrent readers **OR** one exclusive writer | Read-heavy workloads (e.g., caches, configuration) |
| `StampedLock` | Optimistic reads (no locking!) + read/write locks | Highest performance for read-dominated scenarios |

**Why use explicit locks over `synchronized`?**
- **`tryLock()`** — Attempt to acquire a lock without blocking forever. You can specify a timeout, preventing deadlocks.
- **Fairness** — `synchronized` makes no guarantees about which waiting thread gets the lock next. `ReentrantLock` can be configured for fair ordering (FIFO).
- **Multiple conditions** — `ReentrantLock` supports multiple `Condition` objects, unlike `synchronized` which only has one wait set per object.
- **Interruptible** — A thread waiting for a `ReentrantLock` can be interrupted; a thread waiting for a `synchronized` block cannot.

```
  synchronized:                        ReentrantLock:
  synchronized(obj) {                  lock.lock();
      // critical section              try {
  }                                        // critical section
  // auto-release                      } finally {
                                           lock.unlock();  // MUST unlock in finally!
                                       }
```

> **Caution:** With explicit locks, you **must** unlock in a `finally` block. Forgetting to unlock causes threads to wait forever.

---

### 7. Atomic Classes

For simple thread-safe operations on single variables, Java provides atomic classes that use **Compare-And-Swap (CAS)** — a hardware-level instruction that is much faster than locking.

**How CAS works:**
```
  CAS(memory_location, expected_value, new_value)
  ┌─────────────────────────────────────────────┐
  │ 1. Read current value at memory location     │
  │ 2. If current == expected:                   │
  │      Write new_value → SUCCESS               │
  │ 3. If current != expected:                   │
  │      Another thread changed it → RETRY       │
  └─────────────────────────────────────────────┘
```

- **`AtomicInteger`, `AtomicLong`, `AtomicReference`** — Provide thread-safe `get()`, `set()`, `compareAndSet()`, `incrementAndGet()`, and other operations, all using CAS under the hood. No locks are involved, making them very fast for low-to-moderate contention.
- **`LongAdder`, `LongAccumulator`** — Designed for **high-contention** counters (e.g., request counters in a web server). Instead of all threads fighting over one value, each thread updates a separate internal cell, and values are summed on demand. This dramatically reduces contention compared to `AtomicLong`.

---

### 8. Concurrent Collections

Standard Java collections (`HashMap`, `ArrayList`) are **not thread-safe**. Using `Collections.synchronizedMap()` wraps every operation in a lock, which creates a bottleneck. The `java.util.concurrent` package provides collections specifically designed for concurrent access.

| Collection | Thread-Safety Approach | Best For |
|---|---|---|
| `ConcurrentHashMap` | Lock striping (locks individual buckets, not the entire map) | General-purpose concurrent map; high throughput |
| `CopyOnWriteArrayList` | Every write creates a new internal array; reads are lock-free | Read-heavy lists with rare writes (e.g., listener lists) |
| `LinkedBlockingQueue` | Lock-based blocking; `put()` blocks when full, `take()` blocks when empty | Producer-consumer pattern with unbounded queue |
| `ArrayBlockingQueue` | Bounded, lock-based; fixed capacity set at creation | Producer-consumer with backpressure |

```
  Producer-Consumer Pattern with BlockingQueue:
  ┌──────────┐     ┌─────────────────┐     ┌──────────┐
  │ Producer │────►│ BlockingQueue    │────►│ Consumer │
  │ thread   │ put │ ┌───┬───┬───┐   │take │ thread   │
  │          │     │ │ A │ B │ C │   │     │          │
  └──────────┘     │ └───┴───┴───┘   │     └──────────┘
                   │ blocks if full   │
                   │ blocks if empty  │
                   └─────────────────┘
```

---

### 9. Synchronizers

Synchronizers are coordination tools that help multiple threads work together in specific patterns. Think of them as traffic signals for threads.

- **`CountDownLatch`** — A one-time barrier initialized with a count N. Threads call `await()` to wait, and other threads call `countDown()` to decrement the count. When the count reaches zero, all waiting threads proceed. Cannot be reset.
  > *Analogy:* A race starting line — all runners wait until the referee counts down to zero.

- **`CyclicBarrier`** — Similar to `CountDownLatch`, but **reusable**. N threads call `await()`, and when all N have arrived, they all proceed simultaneously. The barrier then resets for the next round.
  > *Analogy:* A group of friends agreeing to meet at a point before continuing a hike together. They meet, continue, then meet again at the next checkpoint.

- **`Semaphore`** — Controls access to a limited number of resources. Initialized with N permits. Threads call `acquire()` to get a permit (blocking if none available) and `release()` to return one.
  > *Analogy:* A parking lot with N spaces. Cars wait at the entrance if full; when one leaves, the next car enters.

- **`Phaser`** — A flexible, reusable barrier that supports dynamic registration/deregistration of parties and multiple phases. More powerful but more complex than `CyclicBarrier`.

```
  CountDownLatch (count=3):            CyclicBarrier (parties=3):
  T1 ──► countDown()                   T1 ──► await() ──┐
  T2 ──► countDown()                   T2 ──► await() ──┼──► all proceed
  T3 ──► countDown()                   T3 ──► await() ──┘
         ↓ count=0                            (barrier resets)
  Waiter ──► proceeds
```

---

### 10. ThreadLocal

`ThreadLocal` provides **per-thread storage** — each thread accessing a `ThreadLocal` variable sees its own independent copy. This eliminates the need for synchronization because there is no shared state.

**Common use cases:**
- Storing user context (e.g., current user, transaction ID) in web applications, where each request is handled by a different thread.
- Storing `SimpleDateFormat` instances (which are not thread-safe) — each thread gets its own formatter.
- Database connection per thread.

```
  ┌────────────┐   ThreadLocal<User>   ┌────────────┐
  │  Thread-1  │ ──► value: Alice      │  Thread-2  │ ──► value: Bob
  └────────────┘                       └────────────┘
  Each thread sees only its own value — no synchronization needed!
```

**Pitfall:** When using thread pools, threads are **reused** across tasks. If you set a `ThreadLocal` value in one task, it will still be there when the same thread picks up the next task. This can cause data leaks between requests and memory leaks if `ThreadLocal` values are never removed. **Always call `threadLocal.remove()` when done** (e.g., in a `finally` block or servlet filter).

---

### 11. Deadlock, Livelock, Starvation

These are the three main liveness hazards in concurrent programming — situations where threads cannot make progress.

- **Deadlock:** Two or more threads are **permanently blocked**, each waiting to acquire a lock that the other holds. Neither can proceed because neither will release its lock.
  ```
    Thread A holds Lock 1, waiting for Lock 2
    Thread B holds Lock 2, waiting for Lock 1
    ──► Neither can proceed = DEADLOCK
  ```
  **Prevention:** Always acquire locks in a consistent, global order. If all threads acquire Lock 1 before Lock 2, deadlock is impossible.

- **Livelock:** Threads are **not blocked** — they are actively executing — but they keep changing state in response to each other without making any real progress. It's like two people meeting in a hallway and both stepping aside in the same direction, then the other direction, repeatedly.
  **Prevention:** Add randomized back-off delays so threads don't keep responding to each other in lockstep.

- **Starvation:** A thread is **perpetually denied** access to a resource because other threads always get priority. For example, with an unfair lock, a low-priority thread may never get to run.
  **Prevention:** Use fair locks (`new ReentrantLock(true)`) or ensure all threads have equal opportunity to acquire resources.

---

### 12. Virtual Threads (Java 21)

Virtual threads are lightweight threads managed by the JVM, not the OS. While a traditional platform thread maps 1:1 to an OS thread and consumes ~1MB of stack memory, a virtual thread is much cheaper (~few KB) and can be created by the millions. This makes the "one thread per task" model viable even for highly concurrent I/O-bound applications.

See **Module 03 — Java 18 to 24 Features** for full virtual thread coverage. This module contrasts virtual threads with platform threads to highlight when each is appropriate.

---

---

## Interview Questions

**Q1: What is the difference between `Thread.start()` and `Thread.run()`?**
`start()` creates a new OS thread and invokes `run()` in that new thread. Calling `run()` directly executes the method on the **current** thread — no new thread is created. This is a common bug that results in sequential execution instead of concurrency.

**Q2: Explain the difference between `Runnable` and `Callable`.**
`Runnable.run()` returns `void` and cannot throw checked exceptions. `Callable.call()` returns a value of type `V` and can throw checked exceptions. `Callable` is used with `ExecutorService.submit()`, which returns a `Future<V>` to retrieve the result.

**Q3: What is a race condition? Give an example.**
A race condition occurs when the correctness of a program depends on the relative timing of threads. Example: two threads incrementing a shared counter simultaneously — both read value 5, both write value 6, resulting in a lost update (expected 7, got 6).

**Q4: How does `synchronized` work internally in Java?**
Every Java object has an associated **monitor** (intrinsic lock). When a thread enters a `synchronized` block, it acquires the monitor; when it exits, it releases it. Other threads trying to enter a `synchronized` block on the same object are put in the BLOCKED state. The JVM uses a **monitor enter/exit** bytecode instruction pair. Modern JVMs optimize locks with biased locking (deprecated in Java 15), thin locks, and fat locks depending on contention.

**Q5: What is the difference between `synchronized` and `ReentrantLock`?**
| Feature | `synchronized` | `ReentrantLock` |
|---|---|---|
| Lock acquisition | Implicit (enter block) | Explicit (`lock()`) |
| Release | Automatic (exit block) | Manual (`unlock()` in `finally`) |
| `tryLock()` with timeout | No | Yes |
| Interruptible waiting | No | Yes (`lockInterruptibly()`) |
| Fairness option | No | Yes (FIFO ordering) |
| Multiple conditions | No (only one wait set) | Yes (`newCondition()`) |

**Q6: What is the `volatile` keyword and when should you use it?**
`volatile` guarantees **visibility** — writes to a volatile variable are immediately visible to all threads. It also prevents **instruction reordering** across the volatile access. Use it for simple flags (e.g., a `boolean` stop signal) where atomicity is not needed. Do NOT use it for compound operations like `count++` (use `AtomicInteger` instead).

**Q7: Explain happens-before relationship in Java Memory Model.**
The JMM defines a partial ordering of actions called happens-before. If action A happens-before action B, then the effects of A are guaranteed to be visible to B. Key rules: (1) Within a thread, each statement happens-before the next. (2) Unlocking a monitor happens-before subsequent locking of that monitor. (3) A volatile write happens-before subsequent volatile reads of that variable. (4) `Thread.start()` happens-before any action in the started thread. (5) All actions in a thread happen-before any thread that successfully `join()`s that thread.

**Q8: What is a deadlock? How do you prevent it?**
Deadlock occurs when two or more threads are permanently blocked, each waiting to acquire a lock held by the other. Four necessary conditions (Coffman conditions): (1) Mutual exclusion, (2) Hold and wait, (3) No preemption, (4) Circular wait. Prevention strategies: acquire locks in a consistent global order (breaks circular wait), use `tryLock()` with timeouts, avoid nested locks when possible, use lock-free algorithms.

**Q9: What is the difference between `wait()` and `sleep()`?**
| Aspect | `wait()` | `sleep()` |
|---|---|---|
| Belongs to | `Object` | `Thread` |
| Releases lock | Yes | No |
| Must be in `synchronized` | Yes | No |
| Wakes up by | `notify()`/`notifyAll()`/timeout | Timeout only |
| Purpose | Inter-thread communication | Pausing execution |

**Q10: What is the `ThreadLocal` class? What problem does it solve?**
`ThreadLocal` provides per-thread isolated storage. Each thread accessing a `ThreadLocal` variable gets its own independent copy, eliminating the need for synchronization. Common uses: storing user context in web apps, storing non-thread-safe objects like `SimpleDateFormat`. **Pitfall**: In thread pools, you must call `remove()` after use to prevent data/memory leaks.

**Q11: Explain the Executor framework. Why prefer it over manually creating threads?**
The Executor framework decouples task submission from execution mechanics. Benefits: (1) Thread reuse — creating OS threads is expensive (~1MB stack each); (2) Bounded thread pools prevent resource exhaustion; (3) Built-in task queuing; (4) Configurable rejection policies; (5) Scheduled execution support. Key implementations: `FixedThreadPool` (fixed workers), `CachedThreadPool` (unbounded, reuses idle threads), `ScheduledThreadPool` (delayed/periodic tasks), `ForkJoinPool` (work-stealing for recursive algorithms).

**Q12: What is the difference between `ConcurrentHashMap` and `Collections.synchronizedMap()`?**
`synchronizedMap` wraps every operation in a single lock on the entire map — only one thread can access the map at a time. `ConcurrentHashMap` uses **lock striping** (Java 7: segment locks; Java 8+: CAS + synchronized on individual bins), allowing multiple threads to read/write concurrently to different buckets. `ConcurrentHashMap` also provides atomic compound operations like `putIfAbsent()`, `computeIfAbsent()`, and `merge()`.

**Q13: Explain CAS (Compare-And-Swap). How do `AtomicInteger` and `LongAdder` use it?**
CAS is a hardware-level atomic instruction: `CAS(address, expected, new)` — if the value at `address` equals `expected`, set it to `new` and return true; otherwise return false. `AtomicInteger` uses CAS for operations like `incrementAndGet()` — it reads the current value, computes the new value, and attempts CAS in a retry loop. `LongAdder` reduces contention by maintaining multiple internal cells — each thread updates a different cell, and `sum()` adds them all up. `LongAdder` is better under high contention; `AtomicLong` is better for low contention and when you need an exact point-in-time value.

**Q14: What is the difference between `CountDownLatch` and `CyclicBarrier`?**
| Aspect | `CountDownLatch` | `CyclicBarrier` |
|---|---|---|
| Reusable | No (one-time use) | Yes (resets automatically) |
| Who waits | One or more threads call `await()` | All parties call `await()` |
| Who counts | Other threads call `countDown()` | The `await()` call itself |
| Optional action | None | Barrier action runs when all parties arrive |

**Q15: What is `ForkJoinPool` and how does work-stealing work?**
`ForkJoinPool` is designed for recursive divide-and-conquer parallelism. Each thread has a double-ended queue (deque). When a thread runs out of tasks, it steals tasks from the tail of another thread's deque (work-stealing). This keeps all threads busy without centralized coordination. Used by parallel streams and `CompletableFuture.supplyAsync()` by default.

**Q16: What is a `Semaphore`? How does it differ from a lock?**
A `Semaphore` controls access to N permits (not just 1 like a lock). `acquire()` blocks until a permit is available; `release()` returns a permit. A `Semaphore(1)` behaves like a mutex. Common use: limiting the number of concurrent connections to a database or API. Unlike locks, semaphores do not have an owner — any thread can release a permit, even one that didn't acquire it.

**Q17: What is `ReadWriteLock`? When is it useful?**
`ReadWriteLock` separates read and write locks. Multiple threads can hold the read lock simultaneously (since reads don't conflict), but the write lock is exclusive. This improves throughput for read-heavy workloads (e.g., caches). However, if writes are frequent, the overhead of managing two locks can outweigh the benefit.

**Q18: What is `StampedLock` and how does optimistic locking work?**
`StampedLock` adds an optimistic read mode to `ReadWriteLock`. In optimistic mode, you don't acquire a lock at all — you get a stamp, read the data, then validate the stamp. If no write occurred during your read, you're done (zero overhead). If a write did occur, you fall back to a pessimistic read lock. This is the fastest approach for read-dominated workloads.

**Q19: How do you detect and avoid thread starvation?**
Starvation occurs when a thread never gets CPU time or never acquires a needed lock. Detection: thread dumps showing a thread perpetually in BLOCKED/WAITING state. Prevention: use fair locks (`new ReentrantLock(true)`), avoid unbounded priority differences, use `ForkJoinPool` with work-stealing, set reasonable timeouts.

**Q20: What is the difference between livelock and deadlock?**
In a **deadlock**, threads are blocked and make no progress. In a **livelock**, threads are actively running but keep responding to each other's state changes without progressing (e.g., two threads repeatedly acquiring and releasing locks in a cycle). Livelock is harder to detect because threads appear active. Solution: add random back-off delays.

**Q21: What are daemon threads?**
Daemon threads are background service threads (e.g., GC thread). When all non-daemon threads finish, the JVM exits — it does not wait for daemon threads. Call `thread.setDaemon(true)` before `start()`. Use for background tasks that should not prevent application shutdown.

**Q22: Explain `Thread.interrupt()`. How does interruption work?**
`interrupt()` sets a thread's interrupt flag. If the thread is blocked in `sleep()`, `wait()`, or `join()`, it throws `InterruptedException` and clears the flag. If the thread is running, it must periodically check `Thread.interrupted()` or `isInterrupted()`. Best practice: when catching `InterruptedException`, either re-throw it or restore the interrupt flag with `Thread.currentThread().interrupt()`.

**Q23: What is the Producer-Consumer pattern? How do you implement it in Java?**
Producers generate data and put it into a shared buffer; consumers take data from the buffer and process it. Java's `BlockingQueue` is the ideal implementation: `put()` blocks when the queue is full (backpressure), and `take()` blocks when the queue is empty. `ArrayBlockingQueue` provides bounded capacity; `LinkedBlockingQueue` can be unbounded or bounded.

**Q24: What is `CopyOnWriteArrayList` and when should you use it?**
`CopyOnWriteArrayList` creates a full copy of the internal array on every write (add/set/remove). Reads are completely lock-free, making it ideal for scenarios with many reads and very few writes (e.g., event listener lists, configuration lists). Writes are expensive due to copying.

**Q25: How does `Phaser` differ from `CyclicBarrier`?**
`Phaser` is more flexible: it supports dynamic registration/deregistration of parties (threads can join or leave), multiple phases with phase numbers, and conditional termination. `CyclicBarrier` has a fixed number of parties and only supports one barrier that resets. Use `Phaser` for complex multi-phase parallel algorithms.

---

## Practice Problems

### Easy
1. **Thread Interleaving:** Create two threads that print numbers 1-10 and letters A-J respectively. Run them and observe the interleaving. Then use `join()` to make one wait for the other.
2. **Shared Counter:** Create a shared counter incremented by 10 threads (1000 increments each). Show the bug without synchronization, then fix it using: (a) `synchronized`, (b) `AtomicInteger`, (c) `ReentrantLock`.
3. **Daemon Thread:** Create a daemon thread that prints "tick" every second. Start it, sleep the main thread for 5 seconds, then exit. Observe that the daemon thread stops automatically.
4. **ThreadLocal Basics:** Create a `ThreadLocal<Integer>` and assign different values in 3 threads. Print the value from each thread to confirm isolation.

### Medium
5. **Producer-Consumer:** Implement a producer-consumer system using `ArrayBlockingQueue`. The producer generates random numbers; the consumer calculates running averages. Use a poison pill to signal shutdown.
6. **Read-Write Cache:** Build a simple cache using `ReadWriteLock`. Multiple reader threads query the cache concurrently; a single writer thread updates values periodically. Measure throughput.
7. **Dining Philosophers:** Implement the dining philosophers problem. First create a version that deadlocks, then fix it by ordering lock acquisition.
8. **CountDownLatch Service Startup:** Simulate a service that depends on 3 subsystems. Use `CountDownLatch` to block the main service until all subsystems report ready.
9. **Parallel Sum with ForkJoinPool:** Compute the sum of a large array using `RecursiveTask`. Split the array until chunks are small enough, then sum directly.
10. **Thread-Safe Singleton:** Implement a singleton using: (a) double-checked locking with `volatile`, (b) `enum`, (c) holder class pattern. Compare trade-offs.

### Hard
11. **Custom Thread Pool:** Implement a simplified thread pool with a `BlockingQueue<Runnable>` and N worker threads. Support `submit(Runnable)` and `shutdown()`.
12. **Rate Limiter:** Implement a simple rate limiter using `Semaphore` that allows at most N requests per second. Test with concurrent threads.
13. **CyclicBarrier Matrix Computation:** Divide a matrix into row partitions. Each thread processes its partition, then all threads synchronize at a `CyclicBarrier` before the next phase.
14. **StampedLock Point:** Implement a thread-safe 2D Point class using `StampedLock` with optimistic reads for `distanceFromOrigin()` and write locks for `move()`.

### Challenge
15. **Non-blocking Stack:** Implement a lock-free stack using `AtomicReference` and CAS. Handle the ABA problem using `AtomicStampedReference`.
16. **Deadlock Detector:** Write a program that creates a deliberate deadlock, then uses `ThreadMXBean.findDeadlockedThreads()` to detect and report it.

---

## Common Mistakes Cheat Sheet

| Mistake | Why It's Wrong | Fix |
|---|---|---|
| Calling `run()` instead of `start()` | Executes on current thread, no concurrency | Call `start()` to create a new thread |
| `volatile` for `count++` | `count++` is not atomic (read-modify-write) | Use `AtomicInteger.incrementAndGet()` |
| Not unlocking `ReentrantLock` | Other threads wait forever | Always `unlock()` in `finally` block |
| `synchronizedMap` + iterating without explicit lock | `ConcurrentModificationException` | Use `ConcurrentHashMap` or sync on the map during iteration |
| Swallowing `InterruptedException` | Interrupt flag is cleared, caller never knows | Re-throw or call `Thread.currentThread().interrupt()` |
| Forgetting `ThreadLocal.remove()` in thread pool | Data leaks between requests, memory leaks | Always `remove()` in `finally` |
| Using `notify()` instead of `notifyAll()` | May wake a thread that can't proceed | Prefer `notifyAll()` (or use `Condition`) |
| Busy-wait loop (`while (!flag) {}`) | Burns CPU cycles doing nothing | Use `wait()`/`notify()` or `BlockingQueue` |
| `Thread.stop()` / `Thread.suspend()` | Deprecated — can leave objects in inconsistent state | Use interruption flag pattern |
| Creating unlimited threads | OOM from ~1MB stack per thread | Use bounded thread pools |

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
| Producer-Consumer | `ProducerConsumerDemo.java` | `ProducerConsumerTest.java` |
| Practice Problems | `ThreadingPracticeProblems.java` | `ThreadingPracticeTest.java` |
