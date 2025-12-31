package org.nkcoder.pattern;

import java.util.List;

/**
 * Pattern Matching for instanceof (Java 16+): Cleaner type checks with automatic casting.
 *
 * <p><strong>Java 25 Status:</strong> Finalized and production-ready. This is the recommended way to do type checks in
 * modern Java code.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Combines instanceof check with variable binding
 *   <li>Eliminates redundant casting
 *   <li>Pattern variable is in scope only where pattern matched
 *   <li>Works with flow scoping (smart compiler)
 * </ul>
 */
public class InstanceofPatternExample {

    static void main(String[] args) {
        beforePatternMatching();
        withPatternMatching();
        flowScoping();
        negatedPatterns();
        complexConditions();
        realWorldExamples();
    }

    static void beforePatternMatching() {
        System.out.println("=== Before Pattern Matching (Old Way) ===");

        Object obj = "Hello, World!";

        // Old way: check, cast, use
        if (obj instanceof String) {
            String s = (String) obj; // Redundant cast
            System.out.println("  Length: " + s.length());
        }

        // Verbose and error-prone
        Object num = 42;
        if (num instanceof Integer) {
            Integer i = (Integer) num;
            System.out.println("  Doubled: " + (i * 2));
        }

        System.out.println();
    }

    static void withPatternMatching() {
        System.out.println("=== With Pattern Matching (Java 16+) ===");

        Object obj = "Hello, World!";

        // New way: check and bind in one step
        if (obj instanceof String s) {
            // s is automatically cast and available here
            System.out.println("  Length: " + s.length());
            System.out.println("  Uppercase: " + s.toUpperCase());
        }

        Object num = 42;
        if (num instanceof Integer i) {
            System.out.println("  Doubled: " + (i * 2));
        }

        // Works with any type
        Object list = List.of(1, 2, 3);
        if (list instanceof List<?> l) {
            System.out.println("  List size: " + l.size());
        }

        System.out.println();
    }

    static void flowScoping() {
        System.out.println("=== Flow Scoping ===");

        Object obj = "test";

        // Pattern variable scope extends where it's definitely matched
        if (obj instanceof String s && s.length() > 2) {
            System.out.println("  String longer than 2: " + s);
        }

        // Also works with OR when checking for NOT instanceof
        if (!(obj instanceof String s)) {
            System.out.println("  Not a string");
            return;
        }
        // s is in scope here because we only reach here if obj IS a String
        System.out.println("  We know it's a string: " + s);

        System.out.println("""

        Flow scoping rules:
        - Variable is in scope where pattern DEFINITELY matched
        - Works with && (both conditions)
        - Works with negation and early return
        - Compiler tracks control flow
        """);
    }

    static void negatedPatterns() {
        System.out.println("=== Negated Patterns ===");

        Object obj = 123;

        // Guard clause pattern - handle non-matching first
        if (!(obj instanceof String s)) {
            System.out.println("  Not a string, handling differently");
            // s is NOT in scope here
        } else {
            // s IS in scope here
            System.out.println("  String value: " + s);
        }

        // Early return pattern
        Object input = "valid";
        String result = processInput(input);
        System.out.println("  Processed: " + result);

        System.out.println();
    }

    private static String processInput(Object input) {
        // Guard clause with pattern matching
        if (!(input instanceof String s)) {
            return "Invalid input: expected String";
        }
        // s is in scope for the rest of the method
        return s.trim().toLowerCase();
    }

    static void complexConditions() {
        System.out.println("=== Complex Conditions ===");

        Object obj = "Hello";

        // Combining with && (s in scope in second condition)
        if (obj instanceof String s && !s.isEmpty() && s.startsWith("H")) {
            System.out.println("  Non-empty string starting with H: " + s);
        }

        // With || - pattern variable NOT in scope (might not have matched)
        // This would NOT compile:
        // if (obj instanceof String s || s.isEmpty()) { }

        // Multiple instanceof checks
        Object a = "text";
        Object b = 42;

        if (a instanceof String sa && b instanceof Integer ib) {
            System.out.println("  String: " + sa + ", Integer: " + ib);
        }

        System.out.println();
    }

    static void realWorldExamples() {
        System.out.println("=== Real-World Examples ===");

        // 1. equals() implementation
        record Point(int x, int y) {
            @Override
            public boolean equals(Object obj) {
                return obj instanceof Point p && x == p.x && y == p.y;
            }
        }

        Point p1 = new Point(1, 2);
        Point p2 = new Point(1, 2);
        Point p3 = new Point(3, 4);

        System.out.println("  p1.equals(p2): " + p1.equals(p2));
        System.out.println("  p1.equals(p3): " + p1.equals(p3));
        System.out.println("  p1.equals(\"string\"): " + p1.equals("string"));

        // 2. Processing heterogeneous collections
        var items = List.of("hello", 42, 3.14, true);
        System.out.println("\n  Processing mixed list:");
        for (Object item : items) {
            String description = describeObject(item);
            System.out.println("    " + description);
        }

        // 3. Method dispatch
        System.out.println("\n  Method dispatch:");
        handleEvent(new ClickEvent(10, 20));
        handleEvent(new KeyEvent('A'));
        handleEvent("unknown");

        System.out.println();
    }

    private static String describeObject(Object obj) {
        if (obj instanceof String s) {
            return "String of length " + s.length();
        } else if (obj instanceof Integer i) {
            return "Integer: " + i;
        } else if (obj instanceof Double d) {
            return "Double: " + String.format("%.2f", d);
        } else if (obj instanceof Boolean b) {
            return "Boolean: " + b;
        }
        return "Unknown type";
    }

    record ClickEvent(int x, int y) {}

    record KeyEvent(char key) {}

    private static void handleEvent(Object event) {
        if (event instanceof ClickEvent click) {
            System.out.println("    Click at (" + click.x() + ", " + click.y() + ")");
        } else if (event instanceof KeyEvent key) {
            System.out.println("    Key pressed: " + key.key());
        } else {
            System.out.println("    Unknown event type");
        }
    }
}
