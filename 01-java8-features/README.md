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

### 8. Method References

**What:** Method references are a shorthand notation for lambdas that simply call an existing method. Instead of writing `x -> x.toString()`, you write `Object::toString`. They make code more readable by referring directly to the method by name.

**Four types of method references:**

```
  ┌────────────────────────────────────────────────────────────────────────────┐
  │  Type                │ Syntax              │ Lambda Equivalent            │
  ├──────────────────────┼─────────────────────┼──────────────────────────────┤
  │ Static method        │ Class::staticMethod │ (args) -> Class.staticMethod │
  │                      │ Integer::parseInt   │ s -> Integer.parseInt(s)     │
  ├──────────────────────┼─────────────────────┼──────────────────────────────┤
  │ Instance method      │ obj::instanceMethod │ (args) -> obj.method(args)   │
  │ (bound)              │ System.out::println │ x -> System.out.println(x)   │
  ├──────────────────────┼─────────────────────┼──────────────────────────────┤
  │ Instance method      │ Class::instanceMeth │ (obj,args) -> obj.method()   │
  │ (unbound/arbitrary)  │ String::length      │ s -> s.length()              │
  ├──────────────────────┼─────────────────────┼──────────────────────────────┤
  │ Constructor          │ Class::new          │ (args) -> new Class(args)    │
  │                      │ ArrayList::new      │ () -> new ArrayList<>()      │
  └────────────────────────────────────────────────────────────────────────────┘
```

**When to prefer method references:**
- When the lambda body is just a single method call with no additional logic
- Method references are generally more readable for simple operations
- The compiler can sometimes optimize method references better than lambdas

**When to stick with lambdas:**
- When you need to add logic (e.g., `x -> x.length() > 5`)
- When the method reference would be ambiguous or less readable
- When you need to adapt parameters (reorder, combine, etc.)

---

### 9. Effectively Final Variables in Lambdas

**What:** A lambda expression can access local variables from its enclosing scope, but only if those variables are **effectively final** — meaning they are never modified after initialization (even if not declared `final`).

```java
String prefix = "Hello";   // effectively final — never reassigned
// prefix = "Hi";          // un-commenting this would make it NOT effectively final

Consumer<String> greeter = name -> System.out.println(prefix + " " + name);
greeter.accept("Alice");   // prints "Hello Alice"
```

**Why this restriction exists:** Lambdas can outlive the method that created them (e.g., stored in a field, passed to another thread). Local variables live on the stack and are destroyed when the method returns. The lambda captures a *copy* of the variable's value. If the variable could change after capture, the copy and original would diverge, causing confusing bugs.

**Workaround for mutable state:** If you need to accumulate results inside a lambda, use:
- An `AtomicInteger` / `AtomicReference` (for simple cases)
- An array or collection (mutating the contents, not the reference)
- Stream's `reduce()` or `collect()` (the recommended functional approach)

---

## Internal Workings: How Lambdas Are Implemented

Understanding how lambdas work under the hood is valuable for interviews and performance tuning.

**Key insight:** Lambdas in Java are NOT anonymous inner classes. They use `invokedynamic` (a JVM bytecode instruction introduced in Java 7) which defers the creation strategy to runtime.

```
  Source code:                          Bytecode generated:
  ┌──────────────────────┐             ┌──────────────────────────────┐
  │ Runnable r =         │ ──javac──► │ invokedynamic                │
  │   () -> print("hi"); │             │   bootstrap: LambdaMetafact │
  └──────────────────────┘             └──────────────────────────────┘
                                               │
                                               ▼ (at first invocation)
                                       ┌──────────────────────────────┐
                                       │ LambdaMetafactory generates  │
                                       │ a lightweight class (no .class│
                                       │ file on disk) that implements │
                                       │ the functional interface     │
                                       └──────────────────────────────┘
```

**Performance advantage over anonymous inner classes:**
1. No `.class` file generated per lambda (anonymous classes create one)
2. No object creation overhead in many cases (JVM can optimize to singleton)
3. The JVM can choose the best implementation strategy at runtime
4. Better inlining by the JIT compiler

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
| Method References | `MethodReferencesDemo.java` | `MethodReferencesTest.java` |
| Practice Problems | `Java8PracticeProblems.java` | `Java8PracticeTest.java` |

