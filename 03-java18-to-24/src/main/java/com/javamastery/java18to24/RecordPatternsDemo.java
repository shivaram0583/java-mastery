package com.javamastery.java18to24;

/**
 * Demonstrates Record Patterns (Java 21, finalized — JEP 440).
 *
 * Record patterns allow you to destructure record components directly in
 * pattern matching contexts (instanceof, switch), enabling concise data extraction.
 */
public class RecordPatternsDemo {

    // Domain records
    record Point(int x, int y) {}
    record Line(Point start, Point end) {}
    record Circle(Point center, double radius) {}
    record ColoredPoint(Point point, String color) {}

    sealed interface Shape permits ShapeCircle, ShapeRect {}
    record ShapeCircle(Point center, double radius) implements Shape {}
    record ShapeRect(Point topLeft, Point bottomRight) implements Shape {}

    public static void main(String[] args) {
        System.out.println("=== Record Patterns Demo (Java 21) ===\n");

        // --- 1. Basic record pattern with instanceof ---
        Object obj = new Point(3, 4);

        // Before record patterns: extract then access
        if (obj instanceof Point p) {
            System.out.println("Point: x=" + p.x() + ", y=" + p.y());
        }

        // With record patterns: destructure directly
        if (obj instanceof Point(int x, int y)) {
            System.out.println("Destructured: x=" + x + ", y=" + y);
            double distance = Math.sqrt(x * x + y * y);
            System.out.printf("Distance from origin: %.2f%n", distance);
        }

        // --- 2. Nested record patterns ---
        var coloredPoint = new ColoredPoint(new Point(5, 10), "RED");
        if (coloredPoint instanceof ColoredPoint(Point(int x, int y), String color)) {
            System.out.println("\nColored point: (" + x + "," + y + ") in " + color);
        }

        // --- 3. Record patterns in switch ---
        Shape shape = new ShapeCircle(new Point(0, 0), 5.0);
        String desc = describeShape(shape);
        System.out.println("\nShape: " + desc);

        Shape rect = new ShapeRect(new Point(0, 0), new Point(10, 10));
        System.out.println("Shape: " + describeShape(rect));

        // --- 4. Deep nesting ---
        Line line = new Line(new Point(1, 2), new Point(3, 4));
        if (line instanceof Line(Point(int x1, int y1), Point(int x2, int y2))) {
            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.printf("%nLine from (%d,%d) to (%d,%d), length=%.2f%n", x1, y1, x2, y2, length);
        }
    }

    static String describeShape(Shape shape) {
        return switch (shape) {
            // Destructure the record components right inside the case
            case ShapeCircle(Point(int x, int y), double r) ->
                    "Circle at (" + x + "," + y + ") with radius " + r;
            case ShapeRect(Point(int x1, int y1), Point(int x2, int y2)) ->
                    "Rectangle from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")";
        };
    }
}
