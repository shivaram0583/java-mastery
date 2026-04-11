# Module 01 — Java 8 Features

Java 8 (released March 2014) was the most transformative release in Java's history, introducing functional programming constructs to a traditionally object-oriented language.

> **Why Java 8 was a game-changer:** Before Java 8, Java was purely object-oriented. To pass behavior as an argument (e.g., a sorting strategy), you had to create an entire anonymous inner class with verbose syntax. Java 8 introduced **lambda expressions**, **streams**, and **functional interfaces** that brought functional programming to Java, making code more concise, readable, and expressive. Almost every modern Java codebase relies heavily on these features today.

---

## Topics Covered

### 1. Lambda Expressions

**What:** Lambda expressions are anonymous functions — short blocks of code that can be passed as arguments to methods, stored in variables, and returned from methods. They provide a compact way to represent a single method interface (functional interface) implementation.

**Why:** Before Java 8, every time you wanted to pass behavior (like a comparator, event handler, or callback), you had to write a verbose anonymous inner class. Lambdas eliminate this boilerplate and enable a functional programming style.

> **Real-world analogy:** Think of a lambda as giving someone verbal instructions rather than writing a formal instruction manual. Instead of creating a class, instantiating it, and passing it, you just say "do this."

**Syntax:** `(parameters) -> expression` or `(parameters) -> { statements; }`

```java
// Before Java 8 (anonymous inner class):
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});

// With lambda (Java 8):
Collections.sort(names, (a, b) -> a.compareTo(b));

// Even shorter with method reference:
Collections.sort(names, String::compareTo);
```

```
  Anonymous inner class:                Lambda expression:
  ┌────────────────────────┐       ┌────────────────────────┐
  │ new Comparator<>() {  │       │ (a, b) -> a.compareTo(b)│
  │   @Override           │       │                        │
  │   int compare(...) {  │       │  Same behavior,        │
  │     return ...;       │  ─►   │  1 line instead of 5   │
  │   }                   │       └────────────────────────┘
  │ }                     │
  └────────────────────────┘
```

**Real-world use cases:** Event handlers in UI frameworks, comparators for sorting, callbacks in async operations, filter/map/reduce operations in stream pipelines.

**Gotchas / Common Mistakes:**
- Lambdas can only capture **effectively final** local variables — variables that are not modified after initialization. This restriction exists because the lambda may execute later (possibly on a different thread), and modifying shared local variables would cause race conditions.
- `this` inside a lambda refers to the **enclosing class**, not the lambda itself (unlike anonymous inner classes where `this` refers to the inner class instance)
- **Method references** (`Class::method`) are preferred over lambdas when the lambda simply delegates to an existing method — they're more readable

---

### 2. Stream API

**What:** The Stream API provides a declarative, pipeline-based approach to process collections of data. A stream is a sequence of elements that supports sequential and parallel aggregate operations like filtering, mapping, and reducing.

**Why:** Before streams, processing a collection required verbose loops with mutable state. Streams let you express "what" you want to do (filter, transform, aggregate) rather than "how" to do it (loop counter, temporary variables, accumulator).

> **Real-world analogy:** Think of a stream as an assembly line in a factory. Raw materials (data) enter at one end and pass through a series of processing stations (operations). Each station transforms or filters the items, and the finished products come out at the other end. The factory doesn't process all items at once — items flow through one at a time (lazy evaluation).

```
  Collection ─► stream() ─► filter() ─► map() ─► sorted() ─► collect()
  [raw data]          [intermediate operations]        [terminal]
                      (lazy — nothing happens           (triggers
                       until terminal op)                execution)
```

**Key operations:**
- **Intermediate (lazy, return a new Stream):**
  - `filter(predicate)` — Keep elements matching a condition
  - `map(function)` — Transform each element
  - `flatMap(function)` — Transform and flatten (one-to-many mapping)
  - `sorted()` — Sort elements
  - `distinct()` — Remove duplicates
  - `peek(consumer)` — Perform a side-effect (useful for debugging)
  - `limit(n)` / `skip(n)` — Take first n / skip first n elements

- **Terminal (trigger execution, produce a result):**
  - `collect(collector)` — Gather results into a collection
  - `forEach(consumer)` — Perform an action on each element
  - `reduce(identity, accumulator)` — Combine all elements into one value
  - `count()` — Count elements
  - `findFirst()` / `findAny()` — Find an element
  - `anyMatch()` / `allMatch()` / `noneMatch()` — Test conditions

**Gotchas / Common Mistakes:**
- Streams are **lazy** — intermediate operations don't execute until a terminal operation is invoked. This is a feature, not a bug: it enables optimizations like short-circuiting.
- A stream can only be **consumed once**; reusing it throws `IllegalStateException`. If you need to process the same data twice, create two streams from the source.
- **Parallel streams** use `ForkJoinPool.commonPool()` — don't use for I/O-bound tasks (they block shared pool threads). Parallel streams are only beneficial for CPU-intensive operations on large datasets.
- `forEach` with parallel streams does not guarantee order; use `forEachOrdered` when order matters.

