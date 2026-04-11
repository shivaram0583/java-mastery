package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Records (Java 16)")
class RecordsTest {

    record Point(int x, int y) {}

    record Range(int min, int max) {
        Range {
            if (min > max) throw new IllegalArgumentException("min > max");
        }
    }

    record Person(String name, int age) {
        boolean isAdult() { return age >= 18; }
    }

    @Test
    @DisplayName("Record accessors return component values")
    void recordAccessors() {
        Point p = new Point(3, 4);
        assertEquals(3, p.x());
        assertEquals(4, p.y());
    }

    @Test
    @DisplayName("Record toString returns descriptive string")
    void recordToString() {
        Point p = new Point(3, 4);
        assertEquals("Point[x=3, y=4]", p.toString());
    }

    @Test
    @DisplayName("Record equals is structural")
    void recordEqualsIsStructural() {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);
        Point p3 = new Point(3, 4);

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

    @Test
    @DisplayName("Record hashCode is consistent with equals")
    void recordHashCode() {
        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);

        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("Compact constructor validates")
    void compactConstructorValidates() {
        assertDoesNotThrow(() -> new Range(1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Range(10, 1));
    }

    @Test
    @DisplayName("Record custom methods work")
    void customMethods() {
        assertTrue(new Person("Alice", 20).isAdult());
        assertFalse(new Person("Bob", 16).isAdult());
    }
}
