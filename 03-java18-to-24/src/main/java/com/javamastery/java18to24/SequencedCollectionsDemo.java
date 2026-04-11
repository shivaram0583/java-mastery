package com.javamastery.java18to24;

import java.util.*;

/**
 * Demonstrates Sequenced Collections (Java 21 — JEP 431).
 *
 * New interfaces provide uniform access to first/last elements
 * and a reversed view for ordered collections.
 *
 * New interfaces:
 *   SequencedCollection<E> extends Collection<E>
 *   SequencedSet<E> extends SequencedCollection<E>, Set<E>
 *   SequencedMap<K,V> extends Map<K,V>
 */
public class SequencedCollectionsDemo {

    public static void main(String[] args) {
        System.out.println("=== Sequenced Collections Demo (Java 21) ===\n");

        // --- 1. SequencedCollection (List, Deque) ---
        System.out.println("--- SequencedCollection (ArrayList) ---");
        var list = new ArrayList<>(List.of("B", "C", "D"));

        // New methods
        list.addFirst("A");     // add at beginning
        list.addLast("E");      // add at end
        System.out.println("List: " + list);
        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());

        // Reversed view — NOT a copy, it's a live view
        List<String> reversed = list.reversed();
        System.out.println("Reversed: " + reversed);

        // removeFirst / removeLast
        list.removeFirst();
        list.removeLast();
        System.out.println("After removing first & last: " + list);

        // --- 2. SequencedSet (LinkedHashSet, TreeSet) ---
        System.out.println("\n--- SequencedSet (LinkedHashSet) ---");
        var set = new LinkedHashSet<>(List.of("Apple", "Banana", "Cherry"));

        System.out.println("Set: " + set);
        System.out.println("First: " + set.getFirst());
        System.out.println("Last: " + set.getLast());
        System.out.println("Reversed: " + set.reversed());

        // --- 3. SequencedMap (LinkedHashMap, TreeMap) ---
        System.out.println("\n--- SequencedMap (LinkedHashMap) ---");
        var map = new LinkedHashMap<String, Integer>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        System.out.println("Map: " + map);
        System.out.println("First entry: " + map.firstEntry());
        System.out.println("Last entry: " + map.lastEntry());

        // putFirst/putLast
        map.putFirst("zero", 0);
        map.putLast("four", 4);
        System.out.println("After putFirst/putLast: " + map);

        // Reversed map view
        var reversedMap = map.reversed();
        System.out.println("Reversed map: " + reversedMap);

        // pollFirstEntry / pollLastEntry
        Map.Entry<String, Integer> polled = map.pollFirstEntry();
        System.out.println("Polled first: " + polled);
        System.out.println("After poll: " + map);

        // --- 4. SortedSet/SortedMap are now SequencedSet/SequencedMap ---
        System.out.println("\n--- TreeSet is a SequencedSet ---");
        var treeSet = new TreeSet<>(List.of(5, 3, 1, 4, 2));
        System.out.println("TreeSet: " + treeSet);
        System.out.println("First: " + treeSet.getFirst());
        System.out.println("Last: " + treeSet.getLast());
        System.out.println("Reversed: " + treeSet.reversed());
    }
}
