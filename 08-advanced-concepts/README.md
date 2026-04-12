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
| `ImmutabilityDemo` | Immutable objects, defensive copies |
| `AdvancedPracticeProblems` | Practice implementations for interview prep |

## How to Run
```bash
mvn -pl 08-advanced-concepts exec:java -Dexec.mainClass="com.javamastery.advanced.ReflectionDemo"
mvn -pl 08-advanced-concepts test
```

---

## Interview Questions

**Q1: What is reflection in Java? What are its advantages and disadvantages?**
Reflection is the ability to inspect and manipulate classes, methods, and fields at runtime. **Advantages:** enables frameworks (Spring DI, Hibernate ORM, JUnit), plugin systems, and dynamic proxies. **Disadvantages:** 10-100x slower than direct calls, bypasses compile-time type safety, breaks encapsulation, makes code harder to understand and refactor.

**Q2: How does Spring use reflection?**
Spring scans the classpath for annotated classes (`@Component`, `@Service`), uses `Class.forName()` to load them, creates instances via `Constructor.newInstance()`, discovers `@Autowired` fields/constructors via `getDeclaredFields()`/`getDeclaredConstructors()`, and injects dependencies using `Field.set()`. Configuration annotations (`@Bean`, `@Value`) are similarly processed via reflection.

**Q3: What is type erasure in Java generics?**
At compile time, the Java compiler enforces generic type safety. At runtime, all generic type information is **erased** — `List<String>` becomes plain `List`, and `T` becomes `Object` (or the bound type). Consequences: you cannot do `new T()`, `instanceof List<String>`, or `T.class` at runtime. This was done for backward compatibility with pre-generics code (Java < 5).

**Q4: Explain PECS (Producer Extends, Consumer Super).**
When a structure **produces** values for you to read, use `? extends T` (upper bound) — the structure contains some subtype of T, you can read items as T but cannot add. When a structure **consumes** values you provide, use `? super T` (lower bound) — you can add items of type T but reads return Object. Example: `Collections.copy(List<? super T> dest, List<? extends T> src)`.

**Q5: What is the Singleton pattern? What's the best implementation in Java?**
Singleton ensures exactly one instance exists. The **enum-based singleton** is the best approach: `enum Singleton { INSTANCE; }`. It's (1) thread-safe (JVM guarantees enum initialization is synchronized), (2) serialization-safe (enum serialization is handled by the JVM), and (3) reflection-proof (you cannot instantiate enum via reflection).

**Q6: Explain the Builder pattern. When should you use it?**
Builder constructs complex objects step by step. Use when a class has many fields (especially optional ones) and constructors would be unwieldy (telescoping constructor anti-pattern). Builder provides readable, named setters with method chaining: `Person.builder().name("Bob").age(30).build()`. In contrast to setters on a mutable object, Builder can produce **immutable** objects.

**Q7: What is the Strategy pattern? How does Java 8 simplify it?**
Strategy defines a family of interchangeable algorithms selected at runtime. Before Java 8, each strategy required a concrete class. Java 8's functional interfaces and lambdas dramatically simplify it: `Comparator<String> strategy = String::compareToIgnoreCase;` — the lambda IS the strategy, no extra class needed.

**Q8: What causes memory leaks in Java?**
Common causes: (1) Static collections that grow forever — objects added but never removed. (2) Unclosed resources (connections, streams). (3) Non-static inner classes holding references to enclosing instances. (4) Listeners/callbacks never unregistered. (5) ThreadLocal values in thread pools not cleaned up. (6) HashMap keys with broken `hashCode()`/`equals()` — entries become unreachable but not removable.

**Q9: How do you detect and fix a memory leak?**
**Detection:** Monitor heap usage over time (increasing trend = leak). Take heap dumps (`jmap -dump:format=b,file=heap.hprof <pid>` or `-XX:+HeapDumpOnOutOfMemoryError`). Analyze with Eclipse MAT or VisualVM — look for objects with suspiciously high retained size. **Fix:** Close resources in `finally`/try-with-resources, use `WeakReference` for caches, unregister listeners, call `ThreadLocal.remove()`.

**Q10: What is `Serializable` and what is `serialVersionUID`?**
`Serializable` is a marker interface indicating a class can be converted to/from bytes. `serialVersionUID` is a version number — if the deserialized bytes have a different UID than the current class, `InvalidClassException` is thrown. Always declare it explicitly (`private static final long serialVersionUID = 1L;`) to control versioning rather than letting Java auto-generate it.

**Q11: Why is Java's native serialization considered a security risk?**
Deserialization creates objects and invokes methods from untrusted byte streams. Attackers can craft malicious payloads that trigger dangerous operations during deserialization (gadget chains). This has led to numerous CVEs. **Alternatives:** JSON serialization (Jackson/Gson), Protocol Buffers, or using deserialization filters (ObjectInputFilter, Java 9+).

**Q12: What is the `transient` keyword?**
`transient` excludes a field from Java serialization. Use for: sensitive data (passwords), derived/computed fields (calculable from other fields), non-serializable fields (DB connections, threads), and large data that can be reconstructed.

**Q13: Explain the Observer pattern. Where is it used in Java?**
Observer defines a one-to-many dependency: when the subject changes, all observers are notified. Java implementations: `java.beans.PropertyChangeListener`, Swing event listeners, RxJava `Observable`, `Flow.Publisher` (Java 9+). Modern alternatives: event bus (Guava), reactive streams (Project Reactor, RxJava).

**Q14: What are the differences between checked and unchecked exceptions?**
| Aspect | Checked | Unchecked |
|---|---|---|
| Extends | `Exception` | `RuntimeException` |
| Compiler enforces | Yes (must catch or declare) | No |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Use for | Recoverable conditions | Programming errors |

