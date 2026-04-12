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
| `AsyncPracticeProblems` | Practice implementations for interview prep |

## How to Run
```bash
# Run any demo
mvn -pl 06-completable-future exec:java -Dexec.mainClass="com.javamastery.async.CompletableFutureBasicsDemo"

# Run tests
mvn -pl 06-completable-future test
```

---

## Interview Questions

**Q1: What is the difference between `Future` and `CompletableFuture`?**
`Future` (Java 5) only supports blocking `get()` — there's no way to attach a callback, chain operations, or handle errors without blocking a thread. `CompletableFuture` (Java 8) adds: (1) non-blocking callbacks (`thenApply`, `thenAccept`), (2) chaining and composition (`thenCompose`, `thenCombine`), (3) error handling (`exceptionally`, `handle`), (4) combining multiple futures (`allOf`, `anyOf`), and (5) the ability to manually complete a future (`complete()`, `completeExceptionally()`).

**Q2: What is the difference between `thenApply()` and `thenCompose()`?**
`thenApply(fn)` is like `map` — it takes a function `T -> U` and returns `CompletableFuture<U>`. `thenCompose(fn)` is like `flatMap` — it takes a function `T -> CompletableFuture<U>` and flattens the result to `CompletableFuture<U>`. Without `thenCompose`, you'd get a nested `CompletableFuture<CompletableFuture<U>>`. Use `thenCompose` when the transformation itself is asynchronous (e.g., calling another async API with the result of the first).

**Q3: What thread executes the callback in `thenApply()` vs `thenApplyAsync()`?**
- `thenApply()`: runs on whichever thread completes the future, OR on the calling thread if the future is already complete when `thenApply` is registered.
- `thenApplyAsync()`: always runs on a thread from `ForkJoinPool.commonPool()`.
- `thenApplyAsync(fn, executor)`: always runs on a thread from the specified executor.
Use the async variant for I/O-bound or CPU-heavy callbacks to avoid blocking the completing thread.

**Q4: How does error handling work in CompletableFuture chains?**
When a stage throws an exception, all subsequent transformation stages (`thenApply`, `thenAccept`) are skipped. The exception propagates until it reaches an error handler: `exceptionally(fn)` catches the exception and provides a fallback value (like `catch`); `handle(bifn)` receives both result and exception (like `finally` with transformation); `whenComplete(biconsumer)` observes both but cannot change the result.

**Q5: What is the difference between `exceptionally()`, `handle()`, and `whenComplete()`?**
| Method | Receives | Can transform result? | Returns |
|---|---|---|---|
| `exceptionally(fn)` | Exception only | Yes (provides fallback) | `CF<T>` |
| `handle(bifn)` | Result + Exception | Yes | `CF<U>` |
| `whenComplete(biconsumer)` | Result + Exception | No (only observes) | `CF<T>` |

**Q6: How do `allOf()` and `anyOf()` work?**
`allOf(cf1, cf2, ...)` returns `CompletableFuture<Void>` that completes when ALL futures complete. It does not carry the results — you must call `get()` on individual futures. `anyOf(cf1, cf2, ...)` returns `CompletableFuture<Object>` that completes when the FIRST future completes. Remaining futures keep running (no cancellation). Use `allOf` for "wait for all" semantics; `anyOf` for "first response wins."

**Q7: What thread pool does `CompletableFuture` use by default?**
`supplyAsync()` and `runAsync()` (without an executor argument) use `ForkJoinPool.commonPool()`, which has `Runtime.getRuntime().availableProcessors() - 1` threads by default. For I/O-bound tasks, this is often insufficient — you should provide a custom `Executor` (e.g., `Executors.newFixedThreadPool(20)`) to avoid starving the common pool.

**Q8: How do you add a timeout to a CompletableFuture?**
Java 9 added `orTimeout(duration, unit)` — completes exceptionally with `TimeoutException` if not done in time. Also `completeOnTimeout(defaultValue, duration, unit)` — completes with a fallback value on timeout. Before Java 9: use `get(timeout, unit)` (blocking) or schedule a delayed task that completes the future exceptionally.

**Q9: How do you implement retry logic with CompletableFuture?**
Create a recursive method: attempt the async operation; if it fails, use `exceptionally()` or `handle()` to catch the error and call the method again with a decremented retry count. Add exponential backoff by scheduling retries with `CompletableFuture.delayedExecutor()` (Java 9) or `ScheduledExecutorService`.

**Q10: What is `thenCombine()` and when do you use it?**
`thenCombine(otherFuture, biFunction)` waits for BOTH the current future and `otherFuture` to complete, then combines their results using the biFunction. Both futures run in parallel. Use it when two independent async operations need to be merged (e.g., fetching user profile and user orders, then combining into a response).

