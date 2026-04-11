package com.javamastery.jvm;

/**
 * Demonstrates JIT Compilation concepts.
 *
 * The JVM interprets bytecode initially, then compiles hot methods
 * to native code using the JIT compiler (C1 → C2 pipeline).
 *
 * Run with JIT logging:
 *   java -XX:+PrintCompilation JitDemo
 *   java -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining JitDemo
 */
public class JitDemo {

    public static void main(String[] args) {
        System.out.println("=== JIT Compilation Demo ===\n");

        // --- 1. Warm-up: trigger JIT compilation ---
        System.out.println("--- Warming up (triggering JIT) ---");

        // Call a method many times so the JIT compiler kicks in
        // Default threshold: ~10,000 invocations for C2 compilation
        long sum = 0;
        int iterations = 100_000;
        for (int i = 0; i < iterations; i++) {
            sum += computeValue(i);
        }
        System.out.println("Warm-up result: " + sum);

        // --- 2. Measure interpreted vs JIT-compiled performance ---
        System.out.println("\n--- Performance Comparison ---");

        // After warm-up, the method should be JIT-compiled (C2)
        long start = System.nanoTime();
        long result = 0;
        for (int i = 0; i < 1_000_000; i++) {
            result += computeValue(i);
        }
        long elapsed = System.nanoTime() - start;
        System.out.printf("  JIT-compiled: %,d ns (result: %d)%n", elapsed, result);

        // --- 3. Escape Analysis demo ---
        System.out.println("\n--- Escape Analysis ---");
        System.out.println("If an object doesn't 'escape' a method, the JIT can:");
        System.out.println("  • Allocate it on the stack (no GC needed)");
        System.out.println("  • Eliminate the allocation entirely (scalar replacement)");

        start = System.nanoTime();
        long total = 0;
        for (int i = 0; i < 1_000_000; i++) {
            // This Point object may be stack-allocated via escape analysis
            // because it doesn't escape the method
            total += createAndSumPoint(i, i + 1);
        }
        elapsed = System.nanoTime() - start;
        System.out.printf("  Point creation loop: %,d ns (result: %d)%n", elapsed, total);
        System.out.println("  (The JIT may have eliminated object allocations entirely)");

        // --- 4. Inlining demo ---
        System.out.println("\n--- Inlining ---");
        System.out.println("The JIT replaces method calls with the method body.");
        System.out.println("Small, frequently-called methods are inlined automatically.");
        System.out.println("Use -XX:+PrintInlining to see inlining decisions.");
        System.out.println("Max inlined method size: -XX:MaxInlineSize=35 (default bytes)");
        System.out.println("Frequent call threshold: -XX:FreqInlineSize=325 (default bytes)");

        // --- 5. OSR (On-Stack Replacement) ---
        System.out.println("\n--- OSR (On-Stack Replacement) ---");
        System.out.println("When a long-running loop is detected, the JIT compiles it");
        System.out.println("and replaces the interpreted version mid-execution.");
        System.out.println("Look for '%' in -XX:+PrintCompilation output for OSR.");

        // --- 6. Key JIT flags ---
        System.out.println("\n--- Key JIT Flags ---");
        System.out.println("  -XX:+PrintCompilation        Show compiled methods");
        System.out.println("  -XX:+PrintInlining           Show inlining decisions");
        System.out.println("  -XX:CompileThreshold=10000   Invocations before compile");
        System.out.println("  -XX:-TieredCompilation        Disable tiered compilation");
        System.out.println("  -XX:+PrintAssembly            Show generated assembly (needs hsdis)");
    }

    /** Simple computation that becomes a hot method after many invocations. */
    static int computeValue(int n) {
        return n * 3 + 7;
    }

    /** Creates a Point and returns the sum — candidate for escape analysis. */
    static long createAndSumPoint(int x, int y) {
        record Point(int x, int y) {}
        Point p = new Point(x, y); // may be eliminated by escape analysis
        return p.x() + p.y();
    }
}
