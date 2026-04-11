# Module 03 — Java 18 to 24 Features

This module covers the latest Java features from Java 18 through Java 24, including virtual threads, record patterns, pattern matching for switch, and structured concurrency.

---

## Java 18 (March 2022)

### UTF-8 by Default
**What:** UTF-8 is now the default charset for the Java SE APIs.  
**Why:** Consistent behavior across OS platforms (previously Windows used the system default, often windows-1252).

### Simple Web Server
**What:** `jwebserver` command-line tool for serving static files.  
**Usage:** `jwebserver -p 8080 -d /path/to/dir`

### Code Snippets in Javadoc
**What:** `@snippet` tag replaces `<pre>{@code ...}</pre>` for code examples in Javadoc.

---

## Java 19–20 (Preview Features)

### Virtual Threads (Preview)
Lightweight threads managed by the JVM (Project Loom). See Java 21 for finalized version.

### Structured Concurrency (Preview)
Manage concurrent tasks as a unit. See Java 21 for details.

### Record Patterns (Preview)
Destructure records in pattern matching. Finalized in Java 21.

---

## Java 21 (September 2023) — LTS

### Virtual Threads (Finalized — JEP 444)
**What:** Lightweight threads (~1KB stack) managed by the JVM, not the OS.  
**Why:** Enables millions of concurrent threads; eliminates the need for reactive frameworks for I/O-bound tasks.  
**How:** `Thread.ofVirtual().start(runnable)` or `Executors.newVirtualThreadPerTaskExecutor()`

**Gotchas:**
- Virtual threads are cheap but not free — avoid CPU-bound tasks on them
- `synchronized` blocks can pin virtual threads to platform threads — prefer `ReentrantLock`
- ThreadLocal works but can be memory-heavy with millions of virtual threads — prefer ScopedValue

### Record Patterns (Finalized — JEP 440)
**What:** Destructure record components directly in pattern matching.  
**How:** `case Point(int x, int y) -> ...`

### Pattern Matching for switch (Finalized — JEP 441)
**What:** Use type patterns, guarded patterns, and null handling in switch.  
**How:** `case String s when s.length() > 5 -> ...`

### Sequenced Collections (JEP 431)
**What:** New interfaces: `SequencedCollection`, `SequencedSet`, `SequencedMap` with defined encounter order.  
**Methods:** `getFirst()`, `getLast()`, `addFirst()`, `addLast()`, `reversed()`

### String Templates (Preview)
**What:** String interpolation: `STR."Hello \{name}"` — note: this was removed/reworked in Java 23.

---

## Java 22–24

### Unnamed Variables and Patterns (JEP 456, Java 22)
**What:** Use `_` for variables/patterns you don't need.  
**How:** `case Point(var x, _) -> ...` or `catch (Exception _) { }`

### Unnamed Classes and Instance Main Methods (JEP 463, Java 22)
**What:** Simplified entry point for beginners.  
**How:** `void main() { println("Hello"); }` — no class declaration needed.

### Scoped Values (Preview → progressing)
**What:** Immutable, inheritable values for structured concurrency. Replacement for ThreadLocal.

### Structured Concurrency (JEP 480, Java 24 final)
**What:** Treats concurrent tasks with clear parent-child relationships. On failure, all subtasks are cancelled.

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
