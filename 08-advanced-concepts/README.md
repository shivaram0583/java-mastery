# Module 08 — Advanced Java Concepts

This module covers advanced Java features that are essential for building robust, maintainable applications and for understanding how popular frameworks (Spring, Hibernate, Jackson) work under the hood.

---

## Topics Covered

### 1. Reflection

**What is Reflection?**
Reflection is Java’s ability to inspect and manipulate classes, methods, fields, and constructors **at runtime** — even if you don’t know their names at compile time. It’s like looking at the blueprint of a building while you’re inside it and being able to open any door, even the locked ones.

> **Real-world analogy:** Imagine you receive a sealed box (a compiled Java class). Normally, you can only use the buttons on the outside (public methods). Reflection lets you open the box, see all the internal wiring (private fields, methods), and even rewire things (change field values, invoke private methods).

**Key operations:**
- `Class.forName("com.example.MyClass")` — Load a class by its fully qualified name at runtime
- `getDeclaredFields()` / `getDeclaredMethods()` — List all fields/methods (including private ones)
- Dynamic instantiation with `getConstructor().newInstance()` — Create objects without knowing the class at compile time
- Accessing private fields with `setAccessible(true)` — Bypass access control checks

```
  Compile-time (normal code):           Runtime (reflection):
  ┌──────────────────────┐              ┌──────────────────────┐
  │ MyClass obj = new    │              │ Class<?> clazz =     │
  │   MyClass();         │              │   Class.forName(...) │
  │ obj.publicMethod();  │              │ Object obj = clazz   │
  │                      │              │   .newInstance();     │
  │ // Can't access      │              │ Field f = clazz      │
  │ // private fields    │              │   .getDeclaredField  │
  │                      │              │   ("secretField");   │
  │                      │              │ f.setAccessible(true)│
  │                      │              │ f.get(obj); // works!│
  └──────────────────────┘              └──────────────────────┘
```

**Where is reflection used?**
- **Frameworks:** Spring uses reflection to scan annotations (`@Autowired`, `@Component`) and inject dependencies. Hibernate uses it to map Java objects to database tables.
- **Serialization:** Jackson/Gson use reflection to read object fields and convert them to JSON.
- **Testing:** Mocking frameworks (Mockito) use reflection to create proxy objects.
- **Plugin systems:** Load and instantiate classes dynamically from JARs.

**Performance note:** Reflection is significantly slower than direct method calls (10–100x). It also bypasses compile-time type safety. Use it only when necessary and cache reflected `Method`/`Field` objects when possible.

---

### 2. Custom Annotations

**What are annotations?**
Annotations are metadata markers that you attach to classes, methods, fields, or parameters. They don’t change what the code does directly, but tools and frameworks can read them (via reflection) to add behavior.

You’ve already used built-in annotations like `@Override` (compile-time check) and `@Deprecated` (marks obsolete code). Custom annotations let you define your own markers.

**Creating a custom annotation:**
```java
@Retention(RetentionPolicy.RUNTIME)  // Available at runtime via reflection
@Target(ElementType.METHOD)          // Can only be placed on methods
public @interface LogExecutionTime {
    String value() default "default";
}
```

**Retention policies** determine when the annotation is available:
```
  SOURCE  ──► Discarded by compiler (e.g., @Override, @SuppressWarnings)
  CLASS   ──► Kept in .class file but not available at runtime (default)
  RUNTIME ──► Available at runtime via reflection (e.g., @Autowired, @Test)
```

**Processing at runtime:**
```java
for (Method method : clazz.getDeclaredMethods()) {
    if (method.isAnnotationPresent(LogExecutionTime.class)) {
        // Execute the method and measure time
    }
}
```

