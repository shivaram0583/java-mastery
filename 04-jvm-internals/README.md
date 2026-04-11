# Module 04 — JVM Internals

This module covers the internal workings of the Java Virtual Machine. Understanding the JVM is essential for writing performant Java code and diagnosing production issues.

---

## 1. JVM Architecture

```
┌────────────────────────────────────────────────────────────────────┐
│                        Java Source Code (.java)                    │
│                              │                                     │
│                         javac (compiler)                           │
│                              │                                     │
│                        Bytecode (.class)                           │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────────┐     │
│  │                    CLASS LOADER SUBSYSTEM                 │     │
│  │  Bootstrap → Extension (Platform) → Application          │     │
│  │  (Delegation model: parent-first)                        │     │
│  └───────────────────────────┬──────────────────────────────┘     │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────────┐     │
│  │                  RUNTIME DATA AREAS                       │     │
│  │                                                           │     │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────────────────┐      │     │
│  │  │  HEAP   │  │  STACK  │  │    METHOD AREA       │      │     │
│  │  │(objects)│  │(per thr)│  │ (class metadata,     │      │     │
│  │  │         │  │ frames  │  │  constant pool,      │      │     │
│  │  │ Young   │  │ locals  │  │  static vars)        │      │     │
│  │  │ Old     │  │ operand │  ├─────────────────────┤      │     │
│  │  │         │  │ stack   │  │   Metaspace (off-heap)│     │     │
│  │  └─────────┘  └─────────┘  └─────────────────────┘      │     │
│  │                                                           │     │
│  │  ┌────────────────┐   ┌─────────────────────────┐       │     │
│  │  │  PC Register   │   │  Native Method Stack    │       │     │
│  │  │  (per thread)  │   │  (JNI calls)            │       │     │
│  │  └────────────────┘   └─────────────────────────┘       │     │
│  └──────────────────────────────────────────────────────────┘     │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────────┐     │
│  │                  EXECUTION ENGINE                         │     │
│  │  Interpreter → JIT Compiler (C1 → C2) → GC               │     │
│  └──────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────┘
```

---

## 2. Class Loading

### Delegation Model (Parent-First)
1. **Bootstrap ClassLoader** — Loads `java.base` module (core classes)
2. **Platform ClassLoader** (was Extension) — Loads platform modules
3. **Application ClassLoader** — Loads application classpath

When asked to load a class, each loader delegates to its parent first. Only if the parent can't load it does the child try.

**Gotchas:**
- Class identity = ClassLoader + Fully Qualified Name
- Same class loaded by two different classloaders are considered different types
- Custom classloaders are used for hot-reloading (e.g., application servers)

---

## 3. Memory Model

### Heap Layout

```
┌──────────────────────── HEAP ──────────────────────────┐
│                                                         │
│  ┌─── Young Generation ────┐   ┌── Old Generation ──┐ │
│  │ Eden  │  S0  │  S1      │   │                     │ │
│  │(new   │(surv)│(surv)    │   │ (long-lived         │ │
│  │objects)│      │          │   │  objects)            │ │
│  └────────────────────────┘   └─────────────────────┘ │
└─────────────────────────────────────────────────────────┘

┌────── Metaspace (off-heap) ──────┐
│ Class metadata, method bytecode  │
│ (replaces PermGen from Java 8+)  │
└──────────────────────────────────┘
```

- **Eden:** New objects are allocated here (fast bump-pointer allocation via **TLAB** — Thread Local Allocation Buffer)
- **Survivor Spaces (S0/S1):** Objects surviving minor GC are copied here
- **Old Generation:** Objects surviving multiple GC cycles are promoted here
- **Metaspace:** Stores class metadata; grows automatically (bounded by `-XX:MaxMetaspaceSize`)

---

## 4. Garbage Collection

| Collector | Type | Use Case | Java Version |
|---|---|---|---|
| **Serial GC** | Stop-the-world, single-thread | Small heaps, client apps | All |
| **Parallel GC** | Stop-the-world, multi-thread | Throughput-priority | All |
| **CMS** | Concurrent mark-sweep | Low-latency (deprecated Java 9, removed 14) | 1.4–13 |
| **G1GC** | Region-based, concurrent | Default since Java 9, balanced | 7+ |
| **ZGC** | Ultra-low latency (<1ms pauses) | Large heaps, latency-critical | 11+ (prod 15+) |
| **Shenandoah** | Concurrent compaction | Low latency (RedHat) | 12+ |

### Common GC Tuning Flags
```
-Xms512m              # Initial heap size
-Xmx4g                # Maximum heap size
-XX:+UseG1GC          # Use G1 garbage collector
-XX:+UseZGC           # Use ZGC
-XX:MaxGCPauseMillis=200  # G1 target pause time
-XX:+PrintGCDetails       # Print GC details (deprecated, use -Xlog:gc*)
-Xlog:gc*:file=gc.log     # Unified logging (Java 9+)
```

---

## 5. JIT Compilation

```
Bytecode → Interpreter → C1 (Client) → C2 (Server)
              │              │              │
           Slow           Fast          Fastest
         All code      Warm methods   Hot methods
                      (simple opts)  (aggressive opts:
                                      inlining, escape
                                      analysis, loop
                                      unrolling, OSR)
```

- **Interpreter:** Executes bytecode line by line. Slow but starts immediately.
- **C1 Compiler:** Compiles warm methods with basic optimizations. Fast compilation.
- **C2 Compiler:** Compiles hot methods with aggressive optimizations. Slower compilation, fastest execution.
- **OSR (On-Stack Replacement):** Replaces interpreted code with compiled code mid-execution (e.g., inside long loops).
- **Escape Analysis:** If an object doesn't escape a method, allocate it on the stack (no GC needed).
- **Inlining:** Replace method call with method body to avoid call overhead.

---

## 6. Bytecode Basics

To view bytecode for a compiled class:
```bash
javap -c -v target/classes/com/javamastery/jvm/BytecodeDemo.class
```

Key instructions: `aload`, `iload`, `astore`, `istore`, `invokevirtual`, `invokespecial`, `invokestatic`, `invokeinterface`, `new`, `dup`, `return`

---

## File Overview

| Topic | Demo Class | Test Class |
|---|---|---|
| Class Loading | `ClassLoaderDemo.java` | `ClassLoaderTest.java` |
| Memory Areas | `MemoryAreasDemo.java` | — |
| Garbage Collection | `GarbageCollectionDemo.java` | — |
| JIT Compilation | `JitDemo.java` | — |
| Bytecode | `BytecodeDemo.java` | — |
| JVM Flags | See README for flags reference | — |
