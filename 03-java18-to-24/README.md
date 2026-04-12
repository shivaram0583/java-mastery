# Module 03 — Java 18 to 24 Features

This module covers the latest Java features from Java 18 through Java 24, including virtual threads, record patterns, pattern matching for switch, and structured concurrency. These releases represent Java’s evolution toward simpler concurrency, more expressive pattern matching, and a lower barrier to entry for beginners.

---

## Java 18 (March 2022)

### UTF-8 by Default
**What:** UTF-8 is now the default charset for all Java SE APIs (file I/O, `System.out`, etc.).

**Why this matters:** Before Java 18, Java used the **operating system’s default charset**, which varied by platform. On Windows, this was often `windows-1252` (Western European), while Linux and macOS used UTF-8. This caused a frustrating class of bugs: code that worked perfectly on a developer’s Mac would produce garbled text on a Windows server, or vice versa. Making UTF-8 the universal default eliminates these cross-platform encoding surprises.

```
  Before Java 18:                       Java 18+:
  ┌──────────────┐  ┌──────────────┐    ┌──────────────┐
  │ macOS: UTF-8 │  │ Windows:     │    │ All platforms:│
  │ “Café” ✓     │  │ windows-1252 │    │ UTF-8         │
  │              │  │ “Café” → “CafÃ©”│    │ “Café” ✓      │
  └──────────────┘  └──────────────┘    └──────────────┘
```

### Simple Web Server
**What:** `jwebserver` is a command-line tool for serving static files over HTTP. It’s included in the JDK — no external dependencies needed.

**Usage:** `jwebserver -p 8080 -d /path/to/dir`

This is intentionally minimal — it’s meant for prototyping, testing, and learning, not for production. Think of it as Java’s equivalent of Python’s `python -m http.server`.

### Code Snippets in Javadoc
**What:** The new `@snippet` tag replaces the old `<pre>{@code ...}</pre>` pattern for including code examples in Javadoc. Snippets support syntax highlighting, regions, and can even reference external files so your documentation examples stay in sync with actual compilable code.

---

## Java 19–20 (Preview Features)

### Virtual Threads (Preview)
Lightweight threads managed by the JVM (Project Loom). These were previewed to gather feedback from the community before finalization. See Java 21 below for the full explanation.

### Structured Concurrency (Preview)
A new API for managing concurrent tasks as a unit with clear parent-child relationships. Previewed across multiple releases before finalization. See Java 21+ below for details.

### Record Patterns (Preview)
Extends pattern matching to destructure record components directly. Finalized in Java 21.

---

## Java 21 (September 2023) — LTS

Java 21 is a landmark Long-Term Support release. It finalizes several features that fundamentally change how Java handles concurrency and pattern matching.

### Virtual Threads (Finalized — JEP 444)

**What:** Virtual threads are lightweight threads (∼few KB of stack) managed entirely by the JVM, not the operating system. Traditional “platform” threads map 1:1 to OS threads and consume ∼1MB of stack memory each. Virtual threads break this constraint.

**Why this is revolutionary:** Before virtual threads, handling 10,000 concurrent I/O-bound tasks (like HTTP requests to a database) required either:
1. **10,000 OS threads** (∼10GB of memory just for stacks) — not feasible, or
2. **Reactive/async frameworks** (WebFlux, RxJava) that are complex and hard to debug

Virtual threads let you write simple, synchronous-looking code while achieving the scalability of async frameworks.

```
  Platform Threads:                      Virtual Threads:
  ┌────────────────────────┐       ┌────────────────────────┐
  │ OS Thread 1 (∼1MB)       │       │ Carrier Thread 1          │
  │ OS Thread 2 (∼1MB)       │       │   ├─ VThread A (∼KBs)      │
  │ OS Thread 3 (∼1MB)       │       │   ├─ VThread B (∼KBs)      │
  │ ...                      │       │   └─ VThread C (∼KBs)      │
  │ OS Thread 10000 (∼1MB)  │       │ Carrier Thread 2          │
  │                          │       │   ├─ VThread D              │
  │ Total: ∼10GB memory      │       │   └─ ... (millions!)        │
  │ OS scheduling overhead  │       │                          │
  └────────────────────────┘       │ Total: MBs of memory    │
                                       │ JVM scheduling (fast)   │
                                       └────────────────────────┘
```

