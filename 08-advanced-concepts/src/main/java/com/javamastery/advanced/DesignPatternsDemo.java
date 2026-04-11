package com.javamastery.advanced;

import java.util.*;
import java.util.function.Function;

/**
 * Demonstrates common design patterns using modern Java (records, sealed, functional).
 */
public class DesignPatternsDemo {

    // ========== 1. SINGLETON (Enum-based — safest approach) ==========
    enum DatabaseConnection {
        INSTANCE;

        private final String url = "jdbc:h2:mem:test";

        public String getUrl() { return url; }
        public void query(String sql) {
            System.out.println("  [DB] Executing: " + sql);
        }
    }

    // ========== 2. BUILDER (Fluent API) ==========
    static class HttpRequest {
        private final String method;
        private final String url;
        private final Map<String, String> headers;
        private final String body;

        private HttpRequest(Builder builder) {
            this.method = builder.method;
            this.url = builder.url;
            this.headers = Map.copyOf(builder.headers);
            this.body = builder.body;
        }

        @Override
        public String toString() {
            return method + " " + url + " headers=" + headers
                    + (body != null ? " body=" + body : "");
        }

        static class Builder {
            private String method = "GET";
            private String url;
            private final Map<String, String> headers = new LinkedHashMap<>();
            private String body;

            Builder url(String url) { this.url = url; return this; }
            Builder method(String method) { this.method = method; return this; }
            Builder header(String key, String value) { headers.put(key, value); return this; }
            Builder body(String body) { this.body = body; return this; }

            HttpRequest build() {
                Objects.requireNonNull(url, "URL is required");
                return new HttpRequest(this);
            }
        }
    }

    // ========== 3. STRATEGY (Functional approach) ==========
    @FunctionalInterface
    interface PricingStrategy {
        double calculatePrice(double basePrice);
    }

    static double applyPricing(double price, PricingStrategy strategy) {
        return strategy.calculatePrice(price);
    }

    // ========== 4. OBSERVER (Event-driven) ==========
    static class EventBus {
        private final Map<String, List<java.util.function.Consumer<Object>>> listeners = new HashMap<>();

        void subscribe(String event, java.util.function.Consumer<Object> handler) {
            listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(handler);
        }

        void publish(String event, Object data) {
            listeners.getOrDefault(event, List.of())
                    .forEach(handler -> handler.accept(data));
        }
    }

    // ========== 5. FACTORY METHOD ==========
    sealed interface Shape permits Circle, Rectangle {
        double area();
    }
    record Circle(double radius) implements Shape {
        public double area() { return Math.PI * radius * radius; }
    }
    record Rectangle(double width, double height) implements Shape {
        public double area() { return width * height; }
    }

    static Shape createShape(String type, double... dims) {
        return switch (type) {
            case "circle" -> new Circle(dims[0]);
            case "rectangle" -> new Rectangle(dims[0], dims[1]);
            default -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }

    public static void main(String[] args) {
        System.out.println("=== Design Patterns Demo ===\n");

        // --- Singleton ---
        System.out.println("--- Singleton (Enum) ---");
        DatabaseConnection.INSTANCE.query("SELECT * FROM users");
        System.out.println("  Same instance: " +
                (DatabaseConnection.INSTANCE == DatabaseConnection.INSTANCE));

        // --- Builder ---
        System.out.println("\n--- Builder ---");
        HttpRequest request = new HttpRequest.Builder()
                .url("https://api.example.com/users")
                .method("POST")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer token")
                .body("{\"name\":\"Alice\"}")
                .build();
        System.out.println("  " + request);

        // --- Strategy (using lambdas!) ---
        System.out.println("\n--- Strategy (Functional) ---");
        double base = 100.0;
        PricingStrategy regular = price -> price;
        PricingStrategy vip = price -> price * 0.8;
        PricingStrategy employee = price -> price * 0.5;

        System.out.println("  Regular: $" + applyPricing(base, regular));
        System.out.println("  VIP: $" + applyPricing(base, vip));
        System.out.println("  Employee: $" + applyPricing(base, employee));

        // Strategy with method reference
        System.out.println("  With tax: $" + applyPricing(base, DesignPatternsDemo::withTax));

        // --- Observer ---
        System.out.println("\n--- Observer (EventBus) ---");
        EventBus bus = new EventBus();
        bus.subscribe("user.created", data -> System.out.println("  [Email] Welcome " + data));
        bus.subscribe("user.created", data -> System.out.println("  [Audit] User created: " + data));
        bus.subscribe("order.placed", data -> System.out.println("  [Inventory] Check stock for " + data));

        bus.publish("user.created", "Alice");
        bus.publish("order.placed", "Order#123");

        // --- Factory Method ---
        System.out.println("\n--- Factory Method ---");
        Shape circle = createShape("circle", 5);
        Shape rect = createShape("rectangle", 4, 6);
        System.out.println("  Circle area: " + String.format("%.2f", circle.area()));
        System.out.println("  Rectangle area: " + rect.area());
    }

    private static double withTax(double price) {
        return price * 1.1;
    }
}