**Q15: What is an immutable class? How do you create one?**
An immutable class cannot be modified after creation. Rules: (1) Declare class `final`. (2) Make all fields `private final`. (3) Don't provide setters. (4) Deep-copy mutable arguments in constructor. (5) Return defensive copies of mutable fields. Examples: `String`, `Integer`, `LocalDate`. Benefits: thread-safe without synchronization, safe as HashMap keys, easier to reason about.

**Q16: What is the difference between deep copy and shallow copy?**
**Shallow copy** copies the object but shares references to internal objects — modifying an internal object in the copy affects the original. **Deep copy** recursively copies all internal objects — the copy is fully independent. Use deep copy for immutable objects that contain mutable collections. `clone()` produces shallow copy by default; for deep copy, implement it manually or use serialization/copy libraries.

**Q17: What is the Diamond Problem and how does Java handle it?**
The diamond problem occurs when a class inherits from two classes that have the same method. Java prevents this by disallowing multiple class inheritance. For interfaces (which allow multiple inheritance of default methods), Java requires the implementing class to override the conflicting method to resolve ambiguity.

**Q18: Explain covariant return types in Java.**
A subclass can override a method and return a more specific type than the parent. Example: `Object clone()` → `MyClass clone()`. This avoids unnecessary casting for the caller. Added in Java 5.

**Q19: What is the difference between `Comparable` and `Comparator`?**
`Comparable<T>` defines the **natural ordering** of a class (implemented by the class itself, `compareTo()` method, single ordering). `Comparator<T>` defines **external orderings** (separate class/lambda, `compare()` method, allows multiple orderings). Use `Comparable` for the default sort; use `Comparator` for custom/alternative sorts.

**Q20: What are Functional Interfaces? Name some built-in ones.**
A functional interface has exactly one abstract method and can be used as a lambda target. Key built-in ones: `Function<T,R>` (T→R), `Predicate<T>` (T→boolean), `Consumer<T>` (T→void), `Supplier<T>` (→T), `BiFunction<T,U,R>` (T,U→R), `UnaryOperator<T>` (T→T), `BinaryOperator<T>` (T,T→T).

---

## Practice Problems

### Easy
1. **Reflection Inspector:** Write a method that takes a `Class<?>` and prints all declared fields (name, type, modifiers) and all declared methods (name, return type, parameters).
2. **Custom Annotation:** Create an `@NotNull` annotation. Write a validator method that uses reflection to check if any `@NotNull`-annotated field is null.
3. **Generic Pair:** Implement a generic `Pair<A, B>` class with `getFirst()`, `getSecond()`, and a static factory method `of(A, B)`.
4. **Immutable Person:** Create an immutable `ImmutablePerson` class with name, age, and a list of hobbies. Ensure the hobbies list cannot be modified externally.

### Medium
5. **Builder Pattern:** Implement a `HttpRequest` class using the Builder pattern with fields: url, method, headers (Map), body, timeout. Make `HttpRequest` immutable.
6. **Strategy Pattern:** Create a `TextProcessor` that accepts a `ProcessingStrategy` (functional interface). Implement strategies: capitalize, reverse, remove vowels, Caesar cipher.
7. **Observer Pattern:** Implement an `EventBus` with `subscribe(eventType, handler)`, `unsubscribe()`, and `publish(event)`. Support multiple event types.
8. **Generic Stack:** Implement a generic `BoundedStack<T>` with a max capacity. Implement `push`, `pop`, `peek`, `isEmpty`, `isFull`.
9. **Type-safe Heterogeneous Container:** Implement a `TypeSafeMap` that can store one value per type: `put(Class<T>, T)` and `get(Class<T>): T`.

### Hard
10. **Annotation-driven Validator:** Create `@Min(value)`, `@Max(value)`, `@Pattern(regex)` annotations. Write a validator that inspects annotated fields and returns validation errors.
11. **Simple Dependency Injection:** Implement a tiny DI container: scan a package for classes annotated with `@Component`, create instances, and inject dependencies annotated with `@Inject`.
12. **Memory Leak Detector:** Write a program that creates a known memory leak (growing static list). Use `Runtime` to monitor heap usage and detect the growing trend.

### Challenge
13. **Dynamic Proxy:** Use `java.lang.reflect.Proxy` to create a logging proxy that logs every method call (name, arguments, return value) on any interface.
14. **Simple ORM:** Using reflection, create a `SimpleORM` that reads `@Table` and `@Column` annotations from a class and generates SQL `CREATE TABLE` and `INSERT` statements.

---

## Common Mistakes Cheat Sheet

| Mistake | Why It's Wrong | Fix |
|---|---|---|
| Overusing reflection in application code | Slow, fragile, bypasses type safety | Use it only in frameworks/libraries/tests |
| Not caching reflected `Method`/`Field` | Lookup is expensive — repeating it is wasteful | Cache in static field or `Map` |
| Mutable objects as `HashMap` keys | If key changes after insert, entry is unreachable | Use immutable keys or don't modify after insert |
| Raw types (`List` instead of `List<String>`) | Loses type safety, defeats the purpose of generics | Always use parameterized types |
| `@SuppressWarnings("unchecked")` everywhere | Hides real type safety problems | Fix the root cause, suppress only when truly safe |
| Forgetting `serialVersionUID` | Auto-generated UID changes with any class change | Always declare explicitly |
| Mutable fields in "immutable" class | External code can modify internal state | Use defensive copies in constructor and getters |
| Singleton via `static` field without lazy init | May cause issues with complex initialization | Use enum singleton or holder class pattern |
