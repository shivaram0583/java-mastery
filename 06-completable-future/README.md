# Module 06 — CompletableFuture & Async Programming

## Why CompletableFuture?
`Future` (Java 5) only supports blocking `get()`. `CompletableFuture` (Java 8) adds:
- **Non-blocking callbacks** — `thenApply`, `thenAccept`, `thenRun`
- **Chaining** — compose async pipelines like functional streams
- **Error handling** — `exceptionally`, `handle`, `whenComplete`
- **Combining** — `thenCombine`, `allOf`, `anyOf`

## Key Concepts

### Creation Patterns
| Method | Description |
|---|---|
| `supplyAsync(Supplier)` | Async with return value |
| `runAsync(Runnable)` | Async without return value |
| `completedFuture(value)` | Already-completed future |
| `failedFuture(ex)` | Already-failed future (Java 9) |

### Transformation Chain
```
supplyAsync → thenApply → thenApply → thenAccept
  (start)     (transform)  (transform)  (consume)
```

### Async vs Sync Variants
Every callback has three forms:
- `thenApply(fn)` — runs on completing thread or caller
- `thenApplyAsync(fn)` — runs on ForkJoinPool.commonPool()
- `thenApplyAsync(fn, executor)` — runs on custom executor

### Error Handling
```
supplyAsync()
  .thenApply(...)      ← skipped if exception
  .exceptionally(ex -> fallback)  ← handles exception
  .thenAccept(...)     ← runs with result or fallback
```

### Combining Futures
- `thenCombine(otherFuture, biFunction)` — combine two results
- `thenCompose(fn)` — flatMap (avoid nested CompletableFuture)
- `allOf(cf1, cf2, cf3)` — wait for ALL
- `anyOf(cf1, cf2, cf3)` — wait for FIRST

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
