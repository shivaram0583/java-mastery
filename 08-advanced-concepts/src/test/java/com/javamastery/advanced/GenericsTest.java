package com.javamastery.advanced;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GenericsTest {

    @Test
    void pairHoldsTypedValues() {
        var pair = new GenericsDemo.Pair<>("key", 42);
        assertEquals("key", pair.first());
        assertEquals(42, pair.second());
    }

    @Test
    void maxReturnsLargestComparable() throws Exception {
        var method = GenericsDemo.class.getDeclaredMethod("max", Comparable.class, Comparable.class);
        method.setAccessible(true);

        assertEquals(7, method.invoke(null, 3, 7));
        assertEquals("banana", method.invoke(null, "apple", "banana"));
    }

    @Test
    void sumWorksWithDifferentNumberTypes() throws Exception {
        var method = GenericsDemo.class.getDeclaredMethod("sum", List.class);
        method.setAccessible(true);

        assertEquals(6.0, method.invoke(null, List.of(1, 2, 3)));
        assertEquals(6.0, method.invoke(null, List.of(1.0, 2.0, 3.0)));
    }

    @Test
    void typeSafeMapPreservesTypes() {
        var map = new GenericsDemo.TypeSafeMap();
        map.put(String.class, "hello");
        map.put(Integer.class, 42);

        assertEquals("hello", map.get(String.class));
        assertEquals(42, map.get(Integer.class));
    }

    @Test
    void typeErasureMeansRuntimeClassIsSame() {
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();
        // Generics are erased at runtime
        assertEquals(strings.getClass(), integers.getClass());
    }
}
