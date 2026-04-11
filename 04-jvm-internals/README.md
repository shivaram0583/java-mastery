# Module 04 — JVM Internals

This module covers the internal workings of the Java Virtual Machine. Understanding the JVM is essential for writing performant Java code and diagnosing production issues.

> **Why learn JVM internals?** Java developers often treat the JVM as a black box — you write code, compile it, and it runs. But understanding what happens inside the JVM unlocks the ability to tune application performance, diagnose memory leaks, understand why certain code is faster than others, and make informed architectural decisions. This knowledge separates a junior developer from a senior one.

---

## 1. JVM Architecture

The JVM is the engine that runs Java programs. It takes your compiled bytecode (`.class` files) and executes it on any operating system — this is what makes Java “write once, run anywhere.” Here’s the big picture of how all the JVM components fit together:

```
┌────────────────────────────────────────────────────────────────┐
│                        Java Source Code (.java)                    │
│                              │                                     │
│                         javac (compiler)                           │
│                              │                                     │
│                        Bytecode (.class)                           │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────┐     │
│  │                    CLASS LOADER SUBSYSTEM                 │     │
│  │  Bootstrap → Extension (Platform) → Application          │     │
│  │  (Delegation model: parent-first)                        │     │
│  └───────────────────────────┬──────────────────────────┘     │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────┐     │
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
│  └────────────────────────────────────────────────────────┘     │
│                              │                                     │
│  ┌───────────────────────────▼──────────────────────────┐     │
│  │                  EXECUTION ENGINE                         │     │
│  │  Interpreter → JIT Compiler (C1 → C2) → GC               │     │
│  └────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────┘
```

**How the flow works:**
1. **You write** Java source code (`.java` files).
2. **`javac` compiles** it to platform-independent **bytecode** (`.class` files).
3. The **Class Loader Subsystem** loads the bytecode into memory, verifies it, and prepares it for execution.
4. The **Runtime Data Areas** store everything the JVM needs while running: objects on the heap, method call stacks, class metadata, etc.
5. The **Execution Engine** actually runs the bytecode — first interpreting it line by line, then compiling hot code paths to native machine code (JIT) for peak performance.

---

## 2. Class Loading

### How Classes Get into the JVM

When your code references a class (e.g., `new ArrayList<>()`), the JVM needs to find the class definition, load it into memory, verify it’s valid, and prepare it for use. This multi-step process is handled by **class loaders**.

### Delegation Model (Parent-First)

Class loaders are organized in a hierarchy. When asked to load a class, each loader **delegates to its parent first**. Only if the parent can’t find the class does the child attempt to load it. This prevents application code from accidentally (or maliciously) replacing core Java classes.

```
  Class loading request: "Load java.util.ArrayList"

  ┌───────────────────────┐
  │  Bootstrap ClassLoader │ ← Loads core Java classes (java.base module)
  │  (native, no parent)   │   Found it! Returns java.util.ArrayList
  └───────────▲───────────┘
              │ delegates up
  ┌───────────┴───────────┐
  │  Platform ClassLoader  │ ← Loads platform modules (java.sql, java.xml, etc.)
  │  (was "Extension")     │
  └───────────▲───────────┘
              │ delegates up
  ┌───────────┴───────────┐
  │ Application ClassLoader│ ← Loads YOUR classes from classpath/modulepath
  │  (classpath)            │   Request starts here
  └────────────────────────┘
```

**Key principles:**
1. **Bootstrap ClassLoader** — The root loader, implemented in native code. Loads fundamental classes from the `java.base` module (like `Object`, `String`, `ArrayList`).
2. **Platform ClassLoader** (was Extension in Java 8) — Loads platform modules such as `java.sql`, `java.xml`, `java.logging`.
3. **Application ClassLoader** — Loads your application classes and third-party libraries from the classpath or module path.

**Gotchas:**
- **Class identity** = ClassLoader + Fully Qualified Class Name. The same `.class` file loaded by two different classloaders produces two **different types** that cannot be cast to each other.
- Custom classloaders are used for **hot-reloading** in application servers (Tomcat, JBoss) — they discard an old classloader and create a new one to reload updated classes without restarting the server.

---

## 3. Memory Model

Understanding how the JVM organizes memory helps you tune performance, diagnose `OutOfMemoryError`, and understand garbage collection.

### Heap Layout

The heap is where all Java objects live. It’s divided into regions based on object age, because most objects are short-lived and the GC can handle them efficiently by separating short-lived objects from long-lived ones.

