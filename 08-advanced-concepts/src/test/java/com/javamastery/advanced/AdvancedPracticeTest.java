package com.javamastery.advanced;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Advanced Practice Problems")
class AdvancedPracticeTest {

    // ==================== Reflection Inspector ====================

    @Test
    @DisplayName("inspectClass returns field and method info")
    void inspectClass() {
        String info = AdvancedPracticeProblems.inspectClass(String.class);
        assertTrue(info.contains("Class: java.lang.String"));
        assertTrue(info.contains("Fields:"));
        assertTrue(info.contains("Methods:"));
    }

    // ==================== @NotNull Validator ====================

    @Test
    @DisplayName("Valid object has no violations")
    void validateNotNullValid() {
        var form = new AdvancedPracticeProblems.UserForm("Alice", "alice@example.com", null);
        List<String> violations = AdvancedPracticeProblems.validateNotNull(form);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Missing @NotNull field reports violation")
    void validateNotNullInvalid() {
        var form = new AdvancedPracticeProblems.UserForm(null, "bob@example.com", "Bobby");
        List<String> violations = AdvancedPracticeProblems.validateNotNull(form);
        assertEquals(1, violations.size());
        assertTrue(violations.get(0).contains("name"));
    }

    @Test
    @DisplayName("Multiple null @NotNull fields report multiple violations")
    void validateNotNullMultiple() {
        var form = new AdvancedPracticeProblems.UserForm(null, null, "Bobby");
        List<String> violations = AdvancedPracticeProblems.validateNotNull(form);
        assertEquals(2, violations.size());
    }

    // ==================== Generic Pair ====================

    @Test
    @DisplayName("Pair stores and retrieves values")
    void pairBasic() {
        var pair = AdvancedPracticeProblems.Pair.of("hello", 42);
        assertEquals("hello", pair.getFirst());
        assertEquals(42, pair.getSecond());
    }

    @Test
    @DisplayName("Pair equality")
    void pairEquality() {
        var p1 = AdvancedPracticeProblems.Pair.of("a", 1);
        var p2 = AdvancedPracticeProblems.Pair.of("a", 1);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ==================== Strategy Pattern ====================

    @Test
    @DisplayName("Capitalize strategy")
    void capitalize() {
        var processor = new AdvancedPracticeProblems.TextProcessor(AdvancedPracticeProblems.CAPITALIZE);
        assertEquals("HELLO", processor.process("hello"));
    }

    @Test
    @DisplayName("Reverse strategy")
    void reverse() {
        var processor = new AdvancedPracticeProblems.TextProcessor(AdvancedPracticeProblems.REVERSE);
        assertEquals("olleh", processor.process("hello"));
    }

    @Test
    @DisplayName("Remove vowels strategy")
    void removeVowels() {
        var processor = new AdvancedPracticeProblems.TextProcessor(AdvancedPracticeProblems.REMOVE_VOWELS);
        assertEquals("hll wrld", processor.process("hello world"));
    }

    @Test
    @DisplayName("Caesar cipher strategy")
    void caesarCipher() {
        var processor = new AdvancedPracticeProblems.TextProcessor(AdvancedPracticeProblems.caesarCipher(3));
        assertEquals("khoor", processor.process("hello"));
    }

    @Test
    @DisplayName("Strategy can be swapped at runtime")
    void swapStrategy() {
        var processor = new AdvancedPracticeProblems.TextProcessor(AdvancedPracticeProblems.CAPITALIZE);
        assertEquals("HELLO", processor.process("hello"));
        processor.setStrategy(AdvancedPracticeProblems.REVERSE);
        assertEquals("olleh", processor.process("hello"));
    }

    // ==================== Bounded Stack ====================

    @Test
    @DisplayName("Stack push and pop")
    void stackPushPop() {
        var stack = new AdvancedPracticeProblems.BoundedStack<Integer>(3);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    @DisplayName("Stack full throws on push")
    void stackFullThrows() {
        var stack = new AdvancedPracticeProblems.BoundedStack<String>(1);
        stack.push("a");
        assertThrows(IllegalStateException.class, () -> stack.push("b"));
    }

    @Test
    @DisplayName("Stack empty throws on pop")
    void stackEmptyThrows() {
        var stack = new AdvancedPracticeProblems.BoundedStack<String>(1);
        assertThrows(NoSuchElementException.class, stack::pop);
    }

    @Test
    @DisplayName("Stack peek returns top without removing")
    void stackPeek() {
        var stack = new AdvancedPracticeProblems.BoundedStack<Integer>(2);
        stack.push(10);
        assertEquals(10, stack.peek());
        assertEquals(1, stack.size());
    }

    // ==================== TypeSafeMap ====================

    @Test
    @DisplayName("TypeSafeMap stores and retrieves by type")
    void typeSafeMap() {
        var map = new AdvancedPracticeProblems.TypeSafeMap();
        map.put(String.class, "hello");
        map.put(Integer.class, 42);
        map.put(Double.class, 3.14);

        assertEquals("hello", map.get(String.class));
        assertEquals(42, map.get(Integer.class));
        assertEquals(3.14, map.get(Double.class));
    }

    @Test
    @DisplayName("TypeSafeMap returns null for missing type")
    void typeSafeMapMissing() {
        var map = new AdvancedPracticeProblems.TypeSafeMap();
        assertNull(map.get(String.class));
    }

    // ==================== Immutability ====================

    @Test
    @DisplayName("ImmutablePerson defensive copy in constructor")
    void immutablePersonDefensiveCopy() {
        List<String> hobbies = new ArrayList<>(List.of("Reading", "Coding"));
        var person = new ImmutabilityDemo.ImmutablePerson("Alice", 30, hobbies);

        // Modifying original list should NOT affect person
        hobbies.add("Gaming");
        assertEquals(2, person.getHobbies().size());
    }

    @Test
    @DisplayName("ImmutablePerson getHobbies returns unmodifiable list")
    void immutablePersonUnmodifiableHobbies() {
        var person = new ImmutabilityDemo.ImmutablePerson("Bob", 25, List.of("Music"));
        assertThrows(UnsupportedOperationException.class, () -> person.getHobbies().add("X"));
    }

    @Test
    @DisplayName("HttpRequest builder creates immutable object")
    void httpRequestBuilder() {
        var request = ImmutabilityDemo.HttpRequest.builder()
                .url("https://example.com")
                .method("POST")
                .header("Content-Type", "application/json")
                .body("{}")
                .timeoutMs(3000)
                .build();

        assertEquals("https://example.com", request.getUrl());
        assertEquals("POST", request.getMethod());
        assertEquals("{}", request.getBody());
        assertEquals(3000, request.getTimeoutMs());
        assertTrue(request.getHeaders().containsKey("Content-Type"));
    }

    @Test
    @DisplayName("HttpRequest headers are unmodifiable")
    void httpRequestHeadersUnmodifiable() {
        var request = ImmutabilityDemo.HttpRequest.builder()
                .url("https://example.com")
                .header("X-Custom", "value")
                .build();
        assertThrows(UnsupportedOperationException.class, () -> request.getHeaders().put("X-New", "val"));
    }
}
