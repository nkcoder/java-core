package org.nkcoder.jvm;

/**
 * Demonstrates JIT (Just-In-Time) Compiler Optimizations: Inlining and Dead Code Elimination.
 *
 * <p>Concept: The JVM starts by interpreting bytecode. Hot methods (executed frequently) are compiled to native code by
 * the C1 and C2 compilers.
 *
 * <p>Optimizations shown: 1. **Inlining**: Replacing a method call with the method body itself to save overhead. 2.
 * **Dead Code Elimination**: Removing code that has no side effects or whose result is unused.
 *
 * <p>To observe JIT in action, run with VM flags: {@code -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions
 * -XX:+PrintInlining} (Note: precise flags depend on Java version)
 */
public class JitInliningExample {

    public static void main(String[] args) {
        System.out.println("=== JIT Optimization Demo ===");
        System.out.println("Warming up to trigger C2 compilation...");

        long start = System.nanoTime();

        // Run enough times to trigger JIT (usually > 10,000 invocations)
        // C2 compiler usually kicks in for very hot loops
        long sum = 0;
        for (int i = 0; i < 1_000_000; i++) {
            sum += calculate(i);
        }

        long duration = System.nanoTime() - start;
        System.out.println("Result: " + sum); // Use result to prevent Dead Code Elimination of the loop
        System.out.println("Duration: " + duration / 1_000_000 + " ms");

        System.out.println("\nNOTE: To truly see Inlining, you must run with VM flags:");
        System.out.println(
                "  java -XX:+PrintCompilation -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining org.nkcoder.jvm.JitInliningExample");
    }

    // This method is small enough to be easily inlined
    // HotSpot default inline limit is ~35 bytes of bytecode for hot methods
    private static long calculate(int i) {
        return add(i, 10);
    }

    private static long add(int a, int b) {
        return a + b;
    }
}
