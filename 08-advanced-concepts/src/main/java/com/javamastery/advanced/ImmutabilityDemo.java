package com.javamastery.advanced;

import java.util.*;

/**
 * Demonstrates immutable object design patterns.
 */
public class ImmutabilityDemo {

    // ==================== Immutable Person ====================

    /** A properly immutable class */
    public static final class ImmutablePerson {
        private final String name;
        private final int age;
        private final List<String> hobbies;

        public ImmutablePerson(String name, int age, List<String> hobbies) {
            this.name = name;
            this.age = age;
            // Defensive copy in constructor — don't trust the caller's list
            this.hobbies = List.copyOf(hobbies);
        }

        public String getName() { return name; }
        public int getAge() { return age; }

        // Return unmodifiable view — caller cannot modify our internal state
        public List<String> getHobbies() { return hobbies; } // already unmodifiable from List.copyOf

        @Override
        public String toString() {
            return "ImmutablePerson{name='%s', age=%d, hobbies=%s}".formatted(name, age, hobbies);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ImmutablePerson other)) return false;
            return age == other.age && name.equals(other.name) && hobbies.equals(other.hobbies);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, hobbies);
        }
    }

    // ==================== Mutable vs Immutable ====================

    /** Shows what happens without defensive copies */
    static class BadImmutable {
        private final List<String> items;

        BadImmutable(List<String> items) {
            this.items = items; // BUG: shares reference with caller
        }

        List<String> getItems() {
            return items; // BUG: caller can modify internal state
        }
    }

    // ==================== Builder for Immutable Object ====================

    public static final class HttpRequest {
        private final String url;
        private final String method;
        private final Map<String, String> headers;
        private final String body;
        private final int timeoutMs;

        private HttpRequest(Builder builder) {
            this.url = Objects.requireNonNull(builder.url, "URL is required");
            this.method = builder.method;
            this.headers = Map.copyOf(builder.headers);
            this.body = builder.body;
            this.timeoutMs = builder.timeoutMs;
        }

        public String getUrl() { return url; }
        public String getMethod() { return method; }
        public Map<String, String> getHeaders() { return headers; }
        public String getBody() { return body; }
        public int getTimeoutMs() { return timeoutMs; }

        public static Builder builder() { return new Builder(); }

        @Override
        public String toString() {
            return "HttpRequest{method='%s', url='%s', headers=%s, body='%s', timeout=%dms}"
                    .formatted(method, url, headers, body, timeoutMs);
        }

        public static class Builder {
            private String url;
            private String method = "GET";
            private final Map<String, String> headers = new LinkedHashMap<>();
            private String body;
            private int timeoutMs = 5000;

            public Builder url(String url) { this.url = url; return this; }
            public Builder method(String method) { this.method = method; return this; }
            public Builder header(String key, String value) { this.headers.put(key, value); return this; }
            public Builder body(String body) { this.body = body; return this; }
            public Builder timeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; return this; }
            public HttpRequest build() { return new HttpRequest(this); }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Immutability Demo ===\n");

        // 1. Immutable Person
        List<String> hobbies = new ArrayList<>(List.of("Reading", "Coding"));
        var person = new ImmutablePerson("Alice", 30, hobbies);
        System.out.println("Person: " + person);

        // Modifying the original list does NOT affect the person
        hobbies.add("Gaming");
        System.out.println("After modifying source list: " + person.getHobbies()); // still [Reading, Coding]

        // Trying to modify the returned list throws UnsupportedOperationException
        try {
            person.getHobbies().add("Hacking");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify hobbies: " + e.getClass().getSimpleName());
        }

        // 2. Bad immutable — DON'T DO THIS
        System.out.println("\n--- Bad Immutable ---");
        List<String> items = new ArrayList<>(List.of("A", "B"));
        var bad = new BadImmutable(items);
        items.add("C"); // modifies internal state!
        System.out.println("Bad immutable (externally modified): " + bad.getItems()); // [A, B, C]

        // 3. Builder
        System.out.println("\n--- Builder ---");
        var request = HttpRequest.builder()
                .url("https://api.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token123")
                .body("{\"name\": \"Bob\"}")
                .timeoutMs(3000)
                .build();
        System.out.println(request);
    }
}
