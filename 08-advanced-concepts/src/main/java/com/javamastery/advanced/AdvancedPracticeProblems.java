package com.javamastery.advanced;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Practice problems for advanced Java concepts.
 */
public class AdvancedPracticeProblems {

    // ==================== Problem 1: Reflection Inspector ====================

    /** Inspect a class and return info about its fields and methods */
    public static String inspectClass(Class<?> clazz) {
        StringBuilder sb = new StringBuilder();
        sb.append("Class: ").append(clazz.getName()).append("\n");

        sb.append("\nFields:\n");
        for (Field f : clazz.getDeclaredFields()) {
            sb.append("  %s %s %s\n".formatted(
                    Modifier.toString(f.getModifiers()),
                    f.getType().getSimpleName(),
                    f.getName()));
        }

        sb.append("\nMethods:\n");
        for (Method m : clazz.getDeclaredMethods()) {
            sb.append("  %s %s %s(%s)\n".formatted(
                    Modifier.toString(m.getModifiers()),
                    m.getReturnType().getSimpleName(),
                    m.getName(),
                    Arrays.stream(m.getParameterTypes())
                            .map(Class::getSimpleName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("")));
        }

        return sb.toString();
    }

    // ==================== Problem 2: Custom @NotNull Validator ====================

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotNull {}

    /** Validates that all @NotNull fields are non-null. Returns list of violation messages. */
    public static List<String> validateNotNull(Object obj) {
        List<String> violations = new ArrayList<>();
        for (Field f : obj.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(NotNull.class)) {
                f.setAccessible(true);
                try {
                    if (f.get(obj) == null) {
                        violations.add("Field '%s' must not be null".formatted(f.getName()));
                    }
                } catch (IllegalAccessException e) {
                    violations.add("Cannot access field '%s'".formatted(f.getName()));
                }
            }
        }
        return violations;
    }

    // Test class for @NotNull validation
    static class UserForm {
        @NotNull String name;
        @NotNull String email;
        String nickname; // optional

        UserForm(String name, String email, String nickname) {
            this.name = name;
            this.email = email;
            this.nickname = nickname;
        }
    }

    // ==================== Problem 3: Generic Pair ====================

    public static class Pair<A, B> {
        private final A first;
        private final B second;

        private Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public static <A, B> Pair<A, B> of(A first, B second) {
            return new Pair<>(first, second);
        }

        public A getFirst() { return first; }
        public B getSecond() { return second; }

        @Override
        public String toString() {
            return "(%s, %s)".formatted(first, second);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Pair<?, ?> other)) return false;
            return Objects.equals(first, other.first) && Objects.equals(second, other.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }

    // ==================== Problem 5: Strategy Pattern ====================

    @FunctionalInterface
    public interface ProcessingStrategy extends UnaryOperator<String> {}

    public static class TextProcessor {
        private ProcessingStrategy strategy;

        public TextProcessor(ProcessingStrategy strategy) {
            this.strategy = strategy;
        }

        public void setStrategy(ProcessingStrategy strategy) {
            this.strategy = strategy;
        }

        public String process(String text) {
            return strategy.apply(text);
        }
    }

