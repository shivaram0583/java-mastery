package com.javamastery.jvm;

import java.io.*;

/**
 * Demonstrates Class Loading in the JVM.
 *
 * The JVM uses a delegation model with three built-in classloaders:
 *   1. Bootstrap ClassLoader — loads core Java classes (java.lang.*, etc.)
 *   2. Platform ClassLoader (was Extension) — loads platform modules
 *   3. Application ClassLoader — loads classes from the application classpath
 */
public class ClassLoaderDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Class Loading Demo ===\n");

        // --- 1. Show the classloader hierarchy ---
        System.out.println("--- Classloader Hierarchy ---");

        // Application classloader loads our class
        ClassLoader appLoader = ClassLoaderDemo.class.getClassLoader();
        System.out.println("ClassLoaderDemo loaded by: " + appLoader);

        // Platform classloader (parent of application)
        ClassLoader platformLoader = appLoader.getParent();
        System.out.println("Parent (Platform): " + platformLoader);

        // Bootstrap classloader (parent of platform) — returns null in Java
        ClassLoader bootstrapLoader = platformLoader.getParent();
        System.out.println("Parent (Bootstrap): " + bootstrapLoader + " (null = bootstrap)");

        // Core classes are loaded by bootstrap
        System.out.println("\nString loaded by: " + String.class.getClassLoader() + " (bootstrap)");

        // --- 2. Delegation model ---
        System.out.println("\n--- Delegation Model ---");
        System.out.println("When loading a class:");
        System.out.println("  1. AppClassLoader asks PlatformClassLoader");
        System.out.println("  2. PlatformClassLoader asks BootstrapClassLoader");
        System.out.println("  3. If Bootstrap can't find it, Platform tries");
        System.out.println("  4. If Platform can't find it, App tries");
        System.out.println("  5. If App can't find it, ClassNotFoundException");

        // --- 3. Class.forName vs classloader.loadClass ---
        // Class.forName initializes the class (runs static blocks)
        // classloader.loadClass does NOT initialize
        System.out.println("\n--- Class.forName vs loadClass ---");
        System.out.println("Class.forName: loads AND initializes the class");
        System.out.println("ClassLoader.loadClass: loads but does NOT initialize");

        // --- 4. Custom ClassLoader example ---
        System.out.println("\n--- Custom ClassLoader ---");
        CustomClassLoader custom = new CustomClassLoader();
        // This would load from a custom source; for demo we show the structure
        System.out.println("Custom classloader created: " + custom);
        System.out.println("Its parent: " + custom.getParent());

        // --- 5. Thread context classloader ---
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("\nThread context classloader: " + contextLoader);
    }

    /**
     * Example of a custom classloader.
     * In practice, used for: plugin systems, hot-reloading, isolation.
     */
    static class CustomClassLoader extends ClassLoader {
        public CustomClassLoader() {
            super(ClassLoaderDemo.class.getClassLoader()); // parent = app classloader
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // In a real implementation, you'd read bytecode from a custom source:
            // - network, database, encrypted file, generated at runtime, etc.
            // byte[] classBytes = loadClassBytesFromCustomSource(name);
            // return defineClass(name, classBytes, 0, classBytes.length);
            throw new ClassNotFoundException("Custom loader cannot find: " + name);
        }
    }
}
