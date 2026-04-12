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
| Text Blocks | `TextBlocksDemo.java` | `TextBlocksTest.java` |
| Practice Problems | `Java9To17PracticeProblems.java` | `Java9To17PracticeTest.java` |

---

## Interview Questions

### Module System (JPMS)

**Q1: What is the Java Platform Module System? What problems does it solve?**

JPMS (Project Jigsaw) was introduced in Java 9 to solve:
- **Classpath hell:** All JARs on the classpath are one flat namespace. Duplicate classes cause silent conflicts.
- **No encapsulation:** Any `public` class is visible to all code, even internal implementation classes.
- **Monolithic JDK:** Even a "Hello World" app required the full ~300MB JDK runtime.

A module is declared in `module-info.java` with `requires` (dependencies) and `exports` (public API packages). Non-exported packages are inaccessible from outside the module, providing strong encapsulation.

**Q2: What is the difference between `requires` and `requires transitive`?**

- `requires java.sql` — This module depends on `java.sql`, but modules that depend on THIS module do NOT automatically get access to `java.sql`.
- `requires transitive java.sql` — This module depends on `java.sql`, AND any module that depends on THIS module also gets access to `java.sql`. Used when your public API exposes types from the dependency.

**Q3: Can you still run a non-modular application on Java 9+?**

Yes. Non-modular JARs placed on the classpath are treated as part of the "unnamed module," which can access all exported packages from named modules. This provides backward compatibility, but you lose the benefits of strong encapsulation.

---

### var (Local Variable Type Inference)

**Q4: Is `var` dynamic typing? What is the type of a `var` variable?**

No. `var` is NOT dynamic typing. The compiler infers the type from the right-hand side at compile time. Once inferred, the type is fixed. `var x = "hello"` makes `x` a `String` — you cannot later assign `x = 42`. Java remains statically typed; `var` is syntactic sugar that reduces verbosity.

**Q5: Where can and cannot you use `var`?**

| Allowed | Not Allowed |
|---------|-------------|
| Local variables with initializer | Method parameters |
| For loop variables (`for (var x : list)`) | Method return types |
| Try-with-resources | Class fields |
| | Variables without initializer (`var x;`) |
| | Lambda parameters (before Java 11) |
| | Null assignments (`var x = null;`) |

**Q6: Is `var` a keyword in Java?**

No. `var` is a **reserved type name**, not a keyword. You can still have a variable named `var` (though you shouldn't). This was done for backward compatibility — existing code that uses `var` as a variable name continues to compile.

---

### Records

**Q7: What are records in Java? How do they differ from regular classes?**

Records are immutable data carriers that automatically generate:
- Canonical constructor (all fields)
- Accessor methods (named after components, e.g., `x()` not `getX()`)
- `equals()` (based on all components)
- `hashCode()` (based on all components)
- `toString()` (includes all components)

Key differences from regular classes:
- Records are implicitly `final` — cannot be extended
- All fields are implicitly `private final` — immutable
- Cannot extend other classes (but CAN implement interfaces)
- Cannot declare instance fields beyond record components
- CAN have static fields, static methods, instance methods, and compact constructors

**Q8: Can a record have a custom constructor?**

Yes, records support:
- **Compact constructor** — for validation/normalization, no parameter list needed:
  ```java
  record Range(int start, int end) {
      Range {  // compact constructor
          if (start > end) throw new IllegalArgumentException();
      }
  }
  ```
- **Custom canonical constructor** — replaces the auto-generated one
- **Additional constructors** — must delegate to the canonical constructor

**Q9: Can a record implement an interface?**

Yes. Records can implement interfaces, including functional interfaces. They just cannot extend classes.
```java
record NamedPoint(String name, int x, int y) implements Comparable<NamedPoint> {
    @Override
    public int compareTo(NamedPoint other) {
        return this.name.compareTo(other.name);
    }
}
```

**Q10: When should you use a record vs a regular class?**

Use **records** when:
- The class is primarily a data carrier (DTO, value object)
- Immutability is desired
- You want automatic `equals`/`hashCode`/`toString`

Use **regular classes** when:
- You need mutable state
- You need inheritance (extending a class)
- You need encapsulation with different field visibility
- The class has complex behavior beyond data storage

---

### Sealed Classes

**Q11: What are sealed classes and what problem do they solve?**

Sealed classes restrict which classes can extend them using a `permits` clause. This creates a **closed type hierarchy** known at compile time.

Problems solved:
- **Modeling domains precisely:** e.g., a payment can only be Card, Cash, or BankTransfer — no other types
- **Exhaustive pattern matching:** The compiler can verify you've handled ALL subtypes in a switch
- **Controlled evolution:** Library authors can prevent unexpected subclassing

**Q12: What are the rules for permitted subclasses?**

Permitted subclasses must:
1. Be in the same package (or module) as the sealed class
2. Declare themselves as `final`, `sealed`, or `non-sealed`
   - `final` — No further extension
   - `sealed` — Further extension restricted to specific subclasses
   - `non-sealed` — Open for extension (breaks the seal from this point down)

**Q13: How do sealed classes enable exhaustive pattern matching?**

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double r) implements Shape {}
record Rectangle(double w, double h) implements Shape {}

