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
| Memory Tuning | `MemoryTuningDemo.java` | — |
| Practice Problems | `JvmPracticeProblems.java` | — |
| JVM Flags | See README for flags reference | — |

---

## Interview Questions

### JVM Architecture

**Q1: Explain the JVM architecture. What are the main components?**

The JVM has three main subsystems:
1. **Class Loader Subsystem** — Loads `.class` files into memory, verifies bytecode, resolves symbolic references, and initializes static variables and blocks.
2. **Runtime Data Areas** — Memory regions used during execution:
   - **Heap** — Shared by all threads; stores all objects and arrays
   - **Stack** — Per-thread; stores local variables, method call frames, partial results
   - **Method Area (Metaspace)** — Stores class metadata, constant pool, static variables
   - **PC Register** — Per-thread; holds address of current executing instruction
   - **Native Method Stack** — Per-thread; for native (JNI) method calls
3. **Execution Engine** — Interprets or compiles bytecode to native code:
   - Interpreter, JIT Compiler (C1/C2), Garbage Collector

**Q2: What is the difference between JDK, JRE, and JVM?**

```
  JDK (Java Development Kit)
  ┌─────────────────────────────────────────┐
  │  javac, jdb, jconsole, jshell, etc.     │ ← Development tools
  │  ┌─────────────────────────────────┐    │
  │  │  JRE (Java Runtime Environment) │    │
  │  │  ┌─────────────────────────┐    │    │
  │  │  │  JVM (Virtual Machine)  │    │    │ ← Executes bytecode
  │  │  └─────────────────────────┘    │    │
  │  │  rt.jar / core libraries        │    │ ← Standard library
  │  └─────────────────────────────────┘    │
  └─────────────────────────────────────────┘
```

- **JVM** — The virtual machine that executes bytecode. Platform-specific (different JVM per OS).
- **JRE** — JVM + core libraries. Enough to RUN Java programs.
- **JDK** — JRE + development tools (compiler, debugger, profiler). Needed to DEVELOP Java programs.

> Note: Since Java 11, JRE is no longer distributed separately. Use `jlink` to create custom runtimes.

**Q3: What is the difference between stack and heap memory?**

| Aspect | Stack | Heap |
|--------|-------|------|
| Stored | Local variables, method frames, references | Objects, instance variables |
| Scope | Per-thread (thread-safe) | Shared by all threads |
| Lifetime | Destroyed when method returns | Garbage collected when unreachable |
| Speed | Very fast (LIFO push/pop) | Slower (complex allocation) |
| Size | Small (default ~1MB per thread) | Large (can be several GB) |
| Error | `StackOverflowError` | `OutOfMemoryError` |
| Management | Automatic (LIFO) | Garbage collector |

---

### Class Loading

**Q4: What is the class loading mechanism in Java? Explain the delegation model.**

When the JVM needs a class, it follows the **parent-first delegation model**:
1. Application ClassLoader receives the request
2. Delegates to Platform (Extension) ClassLoader
3. Delegates to Bootstrap ClassLoader
4. If Bootstrap can't find it → Platform tries → Application tries
5. If no one finds it → `ClassNotFoundException`