**How to use:**
```java
// Create and start a virtual thread
Thread.ofVirtual().start(() -> {
    System.out.println("Running on: " + Thread.currentThread());
});

// Or use an executor that creates a new virtual thread for each task
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            // Each task gets its own virtual thread
            Thread.sleep(Duration.ofSeconds(1));
            return "done";
        });
    }
}  // Executor shuts down here, waits for all tasks
```

**Gotchas:**
- Virtual threads are optimized for **I/O-bound** tasks (waiting for network, database, files). For **CPU-bound** tasks, platform threads are still better.
- **`synchronized` blocks can pin** a virtual thread to its carrier platform thread, reducing concurrency. Prefer `ReentrantLock` instead of `synchronized` when using virtual threads.
- `ThreadLocal` works with virtual threads but can be **memory-heavy** when you have millions of them. Prefer `ScopedValue` (see below).

---

### Record Patterns (Finalized — JEP 440)
**What:** Record patterns let you **destructure** record components directly in pattern matching — extracting the fields in one step instead of matching the record type and then calling accessor methods.

```java
// Without record patterns (two steps):
if (obj instanceof Point p) {
    int x = p.x();      // step 2: extract
    int y = p.y();
    System.out.println(x + ", " + y);
}

// With record patterns (one step):
if (obj instanceof Point(int x, int y)) {
    System.out.println(x + ", " + y);  // x and y directly available
}
```

Record patterns can be **nested** for deep destructuring:
```java
// Destructure a Line that contains two Points
if (obj instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
    double length = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
}
```

---

### Pattern Matching for switch (Finalized — JEP 441)
**What:** Switch statements/expressions can now use **type patterns**, **guarded patterns** (with `when`), and **null handling** — making switch far more powerful than just matching constants.

```java
// Type patterns + guarded patterns + null
String describe(Object obj) {
    return switch (obj) {
        case null            -> "null value";
        case Integer i       -> "integer: " + i;
        case String s
            when s.length() > 5 -> "long string: " + s;
        case String s        -> "short string: " + s;
        case int[] arr       -> "array of length " + arr.length;
        default              -> "something else";
    };
}
```

Combined with **sealed classes**, pattern matching for switch becomes exhaustive — the compiler guarantees you’ve handled all possible subtypes, eliminating the need for a `default` case:

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double r) implements Shape {}
record Rectangle(double w, double h) implements Shape {}

double area(Shape shape) {
    return switch (shape) {
        case Circle(var r)        -> Math.PI * r * r;
        case Rectangle(var w, var h) -> w * h;
        // No default needed! Compiler knows these are ALL cases.
    };
}
```

---

### Sequenced Collections (JEP 431)
**What:** New interfaces — `SequencedCollection`, `SequencedSet`, `SequencedMap` — for collections that have a **defined encounter order** (first element, last element, reversed view).

**The problem they solve:** Before Java 21, getting the first or last element of different collection types required different code:
```
  Before:                               After (Java 21):
  List:   list.get(0)                   list.getFirst()
          list.get(list.size()-1)        list.getLast()
  Deque:  deque.getFirst()              deque.getFirst()   (same!)
          deque.getLast()                deque.getLast()     (same!)
  SortedSet: set.first()               set.getFirst()
             set.last()                 set.getLast()
