package com.javamastery.java9to17;

/**
 * Demonstrates Switch Expressions (Java 14, finalized in Java 17).
 *
 * Switch expressions:
 *   - Can return a value (switch is an expression, not just a statement)
 *   - Arrow syntax (->) eliminates fall-through
 *   - Must be exhaustive when used as expression
 *   - 'yield' keyword for block bodies in expression form
 */
public class SwitchExpressionsDemo {

    enum Season { SPRING, SUMMER, AUTUMN, WINTER }

    public static void main(String[] args) {
        System.out.println("=== Switch Expressions Demo (Java 14+) ===\n");

        // --- 1. Traditional switch (still works) ---
        Season season = Season.SUMMER;
        String oldStyle;
        switch (season) {
            case SPRING:
                oldStyle = "Flowers bloom";
                break;
            case SUMMER:
                oldStyle = "Beach time";
                break;
            default:
                oldStyle = "Other season";
        }
        System.out.println("Old style: " + oldStyle);

        // --- 2. Switch expression with arrow syntax (no fall-through) ---
        String newStyle = switch (season) {
            case SPRING -> "Flowers bloom";
            case SUMMER -> "Beach time";
            case AUTUMN -> "Leaves fall";
            case WINTER -> "Snow falls";
            // No default needed — enum is exhaustive
        };
        System.out.println("New style: " + newStyle);

        // --- 3. Multiple case labels ---
        int dayNumber = 3;
        String dayType = switch (dayNumber) {
            case 1, 7 -> "Weekend";
            case 2, 3, 4, 5, 6 -> "Weekday";
            default -> "Invalid";
        };
        System.out.println("\nDay " + dayNumber + " is: " + dayType);

        // --- 4. yield for complex blocks ---
        int month = 8;
        String quarter = switch (month) {
            case 1, 2, 3 -> "Q1";
            case 4, 5, 6 -> "Q2";
            case 7, 8, 9 -> {
                // When you need multiple statements, use a block with 'yield'
                String q = "Q3";
                System.out.println("  Computing quarter for month " + month);
                yield q;
            }
            case 10, 11, 12 -> "Q4";
            default -> throw new IllegalArgumentException("Invalid month: " + month);
        };
        System.out.println("Month " + month + " is in: " + quarter);

        // --- 5. Switch expression with pattern matching (Java 17+) ---
        printType("hello");
        printType(42);
        printType(3.14);
        printType(java.util.List.of(1, 2, 3));
    }

    static void printType(Object obj) {
        String description = switch (obj) {
            case Integer i -> "Integer: " + i;
            case String s  -> "String: '" + s + "' (length " + s.length() + ")";
            case Double d  -> "Double: " + d;
            default        -> "Other: " + obj.getClass().getSimpleName();
        };
        System.out.println(description);
    }
}
