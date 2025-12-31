package org.nkcoder.pattern;

/**
 * Pattern Matching for switch (Java 21+): Type patterns in switch cases.
 *
 * <p><strong>Java 25 Status:</strong> Finalized in Java 21 and production-ready. Replaces long if-else-instanceof
 * chains with cleaner, more expressive switch statements.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Type patterns in case labels
 *   <li>null handling in switch
 *   <li>Pattern variable binding
 *   <li>Combining with guarded patterns (when)
 * </ul>
 */
public class SwitchPatternExample {

    static void main(String[] args) {
        beforePatternSwitch();
        typePatterns();
        nullHandling();
        dominanceAndOrdering();
        realWorldExamples();
    }

    static void beforePatternSwitch() {
        System.out.println("=== Before Pattern Matching for Switch ===");

        Object obj = "Hello";

        // Old way: chain of if-else-instanceof
        String result;
        if (obj instanceof String s) {
            result = "String: " + s;
        } else if (obj instanceof Integer i) {
            result = "Integer: " + i;
        } else if (obj instanceof Double d) {
            result = "Double: " + d;
        } else if (obj == null) {
            result = "null";
        } else {
            result = "Unknown type";
        }

        System.out.println("  Result: " + result);

        System.out.println("""

        Problems with if-else chains:
        - Verbose and repetitive
        - Easy to miss a type
        - No exhaustiveness checking
        - Harder to read as chain grows
        """);
    }

    static void typePatterns() {
        System.out.println("=== Type Patterns in Switch ===");

        // Type patterns make this much cleaner
        Object obj = 42;

        String result =
                switch (obj) {
                    case String s -> "String of length " + s.length();
                    case Integer i -> "Integer: " + i;
                    case Double d -> "Double: " + String.format("%.2f", d);
                    case Long l -> "Long: " + l;
                    case null -> "null value";
                    default -> "Unknown: " + obj.getClass().getSimpleName();
                };

        System.out.println("  Result: " + result);

        // Works with any Object
        Object[] items = {"hello", 123, 45.67, true, null, new int[] {1, 2, 3}};

        System.out.println("\n  Processing various types:");
        for (Object item : items) {
            String desc =
                    switch (item) {
                        case String s -> "String: \"" + s + "\"";
                        case Integer i -> "Integer: " + i;
                        case Double d -> "Double: " + d;
                        case Boolean b -> "Boolean: " + b;
                        case int[] arr -> "int array of length " + arr.length;
                        case null -> "null";
                        default -> "Other: " + item.getClass().getSimpleName();
                    };
            System.out.println("    " + desc);
        }

        System.out.println();
    }

    static void nullHandling() {
        System.out.println("=== Null Handling ===");

        // Before Java 21: switch on null threw NullPointerException
        // Now: null can be handled explicitly

        String input = null;

        // Explicit null case
        String result =
                switch (input) {
                    case null -> "Input was null";
                    case String s when s.isEmpty() -> "Empty string";
                    case String s -> "String: " + s;
                };

        System.out.println("  null input: " + result);

        // null with default
        Object obj = null;
        String desc =
                switch (obj) {
                    case String s -> "String";
                    case Integer i -> "Integer";
                    case null, default -> "null or unknown"; // Combined!
                };

        System.out.println("  null with default: " + desc);

        // Separate null and default
        Object value = "test";
        String output =
                switch (value) {
                    case null -> "Explicitly null";
                    case String s -> "String: " + s;
                    default -> "Something else";
                };

        System.out.println("  Separate null case: " + output);

        System.out.println("""

        Null handling options:
        - case null -> ... (explicit null handling)
        - case null, default -> ... (combine with default)
        - No null case = NullPointerException if null passed
        """);
    }

    static void dominanceAndOrdering() {
        System.out.println("=== Pattern Dominance and Ordering ===");

        // Order matters! More specific patterns must come first

        Object obj = "test";

        // CORRECT ordering - specific before general
        String result =
                switch (obj) {
                    case String s when s.length() > 10 -> "Long string";
                    case String s when s.isEmpty() -> "Empty string";
                    case String s -> "Regular string: " + s; // More general
                    case CharSequence cs -> "CharSequence"; // String IS-A CharSequence
                    default -> "Other";
                };

        System.out.println("  Result: " + result);

        // Demonstrating hierarchy
        record Animal(String name) {}
        record Dog(String name, String breed) {}

        // This would NOT compile because Dog extends Object, not Animal
        // But conceptually, more specific types must come before more general

        System.out.println("""

        Dominance rules:
        - Guarded patterns (when) before unguarded
        - Subtype patterns before supertype patterns
        - Specific before general
        - Compiler enforces correct ordering!
        """);
    }

    // Sealed types for realWorldExamples() - must be at class level
    sealed interface Event permits ClickEvent, KeyEvent, ScrollEvent {}

    record ClickEvent(int x, int y) implements Event {}

    record KeyEvent(char key, boolean shift) implements Event {}

    record ScrollEvent(int delta) implements Event {}

    sealed interface JsonValue permits JsonString, JsonNumber, JsonBool, JsonNull {}

    record JsonString(String value) implements JsonValue {}

    record JsonNumber(double value) implements JsonValue {}

    record JsonBool(boolean value) implements JsonValue {}

    record JsonNull() implements JsonValue {}

    static void realWorldExamples() {
        System.out.println("=== Real-World Examples ===");

        // 1. Event handling
        Event event = new ClickEvent(100, 200);

        String response =
                switch (event) {
                    case ClickEvent(int x, int y) -> "Click at (" + x + ", " + y + ")";
                    case KeyEvent(char key, boolean shift) -> (shift ? "Shift+" : "") + key + " pressed";
                    case ScrollEvent(int delta) -> "Scroll " + (delta > 0 ? "down" : "up");
                };

        System.out.println("  Event: " + response);

        // 2. JSON-like value processing
        JsonValue json = new JsonString("hello");

        Object javaValue =
                switch (json) {
                    case JsonString(String s) -> s;
                    case JsonNumber(double n) -> n;
                    case JsonBool(boolean b) -> b;
                    case JsonNull() -> null;
                };

        System.out.println("  JSON value: " + javaValue);

        // 3. API response handling
        Object apiResponse = new Success("Data loaded");

        String message =
                switch (apiResponse) {
                    case Success(String data) -> "OK: " + data;
                    case Error(int code, String msg) -> "Error " + code + ": " + msg;
                    case Loading() -> "Please wait...";
                    default -> "Unknown response";
                };

        System.out.println("  API response: " + message);

        // 4. Expression evaluation (using class-level Expr types)
        // 2 + 3 * 4
        Expr expression = new Add(new Num(2), new Mul(new Num(3), new Num(4)));
        int result = evaluate(expression);
        System.out.println("  Expression 2 + 3 * 4 = " + result);

        System.out.println();
    }

    // Helper records for API response example
    record Success(String data) {}

    record Error(int code, String message) {}

    record Loading() {}

    // Recursive evaluation using pattern matching
    static int evaluate(Object expr) {
        return switch (expr) {
            case SwitchPatternExample.Num(int v) -> v;
            case SwitchPatternExample.Add(var l, var r) -> evaluate(l) + evaluate(r);
            case SwitchPatternExample.Mul(var l, var r) -> evaluate(l) * evaluate(r);
            default -> throw new IllegalArgumentException("Unknown expression");
        };
    }

    // Expression types for evaluate
    sealed interface Expr permits Num, Add, Mul {}

    record Num(int value) implements Expr {}

    record Add(Expr left, Expr right) implements Expr {}

    record Mul(Expr left, Expr right) implements Expr {}
}
