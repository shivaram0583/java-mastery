# Module 02 — Java 9 to 17 Features

This module covers the major features introduced from Java 9 through Java 17 (LTS). These releases incrementally modernized the language, culminating in Java 17 as a major LTS milestone.

> **Why these versions matter:** Java 8 was a massive leap (lambdas, streams), and then Java moved to a 6-month release cadence starting with Java 9. While each individual release had fewer features, collectively Java 9–17 brought transformative changes: a module system for better architecture, pattern matching for cleaner code, records for less boilerplate, and sealed classes for controlled inheritance.

---

## Java 9 (September 2017)

### Module System (JPMS)

**What:** The Java Platform Module System (JPMS, Project Jigsaw) is a way to organize code into **modules** — self-contained units with explicit dependencies and controlled visibility. Before JPMS, Java had only packages and JARs, and any public class was accessible to everyone.

**Why this was needed:**
- **Classpath hell:** Before modules, all JARs were dumped onto one flat classpath. If two JARs contained the same class, the JVM would silently pick one. Dependency conflicts were hard to diagnose.
- **No encapsulation beyond `public`:** If you marked a class `public`, anyone could use it — even internal implementation classes you never intended to expose.
- **Monolithic JDK:** Even a simple “Hello World” app shipped with the entire 300MB JDK runtime.

**How it works:**
```
  module-info.java:
  ┌──────────────────────────────────────┐
  │ module com.myapp {                │
  │     requires java.sql;            │  ← I depend on java.sql
  │     requires java.logging;        │  ← I depend on java.logging
  │     exports com.myapp.api;        │  ← Only this package is visible to others
  │     // com.myapp.internal is      │
  │     // hidden from other modules  │
  │ }                                 │
  └──────────────────────────────────────┘
```

- With JPMS you can create **smaller runtime images** using `jlink` — bundle only the modules your app uses, shrinking deployment size from hundreds of MB to tens of MB.

**Gotchas:** Many libraries were not modularized initially; the classpath still works (unnamed module) for backward compatibility, but you lose the benefits of strong encapsulation and explicit dependencies.

---

### Private Interface Methods
**What:** Starting with Java 9, interfaces can have `private` methods. These are helper methods that can be called from `default` methods within the same interface, allowing you to share common logic between multiple default methods without exposing it to implementing classes.

**Before Java 9:** If two default methods needed the same helper logic, you had to either duplicate the code or make the helper a `default` method (which would be visible to all implementing classes).

```java
public interface Logger {
    default void logInfo(String msg)  { log("INFO", msg); }
    default void logError(String msg) { log("ERROR", msg); }

    private void log(String level, String msg) {   // shared helper
        System.out.println("[" + level + "] " + msg);
    }
}
```

**Gotchas:** Private interface methods cannot be abstract (they must have a body) and cannot be accessed by implementing classes.

---

### Stream Enhancements

Java 9 added several useful methods to the Stream API:

- **`takeWhile(predicate)`** — Takes elements from the beginning of the stream as long as the predicate is true, then stops. Think of it as “read until the first failure.” On an ordered stream, it takes a prefix.
  ```
  Stream.of(1, 2, 3, 4, 5, 1).takeWhile(n -> n < 4)
  // Result: [1, 2, 3]  — stops at 4, does NOT include the trailing 1
  ```

- **`dropWhile(predicate)`** — The opposite: drops elements while the predicate is true, then takes everything remaining.
  ```
  Stream.of(1, 2, 3, 4, 5, 1).dropWhile(n -> n < 4)
  // Result: [4, 5, 1]  — drops 1,2,3 then takes the rest
  ```

- **`Stream.ofNullable(element)`** — Returns an empty stream if the element is null, or a single-element stream otherwise. Useful in `flatMap` operations to gracefully handle null values.

---

### Optional Enhancements

Java 9 made `Optional` more powerful and expressive:

- **`ifPresentOrElse(action, emptyAction)`** — Run different logic depending on whether the Optional has a value or is empty. Before Java 9, you needed an `if/else` block with `isPresent()`.
  ```java
  optional.ifPresentOrElse(
      value -> System.out.println("Found: " + value),
      ()    -> System.out.println("Not found")
  );
  ```

- **`or(supplier)`** — If this Optional is empty, return an alternative Optional (from the supplier). Unlike `orElse()`, this returns another Optional rather than an unwrapped value, enabling continued chaining.

- **`stream()`** — Convert the Optional to a Stream with 0 or 1 elements. This is incredibly useful in `flatMap` pipelines to seamlessly handle optional values within streams.

---

## Java 10 (March 2018)

### Local Variable Type Inference (`var`)

