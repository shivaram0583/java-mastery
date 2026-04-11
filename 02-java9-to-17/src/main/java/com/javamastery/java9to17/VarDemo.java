package com.javamastery.java9to17;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates local variable type inference with 'var' (Java 10).
 *
 * 'var' lets the compiler infer the type from the right-hand side.
 * It can only be used for local variables with initializers.
 */
public class VarDemo {

    public static void main(String[] args) {
        System.out.println("=== var Demo (Java 10) ===\n");

        // --- 1. Basic usage — compiler infers the type ---
        var message = "Hello, var!";            // inferred as String
        var count = 42;                          // inferred as int
        var price = 19.99;                       // inferred as double
        var names = List.of("Alice", "Bob");     // inferred as List<String>

        System.out.println("message type: " + message.getClass().getSimpleName());
        System.out.println("names type: " + names.getClass().getSimpleName());

        // --- 2. Useful with complex generics ---
        // Without var: Map<String, List<String>> grouped = ...
        var grouped = names.stream()
                .collect(Collectors.groupingBy(s -> s.substring(0, 1)));
        System.out.println("\nGrouped: " + grouped);

        // --- 3. var in for loops ---
        var numbers = List.of(1, 2, 3, 4, 5);
        for (var n : numbers) {
            System.out.print(n + " ");
        }
        System.out.println();

        // Traditional for loop
        for (var i = 0; i < 3; i++) {
            System.out.print("i=" + i + " ");
        }
        System.out.println();

        // --- 4. var in try-with-resources ---
        // var works in try-with-resources too
        // var reader = new java.io.BufferedReader(new java.io.StringReader("test"))

        // --- 5. Where var CANNOT be used ---
        // var x;                    // no initializer
        // var x = null;             // can't infer type from null
        // var x = {1, 2, 3};        // can't infer array type
        // Fields: var field = 5;    // not allowed
        // Parameters: void foo(var x) // not allowed
        // Return types: var foo() // not allowed

        System.out.println("\nvar is NOT a keyword — it's a 'reserved type name'.");
        System.out.println("You can still use 'var' as a variable name (not recommended).");
    }
}
