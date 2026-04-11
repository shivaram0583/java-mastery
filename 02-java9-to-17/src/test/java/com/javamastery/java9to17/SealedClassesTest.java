package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sealed Classes (Java 17)")
class SealedClassesTest {

    sealed interface Shape permits Circle, Rect {}
    record Circle(double radius) implements Shape {}
    record Rect(double w, double h) implements Shape {}

    static double area(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rect r   -> r.w() * r.h();
        };
    }

    @Test
    @DisplayName("Exhaustive switch over sealed type computes circle area")
    void circleArea() {
        double result = area(new Circle(5));
        assertEquals(Math.PI * 25, result, 0.001);
    }

    @Test
    @DisplayName("Exhaustive switch over sealed type computes rectangle area")
    void rectangleArea() {
        double result = area(new Rect(4, 6));
        assertEquals(24.0, result, 0.001);
    }

    @Test
    @DisplayName("Sealed interface has correct permitted subclasses")
    void permittedSubclasses() {
        assertTrue(Shape.class.isSealed());
        Class<?>[] permitted = Shape.class.getPermittedSubclasses();
        assertEquals(2, permitted.length);
    }

    @Test
    @DisplayName("Records implementing sealed interface have value semantics")
    void recordValueSemantics() {
        Circle c1 = new Circle(5);
        Circle c2 = new Circle(5);
        assertEquals(c1, c2);
    }
}