```
┌──────────────────────── HEAP ────────────────────────┐
│                                                         │
│  ┌─── Young Generation ────┐   ┌── Old Generation ──┐ │
│  │ Eden  │  S0  │  S1      │   │                     │ │
│  │(new   │(surv)│(surv)    │   │ (long-lived         │ │
│  │objects)│      │          │   │  objects)            │ │
│  └────────────────────────┘   └─────────────────────┘ │
└─────────────────────────────────────────────────────┘

┌────── Metaspace (off-heap) ──────┐
│ Class metadata, method bytecode  │
│ (replaces PermGen from Java 8+)  │
└──────────────────────────────────┘
```

**Understanding each region:**

- **Eden Space:** Where **all new objects** are born. Allocation is extremely fast thanks to **TLAB** (Thread Local Allocation Buffer) — each thread gets its own small chunk of Eden, so most allocations require no synchronization. When Eden fills up, a **Minor GC** is triggered.

- **Survivor Spaces (S0/S1):** Objects that survive a Minor GC in Eden are copied to one of the two survivor spaces. The JVM alternates between S0 and S1: during each Minor GC, live objects from Eden and the current survivor space are copied to the other survivor space. Objects that survive multiple GC cycles (their “age” increases each time) are eventually promoted to Old Generation.

- **Old Generation (Tenured):** Objects that have survived many GC cycles in the Young Generation are promoted here. Old Gen is larger and collected less frequently. When Old Gen fills up, a **Major GC** (or Full GC) occurs, which is more expensive and can cause noticeable pauses.

- **Metaspace (off-heap):** Stores class metadata — class structures, method bytecode, constant pools, annotations. Unlike the old PermGen (removed in Java 8), Metaspace grows automatically and uses native memory (not heap). You can limit it with `-XX:MaxMetaspaceSize`.

> **Key insight:** The generational hypothesis states that “most objects die young.” The vast majority of objects (often >95%) become unreachable shortly after creation. By separating young objects from old ones, the GC can efficiently clean up the majority of garbage by scanning only the small Young Generation.

**Stack vs Heap:**
```
  Stack (per thread):              Heap (shared):
  ┌───────────────────┐      ┌──────────────────────┐
  │ Frame: main()     │      │ Object: new String()  │
  │  local var: x=5   │ ──►  │ Object: new ArrayList │
  │  local var: ref ───┤      │ Object: new User()    │
  ├───────────────────┤      │ ...                   │
  │ Frame: calculate()│      └──────────────────────┘
  │  local var: temp  │
  └───────────────────┘
  (auto-freed when           (garbage collected when
   method returns)            no references remain)
```

- **Stack:** Stores primitive values and object references (NOT objects themselves). Each thread has its own stack. Stack frames are created when a method is called and destroyed when it returns — automatic, no GC needed.
- **Heap:** Stores actual objects. Shared by all threads. Objects are created with `new` and cleaned up by the garbage collector when no references point to them.

---

## 4. Garbage Collection

Garbage collection (GC) is the process of automatically reclaiming memory used by objects that are no longer reachable from the application. You don’t need to manually free memory (like in C/C++), but understanding GC behavior is critical for performance tuning.

**How GC determines “garbage”:** The GC starts from a set of “root” references (local variables on the stack, static fields, active threads) and traces all reachable objects. Anything NOT reachable from these roots is garbage and can be reclaimed.

| Collector | Type | Use Case | Java Version |
|---|---|---|---|
| **Serial GC** | Stop-the-world, single-thread | Small heaps (<100MB), client apps, containers | All |
| **Parallel GC** | Stop-the-world, multi-thread | Throughput-priority batch processing | All |
| **CMS** | Concurrent mark-sweep | Low-latency web apps (deprecated Java 9, removed 14) | 1.4–13 |
| **G1GC** | Region-based, concurrent | General purpose, balanced latency/throughput. **Default since Java 9** | 7+ |
| **ZGC** | Ultra-low latency (<1ms pauses) | Large heaps (multi-GB to TB), latency-critical systems | 11+ (prod 15+) |
| **Shenandoah** | Concurrent compaction | Low latency, similar goals to ZGC (RedHat-backed) | 12+ |

**Choosing the right GC:**
- **Don’t know? Use G1GC** (the default). It works well for most applications.
- **Batch processing** (throughput matters, pauses are OK): Parallel GC
- **Web servers/APIs** (low latency needed): G1GC or ZGC
- **Real-time/trading systems** (sub-millisecond pauses required): ZGC or Shenandoah
- **Tiny containers** (limited CPU/memory): Serial GC

