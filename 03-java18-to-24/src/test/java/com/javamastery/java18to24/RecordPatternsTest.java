package com.javamastery.java18to24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Record Patterns (Java 21)")
class RecordPatternsTest {

    record Point(int x, int y) {}
    record ColoredPoint(Point point, String color) {}
    record Line(Point start, Point end) {}

    @Test
    @DisplayName("Record pattern destructures components")
    void destructureComponents() {
        Object obj = new Point(3, 4);
        int sum = 0;
        if (obj instanceof Point(int x, int y)) {
            sum = x + y;
        }
        assertEquals(7, sum);
    }

    @Test
    @DisplayName("Nested record patterns destructure deeply")
    void nestedDestructure() {
        var cp = new ColoredPoint(new Point(5, 10), "RED");
        String result = "";
        if (cp instanceof ColoredPoint(Point(int x, int y), String color)) {
            result = x + "," + y + ":" + color;
        }
        assertEquals("5,10:RED", result);
    }

    @Test
    @DisplayName("Record pattern in switch")
    void recordPatternInSwitch() {
        sealed interface Shape permits Circ, Sq {}
        record Circ(double r) implements Shape {}
        record Sq(double side) implements Shape {}

        Shape s = new Circ(5.0);
        double area = switch (s) {
            case Circ(double r) -> Math.PI * r * r;
            case Sq(double side) -> side * side;
        };

        assertEquals(Math.PI * 25, area, 0.001);
    }

    @Test
    @DisplayName("Line record destructured to compute length")
    void lineLength() {
        var line = new Line(new Point(0, 0), new Point(3, 4));
        double length = 0;
        if (line instanceof Line(Point(int x1, int y1), Point(int x2, int y2))) {
            length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        assertEquals(5.0, length, 0.001);
    }
}