**Annotation composition patterns:** You can create annotations that combine multiple other annotations. For example, Spring’s `@SpringBootApplication` combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan` into one.

---

### 3. Generics Deep Dive

**What are generics?**
Generics allow you to write classes and methods that work with **any type** while still maintaining compile-time type safety. Before generics (Java < 5), collections stored `Object` references and required unsafe casts.

```
  Without generics:                    With generics:
  List list = new ArrayList();         List<String> list = new ArrayList<>();
  list.add("hello");                   list.add("hello");
  list.add(42); // compiles!           list.add(42); // COMPILE ERROR ✓
  String s = (String) list.get(0);     String s = list.get(0); // no cast needed
  String s2 = (String) list.get(1);    
  // ClassCastException at RUNTIME!    
```

**Bounded type parameters:**
- `<T extends Comparable<T>>` — T must implement `Comparable`. This lets you call `compareTo()` on T values inside the method.

**Wildcards and the PECS Rule:**
The acronym **PECS** stands for **Producer Extends, Consumer Super** — it guides you on when to use `? extends` vs `? super`.

```
  PECS: Producer Extends, Consumer Super

  ┌─────────────────────────────────────────────────────────┐
  │ ? extends T  (Upper bound — PRODUCER)                   │
  │ You can READ items as T, but CANNOT add items           │
  │ Example: List<? extends Number> → can read Number,      │
  │          but can’t add Integer (list might be            │
  │          List<Double>!)                                  │
  ├─────────────────────────────────────────────────────────┤
  │ ? super T  (Lower bound — CONSUMER)                     │
  │ You can ADD items of type T, but reads return Object     │
  │ Example: List<? super Integer> → can add Integer,       │
  │          list might be List<Number> or List<Object>      │
  └─────────────────────────────────────────────────────────┘
```

> **When to use PECS:** If a method only reads from a collection, use `? extends T`. If it only writes to a collection, use `? super T`. If it both reads and writes, use an exact type `T`.

**Type erasure:**
At compile time, generics provide type safety. But at runtime, the JVM erases all generic type information — `List<String>` and `List<Integer>` both become plain `List`. This means:
- You cannot do `new T()` or `new T[]` at runtime
- You cannot do `instanceof List<String>`
- This is a trade-off Java made for backward compatibility with pre-generics code

---

### 4. Design Patterns

Design patterns are reusable solutions to common software design problems. They are not code you copy-paste but rather **blueprints** for structuring your code.

**Singleton** — Ensures a class has exactly one instance and provides a global access point to it.
```
  ┌─────────────────────────────────────────┐
  │          Singleton                       │
  │  ┌───────────────────────┐              │
  │  │    INSTANCE (single)  │              │
  │  └───────────┴───────────┘              │
  │              │                           │
  │  getInstance() always returns            │
  │  the same INSTANCE                       │
  └─────────────────────────────────────────┘
```
> The enum-based singleton is the best approach in Java — it’s thread-safe, serialization-safe, and reflection-proof.

**Builder** — Constructs complex objects step by step, allowing you to set only the fields you need. Commonly used for objects with many optional parameters.
```java
User user = User.builder()
    .name("Alice")
    .email("alice@example.com")
    .age(30)              // optional
    .build();
```

**Strategy** — Defines a family of algorithms, encapsulates each one, and makes them interchangeable. In Java 8+, strategies are often expressed as functional interfaces / lambdas.
```
  ┌──────────────┐     ┌──────────────────────────┐
  │   Context    │────►│   Strategy (interface)   │
  │              │     │   execute()               │
  └──────────────┘     └──────────┬───────────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
               StrategyA    StrategyB    StrategyC
```
> Example: A sorting method that accepts a `Comparator` — the comparator IS the strategy.

**Observer** — Defines a one-to-many dependency: when one object (the subject) changes state, all its dependents (observers) are notified automatically.
```
  ┌──────────┐  state change   ┌──────────┐
  │ Subject  │────notify()───►│Observer 1│
  │(e.g.,    │────notify()───►│Observer 2│
  │ EventBus)│────notify()───►│Observer 3│
  └──────────┘                 └──────────┘
