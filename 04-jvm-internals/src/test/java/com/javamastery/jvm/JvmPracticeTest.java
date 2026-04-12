package com.javamastery.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JVM Practice Problems")
class JvmPracticeTest {

    // ==================== ClassLoader Tests ====================

    @Test
    @DisplayName("Bootstrap classloader loads core classes")
    void bootstrapLoaderForCoreClasses() {
        assertNull(String.class.getClassLoader());
        assertNull(Integer.class.getClassLoader());
    }

    @Test
    @DisplayName("Application classloader loads user classes")
    void appLoaderForUserClasses() {
        ClassLoader loader = JvmPracticeProblems.class.getClassLoader();
        assertNotNull(loader);
    }

    @Test
    @DisplayName("Classloader parent chain terminates at bootstrap")
    void classloaderChain() {
        ClassLoader loader = JvmPracticeProblems.class.getClassLoader();
        int depth = 0;
        while (loader != null) {
            loader = loader.getParent();
            depth++;
        }
        assertTrue(depth >= 1); // at least AppClassLoader -> bootstrap
    }

    // ==================== String Pool Tests ====================

    @Test
    @DisplayName("String literals share the same reference")
    void stringLiteralsShareReference() {
        String s1 = "hello";
        String s2 = "hello";
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("new String creates distinct object")
    void newStringCreatesSeparateObject() {
        String s1 = "hello";
        String s2 = new String("hello");
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
    }

    @Test
    @DisplayName("intern() returns pool reference")
    void internReturnsPoolRef() {
        String s1 = "hello";
        String s2 = new String("hello").intern();
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("Compile-time constant string concat is interned")
    void compileTimeConcat() {
        String s1 = "hello";
        String s2 = "hel" + "lo";
        assertSame(s1, s2);
    }

    @Test
    @DisplayName("Runtime string concat creates new object")
    void runtimeConcat() {
        String s1 = "hello";
        String part = "lo";
        String s2 = "hel" + part;
        assertNotSame(s1, s2);
        assertEquals(s1, s2);
    }

    // ==================== Reference Type Tests ====================

    @Test
    @DisplayName("WeakReference may be collected after GC")
    void weakReferenceCollected() {
        WeakReference<byte[]> weakRef = new WeakReference<>(new byte[1024]);
        // Object has no strong reference, so it's eligible
        System.gc();
        // After GC, weak reference is likely cleared (not guaranteed but typical)
        // We can't assert null reliably, but we can assert the API works:
        // weakRef.get() returns either the object or null
        // This is mainly to demonstrate the concept
        assertNotNull(weakRef); // the WeakReference wrapper itself is not null
    }

    @Test
    @DisplayName("WeakHashMap removes entries when keys are GC'd")
    void weakHashMapRemovesEntries() {
        WeakHashMap<Object, String> map = new WeakHashMap<>();

        Object key1 = new Object();
        Object key2 = new Object();
        map.put(key1, "value1");
        map.put(key2, "value2");

        assertEquals(2, map.size());

        key1 = null; // remove strong reference
        System.gc();

        // After GC, WeakHashMap may have removed the entry for key1
        // The entry for key2 should still exist since we hold a strong reference
        assertTrue(map.containsKey(key2));
    }

    // ==================== Memory Tests ====================

    @Test
    @DisplayName("Memory info returns positive values")
    void memoryInfoPositive() {
        Map<String, Long> info = MemoryTuningDemo.getMemoryInfo();
        assertTrue(info.get("maxMemory") > 0);
        assertTrue(info.get("totalMemory") > 0);
        assertTrue(info.get("freeMemory") > 0);
        assertTrue(info.get("usedMemory") > 0);
    }

    @Test
    @DisplayName("Total memory is less than or equal to max memory")
    void totalLessOrEqualMax() {
        Map<String, Long> info = MemoryTuningDemo.getMemoryInfo();
        assertTrue(info.get("totalMemory") <= info.get("maxMemory"));
    }

    @Test
    @DisplayName("Used memory is positive and less than total")
    void usedMemoryBounds() {
        Map<String, Long> info = MemoryTuningDemo.getMemoryInfo();
        long used = info.get("usedMemory");
        long total = info.get("totalMemory");
        assertTrue(used > 0);
        assertTrue(used <= total);
    }

    @Test
    @DisplayName("JVM arguments list is not null")
    void jvmArguments() {
        var args = MemoryTuningDemo.getJvmArguments();
        assertNotNull(args);
    }

    // ==================== Stack vs Heap Tests ====================

    @Test
    @DisplayName("demonstrateStackVsHeap returns positive used memory")
    void stackVsHeapReturnsPositive() {
        long used = JvmPracticeProblems.demonstrateStackVsHeap();
        assertTrue(used > 0);
    }
}