```

**New methods:** `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`, `removeLast()`, `reversed()` — a uniform API across `List`, `Deque`, `SortedSet`, `LinkedHashSet`, etc.

---

### String Templates (Preview)
**What:** String interpolation — embed expressions directly in strings: `STR."Hello \{name}"`.

**Note:** This feature was previewed in Java 21 but was subsequently **removed/reworked** in Java 23. The design is still evolving, so it should not be relied upon in production code.

---

## Java 22–24

### Unnamed Variables and Patterns (JEP 456, Java 22)
**What:** Use `_` (underscore) as a placeholder for variables or pattern components that you don’t need. This makes code cleaner by explicitly signaling “I don’t care about this value.”

```java
// Unused variable in enhanced for loop
for (var _ : collection) {
    count++;  // we only care about the count, not the elements
}

// Unused pattern component in record destructuring
case Point(var x, _) -> "x is " + x;  // we don't need y

// Unused exception variable
try { ... }
catch (Exception _) { log("something failed"); }
```

---

### Unnamed Classes and Instance Main Methods (JEP 463, Java 22)
**What:** Simplified entry point for beginners. You can write a Java program without a class declaration, without `public static`, and without `String[] args`.

```java
// Before (intimidating for beginners):
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello!");
    }
}

// After (Java 22+):
void main() {
    println("Hello!");
}
```

**Why:** Java has always been criticized for requiring too much boilerplate for simple programs. This feature makes Java more accessible for education and scripting while keeping the full language available when needed.

---

### Scoped Values (Preview → progressing)
**What:** Scoped values are **immutable, inheritable** values designed for structured concurrency. They are the modern replacement for `ThreadLocal`.

**Why replace ThreadLocal?**
- `ThreadLocal` values are mutable and can be accidentally overwritten
- With virtual threads (millions of them), `ThreadLocal` allocates per-thread storage that wastes memory
- `ThreadLocal` has no clear lifecycle — values can leak between tasks in thread pools

**Scoped values** solve these problems by being:
- **Immutable** — set once, readable by the current thread and its child threads
- **Bounded** — automatically removed when the scope ends (no cleanup needed)
- **Efficient** — shared across virtual threads without per-thread copies

---

### Structured Concurrency (JEP 480, Java 24 final)
**What:** Structured concurrency treats a group of concurrent tasks as a **single unit of work** with a clear parent-child relationship. If one subtask fails, the others are automatically cancelled.

**The problem it solves:** In traditional concurrent code, managing multiple async tasks is error-prone:
```java
// Traditional approach (error-prone):
Future<User> userFuture = executor.submit(() -> fetchUser(id));
Future<Order> orderFuture = executor.submit(() -> fetchOrder(id));

User user = userFuture.get();    // What if this throws?
Order order = orderFuture.get(); // orderFuture keeps running unnecessarily!
```

If `fetchUser` fails, `fetchOrder` continues running pointlessly, wasting resources. With structured concurrency:

```
  Structured Concurrency:
  ┌───────────────────────────────────┐
  │  try (var scope = new           │
  │    StructuredTaskScope          │
  │      .ShutdownOnFailure()) {    │
  │                                 │
  │    fork ─► fetchUser()           │
  │    fork ─► fetchOrder()          │
  │                                 │
  │    scope.join();  // wait all   │
  │    scope.throwIfFailed();       │
  │                                 │
  │    If fetchUser() fails:        │
  │      → fetchOrder() cancelled   │
  │      → exception propagated    │
  │ }  // scope closes, all done    │
  └───────────────────────────────────┘