```
> Used heavily in event-driven systems: GUI frameworks, message brokers, reactive streams.

---

### 5. Memory Leaks

A memory leak in Java occurs when objects are no longer needed by the application but are still **referenced**, preventing the garbage collector from reclaiming them. Over time, this causes the application to consume more and more memory, eventually leading to `OutOfMemoryError`.

> **Key insight:** Java’s garbage collector can only reclaim objects that have **no references** pointing to them. If your code accidentally keeps references to objects it no longer uses, those objects stay in memory forever.

**Common causes:**
```
  ┌─────────────────────────────────────────────────────┐
  │ 1. Static collections that grow forever             │
  │    static List<Data> cache = new ArrayList<>();     │
  │    → Objects added but never removed                │
  ├─────────────────────────────────────────────────────┤
  │ 2. Unclosed resources                               │
  │    Connection conn = ds.getConnection();            │
  │    → Never closed, holds native memory + pooling    │
  ├─────────────────────────────────────────────────────┤
  │ 3. Inner class references                           │
  │    Non-static inner class holds reference to        │
  │    enclosing class → enclosing can’t be GC’d        │
  ├─────────────────────────────────────────────────────┤
  │ 4. Listeners/callbacks never unregistered           │
  │    button.addListener(myListener);                  │
  │    → myListener (and everything it references)      │
  │      stays alive as long as button exists            │
  ├─────────────────────────────────────────────────────┤
  │ 5. ThreadLocal values in thread pools               │
  │    Values persist across task executions             │
  └─────────────────────────────────────────────────────┘
```

**Detection techniques:**
- **Heap dumps:** Use `jmap -dump:format=b,file=heap.hprof <pid>` and analyze with Eclipse MAT or VisualVM
- **JVM flags:** `-XX:+HeapDumpOnOutOfMemoryError` — automatically create heap dump on OOM
- **Monitoring:** Track heap usage over time; a steadily increasing trend suggests a leak

**Prevention:** Use try-with-resources for `AutoCloseable` objects, use `WeakReference` for caches, always unregister listeners, and call `ThreadLocal.remove()` in thread pools.

---

### 6. Serialization

**What is serialization?**
Serialization is the process of converting a Java object into a sequence of bytes (so it can be saved to a file, sent over a network, or stored in a database). **Deserialization** is the reverse — reconstructing the object from bytes.

```
  Serialization:                         Deserialization:
  ┌──────────┐    ┌──────────────┐      ┌──────────────┐    ┌──────────┐
  │  Java    │───►│  Byte Stream │──────►│  Byte Stream │───►│  Java    │
  │  Object  │    │  (file/net)  │      │  (file/net)  │    │  Object  │
  └──────────┘    └──────────────┘      └──────────────┘    └──────────┘
```

**`Serializable` vs `Externalizable`:**
- `Serializable` — A marker interface (no methods). Java handles serialization automatically using reflection. Simple but less control.
- `Externalizable` — Requires you to implement `writeExternal()` and `readExternal()`. Full control over what is serialized and how.

**Important keywords and concepts:**
- **`transient`** — Marks a field to be excluded from serialization. Use for sensitive data (passwords), derived/computed fields, or non-serializable objects.
- **`serialVersionUID`** — A version number for the serialized form. If you change a class and the UID doesn’t match, deserialization fails with `InvalidClassException`. Always declare it explicitly to avoid surprises.

**Modern approach — JSON serialization:**
In modern Java, binary serialization is largely replaced by JSON (using Jackson or Gson). JSON is human-readable, language-independent, and avoids the security risks of Java’s native serialization (which can be exploited for remote code execution).

---

## Demos in This Module
| Class | Topics |
|---|---|
| `ReflectionDemo` | Class inspection, dynamic invocation, private access |
| `AnnotationsDemo` | Custom annotations, runtime processing |
| `GenericsDemo` | Bounded types, wildcards, PECS, type erasure |
| `DesignPatternsDemo` | Singleton, Builder, Strategy, Observer |
| `MemoryLeaksDemo` | Common leak patterns and fixes |
| `SerializationDemo` | Serializable, transient, custom serialization |

## How to Run
```bash
mvn -pl 08-advanced-concepts exec:java -Dexec.mainClass="com.javamastery.advanced.ReflectionDemo"
mvn -pl 08-advanced-concepts test
```
