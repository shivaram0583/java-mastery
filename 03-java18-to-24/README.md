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
