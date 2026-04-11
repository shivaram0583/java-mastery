package com.javamastery.java8;

import java.util.Optional;

/**
 * Demonstrates Optional in Java 8.
 *
 * Optional is a container that may or may not hold a non-null value.
 * It encourages explicit handling of absent values rather than returning null.
 */
public class OptionalDemo {

    record User(String name, String email) {}

    public static void main(String[] args) {
        System.out.println("=== Optional Demo ===\n");

        // --- 1. Creating Optionals ---
        Optional<String> present = Optional.of("Hello");         // must be non-null
        Optional<String> nullable = Optional.ofNullable(null);   // may be null
        Optional<String> empty = Optional.empty();               // always empty

        System.out.println("present: " + present);
        System.out.println("nullable: " + nullable);
        System.out.println("empty: " + empty);

        // --- 2. Checking and getting values ---
        // isPresent() / isEmpty() (Java 11)
        System.out.println("\npresent.isPresent(): " + present.isPresent());
        System.out.println("empty.isPresent(): " + empty.isPresent());

        // ifPresent — execute action only if value exists
        present.ifPresent(val -> System.out.println("Value is: " + val));

        // --- 3. Default values ---
        // orElse: always evaluates the fallback (eager)
        String value1 = empty.orElse("default");
        System.out.println("\norElse: " + value1);

        // orElseGet: only evaluates if empty (lazy) — preferred for expensive computations
        String value2 = empty.orElseGet(() -> "computed default");
        System.out.println("orElseGet: " + value2);

        // orElseThrow: throw when absent
        try {
            empty.orElseThrow(() -> new IllegalStateException("No value!"));
        } catch (IllegalStateException e) {
            System.out.println("orElseThrow caught: " + e.getMessage());
        }

        // --- 4. Transforming with map ---
        Optional<String> upperName = Optional.of("alice")
                .map(String::toUpperCase);
        System.out.println("\nmap to upper: " + upperName.orElse("N/A"));

        // map on empty returns empty (no NPE risk)
        Optional<String> emptyMapped = Optional.<String>empty()
                .map(String::toUpperCase);
        System.out.println("map on empty: " + emptyMapped.orElse("N/A"));

        // --- 5. flatMap for nested Optionals ---
        // Use flatMap when the mapping function itself returns an Optional
        Optional<User> user = Optional.of(new User("Bob", "bob@example.com"));
        Optional<String> email = user.flatMap(u -> Optional.ofNullable(u.email()));
        System.out.println("\nflatMap email: " + email.orElse("no email"));

        Optional<User> userNoEmail = Optional.of(new User("Eve", null));
        Optional<String> noEmail = userNoEmail.flatMap(u -> Optional.ofNullable(u.email()));
        System.out.println("flatMap null email: " + noEmail.orElse("no email"));

        // --- 6. filter ---
        Optional<String> longName = Optional.of("Alexander")
                .filter(n -> n.length() > 5);
        System.out.println("\nfilter (>5 chars): " + longName.orElse("too short"));

        Optional<String> shortName = Optional.of("Al")
                .filter(n -> n.length() > 5);
        System.out.println("filter (>5 chars): " + shortName.orElse("too short"));

        // --- 7. Chaining for real-world patterns ---
        String result = findUserById(1)
                .map(User::name)
                .map(String::toUpperCase)
                .orElse("UNKNOWN");
        System.out.println("\nChained result: " + result);
    }

    /** Simulates a database lookup that may return null. */
    static Optional<User> findUserById(int id) {
        if (id == 1) {
            return Optional.of(new User("Alice", "alice@example.com"));
        }
        return Optional.empty();
    }
}