---

## Interview Questions

### Lambda & Functional Interfaces

**Q1: What is a lambda expression in Java 8? How does it differ from an anonymous inner class?**

A lambda is a concise way to represent a single-method interface implementation. Key differences:
- **Syntax:** Lambdas are shorter (`(a, b) -> a + b` vs multi-line anonymous class)
- **`this` keyword:** In a lambda, `this` refers to the enclosing class. In an anonymous inner class, `this` refers to the anonymous class itself
- **Implementation:** Lambdas use `invokedynamic` bytecode; anonymous classes generate a separate `.class` file
- **Scope:** Lambdas do not introduce a new scope for variable names; anonymous classes do
- **Performance:** Lambdas are generally more efficient due to JVM optimizations

**Q2: What is a functional interface? Can it have more than one method?**

A functional interface has exactly **one abstract method** (SAM — Single Abstract Method). However, it CAN have:
- Multiple `default` methods (with implementations)
- Multiple `static` methods
- Methods inherited from `Object` (`equals`, `hashCode`, `toString`)

Examples: `Runnable` (one `run()`), `Comparator` (one `compare()`, plus `default reversed()`, etc.)

**Q3: What is the difference between `Predicate`, `Function`, `Consumer`, and `Supplier`?**

| Interface | Input | Output | Method | Purpose |
|-----------|-------|--------|--------|---------|
| `Predicate<T>` | T | boolean | `test(T)` | Test a condition |
| `Function<T,R>` | T | R | `apply(T)` | Transform a value |
| `Consumer<T>` | T | void | `accept(T)` | Consume (side effect) |
| `Supplier<T>` | none | T | `get()` | Produce/create a value |

**Q4: What does "effectively final" mean? Why is it required for lambdas?**

A variable is "effectively final" if it is never reassigned after initialization. Lambdas capture a *copy* of local variables. If the variable could change, the copy and original would diverge, leading to confusing behavior — especially when the lambda executes on a different thread.

**Q5: Explain the four types of method references with examples.**

1. **Static:** `Integer::parseInt` → `s -> Integer.parseInt(s)`
2. **Bound instance:** `System.out::println` → `x -> System.out.println(x)`
3. **Unbound instance:** `String::toUpperCase` → `s -> s.toUpperCase()`
4. **Constructor:** `ArrayList::new` → `() -> new ArrayList<>()`

**Q6: Can a lambda expression throw a checked exception?**

Only if the target functional interface's abstract method declares the exception. Standard functional interfaces (`Predicate`, `Function`, etc.) do NOT declare checked exceptions. To handle checked exceptions in lambdas, you must either:
- Catch the exception inside the lambda and wrap it in an unchecked exception
- Create a custom functional interface that declares the exception
- Use a utility wrapper method

**Q7: What is the difference between `Function.compose()` and `Function.andThen()`?**

Both chain two functions, but in different order:
- `f.compose(g)` → applies `g` first, then `f`: `f(g(x))`
- `f.andThen(g)` → applies `f` first, then `g`: `g(f(x))`

---

### Stream API

**Q8: What is the difference between intermediate and terminal operations?**

| Aspect | Intermediate | Terminal |
|--------|-------------|---------|
| Returns | Another Stream | A non-Stream result (or void) |
| Execution | **Lazy** — nothing happens | **Triggers** pipeline execution |
| Examples | filter, map, sorted, distinct | collect, forEach, reduce, count |
| Chaining | Can chain multiple | Only one per pipeline |

**Q9: What is lazy evaluation in streams? Why is it important?**

Intermediate operations don't execute until a terminal operation is invoked. This enables:
- **Short-circuiting:** `findFirst()` stops processing after the first match
- **Fusion:** Multiple operations can be combined into a single pass
- **Avoiding unnecessary work:** If a terminal operation only needs 3 elements, only 3 elements pass through the pipeline

**Q10: What is the difference between `map()` and `flatMap()`?**

- `map(f)` applies function f to each element, producing a 1:1 mapping: `Stream<T>` → `Stream<R>`
- `flatMap(f)` applies function f that returns a Stream for each element, then flattens all resulting streams into one: `Stream<T>` → `Stream<R>` (one-to-many mapping)

