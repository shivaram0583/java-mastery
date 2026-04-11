package com.javamastery.advanced;

import java.util.*;

/**
 * Demonstrates common memory leak patterns in Java and their fixes.
 */
public class MemoryLeaksDemo {

    // ========== LEAK 1: Static collection growing forever ==========
    // BAD: static list never gets garbage collected
    private static final List<byte[]> staticCache = new ArrayList<>();

    static void staticCollectionLeak() {
        System.out.println("--- Leak: Static Collection ---");
        // Each call adds data that is NEVER removed
        for (int i = 0; i < 5; i++) {
            staticCache.add(new byte[1024]); // accumulates forever
        }
        System.out.println("  Cache size: " + staticCache.size() + " (grows without bound!)");
        System.out.println("  Fix: use bounded cache (LRU), WeakReference, or TTL eviction");

        staticCache.clear(); // cleanup for demo
    }

    // ========== LEAK 2: Non-static inner class holds reference ==========
    static class Outer {
        private final byte[] heavyData = new byte[1024 * 1024]; // 1MB

        // BAD: non-static inner class holds implicit reference to Outer
        class InnerListener {
            void onEvent() {
                System.out.println("  Inner class retains reference to Outer (and its 1MB data)");
            }
        }

        // GOOD: static inner class does NOT hold reference to Outer
        static class StaticListener {
            void onEvent() {
                System.out.println("  Static inner class: no reference to Outer");
            }
        }
    }

    // ========== LEAK 3: Unclosed resources ==========
    static void unclosedResourceLeak() {
        System.out.println("\n--- Leak: Unclosed Resources ---");
        System.out.println("  BAD:  InputStream is = new FileInputStream(f); // never closed");
        System.out.println("  GOOD: try (var is = new FileInputStream(f)) { ... }  // auto-closed");
        System.out.println("  Resources that need closing: streams, connections, ResultSet, etc.");
    }

    // ========== LEAK 4: Map key without equals/hashCode ==========
    static void mapKeyLeak() {
        System.out.println("\n--- Leak: Map Key Without hashCode/equals ---");

        // This class doesn't override equals/hashCode
        class Key {
            final String name;
            Key(String name) { this.name = name; }
            // Missing: equals() and hashCode() — each 'new Key()' is unique!
        }

        Map<Key, String> map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            // Each put creates a NEW entry because Key identity is object identity
            map.put(new Key("same-name"), "value-" + i);
        }
        System.out.println("  Map size: " + map.size() + " (expected 1, got 5 — LEAK!)");
        System.out.println("  Fix: override equals() and hashCode(), or use record");
    }

    // ========== LEAK 5: ThreadLocal without cleanup ==========
    static void threadLocalLeak() {
        System.out.println("\n--- Leak: ThreadLocal in Thread Pool ---");
        System.out.println("  Thread pool threads are reused → ThreadLocal values persist!");
        System.out.println("  Fix: always call threadLocal.remove() in try-finally");
        System.out.println("  See: Module 05 ThreadLocalDemo for full example");
    }

    // ========== FIX: WeakHashMap for caching ==========
    static void weakReferenceCache() {
        System.out.println("\n--- Fix: WeakHashMap ---");
        // Values are garbage-collected when key has no strong references
        WeakHashMap<Object, String> cache = new WeakHashMap<>();

        Object key = new Object();
        cache.put(key, "cached value");
        System.out.println("  Before GC: size=" + cache.size());

        key = null; // remove strong reference
        System.gc(); // suggest GC (not guaranteed)
        // In practice, the entry may be collected
        System.out.println("  After GC hint: size=" + cache.size() + " (may be 0)");
    }

    public static void main(String[] args) {
        System.out.println("=== Memory Leaks Demo ===\n");

        staticCollectionLeak();

        System.out.println("\n--- Leak: Non-Static Inner Class ---");
        new Outer().new InnerListener().onEvent();
        Outer.StaticListener.class.getName(); // static — no leak
        System.out.println("  Fix: use static inner class or top-level class");

        unclosedResourceLeak();
        mapKeyLeak();
        threadLocalLeak();
        weakReferenceCache();

        System.out.println("\n--- Detection Tools ---");
        System.out.println("  jvisualvm: heap dump, object counts");
        System.out.println("  jmap -histo:live <pid>: live object histogram");
        System.out.println("  Eclipse MAT: dominator tree analysis");
        System.out.println("  -XX:+HeapDumpOnOutOfMemoryError: auto dump on OOM");
    }
}