```

The key principle is that **the lifetime of concurrent tasks does not extend beyond the scope that created them** — just like how local variables don’t extend beyond their enclosing block. This makes concurrent code easier to reason about, debug, and maintain.

---

## File Overview

| Topic | Demo Class | Test Class |
|---|---|---|
| Virtual Threads | `VirtualThreadDemo.java` | `VirtualThreadTest.java` |
| Record Patterns | `RecordPatternsDemo.java` | `RecordPatternsTest.java` |
| Pattern Matching Switch | `PatternMatchingSwitchDemo.java` | `PatternMatchingSwitchTest.java` |
| Sequenced Collections | `SequencedCollectionsDemo.java` | `SequencedCollectionsTest.java` |
| Structured Concurrency | `StructuredConcurrencyDemo.java` | — |
| Unnamed Variables | `UnnamedVariablesDemo.java` | `UnnamedVariablesTest.java` |
| Practice Problems | `Java18To24PracticeProblems.java` | `Java18To24PracticeTest.java` |

---

## Interview Questions

### Virtual Threads

**Q1: What are virtual threads and how do they differ from platform threads?**

| Aspect | Platform Threads | Virtual Threads |
|--------|-----------------|----------------|
| Managed by | Operating System | JVM |
| Memory per thread | ~1MB stack | ~Few KB (grows as needed) |
| Max practical count | ~5,000–10,000 | Millions |
| Creation cost | Expensive (OS call) | Cheap (Java object) |
| Best for | CPU-bound tasks | I/O-bound tasks |
| Scheduling | OS scheduler | JVM scheduler (mounted on carrier threads) |

Virtual threads are lightweight threads that decouple the Java thread from the OS thread. When a virtual thread blocks on I/O, the JVM unmounts it from the carrier thread, allowing that carrier to run another virtual thread. This enables the "thread-per-request" model at scale.

**Q2: What is thread pinning? When does it happen with virtual threads?**

Thread pinning occurs when a virtual thread cannot be unmounted from its carrier thread, effectively tying up an OS thread. This happens in two scenarios:

1. **Inside a `synchronized` block/method** — The JVM cannot unmount because `synchronized` is tied to the OS thread's monitor. **Fix:** Use `ReentrantLock` instead.
2. **During a native method call (JNI)** — Native code runs on the OS thread and cannot be suspended.

Pinning reduces the benefit of virtual threads because a pinned virtual thread blocks its carrier, limiting concurrency.

```
  Normal (unmounting):                 Pinned (stuck):
  VThread blocks on I/O               VThread enters synchronized
  → JVM unmounts VThread               → Cannot unmount
  → Carrier picks up VThread2          → Carrier thread blocked
  → VThread resumed when I/O done     → Other VThreads must wait
```

**Q3: Should you pool virtual threads? Why or why not?**

**No.** Thread pooling was invented to amortize the cost of creating expensive OS threads. Virtual threads are cheap to create (~few microseconds, few KB). Pooling them would:
- Add unnecessary complexity
- Limit concurrency (the pool size becomes a bottleneck)
- Defeat the purpose of having millions of lightweight threads

Use `Executors.newVirtualThreadPerTaskExecutor()` which creates a new virtual thread for each task.

**Q4: What is `Executors.newVirtualThreadPerTaskExecutor()`?**

An executor that creates a new virtual thread for each submitted task. Unlike `newFixedThreadPool(n)`, there is no limit on concurrent threads. The executor is autocloseable — when closed, it waits for all submitted tasks to complete.

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 100_000).forEach(i ->
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return i;
        })
    );
}  // waits for all 100,000 tasks
```

**Q5: Can virtual threads be used with existing code?**

Yes, virtual threads are `Thread` objects — they work with all existing `Thread` APIs, `ExecutorService`, `synchronized` (with pinning caveat), `ThreadLocal` (with memory caveat), and `ReentrantLock`. This is a key design goal: migrate existing thread-per-request code to virtual threads with minimal changes.

**Q6: What are Scoped Values and why are they better than ThreadLocal for virtual threads?**

| Aspect | ThreadLocal | ScopedValue |
|--------|------------|-------------|
| Mutability | Mutable (set/get anytime) | Immutable once bound |
| Lifecycle | No clear end (must manually remove) | Automatically removed when scope ends |
| Memory | Per-thread storage (expensive with millions of VThreads) | Shared, inherited efficiently |
| Thread safety | Mutable → potential races | Immutable → inherently safe |
| Inheritance | InheritableThreadLocal (copies per child) | Efficiently inherited by child VThreads |