```java
// map: ["Hello", "World"] → ["Hello", "World"] (1:1)
list.stream().map(String::toUpperCase)

// flatMap: ["Hello World", "Foo Bar"] → ["Hello", "World", "Foo", "Bar"] (1:many)
list.stream().flatMap(s -> Arrays.stream(s.split(" ")))
```

**Q11: Can you reuse a stream? What happens if you try?**

No. A stream can only be consumed once. Calling a terminal operation "closes" the stream. Any subsequent operation throws `IllegalStateException`. To process the same data again, create a new stream from the source.

**Q12: What is the difference between `findFirst()` and `findAny()`?**

- `findFirst()` — Always returns the first element in encounter order. In parallel streams, this may require synchronization.
- `findAny()` — Returns any element, allowing better performance in parallel streams since it doesn't enforce ordering.

**Q13: Explain `reduce()` with an example. What is the identity value?**

`reduce` combines all elements into a single result using an associative accumulator function.
```java
int sum = numbers.stream().reduce(0, Integer::sum);
// 0 is the identity (starting value)
// Integer::sum is the accumulator
// For an empty stream, reduce returns the identity value (0)
```
The identity value must satisfy: `accumulator.apply(identity, x) == x` for all x.

**Q14: When should you use parallel streams? What are the pitfalls?**

**Use when:**
- Data set is large (>10,000 elements)
- Operations are CPU-bound and stateless
- Source supports efficient splitting (ArrayList, arrays — NOT LinkedList)

**Avoid when:**
- Operations involve I/O (network, file) — blocks shared ForkJoinPool threads
- Order matters and using `forEach` (use `forEachOrdered` instead)
- Shared mutable state is involved (race conditions)
- Data set is small (parallelization overhead exceeds benefit)

**Q15: What is the difference between `Stream.of()` and `Arrays.stream()`?**

- `Stream.of(1, 2, 3)` — Creates a stream from var-args or a single element
- `Arrays.stream(arr)` — Creates a stream from an array. For primitive arrays, returns `IntStream`/`LongStream`/`DoubleStream` (no boxing overhead)
- `Stream.of(intArray)` on a primitive array creates `Stream<int[]>` (one element) — a common bug!

**Q16: How does `Collectors.groupingBy` work? Can you nest collectors?**

`groupingBy(classifier)` groups stream elements by a key function, producing a `Map<K, List<V>>`. You can nest downstream collectors:
```java
// Group by department, then count employees per department
employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,    // classifier
        Collectors.counting()       // downstream collector
    ));
// Result: {Engineering=5, Sales=3, Marketing=2}
```
You can nest as deep as needed: `groupingBy(dept, groupingBy(level, counting()))`.

**Q17: What is the difference between `Collection.stream()` and `Collection.parallelStream()`?**

`stream()` creates a sequential stream processed on the calling thread. `parallelStream()` creates a parallel stream that splits the workload across threads in `ForkJoinPool.commonPool()`. You can also convert: `stream.parallel()` and `parallelStream().sequential()`.

---

### Optional

**Q18: What is Optional and why was it introduced?**

`Optional<T>` is a container that explicitly represents the absence of a value instead of using `null`. It was introduced to:
- Reduce `NullPointerException` by making the absence explicit
- Force callers to handle the "no value" case
- Provide a fluent API for value transformation and fallback

**Q19: What is the difference between `orElse()` and `orElseGet()`?**

- `orElse(value)` — **Always** evaluates its argument, even if the Optional has a value. If the fallback is expensive (e.g., a database call), this wastes resources.
- `orElseGet(supplier)` — **Lazily** evaluates the supplier only if the Optional is empty. Use this for expensive fallback operations.

```java
// BAD: createDefaultUser() is called EVEN WHEN user exists
Optional<User> opt = findUser(id);
User user = opt.orElse(createDefaultUser());      // always called!

// GOOD: createDefaultUser() is called only when needed
User user = opt.orElseGet(() -> createDefaultUser()); // lazy
```

**Q20: Should you use Optional for method parameters or fields?**

**No.** Optional was designed only as a **return type** for methods that might not return a value. Using it for fields or method parameters:
- Adds unnecessary wrapping overhead
- Makes serialization problematic (Optional is not `Serializable`)
- Violates the API design intent stated by the Optional's creator (Brian Goetz)

**Q21: How do you convert a stream of Optionals to a stream of present values?**

