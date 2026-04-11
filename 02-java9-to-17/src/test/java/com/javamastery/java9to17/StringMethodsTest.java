package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("String Methods (Java 11)")
class StringMethodsTest {

    @Test
    @DisplayName("isBlank returns true for whitespace-only strings")
    void isBlank() {
        assertTrue("".isBlank());
        assertTrue("   ".isBlank());
        assertTrue("\t\n".isBlank());
        assertFalse("hello".isBlank());
    }

    @Test
    @DisplayName("strip removes Unicode whitespace")
    void strip() {
        assertEquals("hello", "  hello  ".strip());
        assertEquals("hello  ", "  hello  ".stripLeading());
        assertEquals("  hello", "  hello  ".stripTrailing());
    }

    @Test
    @DisplayName("lines returns stream of lines")
    void lines() {
        List<String> result = "one\ntwo\nthree".lines().toList();
        assertEquals(List.of("one", "two", "three"), result);
    }

    @Test
    @DisplayName("repeat repeats string n times")
    void repeat() {
        assertEquals("abcabcabc", "abc".repeat(3));
        assertEquals("", "abc".repeat(0));
    }

    @Test
    @DisplayName("isBlank vs isEmpty")
    void isBlankVsIsEmpty() {
        String whitespace = "   ";
        // isEmpty only checks length == 0
        assertFalse(whitespace.isEmpty());
        // isBlank also checks for whitespace-only
        assertTrue(whitespace.isBlank());
    }
}