This prevents application code from replacing core Java classes (e.g., you can't create your own `java.lang.String`).

**Q5: What are the three phases of class loading?**

1. **Loading** — Find the `.class` file (from JAR, network, etc.) and read the bytecode into memory. A `Class<?>` object is created in the heap.
2. **Linking:**
   - **Verification** — Check bytecode for structural correctness, valid instructions, proper stack manipulation
   - **Preparation** — Allocate memory for static fields and set default values (0, null, false)
   - **Resolution** — Resolve symbolic references (class names, method names) to direct references
3. **Initialization** — Execute static initializers (`static {}` blocks) and set static field values from assignments

**Q6: Can two classes with the same name exist in the JVM?**

Yes! Class identity is determined by **ClassLoader + Fully Qualified Name**. The same `.class` file loaded by two different classloaders produces two DIFFERENT types that cannot be cast to each other. This is used by:
- Application servers (Tomcat, JBoss) for app isolation
- OSGi for module isolation
- Hot-reloading frameworks

**Q7: What is a `ClassNotFoundException` vs `NoClassDefFoundError`?**

| | `ClassNotFoundException` | `NoClassDefFoundError` |
|---|---|---|
| Type | Checked exception | Error (unchecked) |
| When | Class not found during explicit loading (`Class.forName()`) | Class was available at compile-time but missing at runtime |
| Cause | Wrong classpath, missing JAR | Removed JAR, failed static initializer |
| Recovery | Possible (try-catch) | Usually fatal |

---

### Memory Model

**Q8: Explain the Java Memory Model (JMM). What is the happens-before relationship?**

The JMM defines how threads interact through memory and what behaviors are allowed. It addresses the fact that modern CPUs have caches and may reorder instructions.

**Happens-before** guarantees that memory writes by one thread are visible to reads by another thread. Key happens-before rules:
1. **Monitor lock:** Unlock happens-before subsequent lock on the same monitor
2. **Volatile:** Write to volatile variable happens-before subsequent read of the same variable
3. **Thread start:** `thread.start()` happens-before any action in the started thread
4. **Thread join:** Actions in a thread happen-before `join()` returns
5. **Final fields:** Writing a final field in a constructor happens-before the reference is read by another thread

**Q9: What is Metaspace? How is it different from PermGen?**

| Aspect | PermGen (Java ≤ 7) | Metaspace (Java 8+) |
|--------|--------------------|--------------------|
| Location | Heap (fixed size) | Native memory (off-heap) |
| Default size | 64MB–256MB (fixed) | Unlimited (grows as needed) |
| Error | `OutOfMemoryError: PermGen space` | `OutOfMemoryError: Metaspace` |
| Tuning | `-XX:MaxPermSize` | `-XX:MaxMetaspaceSize` (optional limit) |
| GC | Hard to tune, frequent cause of OOM | Automatic, rarely an issue |

Metaspace stores class metadata, method bytecode, constant pools, and annotations. It grows automatically and is only collected when classloaders are garbage collected.

**Q10: What causes `StackOverflowError`? How can you fix it?**

Caused by too-deep recursion (the call stack exceeds its size limit). Each method call adds a frame to the stack. When the stack is full, the JVM throws `StackOverflowError`.

Fixes:
- Convert recursion to iteration
- Use tail-call optimization (Java doesn't support this natively, but you can restructure manually)
- Increase stack size with `-Xss` (e.g., `-Xss4m`) — use sparingly, as each thread gets this amount

---

### Garbage Collection

**Q11: How does garbage collection work? What determines if an object is eligible for GC?**

The GC uses **reachability analysis** starting from **GC roots**:
- Local variables on the stack
- Active threads
- Static fields
- JNI references

Objects reachable from any GC root (directly or transitively) are ALIVE. Unreachable objects are GARBAGE and can be collected.

> Note: Having a reference to an object does NOT prevent GC if the reference itself is unreachable. It's the chain from GC roots that matters.

**Q12: What is the generational garbage collection model? Why does it work?**

The **generational hypothesis** states that most objects die young. The heap is divided by age:

1. **Young Generation (Eden + Survivors):**
   - New objects go to Eden
   - Minor GC: Copy survivors to Survivor space, increment age
   - Fast and frequent (~milliseconds)
2. **Old Generation (Tenured):**
   - Objects surviving many Minor GCs get promoted here
   - Major GC / Full GC: Less frequent but more expensive
   - Can cause noticeable pauses

This works because ~95% of objects become unreachable before the first Minor GC. By concentrating on the Young Generation, the GC avoids scanning the entire heap every time.

**Q13: Compare G1, ZGC, and Shenandoah garbage collectors.**

| Feature | G1GC | ZGC | Shenandoah |
|---------|------|-----|-----------|
| Default since | Java 9 | — | — |
| Max pause target | 200ms (tunable) | <1ms | <10ms |
| Concurrent | Partially | Fully | Fully |
| Compaction | During pause | Concurrent | Concurrent |
| Max heap | ~32GB practical | Multi-TB | Multi-TB |
| Complexity | Medium | Low (fewer tuning knobs) | Low |
| Use case | General purpose | Low-latency, large heaps | Low-latency |

**Q14: What is the difference between Minor GC, Major GC, and Full GC?**

- **Minor GC** — Collects only the Young Generation. Fast (~10ms). Triggered when Eden is full.
- **Major GC** — Collects only the Old Generation. Slower. Triggered when Old Gen is filling up.
- **Full GC** — Collects the ENTIRE heap (Young + Old + Metaspace). Most expensive. Triggered by `System.gc()`, Metaspace expansion, or when concurrent GC fails.

> `System.gc()` is a *suggestion* to the JVM, not a command. The JVM may ignore it. Never rely on it in production.

**Q15: What are weak, soft, and phantom references?**

| Reference Type | GC Behavior | Use Case |
|---------------|-------------|---------|
| **Strong** (`Object o = new Obj()`) | Never collected while referenced | Normal references |
| **Soft** (`SoftReference<T>`) | Collected only under memory pressure | Memory-sensitive caches |
| **Weak** (`WeakReference<T>`) | Collected at next GC regardless of memory | WeakHashMap, canonicalization |
| **Phantom** (`PhantomReference<T>`) | Enqueued after finalization | Resource cleanup, pre-mortem tracking |

**Q16: What are common JVM tuning flags for production?**

```bash
# Heap sizing
-Xms4g                        # Initial heap (set equal to -Xmx for production)
-Xmx4g                        # Maximum heap

# GC selection
-XX:+UseG1GC                   # G1 (default Java 9+)
-XX:+UseZGC                    # ZGC (for ultra-low latency)

# G1 tuning
-XX:MaxGCPauseMillis=200       # Target pause time
-XX:G1HeapRegionSize=16m       # Region size (1-32MB, power of 2)

# Metaspace
-XX:MaxMetaspaceSize=256m      # Limit metaspace growth

# Diagnostics
-Xlog:gc*:file=gc.log:time     # GC logging (Java 9+ unified logging)
-XX:+HeapDumpOnOutOfMemoryError # Auto heap dump on OOM
-XX:HeapDumpPath=/path/dump.hprof

# Performance
-XX:+UseStringDeduplication    # Deduplicate strings in G1 (saves memory)
-XX:+AlwaysPreTouch            # Touch all heap pages at startup (avoid lazy allocation)
```

---

### JIT Compilation

**Q17: What is JIT compilation? How does tiered compilation work?**

JIT (Just-In-Time) compilation converts hot bytecode to native machine code at runtime, achieving near-native performance. Tiered compilation uses multiple levels:

| Level | Compiler | Trigger | Optimizations |
|-------|----------|---------|---------------|
| 0 | Interpreter | All code initially | None (profiling data collected) |
| 1-3 | C1 (Client) | ~1,500 invocations | Basic: inlining, constant folding |
| 4 | C2 (Server) | ~10,000 invocations | Aggressive: escape analysis, loop unrolling, vectorization |

The JVM can also **deoptimize** — if assumptions made during compilation are invalidated (e.g., a class is newly loaded that changes method dispatch), the JVM reverts to interpreted mode and recompiles.

**Q18: What is escape analysis? How does it improve performance?**

Escape analysis determines whether an object created inside a method "escapes" to the outside world (returned, stored in a field, passed to another thread). If it does NOT escape:

1. **Stack allocation** — Object allocated on the stack instead of heap (no GC needed)
2. **Scalar replacement** — Object eliminated entirely; its fields become local variables
3. **Lock elision** — Synchronization on the non-escaping object is removed

This is why creating short-lived objects in Java is often "free" — the JIT compiler eliminates them entirely.

**Q19: What is On-Stack Replacement (OSR)?**

OSR allows the JVM to switch from interpreted to compiled code **mid-execution** — even inside a running loop. Without OSR, a long-running loop would remain in interpreted mode until the method is called again.

---

### Bytecode

**Q20: What is bytecode and why is it important?**

Bytecode is the platform-independent instruction set that the JVM executes. Each `.class` file contains bytecode — an intermediate representation between source code and machine code.

Why it matters:
- **Platform independence** — Same bytecode runs on any JVM (Windows, Linux, macOS)
- **Security** — Bytecode is verified before execution (type safety, stack bounds)
- **Performance** — JIT compiles hot paths to native code
- **Language interoperability** — Kotlin, Scala, Groovy all compile to JVM bytecode

**Q21: What are the key bytecode instructions for method invocation?**

| Instruction | Used For |
|------------|---------|
| `invokevirtual` | Regular instance method calls (with virtual dispatch) |
| `invokeinterface` | Methods called through an interface reference |
| `invokespecial` | Constructors, private methods, super calls (no virtual dispatch) |
| `invokestatic` | Static method calls |
| `invokedynamic` | Lambda expressions, string concatenation (Java 9+), dynamic language support |

`invokedynamic` is particularly important — it's how lambdas achieve better performance than anonymous inner classes.

---

## Practice Problems

### Easy
1. **ClassLoader Identification:** Write a program that prints the classloader for `String.class`, `java.sql.Connection.class`, and your own class. Explain why each uses a different classloader.
2. **Memory Areas Demo:** Create objects of varying sizes and observe Eden/Survivor/Old Gen usage via GC logs (`-Xlog:gc*`).
3. **GC Algorithm Selection:** Run the same memory-intensive program with Serial, Parallel, G1, and ZGC. Compare pause times and throughput.

### Medium
4. **Custom ClassLoader:** Implement a classloader that loads `.class` files from a custom directory (not on the classpath). Demonstrate loading and instantiating the class.
5. **Memory Leak Simulation:** Intentionally create a memory leak using a static collection, detect it with heap dumps, and fix it.
6. **GC Tuning Challenge:** Given a program that allocates/deallocates objects in bursts, tune GC parameters (`-Xmx`, `-XX:MaxGCPauseMillis`, region sizes) to minimize pause times.
7. **String Pool Investigation:** Demonstrate the string pool's behavior with `String.intern()`, `.equals()` vs `==`, and memory implications.

### Hard
8. **Bytecode Analysis:** Write a simple method, compile it, and analyze the bytecode using `javap -c`. Predict what bytecode instructions a given Java snippet produces.
9. **JIT Compilation Observation:** Use `-XX:+PrintCompilation` to observe which methods get compiled, at what tier, and how performance changes during warm-up.
10. **Escape Analysis Verification:** Write code where objects do and don't escape, and verify using GC logs that non-escaping objects are stack-allocated (no GC pressure).

### Challenge
11. **Production GC Analysis:** Analyze a GC log file to determine: average pause time, frequency of Minor vs Major GC, memory allocation rate, and promotion rate. Recommend tuning changes.
12. **ClassLoader Isolation:** Build a plugin system where each plugin is loaded by its own classloader, enabling independent version upgrades and isolation.

---

## Quick Reference: Essential JVM Flags

```
Category          Flag                              Purpose
─────────         ────                              ───────
Heap              -Xms / -Xmx                       Min/max heap size
Stack             -Xss                               Thread stack size
GC                -XX:+UseG1GC/UseZGC/UseShenandoahGC  GC algorithm
GC Tuning         -XX:MaxGCPauseMillis               Target pause (G1)
GC Tuning         -XX:NewRatio                        Young:Old ratio
Metaspace         -XX:MaxMetaspaceSize                Metaspace limit
Logging           -Xlog:gc*                          GC logging
Diagnostics       -XX:+HeapDumpOnOutOfMemoryError    Auto heap dump
JIT               -XX:+PrintCompilation              JIT compilation log
JIT               -XX:CompileThreshold               Invocations before compile
Debug             -XX:+ShowCodeDetailsInExceptionMessages  Helpful NPE messages
```