```java
// Java 8:
stream.filter(Optional::isPresent).map(Optional::get)

// Java 9+:
stream.flatMap(Optional::stream)  // much cleaner!
```

---

### Date/Time API

**Q22: Why was a new Date/Time API introduced in Java 8?**

The old `java.util.Date` and `Calendar` had serious flaws:
- **Mutable** — Date objects could be changed after creation (thread-unsafe)
- **Confusing API** — Months were 0-indexed (January = 0), years offset from 1900
- **No timezone separation** — `Date` mixed date, time, and timezone representation
- **Thread-unsafe formatting** — `SimpleDateFormat` was not thread-safe
- **No duration/period concept** — No built-in way to represent "3 hours" or "2 months"

The new `java.time` API (based on Joda-Time) fixes all of these with immutable, thread-safe classes.

**Q23: What is the difference between `LocalDateTime` and `ZonedDateTime`?**

- `LocalDateTime` — Date + time WITHOUT timezone information. Use for local events (meetings, birthdays) where timezone doesn't matter.
- `ZonedDateTime` — Date + time + timezone. Use for timestamps that need to be compared across regions (flight schedules, API timestamps, log entries).

Rule of thumb: If the event happens at the same **wall-clock time** everywhere, use `LocalDateTime`. If it happens at the same **instant** everywhere, use `ZonedDateTime` or `Instant`.

**Q24: What is the difference between `Duration` and `Period`?**

- `Duration` — Time-based amount measured in seconds and nanoseconds. For hours, minutes, seconds. Used with `LocalTime`, `LocalDateTime`, `Instant`.
- `Period` — Date-based amount measured in years, months, days. For calendar differences. Used with `LocalDate`, `LocalDateTime`.

```java
Duration d = Duration.ofHours(3);           // 3 hours (10800 seconds)
Period p = Period.of(1, 2, 15);             // 1 year, 2 months, 15 days
```

**Q25: How is `DateTimeFormatter` different from `SimpleDateFormat`?**

| Feature | `DateTimeFormatter` | `SimpleDateFormat` |
|---------|-------------------|--------------------|
| Thread-safe | **Yes** (immutable) | **No** |
| Package | `java.time.format` | `java.text` |
| Works with | `java.time` classes | `java.util.Date` |
| Parsing | Strict by default | Lenient by default |

---

### Default Methods & Collectors (Interview Questions)

**Q26: What is the diamond problem with default methods? How does Java resolve it?**

If a class implements two interfaces that both provide the same default method, it's a compile-time error. The class MUST override the method and explicitly choose which implementation to use:
```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }

class C implements A, B {
    @Override
    public void hello() {
        A.super.hello(); // explicitly choose A's implementation
    }
}
```

**Q27: Can default methods override methods from `Object`?**

No. Default methods cannot override `equals()`, `hashCode()`, or `toString()` from `Object`. The `Object` methods always take precedence. This is because every class already inherits from `Object`, and allowing interfaces to override these methods would create ambiguity.

**Q28: What happens when `Collectors.toMap()` encounters duplicate keys?**

It throws `IllegalStateException` with the message "Duplicate key". To handle duplicates, provide a merge function:
```java
// Keep the first value for duplicate keys
.collect(Collectors.toMap(keyFn, valueFn, (existing, replacement) -> existing));

// Combine values for duplicate keys
.collect(Collectors.toMap(keyFn, valueFn, (v1, v2) -> v1 + ", " + v2));
```

**Q29: What is the difference between `Collectors.toList()` and `Collectors.toUnmodifiableList()`?**

- `toList()` — Returns a mutable `ArrayList` (can add/remove elements later)
- `toUnmodifiableList()` (Java 10+) — Returns an unmodifiable list. Any attempt to modify it throws `UnsupportedOperationException`. Also does not allow `null` elements.
- `Stream.toList()` (Java 16+) — Returns an unmodifiable list (shorthand for `collect(Collectors.toUnmodifiableList())`)

---

## Practice Problems

These problems are implemented in `Java8PracticeProblems.java` with tests in `Java8PracticeTest.java`.