**Q11: Can a CompletableFuture be completed manually? Why is this useful?**
Yes — `complete(value)` and `completeExceptionally(exception)` allow manual completion. This is useful for: (1) bridging callback-based APIs to CompletableFuture, (2) testing (providing predetermined results), (3) implementing custom async patterns (e.g., timeout fallbacks).

**Q12: What happens if you call `get()` on a CompletableFuture that has not completed?**
`get()` blocks the calling thread indefinitely until the future completes. `get(timeout, unit)` blocks for the specified duration and throws `TimeoutException` if not complete. Both throw `ExecutionException` if the future completed exceptionally, wrapping the original exception. Always prefer `join()` in non-interruptible contexts — it throws unchecked `CompletionException` instead of checked `ExecutionException`.

**Q13: What is the difference between `get()` and `join()`?**
| Method | Exception | Interrupted? |
|---|---|---|
| `get()` | Checked `ExecutionException` + `InterruptedException` | Yes |
| `join()` | Unchecked `CompletionException` | No |
| `get(timeout, unit)` | + `TimeoutException` | Yes |

Both block, but `join()` is cleaner in stream pipelines and lambdas where checked exceptions are inconvenient.

**Q14: How do you run multiple async tasks and collect all results?**
```java
List<CompletableFuture<String>> futures = urls.stream()
    .map(url -> CompletableFuture.supplyAsync(() -> fetch(url)))
    .toList();
CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
    .thenApply(v -> futures.stream()
        .map(CompletableFuture::join)
        .toList())
    .join();
```

**Q15: What are the pitfalls of using the common `ForkJoinPool` for I/O tasks?**
The common pool has limited threads (typically cores - 1). If all threads are blocked on I/O (HTTP calls, DB queries), the pool is saturated and no tasks can run — including parallel streams in other parts of the application. Always use a dedicated `Executor` for I/O-bound `CompletableFuture` operations.

---

## Practice Problems

### Easy
1. **Basic Pipeline:** Create a pipeline that: gets a user name asynchronously → converts to uppercase → adds a greeting prefix → prints the result.
2. **Fallback on Error:** Write a `CompletableFuture` that simulates an API call that fails. Use `exceptionally()` to provide a default value.
3. **Timing:** Measure how long three simulated API calls take when run sequentially vs. in parallel with `allOf`.

### Medium
4. **thenCombine:** Fetch user profile and user orders in parallel, then combine them into a single response object.
5. **thenCompose chain:** Simulate: authenticate user → fetch user profile → fetch user permissions. Each step depends on the previous result.
6. **Retry with backoff:** Implement a method that retries a failing async operation up to 3 times with 100ms delay between retries.
7. **Timeout handling:** Create an async operation that takes 2 seconds. Apply `orTimeout(1, SECONDS)` and handle the `TimeoutException`.
8. **Parallel batch processing:** Given a list of 10 IDs, fetch data for each in parallel, handle individual failures with fallbacks, and collect all results.

### Hard
9. **Custom async API bridge:** Create a callback-based method that simulates an external SDK, then wrap it with `CompletableFuture` using `complete()`.
10. **Pipeline with error recovery:** Build a 5-stage pipeline where stage 3 fails. Use `handle()` to recover and continue the pipeline.
11. **Fan-out/fan-in:** Given a search query, send it to 3 different "search engines" in parallel using `anyOf` for the fastest result, but also collect all results with `allOf` and merge them.

### Challenge
12. **Rate-limited parallel execution:** Process 100 items, but limit concurrency to 5 at a time using a `Semaphore` with `CompletableFuture`.

---

## Common Mistakes Cheat Sheet

| Mistake | Why It's Wrong | Fix |
|---|---|---|
| Using `get()` without timeout | Blocks thread indefinitely | Use `get(timeout, unit)` or `join()` |
| I/O tasks on common `ForkJoinPool` | Starves pool, blocks parallel streams | Provide custom `Executor` |
| `thenApply` when you need `thenCompose` | Results in nested `CF<CF<T>>` | Use `thenCompose` for async-returning functions |
| Ignoring exceptions in chains | Silent failures | Always add `exceptionally()` or `handle()` |
| Not storing the returned `CompletableFuture` | Callback may never run (GC'd) | Assign to variable, call `join()` at end |
| Calling `get()` in a callback | Deadlock if using same thread pool | Use `thenCompose` instead |
| Using `anyOf` expecting cancellation | Other futures keep running | Cancel manually or use structured concurrency |
| `allOf` then `get()` on individual futures | Still blocks | Use `thenApply(v -> futures.stream().map(CF::join).toList())` |
