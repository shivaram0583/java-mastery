# Module 08 — Advanced Java Concepts

## Topics Covered

### 1. Reflection
- `Class.forName()`, `getDeclaredFields()`, `getDeclaredMethods()`
- Dynamic instantiation with `getConstructor().newInstance()`
- Accessing private fields with `setAccessible(true)`
- Use cases: frameworks, serialization, DI containers

### 2. Custom Annotations
- `@interface` declaration with `@Retention` and `@Target`
- Runtime processing with reflection
- Annotation composition patterns

### 3. Generics Deep Dive
- Bounded type parameters: `<T extends Comparable<T>>`
- Wildcards: `? extends`, `? super` (PECS rule)
- Type erasure — what happens at runtime
- Generic methods and classes

### 4. Design Patterns
- **Singleton** (enum-based, thread-safe)
- **Builder** (fluent API)
- **Strategy** (functional interface approach)
- **Observer** (event-driven)

### 5. Memory Leaks
- Common causes: static collections, unclosed resources, inner classes
- Detection techniques with JVM tools
- Prevention patterns

### 6. Serialization
- `Serializable` vs `Externalizable`
- `transient` keyword
- `serialVersionUID`
- JSON serialization (modern approach)

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