### Easy (Warm-up)
1. **Filter and Transform:** Given a list of strings, return a list of strings that start with "A", converted to uppercase.
2. **Sum of Squares:** Given a list of integers, find the sum of squares of all odd numbers.
3. **Longest String:** Find the longest string in a list using streams. Return Optional.
4. **Comma Joining:** Join a list of strings with commas using `Collectors.joining()`.
5. **Count Elements:** Count how many strings in a list have length > 5.

### Medium (Core Skills)
6. **Flatten Nested Lists:** Given a `List<List<Integer>>`, flatten it to a single `List<Integer>` and remove duplicates, sorted in ascending order.
7. **Word Frequency Counter:** Given a sentence (String), return a `Map<String, Long>` of word frequencies (case-insensitive).
8. **Group by First Letter:** Given a list of names, group them by their first character. Return `Map<Character, List<String>>`.
9. **Second Highest:** Find the second highest number in a list using streams. Return `OptionalInt`.
10. **Partition by Condition:** Partition a list of integers into even and odd numbers using `Collectors.partitioningBy()`.
11. **Custom Collector:** Create a list of employees and find the employee with the highest salary per department using `Collectors.groupingBy` with `Collectors.maxBy`.
12. **Map Inversion:** Given a `Map<String, Integer>`, create the inverse `Map<Integer, List<String>>` using streams.

### Hard (Interview Level)
13. **Fibonacci with Streams:** Generate the first N Fibonacci numbers using `Stream.iterate()` or `Stream.generate()`.
14. **Parallel Processing:** Given a large list of numbers, use parallel streams to find the sum of cubes of all prime numbers. Compare performance with sequential streams.
15. **Custom Functional Interface:** Create a `TriFunction<A, B, C, R>` interface that takes three arguments and returns a result. Demonstrate composition with `andThen`.
16. **Stream from Iterator:** Convert an `Iterator<T>` to a `Stream<T>` using `Spliterator` and `StreamSupport`.
17. **Sliding Window Average:** Implement a method that computes the sliding window average of a list of doubles with a given window size, using streams.
18. **Transaction Analysis:** Given a list of transactions (amount, date, category), use streams to:
    - Find the month with the highest total spending
    - Find the top 3 categories by total amount
    - Compute month-over-month spending growth percentages

### Challenge Problems
19. **Implement `map` using `reduce`:** Implement `Stream.map()` functionality using only `Stream.reduce()`.
20. **Parallel Word Count from Files:** Given a list of file paths, use parallel streams to count total word occurrences across all files.
21. **Build a Query DSL:** Create a fluent API using lambda chains that can filter, sort, and limit a dataset (like a simple SQL SELECT).
22. **Reactive-style Pipeline:** Build an event processing pipeline using `Consumer` chains that can filter, transform, and route events to different handlers based on event type.

---

## Common Mistakes Cheat Sheet

| Mistake | Problem | Fix |
|---------|---------|-----|
| `stream.forEach(list::add)` | Side-effect in stream, not thread-safe with parallel | Use `collect(toList())` |
| `optional.get()` without check | `NoSuchElementException` at runtime | Use `orElse()`, `orElseGet()`, `orElseThrow()` |
| `Optional.of(null)` | `NullPointerException` | Use `Optional.ofNullable(null)` |
| Reusing a consumed stream | `IllegalStateException` | Create a new stream from the source |
| Modifying source during stream | `ConcurrentModificationException` | Collect to new collection, then modify |
| `parallel()` for I/O operations | Blocks shared ForkJoinPool threads | Use custom executor or virtual threads |
| `new Date()` in new code | Uses legacy mutable API | Use `LocalDate.now()`, `Instant.now()` |
| `SimpleDateFormat` shared across threads | Race conditions, garbled output | Use `DateTimeFormatter` (thread-safe) |

---

## Quick Reference Card

```
Lambda:          (params) -> expression | (params) -> { statements; }
Method Ref:      Class::staticMethod | obj::method | Class::instanceMethod | Class::new
Stream:          source.stream().intermediate().terminal()
Optional:        Optional.of(v) | .ofNullable(v) | .empty() → .map() → .orElse()
Date/Time:       LocalDate.now() | LocalTime.now() | LocalDateTime.now() | ZonedDateTime.now()
Collectors:      toList() | toSet() | toMap() | groupingBy() | partitioningBy() | joining()
Functional:      Predicate<T> | Function<T,R> | Consumer<T> | Supplier<T>
```
