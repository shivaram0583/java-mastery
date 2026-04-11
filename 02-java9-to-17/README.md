# Module 02 — Java 9 to 17 Features

This module covers the major features introduced from Java 9 through Java 17 (LTS). These releases incrementally modernized the language, culminating in Java 17 as a major LTS milestone.

---

## Java 9 (September 2017)

### Module System (JPMS)
**What:** Java Platform Module System — a way to organize code into modules with explicit dependencies.  
**Why:** Strong encapsulation; reliable configuration; smaller runtime images with `jlink`.  
**Gotchas:** Many libraries were not modularized initially; classpath still works (unnamed module).

### Private Interface Methods
**What:** Interfaces can have `private` methods to share code between default methods.  
**Gotchas:** Cannot be abstract; cannot be accessed by implementing classes.

### Stream Enhancements
- `takeWhile(predicate)` — takes elements while predicate is true, then stops
- `dropWhile(predicate)` — drops elements while predicate is true, then takes the rest
- `Stream.ofNullable(element)` — returns empty stream for null, single-element stream otherwise

### Optional Enhancements
- `ifPresentOrElse(action, emptyAction)` — run different logic for present vs empty
- `or(supplier)` — return alternative Optional if empty
- `stream()` — convert Optional to a Stream (0 or 1 element)

---

## Java 10 (March 2018)

### Local Variable Type Inference (`var`)
**What:** Use `var` to let the compiler infer the type of local variables.  
**Why:** Reduces verbosity, especially with generics.  
**Gotchas:** Cannot use for fields, method parameters, or return types. Not a keyword — it's a reserved type name.

---

## Java 11 (September 2018) — LTS

### New String Methods
- `isBlank()` — true if empty or whitespace only
- `lines()` — stream of lines from multi-line string
- `strip()` / `stripLeading()` / `stripTrailing()` — Unicode-aware trim
- `repeat(n)` — repeat string n times

### Files Convenience Methods
- `Files.readString(path)` — read entire file to string
- `Files.writeString(path, content)` — write string to file

### New HttpClient
**What:** `java.net.http.HttpClient` — modern, asynchronous HTTP client replacing `HttpURLConnection`.  
**Gotchas:** Supports HTTP/2, WebSocket, async requests.

### Single-File Source-Code Programs
`java MyProgram.java` — no need for explicit `javac` step.

---

## Java 12–13

### Switch Expressions
**What:** Switch becomes an expression that returns a value; arrow syntax eliminates fall-through.  
**Gotchas:** Must be exhaustive when used as expression.

### Text Blocks (Preview in 13)
**What:** Multi-line string literals using `"""`.  
**Gotchas:** Indentation is stripped based on the closing `"""` position.

---

## Java 14

### Records (Preview)
**What:** Compact data classes with auto-generated `equals()`, `hashCode()`, `toString()`, and accessors.  
**Why:** Reduces boilerplate for simple data carriers.

### Pattern Matching for `instanceof` (Preview)
**What:** Combines type check and cast in one step: `if (obj instanceof String s)`.

### Helpful NullPointerExceptions
**What:** JVM now indicates exactly which variable was null in the exception message.

---

## Java 15–16

### Sealed Classes (Preview)
**What:** Restrict which classes can extend/implement a type.  
**Why:** Enables exhaustive pattern matching; controlled hierarchies.

### Records Finalized (Java 16)
### Pattern Matching for instanceof Finalized (Java 16)

---

## Java 17 (September 2021) — LTS

### Sealed Classes Finalized
### Consolidated Switch Expressions
### Strong Encapsulation of JDK Internals
Most `--illegal-access` options removed. Internal APIs are inaccessible by default.

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