double area(Shape s) {
    return switch (s) {
        case Circle c -> Math.PI * c.r() * c.r();
        case Rectangle r -> r.w() * r.h();
        // No default needed! Compiler knows these are all cases.
    };
}
```
If you add a new permitted subclass, the compiler forces you to handle it in every switch — preventing bugs.

---

### Switch Expressions

**Q14: What is the difference between switch statements and switch expressions?**

| Feature | Switch Statement | Switch Expression |
|---------|-----------------|-------------------|
| Returns a value | No | Yes |
| Arrow syntax (`->`) | No (before Java 14) | Yes |
| Fall-through | Yes (common source of bugs) | No (with `->`) |
| Exhaustiveness | Not checked | Required (must cover all cases) |
| Multiple labels | Stacked cases | Comma-separated: `case A, B ->` |

**Q15: What is the `yield` keyword in switch expressions?**

`yield` is used to return a value from a switch expression when you need a block of statements (not just a single expression):
```java
int result = switch (day) {
    case MONDAY -> 1;
    case TUESDAY -> {
        int value = computeComplex();
        yield value;   // returns this value from the block
    }
    default -> 0;
};
```

---

### Pattern Matching & Text Blocks

**Q16: What is pattern matching for `instanceof`? What is flow scoping?**

Pattern matching combines the type check and cast: `if (obj instanceof String s)` checks if `obj` is a `String` and assigns it to `s` in one step.

**Flow scoping** means the pattern variable is only in scope where the compiler can prove the pattern matched:
```java
if (obj instanceof String s) {
    // s is in scope here
} else {
    // s is NOT in scope here
}

// s is in scope here ONLY if the code above guarantees obj IS a String
if (!(obj instanceof String s)) {
    return;  // early return
}
// s IS in scope here (compiler knows instanceof must have matched)
```

**Q17: What are text blocks and how is indentation handled?**

Text blocks (finalized Java 15) are multi-line string literals using `"""`. Incidental indentation is stripped based on the position of the closing `"""`:

```java
String html = """
        <html>
            <body>Hello</body>
        </html>
        """;
