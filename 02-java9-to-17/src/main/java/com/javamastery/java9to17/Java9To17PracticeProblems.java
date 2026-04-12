package com.javamastery.java9to17;

import java.util.*;
import java.util.stream.*;

/**
 * Practice problems for Java 9–17 features.
 */
public class Java9To17PracticeProblems {

    // ==================== Records ====================

    /** Problem 1: Student record with validation */
    public record Student(String name, int age, double grade) {
        public Student {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
            if (age <= 0) throw new IllegalArgumentException("Age must be positive");
            if (grade < 0 || grade > 100) throw new IllegalArgumentException("Grade must be 0-100");
        }
    }

    // ==================== Sealed Hierarchy ====================

    /** Problem 6: Expression evaluator using sealed interfaces */
    public sealed interface Expr permits Num, Add, Mul {
        double evaluate();
    }

    public record Num(double value) implements Expr {
        public double evaluate() { return value; }
    }

    public record Add(Expr left, Expr right) implements Expr {
        public double evaluate() { return left.evaluate() + right.evaluate(); }
    }

    public record Mul(Expr left, Expr right) implements Expr {
        public double evaluate() { return left.evaluate() * right.evaluate(); }
    }

    /** Evaluate an expression tree */
    public static double evaluateExpr(Expr expr) {
        // Pattern matching instanceof (Java 16+) instead of pattern switch (Java 21+)
        if (expr instanceof Num n) return n.value();
        if (expr instanceof Add a) return evaluateExpr(a.left()) + evaluateExpr(a.right());
        if (expr instanceof Mul m) return evaluateExpr(m.left()) * evaluateExpr(m.right());
        throw new IllegalArgumentException("Unknown expression type: " + expr.getClass());
    }

    // ==================== Pattern Matching ====================

    /** Problem 8: Describe any object using pattern matching */
    public static String describeObject(Object obj) {
        // Pattern matching instanceof (Java 16+) instead of pattern switch (Java 21+)
        if (obj == null) return "null value";
        if (obj instanceof Integer i && i < 0) return "negative integer: " + i;
        if (obj instanceof Integer i) return "integer: " + i;
        if (obj instanceof String s && s.isBlank()) return "blank string";
        if (obj instanceof String s) return "string of length " + s.length() + ": \"" + s + "\"";
        if (obj instanceof List<?> list) return "list with " + list.size() + " elements";
        if (obj instanceof Map<?, ?> map) return "map with " + map.size() + " entries";
        if (obj instanceof int[] arr) return "int array of length " + arr.length;
        if (obj instanceof Double d) return "double: " + d;
        return "unknown type: " + obj.getClass().getSimpleName();
    }

    // ==================== Switch Expressions ====================

    /** Problem 5: Day type classifier using switch expression */
    public static String classifyDay(String day) {
        return switch (day.toUpperCase()) {
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
            case "SATURDAY", "SUNDAY" -> "Weekend";
            default -> "Invalid day";
        };
    }

