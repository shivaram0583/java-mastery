package com.javamastery.java18to24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unnamed Variables (Java 22)")
class UnnamedVariablesTest {

    sealed interface Shape permits Circ, Rect {}
    record Circ(double r) implements Shape {}
    record Rect(double w, double h) implements Shape {}

    @Test
    @DisplayName("Unnamed catch variable compiles and runs")
    void unnamedCatch() {
        boolean caught = false;
        try {
            Integer.parseInt("abc");
        } catch (NumberFormatException _) {
            caught = true;
        }
        assertTrue(caught);
    }

    @Test
    @DisplayName("Unnamed pattern in switch ignores components")
    void unnamedPatternInSwitch() {
        Shape shape = new Circ(5.0);
        String type = switch (shape) {
            case Circ(_) -> "circle";
            case Rect(_, _) -> "rectangle";
        };
        assertEquals("circle", type);
    }
}