---

### 3. Optional

**What:** `Optional<T>` is a container object that may or may not contain a non-null value. It was introduced to provide a better alternative to returning `null` from methods, making the absence of a value explicit in the API.

**Why:** `NullPointerException` is one of the most common runtime errors in Java. When a method returns `null`, the caller often forgets to check for it. `Optional` forces the caller to explicitly handle the "no value" case, making the code's intent clear.

```
  Without Optional:                     With Optional:
  ┌────────────────────────┐       ┌────────────────────────┐
  │ User user = findUser();│       │ Optional<User> opt =   │
  │ // user might be null! │       │   findUser();           │
  │ user.getName();        │       │ opt.map(User::getName) │
  │ // NPE if null!        │       │    .orElse("Unknown"); │
  └────────────────────────┘       │ // safe! no NPE        │
                                   └────────────────────────┘
```

**Creating an Optional:**
- `Optional.of(value)` — Creates an Optional with a non-null value. Throws `NullPointerException` if value is null.
- `Optional.ofNullable(value)` — Creates an Optional that may be empty if value is null.
- `Optional.empty()` — Creates an empty Optional.

**Gotchas / Common Mistakes:**
- **Never** use `Optional` for fields, method parameters, or collections — it's designed only as a return type for methods that might not have a result
- Don't call `get()` without `isPresent()` — use `orElse()`, `orElseGet()`, or `orElseThrow()` instead for safe value extraction
- `orElse(fallback)` **always** evaluates its argument, even if the Optional has a value. Use `orElseGet(supplier)` for expensive fallback computations (it's lazy)

---

### 4. Default & Static Methods in Interfaces

**What:** Before Java 8, interfaces could only contain abstract method declarations — no implementations. Java 8 allows interfaces to have **default methods** (with a body) and **static methods**, enabling API evolution without breaking existing code.

**Why:** The problem was simple: if you added a new method to an interface, every single class implementing that interface would break because it didn't implement the new method. Default methods solve this by providing a fallback implementation that existing classes inherit automatically.

> **Real-world analogy:** Imagine a franchise restaurant (interface) that tells all franchisees (implementing classes) "you must serve burgers and fries." Now the headquarters wants to add "serve salads." Without default methods, every franchisee must immediately add salads to their menu or they're in violation. With default methods, headquarters provides a standard salad recipe (default implementation) that franchisees can use as-is or customize.

```java
public interface Collection<E> {
    // Existing abstract method:
    boolean add(E element);

    // New default method added in Java 8:
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
    // All existing Collection implementations automatically
    // get stream() without code changes!

    // Static utility method:
    static <T> Collection<T> emptyCollection() {
        return Collections.emptyList();
    }
}
```

**Gotchas / Common Mistakes:**
- **Diamond problem:** If a class implements two interfaces that both provide the same default method, the class **must** override the method to resolve the ambiguity.
- Default methods **cannot** override `Object` methods (`equals`, `hashCode`, `toString`) — the `Object` implementations always take precedence.

---

### 5. New Date/Time API (java.time)

**What:** Java 8 introduced a completely new date/time API in the `java.time` package, replacing the deeply flawed `java.util.Date` and `java.util.Calendar` classes.

**Why the old API was broken:**
- `Date` was **mutable** — you could accidentally change a date after creating it, causing bugs in multi-threaded code
- `Calendar` was **not thread-safe** and had unintuitive design (months started at 0, so January was month 0)
- `SimpleDateFormat` was **not thread-safe** — sharing one formatter across threads caused data corruption
- No separate types for dates vs. times vs. timestamps

The new API fixes all of these: everything is **immutable**, **thread-safe**, and **well-designed**.

**Key classes:**
```
  ┌───────────────┐  ┌──────────────┐  ┌──────────────────┐
  │  LocalDate     │  │  LocalTime    │  │  LocalDateTime    │
  │  2024-03-15    │  │  14:30:00     │  │  2024-03-15T14:30 │
  │  (date only,   │  │  (time only,  │  │  (date + time,    │
  │   no timezone) │  │   no timezone)│  │   no timezone)    │
  └───────────────┘  └──────────────┘  └──────────────────┘

  ┌──────────────────────┐  ┌──────────────────────┐
  │  ZonedDateTime        │  │  Instant              │
  │  2024-03-15T14:30     │  │  Epoch timestamp      │
  │  +05:30 [Asia/Kolkata]│  │  (machine time,       │
  │  (date+time+timezone) │  │   UTC, nanosecond     │
  │                      │  │   precision)           │
  └──────────────────────┘  └──────────────────────┘

  Duration — time-based amount (hours, minutes, seconds)
  Period   — date-based amount (years, months, days)
  DateTimeFormatter — thread-safe parsing/formatting
```

**Gotchas / Common Mistakes:**
- `LocalDateTime` has **no timezone** — don't use it for timestamps that need to be compared across timezones. Use `ZonedDateTime` or `Instant` for that.
- `Duration` is for time-based amounts (hours, minutes, seconds); `Period` is for date-based amounts (years, months, days). Don't mix them up.
- Always use `DateTimeFormatter` instead of `SimpleDateFormat` (which is not thread-safe and belongs to the old API).

---

### 6. Collectors

**What:** The `Collectors` utility class provides predefined implementations of the `Collector` interface for use with `Stream.collect()`. Collectors enable complex aggregation operations (grouping, partitioning, joining, summarizing) in a single pipeline pass.

**Why:** While `filter` and `map` transform individual elements, collectors let you aggregate the entire stream into structured results — like grouping employees by department, or creating a comma-separated string from a list.

**Key collectors explained:**
- **`toList()`** / **`toSet()`** — Collect elements into a List or Set
- **`toMap(keyFn, valueFn)`** — Create a Map from stream elements
- **`groupingBy(classifier)`** — Group elements by a key (like SQL `GROUP BY`)
- **`partitioningBy(predicate)`** — Split into two groups: true/false
- **`joining(delimiter)`** — Concatenate strings with a delimiter
- **`counting()`** — Count elements in each group
- **`summarizingInt(fn)`** — Compute count, sum, min, average, max in one pass

```
  employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,       // group key
        Collectors.averagingDouble(    // downstream: what to compute per group
            Employee::getSalary
        )
    ));

  Result: { "Engineering" -> 95000.0,
            "Marketing"   -> 85000.0,
            "Sales"       -> 72000.0 }
```

**Gotchas / Common Mistakes:**
- `toMap` throws `IllegalStateException` on **duplicate keys** unless you provide a merge function: `toMap(keyFn, valueFn, (v1, v2) -> v1)`
- `groupingBy` with a **downstream collector** enables multi-level grouping (e.g., group by department, then count per department)

---

### 7. Functional Interfaces: Predicate, Function, Consumer, Supplier

**What:** Functional interfaces are interfaces with a **single abstract method** (SAM), which makes them eligible as targets for lambda expressions. Java 8 provides four core functional interfaces in `java.util.function` that serve as building blocks for functional composition.

**Why:** These interfaces standardize common patterns — testing a condition, transforming a value, consuming a value, or producing a value — so you don't have to define custom interfaces for every lambda.

```
  ┌─────────────────┐   ┌─────────────────────────────────────────────┐
  │ Predicate<T>    │   │ T → boolean                                │
  │ test(T) → bool  │   │ "Does this element match?"                 │
  │                 │   │ e.g., x -> x > 5, s -> s.isEmpty()         │
  ├─────────────────┤   ├─────────────────────────────────────────────┤
  │ Function<T,R>   │   │ T → R                                      │
  │ apply(T) → R    │   │ "Transform this element"                   │
  │                 │   │ e.g., s -> s.length(), u -> u.getName()    │
  ├─────────────────┤   ├─────────────────────────────────────────────┤
  │ Consumer<T>     │   │ T → void                                   │
  │ accept(T)       │   │ "Use this element (side effect)"           │
  │                 │   │ e.g., System.out::println                  │
  ├─────────────────┤   ├─────────────────────────────────────────────┤
  │ Supplier<T>     │   │ () → T                                     │
  │ get() → T       │   │ "Produce a value"                          │
  │                 │   │ e.g., () -> new ArrayList<>(), Math::random│
  └─────────────────┘   └─────────────────────────────────────────────┘
```

**Composition methods** let you chain functional interfaces:
- `Predicate.and(other)`, `.or(other)`, `.negate()` — Combine conditions
- `Function.compose(before)` — Apply `before` first, then this function
- `Function.andThen(after)` — Apply this function first, then `after`
- `Consumer.andThen(after)` — Execute this consumer, then `after`

**Gotchas / Common Mistakes:**
- `Function.compose(g)` applies **g first**, then f. `andThen(g)` applies **f first**, then g. The naming can be confusing.
- The `@FunctionalInterface` annotation is **optional** but recommended — it provides a compile-time check that the interface has exactly one abstract method.

---

### 8. Nashorn JavaScript Engine (Removed)
**Note:** Nashorn was introduced in Java 8 as a replacement for Rhino, allowing JavaScript execution on the JVM. It was **deprecated in Java 11** and **removed in Java 15**. Modern alternatives include GraalVM's JavaScript engine. No runnable demo is provided since the API is no longer available.

---

## File Overview

| Topic | Demo Class | Test Class |
|---|---|---|
| Lambda Expressions | `LambdaDemo.java` | `LambdaTest.java` |
| Stream API | `StreamApiDemo.java` | `StreamApiTest.java` |
| Optional | `OptionalDemo.java` | `OptionalTest.java` |
| Default/Static Methods | `DefaultMethodsDemo.java` | `DefaultMethodsTest.java` |
| Date/Time API | `DateTimeApiDemo.java` | `DateTimeApiTest.java` |
| Collectors | `CollectorsDemo.java` | `CollectorsTest.java` |
| Functional Interfaces | `FunctionalInterfacesDemo.java` | `FunctionalInterfacesTest.java` |