**What:** The `var` keyword lets you declare local variables without explicitly stating the type. The compiler infers it from the right-hand side of the assignment.

**Why:** Reduces verbosity, especially with complex generic types, without losing type safety. The type is still checked at compile time — `var` is NOT dynamic typing.

```java
// Without var (verbose):
Map<String, List<Employee>> departmentMap = new HashMap<String, List<Employee>>();

// With var (concise, same type safety):
var departmentMap = new HashMap<String, List<Employee>>();
```

**Where you CAN use `var`:**
- Local variables with initializers
- Loop variables (`for (var item : list)`)
- Try-with-resources (`try (var stream = Files.lines(path))`)

**Where you CANNOT use `var`:**
- Method parameters, return types, or fields
- Variables without an initializer (`var x;` — what type would it be?)
- Null assignments (`var x = null;` — null has no type)

**Gotchas:** `var` is not a keyword — it’s a reserved type name. This means you can still have a variable named `var` (though you shouldn’t). Use `var` when the type is obvious from context; avoid it when it makes code harder to read.

---

## Java 11 (September 2018) — LTS

Java 11 was the first Long-Term Support release under the new 6-month cadence. It’s widely used in production today.

### New String Methods

Java 11 added several convenience methods to `String` that developers had been implementing manually or pulling from libraries like Apache Commons:

- **`isBlank()`** — Returns `true` if the string is empty or contains only whitespace. Unlike `isEmpty()` which only checks for zero length, `isBlank()` also catches strings like `"   "`.
- **`lines()`** — Returns a `Stream<String>` of lines from a multi-line string, split by line terminators (`\n`, `\r\n`, `\r`). Replaces manual `split("\\n")` patterns.
- **`strip()`** / **`stripLeading()`** / **`stripTrailing()`** — Unicode-aware alternatives to `trim()`. While `trim()` only removes ASCII whitespace (chars ≤ `\u0020`), `strip()` handles all Unicode whitespace characters.
- **`repeat(n)`** — Repeat a string n times. `"ha".repeat(3)` gives `"hahaha"`.

### Files Convenience Methods
- `Files.readString(path)` — Read an entire file to a String in one call (previously required `Files.readAllBytes()` + `new String(...)`)
- `Files.writeString(path, content)` — Write a String to a file in one call

### New HttpClient

**What:** `java.net.http.HttpClient` is a modern, full-featured HTTP client that replaces the legacy `HttpURLConnection` (which was awkward, verbose, and hard to use).

**Key features:**
- Supports HTTP/1.1 and **HTTP/2** (multiplexed connections)
- **Asynchronous** requests with `CompletableFuture` (non-blocking I/O)
- WebSocket support built in
- Builder pattern for clean configuration

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/data"))
    .GET()
    .build();

// Synchronous
HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

// Asynchronous (non-blocking)
client.sendAsync(request, BodyHandlers.ofString())
    .thenApply(HttpResponse::body)
    .thenAccept(System.out::println);
```

**Gotchas:** `HttpClient` instances are immutable and thread-safe — create one and reuse it across your application.

### Single-File Source-Code Programs
`java MyProgram.java` — No need for an explicit `javac` compilation step. The JVM compiles and runs in one command, making Java more accessible for scripting and learning.

---

## Java 12–13

### Switch Expressions

**What:** Switch can now be used as an **expression** (returns a value) in addition to a statement. The new arrow syntax (`->`) eliminates fall-through bugs — one of the most common sources of errors in traditional switch statements.

**Before (statement, error-prone):**
```java
String result;
switch (day) {
    case MONDAY:
    case TUESDAY:
        result = "early week";
        break;           // forget this break = bug!
    case FRIDAY:
        result = "TGIF";
        break;
    default:
        result = "other";
}
```

**After (expression, safe):**
```java
String result = switch (day) {
    case MONDAY, TUESDAY -> "early week";
    case FRIDAY          -> "TGIF";
    default              -> "other";
};
```

**Gotchas:** When used as an expression, the switch must be **exhaustive** — it must cover all possible values (use `default` as a catch-all).

### Text Blocks (Preview in 13)
**What:** Multi-line string literals using triple quotes (`\"\"\"`) that preserve formatting. No more concatenating strings with `\n` or escaping quotes.

```java
String json = \"\"\"
    {
        "name": "Alice",
        "age": 30
    }
    \"\"\";
```

**Gotchas:** Indentation is stripped based on the position of the closing `\"\"\"` — move it to control how much whitespace is removed.

---

## Java 14

### Records (Preview)

**What:** Records are compact, immutable data classes. The compiler automatically generates the constructor, `equals()`, `hashCode()`, `toString()`, and accessor methods based on the components you declare.

