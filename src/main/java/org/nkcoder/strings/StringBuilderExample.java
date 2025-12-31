package org.nkcoder.strings;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * StringBuilder: Efficient mutable string building.
 *
 * <p><strong>Java 25 Status:</strong> StringBuilder is still the recommended way for building strings dynamically,
 * especially in loops. StringBuffer is legacy (rarely needed).
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Mutable character sequences
 *   <li>Efficient string concatenation
 *   <li>When to use StringBuilder vs String
 *   <li>StringBuffer (legacy, thread-safe alternative)
 * </ul>
 */
public class StringBuilderExample {

    static void main(String[] args) {
        whyStringBuilder();
        basicOperations();
        chainingMethods();
        performanceComparison();
        capacityAndMemory();
        stringJoinerAlternative();
        whenToUseWhat();
    }

    // ===== Why StringBuilder? =====

    static void whyStringBuilder() {
        System.out.println("=== Why StringBuilder? ===");

        // Problem: String concatenation creates many objects

        System.out.println("""
        String concatenation in loop (inefficient):
          String result = "";
          for (int i = 0; i < 5; i++) {
            result += i;  // Creates new String each time!
          }
        """);

        // What actually happens:
        // result = new StringBuilder(result).append(i).toString();
        // Each iteration: StringBuilder created, String created

        // Solution: Use StringBuilder directly
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i);
        }
        String result = sb.toString();
        System.out.println("    Result: " + result);

        System.out.println("""
        Why StringBuilder is better:
        - Mutable: modifies internal buffer, no new objects
        - Efficient: amortized O(1) for append
        - Less GC pressure: fewer temporary objects
        """);
    }

    // ===== Basic Operations =====

    static void basicOperations() {
        System.out.println("=== Basic Operations ===");

        StringBuilder sb = new StringBuilder();

        // append - add to end
        sb.append("Hello");
        sb.append(" ");
        sb.append("World");
        System.out.println("  After appends: " + sb);

        // insert - add at position
        sb.insert(6, "Java ");
        System.out.println("  After insert(6, \"Java \"): " + sb);

        // delete - remove range
        sb.delete(6, 11); // Remove "Java "
        System.out.println("  After delete(6, 11): " + sb);

        // deleteCharAt - remove single char
        sb.deleteCharAt(5); // Remove space
        System.out.println("  After deleteCharAt(5): " + sb);

        // replace - replace range
        sb.replace(0, 5, "Hi");
        System.out.println("  After replace(0, 5, \"Hi\"): " + sb);

        // reverse
        sb.reverse();
        System.out.println("  After reverse(): " + sb);

        // setCharAt - modify single char
        sb.reverse(); // Back to normal
        sb.setCharAt(0, 'h');
        System.out.println("  After setCharAt(0, 'h'): " + sb);

        // setLength - truncate or extend
        sb.setLength(2);
        System.out.println("  After setLength(2): " + sb);

        System.out.println();
    }

    // ===== Method Chaining =====

    static void chainingMethods() {
        System.out.println("=== Method Chaining ===");

        // Most methods return 'this' for chaining
        String result = new StringBuilder()
                .append("SELECT * FROM ")
                .append("users")
                .append(" WHERE ")
                .append("status = 'ACTIVE'")
                .append(" ORDER BY ")
                .append("created_at")
                .toString();

        System.out.println("  Chained SQL: " + result);

        // Building formatted output
        String table = new StringBuilder()
                .append("| Name    | Age |\n")
                .append("|---------|-----|\n")
                .append("| Alice   | 30  |\n")
                .append("| Bob     | 25  |\n")
                .toString();

        System.out.println("\n  Chained table:\n" + table);

        // Append with different types
        String mixed = new StringBuilder()
                .append("Count: ")
                .append(42)
                .append(", Price: $")
                .append(19.99)
                .append(", Active: ")
                .append(true)
                .toString();

        System.out.println("  Mixed types: " + mixed);

        System.out.println();
    }

    // ===== Performance Comparison =====

    static void performanceComparison() {
        System.out.println("=== Performance Comparison ===");

        int iterations = 10000;

        // String concatenation (slow)
        long start = System.nanoTime();
        String s = "";
        for (int i = 0; i < iterations; i++) {
            s += "a";
        }
        long stringTime = System.nanoTime() - start;
        System.out.println("  String +=, result: " + s + "    : " + (stringTime / 1_000_000) + " ms");

        // StringBuilder (fast)
        start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("a");
        }
        String sbResult = sb.toString();
        long sbTime = System.nanoTime() - start;
        System.out.println("  StringBuilder result: " + sbResult + "    : " + (sbTime / 1_000_000) + " ms");

        System.out.println("  " + iterations + " concatenations:");
        System.out.println("    String +=    : " + (stringTime / 1_000_000) + " ms");
        System.out.println("    StringBuilder: " + (sbTime / 1_000_000) + " ms");
        System.out.println("    Speedup: ~" + (stringTime / Math.max(1, sbTime)) + "x faster");

        // Note: Modern JVM optimizes simple concatenation
        System.out.println("""

        Modern JVM optimizations:
        - Simple concatenation: "a" + "b" + "c" is optimized
        - Loop concatenation: Still needs StringBuilder
        - Rule: Use StringBuilder in loops or unknown iterations
        """);
    }

    // ===== Capacity and Memory =====

    static void capacityAndMemory() {
        System.out.println("=== Capacity and Memory ===");

        // Default capacity is 16
        StringBuilder sb1 = new StringBuilder();
        System.out.println("  Default capacity: " + sb1.capacity());

        // With initial capacity
        StringBuilder sb2 = new StringBuilder(100);
        System.out.println("  Specified capacity: " + sb2.capacity());

        // From string (length + 16)
        StringBuilder sb3 = new StringBuilder("Hello");
        System.out.println("  From \"Hello\": capacity = " + sb3.capacity() + " (5 + 16)");

        // Capacity grows automatically
        StringBuilder sb = new StringBuilder(4);
        System.out.println("\n  Capacity growth:");
        System.out.println("    Initial: " + sb.capacity());
        sb.append("12345"); // Exceeds 4, triggers growth
        System.out.println("    After \"12345\": " + sb.capacity());
        sb.append("1234567890");
        System.out.println("    After more: " + sb.capacity());

        // ensureCapacity - pre-allocate
        StringBuilder sb4 = new StringBuilder();
        sb4.ensureCapacity(1000);
        System.out.println("\n  After ensureCapacity(1000): " + sb4.capacity());

        // trimToSize - reduce memory
        sb4.append("Short");
        sb4.trimToSize();
        System.out.println("  After trimToSize(): " + sb4.capacity());

        System.out.println("""

        Capacity tips:
        - Pre-allocate if you know approximate size
        - Avoids multiple array copies during growth
        - trimToSize() after building if memory matters
        """);
    }

    // ===== StringJoiner Alternative =====

    static void stringJoinerAlternative() {
        System.out.println("=== StringJoiner Alternative ===");

        // For joining with delimiters, StringJoiner is cleaner
        StringJoiner sj = new StringJoiner(", ");
        sj.add("Apple");
        sj.add("Banana");
        sj.add("Cherry");
        System.out.println("  Basic join: " + sj);

        // With prefix and suffix
        StringJoiner withBrackets = new StringJoiner(", ", "[", "]");
        withBrackets.add("1");
        withBrackets.add("2");
        withBrackets.add("3");
        System.out.println("  With brackets: " + withBrackets);

        // Even cleaner: String.join()
        String joined = String.join(" | ", "A", "B", "C");
        System.out.println("  String.join(): " + joined);

        // From collection
        List<String> items = List.of("Red", "Green", "Blue");
        String colors = String.join(", ", items);
        System.out.println("  From list: " + colors);

        // Stream Collectors.joining() - most flexible
        String collected = items.stream().map(String::toUpperCase).collect(Collectors.joining(" + ", "(", ")"));
        System.out.println("  Collectors.joining(): " + collected);

        System.out.println();
    }

    // ===== When to Use What =====

    static void whenToUseWhat() {
        System.out.println("=== When to Use What ===");

        System.out.println("""
        StringBuilder:
        ✓ Building strings in loops
        ✓ Complex string construction
        ✓ When you need insert/delete/replace
        ✓ Performance-critical code

        String concatenation (+):
        ✓ Simple, one-line concatenation
        ✓ Constant expressions (compile-time optimized)
        ✓ Readability over performance

        String.join() / StringJoiner:
        ✓ Joining with delimiters
        ✓ Creating CSV, paths, etc.

        Collectors.joining():
        ✓ Joining stream elements
        ✓ With transformation

        StringBuffer (LEGACY):
        ✗ Rarely needed - use StringBuilder
        ✗ Only if thread-safety required (rare)
        ✗ Synchronized = slower
        """);

        // StringBuffer note
        System.out.println("  StringBuffer vs StringBuilder:");
        System.out.println("    StringBuffer: synchronized (thread-safe, slower)");
        System.out.println("    StringBuilder: not synchronized (faster)");
        System.out.println("    In practice: StringBuilder is almost always correct");
        System.out.println("    If you need thread-safety: use external synchronization");

        System.out.println();
    }
}
