package com.javamastery.java9to17;

/**
 * Demonstrates new String methods from Java 11.
 *
 * Java 11 added several useful methods to String:
 * isBlank(), lines(), strip(), stripLeading(), stripTrailing(), repeat()
 */
public class StringMethodsDemo {

    public static void main(String[] args) {
        System.out.println("=== String Methods Demo (Java 11) ===\n");

        // --- 1. isBlank() — true if empty or only whitespace ---
        System.out.println("''.isBlank(): " + "".isBlank());           // true
        System.out.println("'  '.isBlank(): " + "  ".isBlank());       // true
        System.out.println("'hi'.isBlank(): " + "hi".isBlank());       // false
        // Note: isEmpty() only checks length == 0; isBlank() also checks whitespace

        // --- 2. strip(), stripLeading(), stripTrailing() ---
        // Unlike trim(), strip() is Unicode-aware
        String padded = "  Hello World  ";
        System.out.println("\nstrip: '" + padded.strip() + "'");
        System.out.println("stripLeading: '" + padded.stripLeading() + "'");
        System.out.println("stripTrailing: '" + padded.stripTrailing() + "'");

        // --- 3. lines() — stream of lines from multi-line string ---
        String multiLine = "Line 1\nLine 2\nLine 3\n\nLine 5";
        System.out.println("\nlines():");
        multiLine.lines().forEach(line -> System.out.println("  '" + line + "'"));

        long lineCount = multiLine.lines().count();
        System.out.println("Line count: " + lineCount);

        // --- 4. repeat(n) — repeat string n times ---
        System.out.println("\n'abc'.repeat(3): " + "abc".repeat(3));
        System.out.println("'-'.repeat(20): " + "-".repeat(20));
        System.out.println("'x'.repeat(0): '" + "x".repeat(0) + "'"); // empty string
    }
}
