# Module 01 — Java 8 Features

Java 8 (released March 2014) was the most transformative release in Java's history, introducing functional programming constructs to a traditionally object-oriented language.

---

## Topics Covered

### 1. Lambda Expressions
**What:** Anonymous functions that can be passed as arguments to methods.  
**Why:** Eliminates boilerplate of anonymous inner classes; enables functional programming style.  
**How:** `(parameters) -> expression` or `(parameters) -> { statements; }`

**Real-world use case:** Event handlers, comparators, callbacks, stream operations.

**Gotchas / Common Mistakes:**
- Lambdas can only capture effectively final local variables
- `this` inside a lambda refers to the enclosing class, not the lambda itself
- Method references (`Class::method`) are preferred over lambdas when they simply delegate

---

### 2. Stream API
**What:** A sequence of elements supporting sequential and parallel aggregate operations.  
**Why:** Declarative data processing; replaces verbose loops with readable pipelines.  
**How:** `collection.stream().filter(...).map(...).collect(...)`

**Key operations:**
- **Intermediate:** `map`, `filter`, `flatMap`, `sorted`, `distinct`, `peek`, `limit`, `skip`
- **Terminal:** `collect`, `forEach`, `reduce`, `count`, `findFirst`, `findAny`, `anyMatch`, `allMatch`

**Gotchas / Common Mistakes:**
- Streams are lazy — intermediate operations don't execute until a terminal operation is invoked
- A stream can only be consumed once; reusing throws `IllegalStateException`
- Parallel streams use ForkJoinPool.commonPool() — don't use for I/O-bound tasks
- `forEach` with parallel streams does not guarantee order; use `forEachOrdered`

---

### 3. Optional
**What:** A container that may or may not contain a non-null value.  
**Why:** Explicit handling of absent values; reduces `NullPointerException` risk.  
**How:** `Optional.of(value)`, `Optional.ofNullable(value)`, `Optional.empty()`

**Gotchas / Common Mistakes:**
- Never use `Optional` for fields, method parameters, or collections
- Don't call `get()` without `isPresent()` — use `orElse`, `orElseGet`, or `orElseThrow`
- `orElse()` always evaluates its argument; `orElseGet()` is lazy

---

### 4. Default & Static Methods in Interfaces
**What:** Interfaces can now have method implementations.  
**Why:** Enables API evolution without breaking existing implementations; utility methods in interfaces.  
**How:** `default void method() { }` and `static void method() { }`

**Gotchas / Common Mistakes:**
- Diamond problem: if two interfaces provide the same default method, the implementing class must override it
- Default methods cannot override `Object` methods (`equals`, `hashCode`, `toString`)

---

### 5. New Date/Time API (java.time)
**What:** Immutable, thread-safe date/time classes replacing `java.util.Date`/`Calendar`.  
**Why:** The old API was mutable, not thread-safe, poorly designed with months starting at 0.  
**How:** `LocalDate.now()`, `LocalDateTime.of(...)`, `ZonedDateTime.parse(...)`

**Key classes:** `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`, `Instant`, `Duration`, `Period`, `DateTimeFormatter`

**Gotchas / Common Mistakes:**
- `LocalDateTime` has no timezone — don't use it for timestamps across timezones
- `Duration` is for time-based amounts; `Period` is for date-based amounts
- Always use `DateTimeFormatter` instead of `SimpleDateFormat` (which is not thread-safe)

---

### 6. Collectors
**What:** Predefined implementations of `Collector` for use with `Stream.collect()`.  
**Why:** Complex aggregation (grouping, partitioning, joining) in a single pass.  
**How:** `stream.collect(Collectors.groupingBy(...))`

**Key collectors:** `toList`, `toSet`, `toMap`, `groupingBy`, `partitioningBy`, `joining`, `counting`, `summarizingInt`

**Gotchas / Common Mistakes:**
- `toMap` throws `IllegalStateException` on duplicate keys unless a merge function is provided
- `groupingBy` with a downstream collector enables multi-level grouping

---

### 7. Functional Interfaces: Predicate, Function, Consumer, Supplier
**What:** Interfaces with a single abstract method, usable as lambda targets.  
**Why:** Building blocks for functional composition.  
**How:** `Predicate.and()`, `Function.compose()`, `Consumer.andThen()`

**Gotchas / Common Mistakes:**
- `Function.compose(g)` applies g first, then f; `andThen(g)` applies f first, then g
- `@FunctionalInterface` annotation is optional but recommended for compile-time checks

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