### Common GC Tuning Flags
```
-Xms512m              # Initial heap size (set equal to -Xmx to avoid resizing)
-Xmx4g                # Maximum heap size
-XX:+UseG1GC          # Use G1 garbage collector
-XX:+UseZGC           # Use ZGC
-XX:MaxGCPauseMillis=200  # G1 target pause time (G1 tries to stay under this)
-XX:+PrintGCDetails       # Print GC details (deprecated, use -Xlog:gc*)
-Xlog:gc*:file=gc.log     # Unified logging for GC (Java 9+)
```

> **Best practice:** Set `-Xms` equal to `-Xmx` in production to avoid the overhead of heap resizing. Monitor GC logs to understand your application’s allocation patterns before tuning.

---

## 5. JIT Compilation

Java achieves near-native performance through **Just-In-Time (JIT) compilation**. Instead of interpreting bytecode forever, the JVM identifies frequently executed (“hot”) code and compiles it to optimized native machine code at runtime.

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

**How tiered compilation works:**

1. **Interpreter:** When code first runs, the JVM interprets bytecode one instruction at a time. This is slow but starts immediately with zero warm-up time. Meanwhile, the JVM collects **profiling data** about which methods are called frequently.

2. **C1 Compiler (Client):** Methods that are called enough times (“warm” methods) are compiled by C1 with basic optimizations (simple inlining, constant folding). C1 compiles quickly, so the speed improvement comes fast.

3. **C2 Compiler (Server):** Methods that are called much more frequently (“hot” methods) are recompiled by C2 with aggressive optimizations. C2 takes longer to compile but produces the fastest possible native code.

**Key JIT optimizations explained:**

- **Inlining:** The compiler replaces a method call with the method’s body directly at the call site. This eliminates the overhead of method invocation (stack frame creation, parameter passing) and enables further optimizations. Small, frequently called methods benefit most.

- **Escape Analysis:** The compiler analyzes whether an object created inside a method “escapes” that method (e.g., is returned or stored in a field). If the object does NOT escape, the JVM can allocate it on the **stack** instead of the heap — no garbage collection needed! It can even eliminate the object entirely and use its fields as local variables (**scalar replacement**).

- **Loop Unrolling:** The compiler transforms a loop that executes N iterations into a sequence of N explicit statements (or fewer iterations with multiple operations each). This reduces loop overhead and enables further parallelism.

- **OSR (On-Stack Replacement):** If a method is currently running in the interpreter (e.g., inside a long loop), the JVM can replace the interpreted version with the compiled version **mid-execution**, without restarting the method.

> **Practical takeaway:** JIT compilation means Java applications get faster as they run. The first few seconds/minutes may be slow (“warm-up” period), but once hot paths are JIT-compiled, Java’s performance approaches C++ levels. This is why benchmarks that measure only startup time are misleading for long-running applications.

---

## 6. Bytecode Basics

**What is bytecode?** When you compile Java source code with `javac`, it produces `.class` files containing **bytecode** — a platform-independent instruction set for the JVM. Bytecode is the “intermediate language” between your source code and the native machine code that actually runs on the CPU.

> **Analogy:** If source code is a recipe written in English, bytecode is the same recipe translated into a universal pictographic language. Any kitchen (JVM) in any country (operating system) can follow it, and the head chef (JIT compiler) can optimize the cooking steps for their specific kitchen layout (hardware).

To view bytecode for a compiled class:
```bash
javap -c -v target/classes/com/javamastery/jvm/BytecodeDemo.class
```

**Key bytecode instructions:**
| Category | Instructions | Description |
|---|---|---|
| Load/Store | `aload`, `iload`, `astore`, `istore` | Move values between local variables and the operand stack |
| Method calls | `invokevirtual`, `invokespecial`, `invokestatic`, `invokeinterface` | Call methods (virtual dispatch, constructors, static, interface) |
| Object creation | `new`, `dup` | Allocate memory for a new object and duplicate the reference |
| Return | `return`, `ireturn`, `areturn` | Return from a method (void, int, object reference) |

Understanding bytecode helps you:
- **Verify what the compiler generates** for your code (e.g., does `String` concatenation use `StringBuilder`?)
- **Understand performance** at a deeper level
- **Debug classloader/compatibility issues** by inspecting the class file version
- **Work with bytecode manipulation libraries** like ASM, ByteBuddy, or Javassist (used by frameworks like Hibernate and Mockito)

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
