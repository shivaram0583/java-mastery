package com.javamastery.java8;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Practice problems for Java 8 features.
 * Each method has a TODO comment describing what to implement.
 * Solutions are provided — try solving before looking!
 */
public class Java8PracticeProblems {

    // ==================== EASY ====================

    /** Problem 1: Filter strings starting with "A" and convert to uppercase */
    public static List<String> filterAndTransform(List<String> input) {
        return input.stream()
                .filter(s -> s.startsWith("A"))
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /** Problem 2: Sum of squares of all odd numbers */
    public static int sumOfSquaresOfOdds(List<Integer> numbers) {
        return numbers.stream()
                .filter(n -> n % 2 != 0)
                .mapToInt(n -> n * n)
                .sum();
    }

    /** Problem 3: Find the longest string */
    public static Optional<String> findLongest(List<String> strings) {
        return strings.stream()
                .max(Comparator.comparingInt(String::length));
    }

    /** Problem 4: Join strings with commas */
    public static String joinWithCommas(List<String> strings) {
        return strings.stream()
                .collect(Collectors.joining(", "));
    }

    /** Problem 5: Count strings with length > 5 */
    public static long countLongStrings(List<String> strings) {
        return strings.stream()
                .filter(s -> s.length() > 5)
                .count();
    }

    // ==================== MEDIUM ====================

    /** Problem 6: Flatten nested lists, remove duplicates, sort ascending */
    public static List<Integer> flattenAndSort(List<List<Integer>> nestedLists) {
        return nestedLists.stream()
                .flatMap(Collection::stream)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** Problem 7: Word frequency counter (case-insensitive) */
    public static Map<String, Long> wordFrequency(String sentence) {
        return Arrays.stream(sentence.toLowerCase().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    /** Problem 8: Group names by first character */
    public static Map<Character, List<String>> groupByFirstLetter(List<String> names) {
        return names.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.groupingBy(s -> s.charAt(0)));
    }

    /** Problem 9: Find the second highest number */
    public static OptionalInt findSecondHighest(List<Integer> numbers) {
        List<Integer> sorted = numbers.stream()
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (sorted.size() < 2) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(sorted.get(1));
    }

    /** Problem 10: Partition integers into even and odd */
    public static Map<Boolean, List<Integer>> partitionEvenOdd(List<Integer> numbers) {
        return numbers.stream()
                .collect(Collectors.partitioningBy(n -> n % 2 == 0));
    }

    /** Problem 11: Find highest-salary employee per department */
    public static Map<String, Optional<Employee>> highestSalaryPerDept(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::getDepartment,
                        Collectors.maxBy(Comparator.comparingDouble(Employee::getSalary))
                ));
    }

    /** Problem 12: Invert a map: Map<String,Integer> → Map<Integer, List<String>> */
    public static Map<Integer, List<String>> invertMap(Map<String, Integer> original) {
        return original.entrySet().stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                ));
    }

    // ==================== HARD ====================

    /** Problem 13: Generate first N Fibonacci numbers using streams */
    public static List<Long> fibonacci(int n) {
        return Stream.iterate(new long[]{0, 1}, f -> new long[]{f[1], f[0] + f[1]})
                .limit(n)
                .map(f -> f[0])
                .collect(Collectors.toList());
    }

    /** Problem 14: Sum of cubes of prime numbers using parallel streams */
    public static long sumOfCubesOfPrimes(List<Integer> numbers) {
        return numbers.parallelStream()
                .filter(Java8PracticeProblems::isPrime)
                .mapToLong(n -> (long) n * n * n)
                .sum();
    }

    /** Problem 16: Convert an Iterator to a Stream */
    public static <T> Stream<T> iteratorToStream(Iterator<T> iterator) {
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    /** Problem 17: Sliding window average */
    public static List<Double> slidingWindowAverage(List<Double> values, int windowSize) {
        if (windowSize <= 0 || windowSize > values.size()) {
            return Collections.emptyList();
        }
        return IntStream.rangeClosed(0, values.size() - windowSize)
                .mapToObj(i -> values.subList(i, i + windowSize).stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0))
                .collect(Collectors.toList());
    }

    /** Problem 19: Implement map using reduce */
    public static <T, R> List<R> mapUsingReduce(List<T> input, Function<T, R> mapper) {
        return input.stream()
                .<List<R>>reduce(
                        new ArrayList<>(),
                        (list, element) -> {
                            List<R> newList = new ArrayList<>(list);
                            newList.add(mapper.apply(element));
                            return newList;
                        },
                        (list1, list2) -> {
                            List<R> merged = new ArrayList<>(list1);
                            merged.addAll(list2);
                            return merged;
                        }
                );
    }

    // ==================== HELPER ====================

    private static boolean isPrime(int n) {
        if (n < 2) return false;
        if (n < 4) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    /** Simple Employee class for practice problems */
    public static class Employee {
        private final String name;
        private final String department;
        private final double salary;

        public Employee(String name, String department, double salary) {
            this.name = name;
            this.department = department;
            this.salary = salary;
        }

        public String getName() { return name; }
        public String getDepartment() { return department; }
        public double getSalary() { return salary; }

        @Override
        public String toString() {
            return name + " (" + department + ", $" + salary + ")";
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Java 8 Practice Problems ===\n");

        // Problem 1
        var names = List.of("Alice", "Bob", "Anna", "Charlie", "Andrew");
        System.out.println("1. Filter & Transform: " + filterAndTransform(names));

        // Problem 2
        var nums = List.of(1, 2, 3, 4, 5);
        System.out.println("2. Sum of squares of odds: " + sumOfSquaresOfOdds(nums));

        // Problem 3
        System.out.println("3. Longest string: " + findLongest(names));

        // Problem 4
        System.out.println("4. Joined: " + joinWithCommas(names));

        // Problem 5
        System.out.println("5. Count long strings: " + countLongStrings(names));

        // Problem 6
        var nested = List.of(List.of(3, 1, 2), List.of(2, 4, 3), List.of(5, 1));
        System.out.println("6. Flatten & sort: " + flattenAndSort(nested));

        // Problem 7
        System.out.println("7. Word freq: " + wordFrequency("the quick brown fox jumps over the lazy fox"));

        // Problem 8
        System.out.println("8. Group by first letter: " + groupByFirstLetter(names));

        // Problem 9
        System.out.println("9. Second highest: " + findSecondHighest(List.of(5, 3, 8, 1, 8, 7)));

        // Problem 13
        System.out.println("13. Fibonacci(10): " + fibonacci(10));

        // Problem 17
        System.out.println("17. Sliding avg: " + slidingWindowAverage(
                List.of(1.0, 2.0, 3.0, 4.0, 5.0), 3));

        // Problem 19
        System.out.println("19. Map via reduce: " + mapUsingReduce(
                List.of("hello", "world"), String::toUpperCase));
    }
}
