package com.javamastery.java18to24;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sequenced Collections (Java 21)")
class SequencedCollectionsTest {

    @Test
    @DisplayName("getFirst and getLast on ArrayList")
    void getFirstAndLast() {
        var list = new ArrayList<>(List.of("A", "B", "C"));

        assertEquals("A", list.getFirst());
        assertEquals("C", list.getLast());
    }

    @Test
    @DisplayName("addFirst and addLast on ArrayList")
    void addFirstAndLast() {
        var list = new ArrayList<>(List.of("B", "C"));
        list.addFirst("A");
        list.addLast("D");

        assertEquals(List.of("A", "B", "C", "D"), list);
    }

    @Test
    @DisplayName("reversed() returns live reversed view")
    void reversedView() {
        var list = new ArrayList<>(List.of("A", "B", "C"));
        var reversed = list.reversed();

        assertEquals(List.of("C", "B", "A"), reversed);
    }

    @Test
    @DisplayName("LinkedHashSet supports getFirst/getLast")
    void linkedHashSet() {
        var set = new LinkedHashSet<>(List.of("X", "Y", "Z"));

        assertEquals("X", set.getFirst());
        assertEquals("Z", set.getLast());
    }

    @Test
    @DisplayName("LinkedHashMap firstEntry/lastEntry")
    void linkedHashMap() {
        var map = new LinkedHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        assertEquals(Map.entry("one", 1), map.firstEntry());
        assertEquals(Map.entry("three", 3), map.lastEntry());
    }

    @Test
    @DisplayName("TreeSet reversed follows natural order")
    void treeSetReversed() {
        var treeSet = new TreeSet<>(List.of(3, 1, 4, 1, 5, 9, 2));
        // TreeSet: [1, 2, 3, 4, 5, 9]

        assertEquals(1, treeSet.getFirst());
        assertEquals(9, treeSet.getLast());
        assertEquals(List.of(9, 5, 4, 3, 2, 1), new ArrayList<>(treeSet.reversed()));
    }
}
