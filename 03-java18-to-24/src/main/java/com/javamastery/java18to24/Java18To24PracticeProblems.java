package com.javamastery.java18to24;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Practice problems for Java 18–24 features.
 */
public class Java18To24PracticeProblems {

    // ==================== Virtual Threads ====================

    /** Problem 1: Create virtual threads using different approaches */
    public static List<String> createVirtualThreads() throws Exception {
        List<String> results = Collections.synchronizedList(new ArrayList<>());

        // Approach 1: Thread.ofVirtual().start()
        Thread t1 = Thread.ofVirtual().name("vt-1").start(() ->
                results.add("approach1: " + Thread.currentThread().isVirtual()));
        t1.join();

        // Approach 2: Thread.startVirtualThread()
        Thread t2 = Thread.startVirtualThread(() ->
                results.add("approach2: " + Thread.currentThread().isVirtual()));
        t2.join();

        // Approach 3: Executor
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future = executor.submit(() ->
                    "approach3: " + Thread.currentThread().isVirtual());
            results.add(future.get());
        }

        return results;
    }

    /** Problem 4: Simulate handling many concurrent requests */
    public static int simulateConcurrentRequests(int numRequests) throws Exception {
        var counter = new java.util.concurrent.atomic.AtomicInteger(0);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < numRequests; i++) {
                futures.add(executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(10)); // simulate I/O
                    counter.incrementAndGet();
                    return null;
                }));
            }
            for (Future<?> f : futures) {
                f.get();
            }
        }

        return counter.get();
    }

    // ==================== Record Patterns ====================

    /** Problem 5: Nested record destructuring */
    public record Employee(String name, int age) {}
    public record Department(String name, Employee head) {}
    public record Company(String name, List<Department> departments) {}

    /** Extract all department head names from a company */
    public static List<String> extractHeadNames(Company company) {
        return company.departments().stream()
                .map(dept -> dept.head().name())
                .collect(Collectors.toList());
    }

    /** Describe an employee using record pattern */
    public static String describeEmployee(Object obj) {
        if (obj instanceof Employee(String name, int age)) {
            return name + " is " + age + " years old";
        }
        return "Not an employee";
    }

    /** Describe a department with nested destructuring */
    public static String describeDepartment(Object obj) {
        if (obj instanceof Department(String deptName, Employee(String empName, int age))) {
            return deptName + " headed by " + empName + " (age " + age + ")";
        }
        return "Not a department";
    }

    // ==================== Pattern Matching Switch ====================

    /** Problem 6: Calculator using sealed hierarchy + pattern matching */
    public sealed interface Expression permits
            Literal, Negate, Add, Multiply, Divide {}

    public record Literal(double value) implements Expression {}
    public record Negate(Expression expr) implements Expression {}
    public record Add(Expression left, Expression right) implements Expression {}
    public record Multiply(Expression left, Expression right) implements Expression {}
    public record Divide(Expression left, Expression right) implements Expression {}

    public static double evaluate(Expression expr) {
        return switch (expr) {
            case Literal(double v) -> v;
            case Negate(var inner) -> -evaluate(inner);
            case Add(var l, var r) -> evaluate(l) + evaluate(r);
            case Multiply(var l, var r) -> evaluate(l) * evaluate(r);
            case Divide(var l, var r) -> {
                double divisor = evaluate(r);
                if (divisor == 0) throw new ArithmeticException("Division by zero");
                yield evaluate(l) / divisor;
            }
        };
    }

    /** Convert expression to string representation */
    public static String expressionToString(Expression expr) {
        return switch (expr) {
            case Literal(double v) -> String.valueOf(v);
            case Negate(var inner) -> "-(" + expressionToString(inner) + ")";
            case Add(var l, var r) -> "(" + expressionToString(l) + " + " + expressionToString(r) + ")";
            case Multiply(var l, var r) -> "(" + expressionToString(l) + " * " + expressionToString(r) + ")";
            case Divide(var l, var r) -> "(" + expressionToString(l) + " / " + expressionToString(r) + ")";
        };
    }

    // ==================== Sequenced Collections ====================

    /** Problem 7: Rotate elements in a sequenced collection */
    public static <T> List<T> rotateLeft(List<T> list) {
        if (list.isEmpty()) return new ArrayList<>();
        var result = new ArrayList<>(list.subList(1, list.size()));
        result.add(list.getFirst());
        return result;
    }

    /** Swap first and last elements */
    public static <T> List<T> swapFirstLast(List<T> list) {
        if (list.size() < 2) return new ArrayList<>(list);
        var result = new ArrayList<>(list);
        T first = result.getFirst();
        T last = result.getLast();
        result.set(0, last);
        result.set(result.size() - 1, first);
        return result;
    }

    /** Find middle element */
    public static <T> Optional<T> findMiddle(List<T> list) {
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(list.size() / 2));
    }

    // ==================== Pattern Matching State Machine ====================

    /** Problem 10: Traffic light state machine */
    public sealed interface TrafficState permits Red, Yellow, Green {}
    public record Red() implements TrafficState {}
    public record Yellow() implements TrafficState {}
    public record Green() implements TrafficState {}

    public static TrafficState nextState(TrafficState current) {
        return switch (current) {
            case Red() -> new Green();
            case Green() -> new Yellow();
            case Yellow() -> new Red();
        };
    }

    public static String describeState(TrafficState state) {
        return switch (state) {
            case Red() -> "STOP - Red light";
            case Yellow() -> "CAUTION - Yellow light";
            case Green() -> "GO - Green light";
        };
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Java 18-24 Practice Problems ===\n");

        // Virtual threads
        System.out.println("--- Virtual Threads ---");
        var results = createVirtualThreads();
        results.forEach(System.out::println);

        // Record patterns
        System.out.println("\n--- Record Patterns ---");
        var emp = new Employee("Alice", 30);
        System.out.println(describeEmployee(emp));

        var dept = new Department("Engineering", emp);
        System.out.println(describeDepartment(dept));

        // Calculator
        System.out.println("\n--- Calculator ---");
        var expr = new Add(new Multiply(new Literal(2), new Literal(3)), new Literal(4));
        System.out.println(expressionToString(expr) + " = " + evaluate(expr));

        // Sequenced collections
        System.out.println("\n--- Sequenced Collections ---");
        var list = List.of(1, 2, 3, 4, 5);
        System.out.println("Original: " + list);
        System.out.println("Rotated left: " + rotateLeft(new ArrayList<>(list)));
        System.out.println("Swapped first/last: " + swapFirstLast(new ArrayList<>(list)));
        System.out.println("Middle: " + findMiddle(list));

        // State machine
        System.out.println("\n--- State Machine ---");
        TrafficState state = new Red();
        for (int i = 0; i < 6; i++) {
            System.out.println(describeState(state));
            state = nextState(state);
        }
    }
}
