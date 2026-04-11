package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pattern Matching for instanceof (Java 16)")
class PatternMatchingTest {

    @Test
    @DisplayName("Pattern variable is available in true branch")
    void patternVariable() {
        Object obj = "hello";

        if (obj instanceof String s) {
            assertEquals(5, s.length());
        } else {
            fail("Should match String");
        }
    }

    @Test
    @DisplayName("Pattern variable with guard condition")
    void patternWithGuard() {
        Object obj = 42;

        if (obj instanceof Integer n && n > 10) {
            assertTrue(n > 10);
        } else {
            fail("Should match Integer > 10");
        }
    }

    @Test
    @DisplayName("Pattern does not match wrong type")
    void patternDoesNotMatch() {
        Object obj = 42;

        assertFalse(obj instanceof String);
    }

    @Test
    @DisplayName("Pattern matching eliminates redundant casts")
    void eliminatesRedundantCasts() {
        record Shape(String type, double size) {}
        Object obj = new Shape("circle", 5.0);

        String result;
        if (obj instanceof Shape s) {
            result = s.type() + ":" + s.size();
        } else {
            result = "unknown";
        }

        assertEquals("circle:5.0", result);
    }
}
