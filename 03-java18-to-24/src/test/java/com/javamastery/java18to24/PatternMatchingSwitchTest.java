package com.javamastery.java18to24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pattern Matching for switch (Java 21)")
class PatternMatchingSwitchTest {

    sealed interface Animal permits Dog, Cat {}
    record Dog(String name) implements Animal {}
    record Cat(String name) implements Animal {}

    @Test
    @DisplayName("Type pattern matches correct type")
    void typePatternMatches() {
        Object obj = "hello";
        String result = switch (obj) {
            case Integer i -> "int:" + i;
            case String s  -> "str:" + s;
            default        -> "other";
        };
        assertEquals("str:hello", result);
    }

    @Test
    @DisplayName("Null case handled in switch")
    void nullCase() {
        Object obj = null;
        String result = switch (obj) {
            case null   -> "null";
            case String s -> "string";
            default -> "other";
        };
        assertEquals("null", result);
    }

    @Test
    @DisplayName("Guarded pattern with when clause")
    void guardedPattern() {
        Object obj = "hello world";
        String result = switch (obj) {
            case String s when s.length() > 5 -> "long string";
            case String s -> "short string";
            default -> "not a string";
        };
        assertEquals("long string", result);
    }

    @Test
    @DisplayName("Exhaustive matching over sealed types")
    void exhaustiveSealed() {
        Animal animal = new Dog("Rex");
        String result = switch (animal) {
            case Dog d -> "Dog: " + d.name();
            case Cat c -> "Cat: " + c.name();
        };
        assertEquals("Dog: Rex", result);
    }
}
