package com.javamastery.java8;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Demonstrates all four types of method references in Java 8.
 */
public class MethodReferencesDemo {

    public static void main(String[] args) {
        System.out.println("=== Method References Demo ===\n");

        // 1. Static method reference: Class::staticMethod
        System.out.println("--- 1. Static Method Reference ---");
        List<String> numberStrings = List.of("1", "2", "3", "4", "5");
        List<Integer> numbers = numberStrings.stream()
                .map(Integer::parseInt)      // equivalent to: s -> Integer.parseInt(s)
                .collect(Collectors.toList());
        System.out.println("Parsed: " + numbers);

        // 2. Bound instance method reference: object::instanceMethod
        System.out.println("\n--- 2. Bound Instance Method Reference ---");
        String prefix = "Hello, ";
        Function<String, String> greeter = prefix::concat;   // prefix.concat(name)
        System.out.println(greeter.apply("Alice"));
        System.out.println(greeter.apply("Bob"));

        // System.out::println — the most common bound method reference
        List.of("Alpha", "Beta", "Gamma").forEach(System.out::println);

        // 3. Unbound instance method reference: Class::instanceMethod
        System.out.println("\n--- 3. Unbound Instance Method Reference ---");
        List<String> names = List.of("alice", "bob", "charlie");
        // String::toUpperCase → (String s) -> s.toUpperCase()
        List<String> uppercased = names.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        System.out.println("Uppercased: " + uppercased);

        // Comparator using unbound method reference
        List<String> sortedNames = names.stream()
                .sorted(String::compareToIgnoreCase)  // (a, b) -> a.compareToIgnoreCase(b)
                .collect(Collectors.toList());
        System.out.println("Sorted: " + sortedNames);

        // 4. Constructor reference: Class::new
        System.out.println("\n--- 4. Constructor Reference ---");
        List<String> words = List.of("hello", "world", "java");
        // StringBuilder::new → (String s) -> new StringBuilder(s)
        List<StringBuilder> builders = words.stream()
                .map(StringBuilder::new)
                .collect(Collectors.toList());
        builders.forEach(sb -> System.out.println("StringBuilder: " + sb));

        // Array constructor reference
        String[] array = names.stream()
                .toArray(String[]::new);     // n -> new String[n]
        System.out.println("Array: " + Arrays.toString(array));

        // 5. Practical comparisons: lambda vs method reference
        System.out.println("\n--- 5. Lambda vs Method Reference Comparison ---");
        List<Integer> nums = List.of(3, 1, 4, 1, 5, 9, 2, 6);

        // Lambda version
        List<Integer> sorted1 = nums.stream()
                .sorted((a, b) -> Integer.compare(a, b))
                .collect(Collectors.toList());

        // Method reference version (cleaner)
        List<Integer> sorted2 = nums.stream()
                .sorted(Integer::compare)
                .collect(Collectors.toList());

        System.out.println("Lambda sort: " + sorted1);
        System.out.println("Method ref sort: " + sorted2);

        // 6. Method reference with custom class
        System.out.println("\n--- 6. Method Reference with Custom Class ---");
        List<Person> people = List.of(
                new Person("Alice", 30),
                new Person("Bob", 25),
                new Person("Charlie", 35)
        );

        // Extracting names using method reference
        List<String> personNames = people.stream()
                .map(Person::getName)           // unbound: p -> p.getName()
                .collect(Collectors.toList());
        System.out.println("Names: " + personNames);

        // Sorting by age using method reference
        people.stream()
                .sorted(Comparator.comparingInt(Person::getAge))
                .forEach(System.out::println);
    }

    static class Person {
        private final String name;
        private final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }

        @Override
        public String toString() {
            return name + " (age " + age + ")";
        }
    }
}
