package com.javamastery.advanced;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Demonstrates Java Reflection API:
 * inspecting classes, fields, methods, constructors at runtime,
 * and dynamically creating instances / invoking methods.
 */
public class ReflectionDemo {

    // Sample class to inspect
    static class Person {
        private String name;
        private int age;

        public Person() { this("Unknown", 0); }
        public Person(String name, int age) { this.name = name; this.age = age; }

        public String getName() { return name; }
        private String secret() { return "hidden-" + name; }

        @Override
        public String toString() { return "Person{name='" + name + "', age=" + age + "}"; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Reflection Demo ===\n");

        // --- 1. Getting Class object ---
        System.out.println("--- Getting Class Object ---");
        Class<?> clazz = Class.forName("com.javamastery.advanced.ReflectionDemo$Person");
        System.out.println("  Class: " + clazz.getName());
        System.out.println("  Simple: " + clazz.getSimpleName());
        System.out.println("  Enclosing: " + clazz.getEnclosingClass());

        // --- 2. Inspecting fields ---
        System.out.println("\n--- Fields ---");
        for (Field field : clazz.getDeclaredFields()) {
            System.out.println("  " + Modifier.toString(field.getModifiers()) + " "
                    + field.getType().getSimpleName() + " " + field.getName());
        }

        // --- 3. Inspecting methods ---
        System.out.println("\n--- Declared Methods ---");
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  " + Modifier.toString(method.getModifiers()) + " "
                    + method.getReturnType().getSimpleName() + " " + method.getName()
                    + "(" + Arrays.toString(method.getParameterTypes()) + ")");
        }

        // --- 4. Dynamic instantiation ---
        System.out.println("\n--- Dynamic Instantiation ---");
        Constructor<?> ctor = clazz.getConstructor(String.class, int.class);
        Object person = ctor.newInstance("Alice", 30);
        System.out.println("  Created: " + person);

        // --- 5. Invoking methods dynamically ---
        System.out.println("\n--- Dynamic Method Invocation ---");
        Method getName = clazz.getMethod("getName");
        String name = (String) getName.invoke(person);
        System.out.println("  getName(): " + name);

        // --- 6. Accessing private fields ---
        System.out.println("\n--- Accessing Private Field ---");
        Field ageField = clazz.getDeclaredField("age");
        ageField.setAccessible(true); // bypass access check
        int age = (int) ageField.get(person);
        System.out.println("  Private age: " + age);

        // Modify private field
        ageField.set(person, 31);
        System.out.println("  Modified: " + person);

        // --- 7. Invoking private method ---
        System.out.println("\n--- Invoking Private Method ---");
        Method secret = clazz.getDeclaredMethod("secret");
        secret.setAccessible(true);
        String result = (String) secret.invoke(person);
        System.out.println("  secret(): " + result);

        System.out.println("\n--- When to Use Reflection ---");
        System.out.println("  ✓ Frameworks (Spring, Hibernate, JUnit)");
        System.out.println("  ✓ Serialization libraries (Gson, Jackson)");
        System.out.println("  ✓ Plugin/module systems");
        System.out.println("  ✗ Normal application code (use interfaces instead)");
        System.out.println("  ✗ Performance-critical code (reflection is slow)");
    }
}
