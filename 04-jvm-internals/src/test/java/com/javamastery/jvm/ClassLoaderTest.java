package com.javamastery.jvm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Class Loading")
class ClassLoaderTest {

    @Test
    @DisplayName("Application classes loaded by AppClassLoader")
    void appClassLoader() {
        ClassLoader loader = ClassLoaderTest.class.getClassLoader();
        assertNotNull(loader);
        // AppClassLoader name varies by JVM implementation
        assertTrue(loader.getClass().getName().contains("AppClassLoader")
                || loader.getClass().getName().contains("Launcher"));
    }

    @Test
    @DisplayName("Core classes loaded by bootstrap (null classloader)")
    void bootstrapClassLoader() {
        // Bootstrap classloader is represented as null in Java
        assertNull(String.class.getClassLoader());
        assertNull(Object.class.getClassLoader());
        assertNull(Integer.class.getClassLoader());
    }

    @Test
    @DisplayName("Classloader hierarchy has parent chain")
    void classloaderHierarchy() {
        ClassLoader app = ClassLoaderTest.class.getClassLoader();
        ClassLoader platform = app.getParent();

        assertNotNull(platform);
        // Platform's parent is bootstrap (null)
        assertNull(platform.getParent());
    }

    @Test
    @DisplayName("Same class from same classloader is the same type")
    void sameClassSameLoader() throws ClassNotFoundException {
        ClassLoader loader = ClassLoaderTest.class.getClassLoader();
        Class<?> c1 = loader.loadClass("com.javamastery.jvm.BytecodeDemo");
        Class<?> c2 = loader.loadClass("com.javamastery.jvm.BytecodeDemo");

        assertSame(c1, c2); // same Class object since same classloader
    }

    @Test
    @DisplayName("Thread context classloader defaults to app classloader")
    void threadContextClassLoader() {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader appLoader = ClassLoaderTest.class.getClassLoader();

        assertEquals(appLoader, contextLoader);
    }
}
