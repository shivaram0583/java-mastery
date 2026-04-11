package com.javamastery.java9to17;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("var (Java 10)")
class VarTest {

    @Test
    @DisplayName("var infers String type")
    void varInfersString() {
        var message = "hello";
        assertEquals(String.class, message.getClass());
        assertEquals(5, message.length());
    }

    @Test
    @DisplayName("var infers complex generic types")
    void varInfersGenericTypes() {
        var list = List.of("a", "b", "c");
        assertEquals(3, list.size());

        var grouped = list.stream()
                .collect(Collectors.groupingBy(s -> s));
        // grouped is Map<String, List<String>>
        assertTrue(grouped instanceof Map);
    }

    @Test
    @DisplayName("var works in for-each loop")
    void varInForEach() {
        var numbers = List.of(1, 2, 3);
        var sum = 0;
        for (var n : numbers) {
            sum += n;
        }
        assertEquals(6, sum);
    }

    @Test
    @DisplayName("var works in traditional for loop")
    void varInTraditionalFor() {
        var result = new StringBuilder();
        for (var i = 0; i < 3; i++) {
            result.append(i);
        }
        assertEquals("012", result.toString());
    }
}
