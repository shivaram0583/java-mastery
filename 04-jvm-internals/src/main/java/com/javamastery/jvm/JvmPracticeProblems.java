package com.javamastery.jvm;

import java.lang.ref.*;
import java.util.*;

/**
 * Practice problems for JVM internals — classloading, memory, GC, references.
 */
public class JvmPracticeProblems {

    // ==================== ClassLoader Investigation ====================

    /** Problem 1: Print classloader hierarchy for different classes */
    public static void printClassLoaderHierarchy() {
        System.out.println("=== ClassLoader Hierarchy ===");

        // String is loaded by Bootstrap ClassLoader (returns null in Java)
        System.out.println("String ClassLoader: " + String.class.getClassLoader());

        // Our own class is loaded by Application ClassLoader
        ClassLoader appLoader = JvmPracticeProblems.class.getClassLoader();
        System.out.println("JvmPracticeProblems ClassLoader: " + appLoader);

        // Walk the hierarchy
        System.out.println("\n=== ClassLoader Chain ===");
        ClassLoader current = appLoader;
        while (current != null) {
            System.out.println("  " + current.getClass().getName());
            current = current.getParent();
        }
        System.out.println("  Bootstrap ClassLoader (null)");
    }

    // ==================== Reference Types ====================

    /** Problem: Demonstrate Strong, Weak, Soft, and Phantom references */
    public static void demonstrateReferences() {
        System.out.println("\n=== Reference Types Demo ===");

        // Strong reference — never GC'd while referenced
        Object strongRef = new Object();
        System.out.println("Strong ref: " + strongRef);

        // Weak reference — collected at next GC
        WeakReference<Object> weakRef = new WeakReference<>(new Object());
        System.out.println("Weak ref before GC: " + weakRef.get());
        System.gc();
        System.out.println("Weak ref after GC: " + weakRef.get()); // likely null

        // Soft reference — collected only when memory is low
        SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024]);
        System.out.println("Soft ref before GC: " + (softRef.get() != null ? "alive" : "collected"));
        System.gc();
        System.out.println("Soft ref after GC: " + (softRef.get() != null ? "alive" : "collected"));

        // Phantom reference — must use ReferenceQueue
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        Object phantomObject = new Object();
        PhantomReference<Object> phantomRef = new PhantomReference<>(phantomObject, queue);
        System.out.println("Phantom ref.get() always returns: " + phantomRef.get()); // always null
        phantomObject = null; // make eligible for GC
        System.gc();
        Reference<?> polled = queue.poll();
        System.out.println("Phantom ref enqueued after GC: " + (polled != null));
    }

    // ==================== String Pool ====================

    /** Problem 7: Investigate string pool behavior */
    public static void investigateStringPool() {
        System.out.println("\n=== String Pool Investigation ===");

        // Literal strings are automatically interned
        String s1 = "hello";
        String s2 = "hello";
        System.out.println("s1 == s2 (both literals): " + (s1 == s2)); // true

        // new String() creates a new object on heap
        String s3 = new String("hello");
        System.out.println("s1 == s3 (literal vs new): " + (s1 == s3)); // false
        System.out.println("s1.equals(s3): " + s1.equals(s3)); // true

        // intern() returns the pool reference
        String s4 = s3.intern();
        System.out.println("s1 == s4 (literal vs interned): " + (s1 == s4)); // true

        // String concatenation at compile time
        String s5 = "hel" + "lo"; // compiler optimizes to "hello"
        System.out.println("s1 == s5 (concat literals): " + (s1 == s5)); // true

        // Runtime concatenation creates new object
        String part = "lo";
        String s6 = "hel" + part; // runtime concat
        System.out.println("s1 == s6 (runtime concat): " + (s1 == s6)); // false
    }

    // ==================== Memory Layout ====================

    /** Demonstrate stack vs heap allocation */
    public static long demonstrateStackVsHeap() {
        // Local primitives → stack
        int x = 10;
        int y = 20;
        int sum = x + y;  // all on stack, no heap allocation

        // Object creation → heap (reference on stack)
        var list = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            list.add(i);  // Integer autoboxing creates heap objects
        }

        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /** Demonstrate object lifetimes in GC generations */
    public static void demonstrateObjectLifetimes() {
        System.out.println("\n=== Object Lifetimes ===");

        long before = usedMemory();

        // Short-lived objects (die in Young Generation)
        for (int i = 0; i < 10_000; i++) {
            var temp = new byte[100]; // created and immediately unreachable
        }

        // Long-lived objects (promoted to Old Generation after surviving multiple GCs)
        List<byte[]> longLived = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            longLived.add(new byte[1024]);
        }

        long after = usedMemory();
        System.out.println("Memory used by long-lived objects: ~" + (after - before) / 1024 + " KB");
        System.out.println("Short-lived objects: already garbage collected");

        longLived.clear(); // now eligible for GC
    }

    // ==================== WeakHashMap ====================

    /** Demonstrate WeakHashMap behavior — keys are weakly referenced */
    public static void demonstrateWeakHashMap() {
        System.out.println("\n=== WeakHashMap Demo ===");

        var map = new WeakHashMap<Object, String>();

        Object key1 = new Object();
        Object key2 = new Object();

        map.put(key1, "value1");
        map.put(key2, "value2");

        System.out.println("Map size before GC: " + map.size()); // 2

        key1 = null; // remove strong reference to key1
        System.gc();

        // After GC, entry with key1 may be removed
        System.out.println("Map size after GC (key1 nulled): " + map.size());
    }

    // ==================== Helpers ====================

    private static long usedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    public static void main(String[] args) {
        printClassLoaderHierarchy();
        demonstrateReferences();
        investigateStringPool();
        demonstrateStackVsHeap();
        demonstrateObjectLifetimes();
        demonstrateWeakHashMap();

        System.out.println("\n=== JVM Info ===");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JVM name: " + Runtime.getRuntime().toString());
        System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        System.out.println("Total memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB");
    }
}