// The 8-space common prefix is stripped, giving:
// <html>
//     <body>Hello</body>
// </html>
```

Special characters: `\s` (preserved space), `\` at end of line (no newline — line continuation).

---

### String Methods & HttpClient

**Q18: What is the difference between `strip()` and `trim()`?**

- `trim()` (legacy) — Removes characters with values ≤ `\u0020` (ASCII whitespace only)
- `strip()` (Java 11) — Removes all **Unicode** whitespace characters, including non-breaking spaces, ideographic spaces, etc.

In most cases they produce the same result, but `strip()` is more correct for internationalized text.

**Q19: Explain the new HttpClient API. How is it better than HttpURLConnection?**

| Feature | `HttpURLConnection` | `HttpClient` (Java 11) |
|---------|--------------------|-----------------------|
| API style | Imperative, verbose | Builder pattern, fluent |
| HTTP/2 | No | Yes |
| Async support | No | Yes (CompletableFuture) |
| WebSocket | No | Yes |
| Thread safety | Not reusable | Immutable, thread-safe |
| Redirect handling | Manual | Built-in policies |

---

## Practice Problems

These problems are implemented in `Java9To17PracticeProblems.java` with tests in `Java9To17PracticeTest.java`.

### Easy
1. **Record Creation:** Create a `Student` record with name, age, and grade fields. Add a compact constructor that validates age is positive.
2. **var Usage:** Refactor a block of code to use `var` where appropriate and identify where it CAN'T be used.
3. **Strip vs Trim:** Write a program that demonstrates the difference between `strip()` and `trim()` with Unicode whitespace.
4. **String Methods:** Use `isBlank()`, `lines()`, `strip()`, and `repeat()` to process a multi-line text block.
5. **Switch Expression:** Convert a traditional switch statement (with fall-through) to a switch expression with arrow syntax.

### Medium
6. **Sealed Hierarchy:** Model a simple expression evaluator using sealed interfaces: `Expr` with subtypes `Num`, `Add`, `Mul`. Implement an `evaluate` method using pattern matching switch.
7. **Record Builder:** Since records don't have a builder pattern built in, create a builder for a complex record with 5+ fields.
8. **Pattern Matching Chain:** Using pattern matching for `instanceof`, write a method that describes any `Object` (String, Integer, List, Map, array, null) with detailed type-specific info.
9. **Optional Chaining:** Chain `Optional` operations using Java 9's `or()` to create a fallback lookup: try cache → try database → return default.
10. **Text Block Formatting:** Use text blocks to create JSON and SQL templates with dynamic values (using `formatted()` or `String.format()`).

### Hard
11. **Module Design:** Design a module system for a small application with `api`, `impl`, and `app` modules. Determine which packages to export and require.
12. **Sealed + Records + Pattern Matching:** Model a complete payment processing system using sealed interfaces, records, and pattern matching:
    - Payment types: CreditCard, DebitCard, BankTransfer, DigitalWallet
    - Process each payment type differently
    - Validate payment details in compact constructors
13. **Stream Enhancements Pipeline:** Use `takeWhile`, `dropWhile`, `Stream.ofNullable`, and `Stream.iterate` with a predicate-based stopping condition to implement a pagination system.
14. **HttpClient Patterns:** Write a retryable HTTP client that uses Java 11's HttpClient with exponential backoff, timeout handling, and response caching.

### Challenge
15. **AST with Sealed Classes:** Implement an arithmetic expression parser and evaluator using sealed interfaces and records: parse "2 + 3 * 4" into an AST and evaluate it.
16. **Type-Safe Heterogeneous Container:** Using records and pattern matching, build a type-safe container that can store values of different types and retrieve them with compile-time type safety.

---

## Common Mistakes Cheat Sheet

| Mistake | Problem | Fix |
|---------|---------|-----|
| `var x = null;` | Compiler can't infer type | Specify the type: `String x = null;` |
| `var list = List.of(1, 2, 3)` then adding elements | `List.of()` returns unmodifiable list | Use `new ArrayList<>(List.of(1, 2, 3))` |
| Record with mutable field (`record R(List<String> items)`) | List can be modified externally | Defensive copy in compact constructor |
| `non-sealed` subclass without understanding | Breaks the sealed guarantee from that point | Use `final` unless you explicitly want open extension |
| Using text block for single-line strings | Unnecessary verbosity | Use regular strings for single lines |
| Mixing old switch style with new | Fall-through in arrow cases | Stick to one style per switch |
| `takeWhile` on unordered stream | Non-deterministic results | Use with ordered streams only |
