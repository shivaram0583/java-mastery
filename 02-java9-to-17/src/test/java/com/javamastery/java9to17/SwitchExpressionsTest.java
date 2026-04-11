package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Switch Expressions (Java 14+)")
class SwitchExpressionsTest {

    enum Color { RED, GREEN, BLUE }

    @Test
    @DisplayName("Switch expression returns a value")
    void switchExpressionReturnsValue() {
        Color color = Color.GREEN;

        String result = switch (color) {
            case RED -> "Stop";
            case GREEN -> "Go";
            case BLUE -> "Sky";
        };

        assertEquals("Go", result);
    }

    @Test
    @DisplayName("Multiple case labels in one branch")
    void multipleCaseLabels() {
        int day = 6;
        String type = switch (day) {
            case 1, 7 -> "Weekend";
            case 2, 3, 4, 5, 6 -> "Weekday";
            default -> "Invalid";
        };

        assertEquals("Weekday", type);
    }

    @Test
    @DisplayName("yield in block returns value")
    void yieldInBlock() {
        int month = 2;
        int daysIn2024 = switch (month) {
            case 2 -> {
                boolean leapYear = true;
                yield leapYear ? 29 : 28;
            }
            case 4, 6, 9, 11 -> 30;
            default -> 31;
        };

        assertEquals(29, daysIn2024);
    }

    @Test
    @DisplayName("Pattern matching in switch with types")
    void patternMatchingSwitch() {
        Object obj = 42;
        String result = switch (obj) {
            case Integer i -> "int:" + i;
            case String s -> "str:" + s;
            default -> "other";
        };

        assertEquals("int:42", result);
    }
}
