package com.javamastery.advanced;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * Demonstrates custom annotations and runtime processing.
 */
public class AnnotationsDemo {

    // --- 1. Define custom annotations ---

    @Retention(RetentionPolicy.RUNTIME)  // available at runtime via reflection
    @Target(ElementType.METHOD)          // can only annotate methods
    @interface Test {
        String description() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Timeout {
        long millis();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Component {
        String value() default "";
    }

    // --- 2. Use annotations ---

    @Component("calculator")
    static class Calculator {

        @Test(description = "adds two numbers")
        public int add(int a, int b) { return a + b; }

        @Test(description = "multiplies two numbers")
        @Timeout(millis = 100)
        public int multiply(int a, int b) { return a * b; }

        public int subtract(int a, int b) { return a - b; } // no @Test
    }

    // --- 3. Repeatable annotation (Java 8+) ---

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Roles.class) // container
    @interface Role {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Roles {
        Role[] value();
    }

    @Role("admin")
    @Role("manager")
    static void securedMethod() {
        System.out.println("  Secured method executed");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Annotations Demo ===\n");

        // --- Process @Component ---
        System.out.println("--- @Component ---");
        Class<?> clazz = Calculator.class;
        if (clazz.isAnnotationPresent(Component.class)) {
            Component comp = clazz.getAnnotation(Component.class);
            System.out.println("  Component name: '" + comp.value() + "'");
        }

        // --- Process @Test annotations (like a mini test framework) ---
        System.out.println("\n--- Running @Test methods ---");
        Object calc = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Test.class)) {
                Test test = method.getAnnotation(Test.class);
                System.out.println("  Running: " + method.getName() + " (" + test.description() + ")");

                // Check for timeout
                if (method.isAnnotationPresent(Timeout.class)) {
                    Timeout timeout = method.getAnnotation(Timeout.class);
                    System.out.println("    Timeout: " + timeout.millis() + "ms");
                }

                // Invoke with sample args
                Object result = method.invoke(calc, 3, 4);
                System.out.println("    Result: " + result);
            }
        }

        // --- Process @Repeatable ---
        System.out.println("\n--- @Repeatable Annotation ---");
        Method secured = AnnotationsDemo.class.getDeclaredMethod("securedMethod");
        Role[] roles = secured.getAnnotationsByType(Role.class);
        for (Role role : roles) {
            System.out.println("  Required role: " + role.value());
        }

        // --- All annotations on a method ---
        System.out.println("\n--- All Annotations ---");
        for (Method m : clazz.getDeclaredMethods()) {
            Annotation[] annotations = m.getAnnotations();
            if (annotations.length > 0) {
                System.out.println("  " + m.getName() + ": " +
                        java.util.Arrays.toString(annotations));
            }
        }
    }
}
