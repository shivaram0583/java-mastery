package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Optional Enhancements (Java 9)")
class OptionalEnhancementsTest {

    @Test
    @DisplayName("ifPresentOrElse runs action when present")
    void ifPresentOrElsePresent() {
        AtomicBoolean presentCalled = new AtomicBoolean(false);
        Optional.of("hello").ifPresentOrElse(
                val -> presentCalled.set(true),
                () -> fail("Should not call empty action")
        );
        assertTrue(presentCalled.get());
    }

    @Test
    @DisplayName("ifPresentOrElse runs empty action when empty")
    void ifPresentOrElseEmpty() {
        AtomicBoolean emptyCalled = new AtomicBoolean(false);
        Optional.empty().ifPresentOrElse(
                val -> fail("Should not call present action"),
                () -> emptyCalled.set(true)
        );
        assertTrue(emptyCalled.get());
    }

    @Test
    @DisplayName("or provides alternative Optional when empty")
    void orProvidesAlternative() {
        Optional<String> result = Optional.<String>empty()
                .or(() -> Optional.of("fallback"));

        assertEquals("fallback", result.get());
    }

    @Test
    @DisplayName("or does not evaluate supplier when present")
    void orDoesNotEvaluateWhenPresent() {
        Optional<String> result = Optional.of("original")
                .or(() -> { fail("Supplier should not be called"); return Optional.of("x"); });

        assertEquals("original", result.get());
    }

    @Test
    @DisplayName("stream returns single-element stream when present")
    void streamPresent() {
        long count = Optional.of("hello").stream().count();
        assertEquals(1, count);
    }

    @Test
    @DisplayName("stream returns empty stream when empty")
    void streamEmpty() {
        long count = Optional.empty().stream().count();
        assertEquals(0, count);
    }
}