**Why:** Java was notorious for boilerplate in simple data-carrying classes. A class with three fields required ~50 lines of code for constructors, getters, equals, hashCode, and toString. Records reduce this to one line.

```java
// Before: ~50 lines of boilerplate
public class Point {
    private final int x;
    private final int y;
    public Point(int x, int y) { this.x = x; this.y = y; }
    public int x() { return x; }
    public int y() { return y; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// After: 1 line!
public record Point(int x, int y) { }
```

Records are implicitly `final`, their fields are implicitly `private final`, and they cannot extend other classes (but can implement interfaces).

### Pattern Matching for `instanceof` (Preview)
**What:** Combines the type check and cast into a single step, eliminating the redundant cast that traditionally followed `instanceof`.

```java
// Before (redundant cast):
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// After (pattern variable):
if (obj instanceof String s) {
    System.out.println(s.length());  // s is already cast
}
```

The pattern variable `s` is scoped to the `if` block (and the else block receives the negative — `s` is not in scope there).

### Helpful NullPointerExceptions
**What:** The JVM now tells you exactly **which variable** was null in the exception message, not just the line number. This is especially valuable for chained method calls.

```
// Before: "NullPointerException" at line 42 (which part of the chain?)
// After:  "Cannot invoke String.length() because the return value of
//          User.getName() is null"
```

---

## Java 15–16

### Sealed Classes (Preview)
**What:** Sealed classes restrict which classes can extend or implement them, using a `permits` clause. This creates a **closed hierarchy** where you know all possible subtypes at compile time.

**Why this matters:**
- **Exhaustive pattern matching:** When you switch over a sealed type, the compiler knows all possible subtypes, so it can verify you’ve handled every case (no `default` needed).
- **Controlled extension:** Library authors can define a type hierarchy and prevent users from adding unexpected subtypes.

```java
public sealed interface Shape
    permits Circle, Rectangle, Triangle {
    double area();
}

// Only these three classes can implement Shape:
public record Circle(double radius) implements Shape { ... }
public record Rectangle(double w, double h) implements Shape { ... }
public record Triangle(double base, double height) implements Shape { ... }
```

```
  Sealed hierarchy:
  ┌──────────────────┐
  │   Shape (sealed)  │
  └────────┴─────────┘
           │
     ┌─────┼───────┐
     │     │       │
  Circle  Rect   Triangle
  (final) (final) (final)

  No other class can implement Shape!
  The compiler knows ALL subtypes.
```

### Records Finalized (Java 16)
Records moved from preview to a finalized, stable feature.

### Pattern Matching for instanceof Finalized (Java 16)
Pattern matching for `instanceof` moved from preview to a finalized, stable feature.

---

## Java 17 (September 2021) — LTS

Java 17 is the second major LTS release under the new cadence and is widely adopted in enterprise applications. It finalizes several important features:

### Sealed Classes Finalized
Sealed classes moved from preview to a finalized, stable feature (see Java 15 section for details).

### Consolidated Switch Expressions
Switch expressions (first previewed in Java 12) are now fully stable with all refinements incorporated.

### Strong Encapsulation of JDK Internals
Most `--illegal-access` options were removed. Internal APIs (like `sun.misc.Unsafe`) are inaccessible by default. This was the final step in a gradual process that started in Java 9:

```
  Java 9–15: --illegal-access=permit (default, warning only)
  Java 16:   --illegal-access=deny (default, access blocked)
  Java 17:   --illegal-access removed entirely

  If your code uses internal JDK APIs, you must use
  --add-opens/--add-exports flags explicitly.
```

This change improves security and allows the JDK team to evolve internal implementations without breaking user code.

---

## File Overview

| Topic | Demo Class | Test Class |
|---|---|---|
| Private Interface Methods | `PrivateInterfaceMethodsDemo.java` | — |
| Stream takeWhile/dropWhile | `StreamEnhancementsDemo.java` | `StreamEnhancementsTest.java` |
| Optional Enhancements | `OptionalEnhancementsDemo.java` | `OptionalEnhancementsTest.java` |
| var (Java 10) | `VarDemo.java` | `VarTest.java` |
| String Methods (Java 11) | `StringMethodsDemo.java` | `StringMethodsTest.java` |
| HttpClient (Java 11) | `HttpClientDemo.java` | — |
| Switch Expressions | `SwitchExpressionsDemo.java` | `SwitchExpressionsTest.java` |
| Records | `RecordsDemo.java` | `RecordsTest.java` |
| Pattern Matching instanceof | `PatternMatchingDemo.java` | `PatternMatchingTest.java` |
| Sealed Classes | `SealedClassesDemo.java` | `SealedClassesTest.java` |