---

### Record Patterns

**Q7: What are record patterns? How do they differ from type patterns?**

- **Type pattern:** `case String s` — matches a type and binds the whole object
- **Record pattern:** `case Point(int x, int y)` — matches a record type AND destructures its components

Record patterns eliminate the need to call accessor methods after matching:
```java
// Type pattern (two steps):
if (obj instanceof Point p) { int x = p.x(); int y = p.y(); }

// Record pattern (one step):
if (obj instanceof Point(int x, int y)) { /* x and y directly available */ }
```

**Q8: Can record patterns be nested?**

Yes. Nested record patterns allow deep destructuring in a single pattern:
```java
record Point(int x, int y) {}
record Line(Point start, Point end) {}

if (obj instanceof Line(Point(var x1, var y1), Point(var x2, var y2))) {
    double length = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
}
```

---

### Pattern Matching for Switch

**Q9: What are guarded patterns (the `when` clause)?**

Guarded patterns add a boolean condition to a pattern using `when`:
```java
case String s when s.length() > 10 -> "long string"
case String s -> "short string"
```
Order matters: more specific guarded patterns must come before more general ones. The `when` clause is evaluated only if the pattern matches.

**Q10: How does null handling work in pattern matching switch?**

Before Java 21, passing `null` to a switch always threw `NullPointerException`. Now you can explicitly handle null:
```java
switch (obj) {
    case null -> "it's null";
    case String s -> "it's a string";
    default -> "something else";
}
```
If you don't include a `case null`, the old behavior (NPE) is preserved for backward compatibility.

**Q11: How do sealed classes interact with pattern matching switch for exhaustiveness?**

When switching over a sealed type, the compiler knows all permitted subtypes. If your switch cases cover all subtypes, no `default` is needed. If you later add a new subtype to the sealed hierarchy, the compiler forces you to handle it everywhere — catching potential bugs at compile time.

---

### Sequenced Collections

**Q12: What problem do Sequenced Collections solve?**

Before Java 21, there was no common interface for collections with a defined encounter order. Getting the first/last element required type-specific code:
- `List`: `list.get(0)` / `list.get(list.size()-1)`
- `Deque`: `deque.getFirst()` / `deque.getLast()`
- `SortedSet`: `set.first()` / `set.last()`