    /** HTTP status code description using switch expression */
    public static String httpStatus(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 301 -> "Moved Permanently";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> {
                if (code >= 200 && code < 300) yield "Success";
                else if (code >= 300 && code < 400) yield "Redirection";
                else if (code >= 400 && code < 500) yield "Client Error";
                else if (code >= 500 && code < 600) yield "Server Error";
                else yield "Unknown";
            }
        };
    }

    // ==================== String Methods ====================

    /** Problem 4: Process text using Java 11 String methods */
    public static List<String> processMultilineText(String text) {
        return text.lines()
                .map(String::strip)
                .filter(line -> !line.isBlank())
                .collect(Collectors.toList());
    }

    /** Count blank vs non-blank lines */
    public static Map<String, Long> lineStatistics(String text) {
        var lines = text.lines().collect(Collectors.toList());
        return Map.of(
                "total", (long) lines.size(),
                "blank", lines.stream().filter(String::isBlank).count(),
                "nonBlank", lines.stream().filter(l -> !l.isBlank()).count()
        );
    }

    // ==================== Stream Enhancements ====================

    /** Problem 13: Simple pagination using takeWhile/dropWhile + iterate */
    public static <T> List<T> paginate(List<T> items, int page, int pageSize) {
        return items.stream()
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    /** Generate numbers until a condition is met, then take the next N */
    public static List<Integer> takeAfterDropping(List<Integer> numbers, int threshold) {
        return numbers.stream()
                .dropWhile(n -> n < threshold)
                .collect(Collectors.toList());
    }

    // ==================== Optional Enhancements ====================

    /** Problem 9: Fallback lookup chain using Optional.or() */
    public static String lookupValue(String key,
                                      Map<String, String> cache,
                                      Map<String, String> database) {
        return Optional.ofNullable(cache.get(key))
                .or(() -> Optional.ofNullable(database.get(key)))
                .orElse("DEFAULT_VALUE");
    }

    // ==================== Text Blocks ====================

    /** Problem 10: Create a JSON template using text blocks */
    public static String createJsonUser(String name, int age, String email) {
        return """
                {
                    "name": "%s",
                    "age": %d,
                    "email": "%s"
                }
                """.formatted(name, age, email);
    }

    /** Create SQL query with text block */
    public static String createSelectQuery(String table, String condition) {
        return """
                SELECT *
                FROM %s
                WHERE %s
                ORDER BY id ASC
                """.formatted(table, condition);
    }

    // ==================== Sealed Payment System ====================

    /** Problem 12: Payment processing with sealed interfaces */
    public sealed interface Payment permits CreditCard, DebitCard, BankTransfer, DigitalWallet {}

    public record CreditCard(String cardNumber, String expiry, double amount) implements Payment {
        public CreditCard {
            if (cardNumber == null || cardNumber.length() < 13)
                throw new IllegalArgumentException("Invalid card number");
            if (amount <= 0)
                throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public record DebitCard(String cardNumber, String pin, double amount) implements Payment {
        public DebitCard {
            if (amount <= 0)
                throw new IllegalArgumentException("Amount must be positive");
        }
    }

    public record BankTransfer(String iban, String bic, double amount) implements Payment {
        public BankTransfer {
            if (iban == null || iban.isBlank())
                throw new IllegalArgumentException("IBAN required");
        }
    }

    public record DigitalWallet(String provider, String email, double amount) implements Payment {}

    /** Process payment with type-specific logic */
    public static String processPayment(Payment payment) {
        // Pattern matching instanceof (Java 16+) instead of pattern switch (Java 21+)
        if (payment instanceof CreditCard cc) return "Processing credit card ending in " +
                cc.cardNumber().substring(cc.cardNumber().length() - 4) +
                " for $" + cc.amount();
        if (payment instanceof DebitCard dc) return "Processing debit card with PIN verification for $" + dc.amount();
        if (payment instanceof BankTransfer bt) return "Initiating bank transfer to IBAN " + bt.iban() +
                " for $" + bt.amount();
        if (payment instanceof DigitalWallet dw) return "Processing " + dw.provider() + " payment for $" + dw.amount();
        throw new IllegalArgumentException("Unknown payment type: " + payment.getClass());
    }

    public static void main(String[] args) {
        System.out.println("=== Java 9-17 Practice Problems ===\n");

        // Records
        var student = new Student("Alice", 20, 95.5);
        System.out.println("Student: " + student);

        // Expression evaluator: (2 + 3) * 4 = 20
        var expr = new Mul(new Add(new Num(2), new Num(3)), new Num(4));
        System.out.println("(2 + 3) * 4 = " + evaluateExpr(expr));

        // Pattern matching
        System.out.println(describeObject("Hello"));
        System.out.println(describeObject(42));
        System.out.println(describeObject(List.of(1, 2, 3)));

        // Switch expression
        System.out.println("Monday: " + classifyDay("Monday"));
        System.out.println("HTTP 404: " + httpStatus(404));

        // Text blocks
        System.out.println(createJsonUser("Bob", 30, "bob@example.com"));

        // Payment processing
        var payment = new CreditCard("4111111111111111", "12/25", 99.99);
        System.out.println(processPayment(payment));
    }
}
