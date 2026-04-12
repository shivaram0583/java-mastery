# Module 06 — CompletableFuture & Async Programming

## What is Asynchronous Programming?

In traditional **synchronous** programming, each operation blocks the current thread until it completes. If you need to call three external APIs, you call them one after another, waiting for each to finish before starting the next. This wastes time — while waiting for a network response, the thread sits idle.

**Asynchronous programming** lets you start an operation and move on to other work without waiting for it to finish. When the result is ready, a callback is invoked to handle it.

> **Real-world analogy:** Imagine ordering food at three different counters in a food court. **Synchronous:** you order at counter 1, wait for your food, then go to counter 2, wait, then counter 3. **Asynchronous:** you order at all three counters, take a seat, and each counter calls your name when your food is ready. You get all three meals much faster.

```
  Synchronous:                          Asynchronous:
  ┌────────────────────────┐       ┌────────────────────────┐
  │ API-1 call (███████)     │       │ API-1 (███████)       │
  │      wait...            │       │ API-2 (█████)         │
  │ API-2 call (█████)       │       │ API-3 (██████)        │
  │      wait...            │       │                        │
  │ API-3 call (██████)      │       │ Total: ~max(7,5,6)     │
  │      wait...            │       │       = 7 seconds       │
  │ Total: 7+5+6 = 18 sec  │       └────────────────────────┘
  └────────────────────────┘
```

---

## Why CompletableFuture?

`Future` (Java 5) was Java’s first attempt at representing an async result, but it only supports **blocking** `get()`. You had no way to attach a callback or chain operations without blocking a thread.

`CompletableFuture` (Java 8) is a major upgrade that adds:
- **Non-blocking callbacks** — `thenApply`, `thenAccept`, `thenRun` — execute automatically when the result is ready, without blocking any thread.
- **Chaining** — Compose multi-step async pipelines like functional streams. Each stage transforms the result and passes it to the next.
- **Error handling** — `exceptionally`, `handle`, `whenComplete` — gracefully recover from failures at any point in the chain.
- **Combining** — `thenCombine`, `allOf`, `anyOf` — coordinate multiple concurrent operations.

> Think of `CompletableFuture` as a **promise** (similar to JavaScript’s `Promise`) — it represents a value that will be available in the future and lets you describe what to do when it arrives.

---

## Key Concepts

### Creation Patterns
| Method | Description | When to Use |
|---|---|---|
| `supplyAsync(Supplier)` | Run async task that returns a value | Fetching data, calling APIs |
| `runAsync(Runnable)` | Run async task with no return value | Fire-and-forget: logging, notifications |
| `completedFuture(value)` | Already-completed future | Testing, returning cached values |
| `failedFuture(ex)` | Already-failed future (Java 9) | Testing error handling paths |

### Transformation Chain

The power of `CompletableFuture` is the ability to chain transformations. Each stage receives the output of the previous one:

```
  supplyAsync() ─► thenApply() ─► thenApply() ─► thenAccept()
  (produce       (transform    (transform    (consume
   a value)       the value)    again)        final value)

  Example: Fetch user ─► Extract name ─► Uppercase ─► Print
```

- **`thenApply(fn)`** — Transform the result (like `map` in streams). Returns `CompletableFuture<NewType>`.
- **`thenAccept(consumer)`** — Consume the result without returning anything. Returns `CompletableFuture<Void>`.
- **`thenRun(runnable)`** — Run an action after completion, ignoring the result. Returns `CompletableFuture<Void>`.

### Async vs Sync Variants

Every callback has three forms, controlling **which thread** runs the callback:

```
  thenApply(fn)               ─► Runs on the completing thread or the caller
  thenApplyAsync(fn)          ─► Runs on ForkJoinPool.commonPool()
  thenApplyAsync(fn, executor)─► Runs on your custom executor

  Rule of thumb:
  • Use sync version for lightweight transforms (string operations, mapping)
  • Use async version for I/O or heavy computation (prevents blocking the completing thread)
```

### Error Handling

Errors propagate through the chain just like exceptions in synchronous code. If any stage fails, subsequent `thenApply`/`thenAccept` stages are skipped until an error handler is reached.

```
  supplyAsync()          ────► Success path
    │
    ├─ thenApply(...)      ← SKIPPED if exception occurred above
    │
    ├─ exceptionally(ex → fallback)  ← Catches the exception, provides fallback
    │
    └─ thenAccept(...)     ← Runs with result OR fallback value
```

- **`exceptionally(fn)`** — Catches an exception and provides a fallback value. Like a `catch` block.
- **`handle(bifn)`** — Receives both the result and exception (one is always null). Like a `finally` that can transform the value.
- **`whenComplete(biconsumer)`** — Like `handle`, but cannot change the result — only observe it. Good for logging.

### Combining Futures

When you need to coordinate multiple async operations:

```
  thenCombine:                         thenCompose (flatMap):
  ┌──────────┐                          ┌──────────┐
  │ Future A │──────┐                   │ Future A │───► result A
  └──────────┘      ├─► combine(a,b)      └──────────┘       │
  ┌──────────┐      │                                      ▼
  │ Future B │──────┘                   ┌──────────┐
  └──────────┘                          │ Future B │ (uses result A)
                                        └──────────┘
  (parallel: both run at same time)     (sequential: B depends on A)
```

- **`thenCombine(otherFuture, biFunction)`** — Wait for **both** futures, then combine their results. Both futures run in parallel.
- **`thenCompose(fn)`** — FlatMap: use the result of one future to start another. Sequential dependency. Prevents `CompletableFuture<CompletableFuture<T>>` nesting.
- **`allOf(cf1, cf2, cf3)`** — Returns a `CompletableFuture<Void>` that completes when **ALL** given futures complete.
- **`anyOf(cf1, cf2, cf3)`** — Returns a `CompletableFuture<Object>` that completes when the **FIRST** future completes.

---

## Demos in This Module
| Class | Topics |
|---|---|
| `CompletableFutureBasicsDemo` | supplyAsync, runAsync, thenApply, thenAccept |
| `ChainingDemo` | thenCompose, multi-stage pipelines |
| `ErrorHandlingDemo` | exceptionally, handle, whenComplete |
| `CombiningFuturesDemo` | thenCombine, allOf, anyOf |
| `RealWorldPatternsDemo` | parallel API calls, timeout, retry |

## How to Run
```bash
# Run any demo
mvn -pl 06-completable-future exec:java -Dexec.mainClass="com.javamastery.async.CompletableFutureBasicsDemo"

# Run tests
mvn -pl 06-completable-future test
```