`SequencedCollection` provides `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `removeFirst()`, `removeLast()`, and `reversed()` across all ordered collections.

**Q13: What is the `reversed()` method and does it create a copy?**

`reversed()` returns a **reversed view** of the collection — NOT a copy. Changes to the original collection are reflected in the reversed view and vice versa. It's O(1) because it just wraps the original collection with reversed indexing.

---

### Structured Concurrency

**Q14: What is structured concurrency and what problem does it solve?**

Structured concurrency ensures that the lifetime of concurrent tasks does not extend beyond the scope that created them. Problems it solves:
- **Resource leaks:** Threads continuing to run after the parent scope exits
- **Orphaned tasks:** If task A fails, task B continues running wastefully
- **Error propagation:** Difficult to propagate exceptions from subtasks to the parent

With structured concurrency, all subtasks must complete (or be cancelled) before the scope closes — like how local variables can't escape their enclosing block.

**Q15: What are the shutdown policies in StructuredTaskScope?**

- **`ShutdownOnFailure`** — If any subtask fails, cancel all remaining subtasks. Useful when all subtasks must succeed (e.g., fetch user AND order data).
- **`ShutdownOnSuccess`** — If any subtask succeeds, cancel remaining subtasks. Useful for racing (e.g., query primary AND replica database, take whichever responds first).

**Q16: How does structured concurrency differ from CompletableFuture?**

| Aspect | CompletableFuture | Structured Concurrency |
|--------|-------------------|----------------------|
| Task lifetime | Unscoped — can outlive creator | Bounded — must complete within scope |
| Cancellation | Manual (must track and cancel each) | Automatic (scope cancels all on failure) |
| Error handling | Chain-based (exceptionally, handle) | Scope-based (throwIfFailed) |
| Thread dump visibility | Tasks not linked to parent | Clear parent-child relationship |
| Style | Reactive/callback | Imperative/synchronous-looking |

---

### Unnamed Variables

**Q17: What are unnamed variables and when should you use them?**

Unnamed variables (`_`) explicitly signal that a value is intentionally unused. Use them for:
- Unused loop variables: `for (var _ : collection) { count++; }`
- Unused catch parameters: `catch (Exception _) { logGenericError(); }`
- Unused pattern components: `case Point(var x, _) -> processX(x);`
- Unused lambda parameters: `map.forEach((_, value) -> process(value));`

They improve readability by making the developer's intent clear and eliminate IDE warnings about unused variables.

---

## Practice Problems

### Easy
1. **Virtual Thread Creation:** Create a virtual thread using three different approaches: `Thread.ofVirtual().start()`, `Thread.startVirtualThread()`, and `Executors.newVirtualThreadPerTaskExecutor()`.
2. **Sequenced Collections:** Create a `LinkedHashMap`, add elements, then use `getFirst()`, `getLast()`, and iterate using `reversed()`.
3. **Unnamed Variables:** Refactor code with unused variables to use `_` where appropriate.

### Medium
4. **Virtual Thread HTTP Server:** Simulate a simple HTTP server that handles each "request" on a virtual thread. Demonstrate handling >10,000 concurrent requests.
5. **Record Pattern Destructuring:** Create nested records representing a company structure (Company → Department → Employee) and use nested record patterns to extract data.
6. **Pattern Matching Switch Calculator:** Build a calculator that accepts different expression types (sealed hierarchy) and evaluates them using pattern matching switch with guarded patterns.
7. **Sequenced Collections Utilities:** Write utility methods that work with `SequencedCollection` to rotate elements, swap first/last, and find the middle element.

### Hard
8. **Virtual Thread Performance Comparison:** Write a benchmark comparing virtual threads vs platform threads for:
   - I/O-bound tasks (simulated with Thread.sleep)
   - CPU-bound tasks (prime number calculation)
   - Show when each is better
9. **Structured Concurrency Patterns:** Implement a service that uses structured concurrency to:
   - Fetch data from 3 sources concurrently
   - Retry failed sources up to 3 times
   - Timeout the entire operation after 5 seconds
   - Use ShutdownOnFailure and ShutdownOnSuccess for different use cases
10. **Pattern Matching State Machine:** Model a state machine using sealed interfaces and pattern matching, where transitions are validated at compile time.

### Challenge
11. **Web Scraper with Virtual Threads:** Build a concurrent web scraper that processes URLs using virtual threads, with structured concurrency for error handling and a bounded semaphore for rate limiting.
12. **Event Sourcing with Sealed Types:** Model a complete event-sourcing system using sealed interfaces for events, record patterns for handlers, and structured concurrency for projections.

---

## Common Mistakes Cheat Sheet

| Mistake | Problem | Fix |
|---------|---------|-----|
| Pooling virtual threads | Limits concurrency, adds overhead | Use `newVirtualThreadPerTaskExecutor()` |
| `synchronized` with virtual threads | Pins virtual thread to carrier | Use `ReentrantLock` |
| `ThreadLocal` with virtual threads | Huge memory with millions of VThreads | Use `ScopedValue` |
| Virtual threads for CPU-bound work | No benefit; overhead may hurt | Use platform threads for CPU work |
| Missing `case null` in pattern switch | NPE when null passed | Add explicit `case null` handling |
| Non-exhaustive sealed switch | Compile error when new subtype added | Handle all subtypes or use `default` |
| Mutating `reversed()` view unexpectedly | Changes affect original collection | Document that `reversed()` is a view |