    // Built-in strategies
    public static final ProcessingStrategy CAPITALIZE = String::toUpperCase;
    public static final ProcessingStrategy REVERSE = s -> new StringBuilder(s).reverse().toString();
    public static final ProcessingStrategy REMOVE_VOWELS = s -> s.replaceAll("[AEIOUaeiou]", "");
    public static ProcessingStrategy caesarCipher(int shift) {
        return s -> {
            char[] chars = s.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (Character.isLetter(chars[i])) {
                    char base = Character.isUpperCase(chars[i]) ? 'A' : 'a';
                    chars[i] = (char) ((chars[i] - base + shift) % 26 + base);
                }
            }
            return new String(chars);
        };
    }

    // ==================== Problem 6: Generic Bounded Stack ====================

    public static class BoundedStack<T> {
        private final Object[] elements;
        private int size;

        public BoundedStack(int capacity) {
            elements = new Object[capacity];
            size = 0;
        }

        public void push(T item) {
            if (isFull()) throw new IllegalStateException("Stack is full");
            elements[size++] = item;
        }

        @SuppressWarnings("unchecked")
        public T pop() {
            if (isEmpty()) throw new NoSuchElementException("Stack is empty");
            T item = (T) elements[--size];
            elements[size] = null; // help GC
            return item;
        }

        @SuppressWarnings("unchecked")
        public T peek() {
            if (isEmpty()) throw new NoSuchElementException("Stack is empty");
            return (T) elements[size - 1];
        }

        public boolean isEmpty() { return size == 0; }
        public boolean isFull() { return size == elements.length; }
        public int size() { return size; }
    }

    // ==================== Problem 7: Type-Safe Heterogeneous Container ====================

    public static class TypeSafeMap {
        private final Map<Class<?>, Object> map = new HashMap<>();

        public <T> void put(Class<T> type, T value) {
            map.put(type, value);
        }

        public <T> T get(Class<T> type) {
            return type.cast(map.get(type));
        }

        public boolean containsKey(Class<?> type) {
            return map.containsKey(type);
        }
    }

    // ==================== Problem 8: Observer / EventBus ====================

    public static class EventBus {
        private final Map<Class<?>, List<java.util.function.Consumer<Object>>> handlers = new HashMap<>();

        @SuppressWarnings("unchecked")
        public <T> void subscribe(Class<T> eventType, java.util.function.Consumer<T> handler) {
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                    .add(event -> handler.accept((T) event));
        }

        public void publish(Object event) {
            List<java.util.function.Consumer<Object>> eventHandlers = handlers.get(event.getClass());
            if (eventHandlers != null) {
                for (var handler : eventHandlers) {
                    handler.accept(event);
                }
            }
        }
    }

    // Event types for testing
    record UserCreated(String name) {}
    record OrderPlaced(int orderId, double amount) {}

    public static void main(String[] args) {
        System.out.println("=== Advanced Practice Problems ===\n");

        // Problem 1: Reflection Inspector
        System.out.println("--- Problem 1: Reflection Inspector ---");
        System.out.println(inspectClass(UserForm.class));

        // Problem 2: @NotNull Validator
        System.out.println("--- Problem 2: @NotNull Validator ---");
        var validForm = new UserForm("Alice", "alice@example.com", null);
        System.out.println("Valid form violations: " + validateNotNull(validForm));
        var invalidForm = new UserForm(null, "bob@example.com", "Bobby");
        System.out.println("Invalid form violations: " + validateNotNull(invalidForm));

        // Problem 3: Generic Pair
        System.out.println("\n--- Problem 3: Generic Pair ---");
        var pair = Pair.of("Hello", 42);
        System.out.println("Pair: " + pair);
        System.out.println("First: " + pair.getFirst() + ", Second: " + pair.getSecond());

        // Problem 5: Strategy Pattern
        System.out.println("\n--- Problem 5: Strategy Pattern ---");
        var processor = new TextProcessor(CAPITALIZE);
        System.out.println("Capitalize: " + processor.process("hello world"));
        processor.setStrategy(REVERSE);
        System.out.println("Reverse: " + processor.process("hello world"));
        processor.setStrategy(REMOVE_VOWELS);
        System.out.println("Remove vowels: " + processor.process("hello world"));
        processor.setStrategy(caesarCipher(3));
        System.out.println("Caesar(3): " + processor.process("hello"));

        // Problem 6: Bounded Stack
        System.out.println("\n--- Problem 6: Bounded Stack ---");
        var stack = new BoundedStack<Integer>(3);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        System.out.println("Full: " + stack.isFull() + ", Peek: " + stack.peek());
        System.out.println("Pop: " + stack.pop() + ", " + stack.pop() + ", " + stack.pop());
        System.out.println("Empty: " + stack.isEmpty());

        // Problem 7: Type-Safe Map
        System.out.println("\n--- Problem 7: Type-Safe Map ---");
        var tsm = new TypeSafeMap();
        tsm.put(String.class, "Hello");
        tsm.put(Integer.class, 42);
        tsm.put(Double.class, 3.14);
        System.out.println("String: " + tsm.get(String.class));
        System.out.println("Integer: " + tsm.get(Integer.class));
        System.out.println("Double: " + tsm.get(Double.class));

        // Problem 8: EventBus
        System.out.println("\n--- Problem 8: EventBus ---");
        var bus = new EventBus();
        bus.subscribe(UserCreated.class, e -> System.out.println("  User created: " + e.name()));
        bus.subscribe(OrderPlaced.class, e -> System.out.println("  Order placed: #" + e.orderId()));
        bus.publish(new UserCreated("Alice"));
        bus.publish(new OrderPlaced(1001, 99.99));
    }
}
