package com.javamastery.java8;

import java.util.List;
import java.util.function.*;

/**
 * Demonstrates the core functional interfaces from java.util.function:
 * Predicate, Function, Consumer, Supplier — and their composition methods.
 */
public class FunctionalInterfacesDemo {

    public static void main(String[] args) {
        System.out.println("=== Functional Interfaces Demo ===\n");

        // =====================================================
        // PREDICATE<T>: T -> boolean
        // =====================================================
        Predicate<String> isNotEmpty = s -> !s.isEmpty();
        Predicate<String> isShort = s -> s.length() < 5;

        // Composing predicates with and(), or(), negate()
        Predicate<String> isNotEmptyAndShort = isNotEmpty.and(isShort);
        Predicate<String> isLong = isShort.negate();

        System.out.println("--- Predicate ---");
        System.out.println("'Hi' is not empty and short: " + isNotEmptyAndShort.test("Hi"));
        System.out.println("'' is not empty and short: " + isNotEmptyAndShort.test(""));
        System.out.println("'Hello World' is long: " + isLong.test("Hello World"));

        // Predicate used with stream filter
        List<String> words = List.of("", "hi", "hello", "world", "ok");
        List<String> filtered = words.stream()
                .filter(isNotEmptyAndShort)
                .toList();
        System.out.println("Filtered words: " + filtered);

        // =====================================================
        // FUNCTION<T, R>: T -> R
        // =====================================================
        Function<String, Integer> strLength = String::length;
        Function<Integer, Integer> doubleIt = n -> n * 2;

        // andThen: apply this function, then the other
        Function<String, Integer> doubleLength = strLength.andThen(doubleIt);

        // compose: apply the other function first, then this one
        // doubleIt.compose(strLength) is equivalent to doubleLength
        Function<String, Integer> composedDoubleLength = doubleIt.compose(strLength);

        System.out.println("\n--- Function ---");
        System.out.println("Length of 'hello': " + strLength.apply("hello"));
        System.out.println("Double length of 'hello': " + doubleLength.apply("hello"));
        System.out.println("Composed double length: " + composedDoubleLength.apply("hello"));

        // Function.identity() returns input as-is
        Function<String, String> identity = Function.identity();
        System.out.println("Identity: " + identity.apply("same"));

        // =====================================================
        // CONSUMER<T>: T -> void (side-effect operation)
        // =====================================================
        Consumer<String> printer = System.out::println;
        Consumer<String> yeller = s -> System.out.println(s.toUpperCase() + "!");

        // andThen: chain consumers
        Consumer<String> printThenYell = printer.andThen(yeller);

        System.out.println("\n--- Consumer ---");
        System.out.print("printThenYell 'hello': ");
        printThenYell.accept("hello");

        // =====================================================
        // SUPPLIER<T>: () -> T (factory/generator)
        // =====================================================
        Supplier<Double> randomSupplier = Math::random;
        Supplier<List<String>> emptyListSupplier = List::of;

        System.out.println("\n--- Supplier ---");
        System.out.println("Random: " + randomSupplier.get());
        System.out.println("Empty list: " + emptyListSupplier.get());

        // =====================================================
        // BIFUNCTION<T, U, R>: (T, U) -> R
        // =====================================================
        BiFunction<String, String, String> concat = (a, b) -> a + " " + b;
        System.out.println("\n--- BiFunction ---");
        System.out.println("Concat: " + concat.apply("Hello", "World"));

        // =====================================================
        // UNARYOPERATOR<T>: T -> T (specialization of Function)
        // =====================================================
        UnaryOperator<String> trim = String::trim;
        UnaryOperator<String> upper = String::toUpperCase;
        UnaryOperator<String> trimAndUpper = trim.andThen(upper)::apply;

        System.out.println("\n--- UnaryOperator ---");
        System.out.println("Trim and upper '  hello  ': '" + trimAndUpper.apply("  hello  ") + "'");

        // =====================================================
        // BINARYOPERATOR<T>: (T, T) -> T (specialization of BiFunction)
        // =====================================================
        BinaryOperator<Integer> add = Integer::sum;
        BinaryOperator<Integer> max = BinaryOperator.maxBy(Integer::compareTo);

        System.out.println("\n--- BinaryOperator ---");
        System.out.println("Add 3 + 4: " + add.apply(3, 4));
        System.out.println("Max 3, 7: " + max.apply(3, 7));
    }
}
