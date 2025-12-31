package org.nkcoder.pattern;

import java.time.LocalDate;
import java.util.List;

/**
 * Guarded Patterns (Java 21+): Add conditions to pattern cases with 'when' clause.
 *
 * <p><strong>Java 25 Status:</strong> Finalized in Java 21 and production-ready. The 'when' keyword allows adding
 * boolean conditions to pattern cases for more precise matching.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>when clause adds conditions to pattern cases
 *   <li>Pattern variable is available in the when condition
 *   <li>Guarded patterns must come before unguarded ones
 *   <li>Enables complex matching logic within switch
 * </ul>
 */
public class GuardedPatternExample {

    static void main(String[] args) {
        basicWhenClause();
        multipleGuards();
        guardOrdering();
        withRecordPatterns();
        realWorldExamples();
    }

    static void basicWhenClause() {
        System.out.println("=== Basic when Clause ===");

        Object obj = "Hello, World!";

        String result =
                switch (obj) {
                    case String s when s.isEmpty() -> "Empty string";
                    case String s when s.length() < 5 -> "Short string: " + s;
                    case String s when s.length() > 20 -> "Long string (truncated): " + s.substring(0, 20) + "...";
                    case String s -> "Regular string: " + s; // Unguarded - catches remaining
                    case null -> "null";
                    default -> "Not a string";
                };

        System.out.println("  Result: " + result);

        // Test with different inputs
        for (Object input : List.of("", "Hi", "Hello, World!", "A".repeat(50), 42)) {
            String desc =
                    switch (input) {
                        case String s when s.isEmpty() -> "empty";
                        case String s when s.length() < 5 -> "short";
                        case String s when s.length() > 20 -> "long";
                        case String s -> "medium";
                        case Integer i -> "integer";
                        case null -> "null";
                        default -> "other";
                    };
            System.out.println("    "
                    + (input == null
                            ? "null"
                            : input.toString()
                                    .substring(0, Math.min(10, input.toString().length()))) + " -> " + desc);
        }

        System.out.println();
    }

    static void multipleGuards() {
        System.out.println("=== Multiple Guards on Same Type ===");

        record Person(String name, int age) {}

        Person[] people = {
            new Person("Child", 8),
            new Person("Teen", 15),
            new Person("Adult", 30),
            new Person("Senior", 70),
            new Person("Invalid", -5)
        };

        for (Person p : people) {
            String category =
                    switch (p) {
                        case Person(String _, int age) when age < 0 -> "Invalid age";
                        case Person(String name, int age) when age < 13 -> name + " is a child";
                        case Person(String name, int age) when age < 20 -> name + " is a teenager";
                        case Person(String name, int age) when age < 65 -> name + " is an adult";
                        case Person(String name, int _) -> name + " is a senior"; // age >= 65
                    };
            System.out.println("  " + category);
        }

        System.out.println();
    }

    static void guardOrdering() {
        System.out.println("=== Guard Ordering ===");

        // Guarded patterns MUST come before unguarded patterns of the same type
        // Otherwise compiler error: "this case label is dominated by a preceding case label"

        Integer number = 15; // Use Integer for pattern matching

        String desc =
                switch (number) {
                    // More specific (guarded) first
                    case Integer n when n < 0 -> "Negative";
                    case Integer n when n == 0 -> "Zero";
                    case Integer n when n % 2 == 0 -> "Positive even";
                    case Integer _ -> "Positive odd"; // Unguarded catch-all
                };

        System.out.println("  " + number + " is: " + desc);

        // Testing various numbers
        for (Integer n : List.of(-5, 0, 2, 7, 100)) {
            String result =
                    switch (n) {
                        case Integer i when i < 0 -> "negative";
                        case Integer i when i == 0 -> "zero";
                        case Integer i when i % 2 == 0 -> "positive even";
                        case Integer i -> "positive odd"; // Catch-all for Integer
                    };
            System.out.println("    " + n + " -> " + result);
        }

        System.out.println("""

        Ordering rules:
        - Guarded patterns before unguarded (same type)
        - More specific guards before less specific
        - Compiler enforces no dominated/unreachable cases
        """);
    }

    static void withRecordPatterns() {
        System.out.println("=== Guards with Record Patterns ===");

        record Point(int x, int y) {}
        record Circle(Point center, int radius) {}

        Circle[] circles = {
            new Circle(new Point(0, 0), 5), // At origin, small
            new Circle(new Point(0, 0), 100), // At origin, large
            new Circle(new Point(10, 20), 5), // Off origin, small
            new Circle(new Point(100, 100), 50) // Far from origin, medium
        };

        for (Circle c : circles) {
            String desc =
                    switch (c) {
                        case Circle(Point(int x, int y), int r)
                        when x == 0 && y == 0 && r < 10 -> "Small circle at origin";

                        case Circle(Point(int x, int y), int r)
                        when x == 0 && y == 0 -> "Circle at origin with radius " + r;

                        case Circle(Point(int x, int y), int r)
                        when Math.sqrt(x * x + y * y) > 50 -> "Circle far from origin at (" + x + "," + y + ")";

                        case Circle(Point(int x, int y), int r) -> "Circle at (" + x + "," + y + ") with radius " + r;
                    };
            System.out.println("  " + desc);
        }

        System.out.println();
    }

    static void realWorldExamples() {
        System.out.println("=== Real-World Examples ===");

        // 1. HTTP Response handling
        record HttpResponse(int status, String body) {}

        HttpResponse[] responses = {
            new HttpResponse(200, "{\"data\": \"success\"}"),
            new HttpResponse(201, "{\"id\": 123}"),
            new HttpResponse(400, "{\"error\": \"bad request\"}"),
            new HttpResponse(404, ""),
            new HttpResponse(500, "Internal Server Error")
        };

        System.out.println("  HTTP Response handling:");
        for (HttpResponse r : responses) {
            String action =
                    switch (r) {
                        case HttpResponse(int s, String b)
                        when s >= 200 && s < 300 && !b.isEmpty() -> "Success with body";
                        case HttpResponse(int s, String b) when s >= 200 && s < 300 -> "Success (no body)";
                        case HttpResponse(int s, String b) when s == 400 -> "Bad request: " + b;
                        case HttpResponse(int s, String b) when s == 404 -> "Not found";
                        case HttpResponse(int s, String b) when s >= 500 -> "Server error: " + b;
                        case HttpResponse(int s, String b) -> "Unhandled status: " + s;
                    };
            System.out.println("    " + r.status() + " -> " + action);
        }

        // 2. Date validation and categorization
        record Event(String name, LocalDate date) {}

        LocalDate today = LocalDate.now();
        Event[] events = {
            new Event("Past Event", today.minusDays(10)),
            new Event("Today's Event", today),
            new Event("Tomorrow", today.plusDays(1)),
            new Event("Next Week", today.plusDays(7)),
            new Event("Next Month", today.plusMonths(1))
        };

        System.out.println("\n  Event scheduling:");
        for (Event e : events) {
            String timing =
                    switch (e) {
                        case Event(String name, LocalDate d) when d.isBefore(today) -> name + " - Already passed";
                        case Event(String name, LocalDate d) when d.isEqual(today) -> name + " - Happening today!";
                        case Event(String name, LocalDate d)
                        when d.isBefore(today.plusDays(7)) -> name + " - This week";
                        case Event(String name, LocalDate d)
                        when d.isBefore(today.plusMonths(1)) -> name + " - This month";
                        case Event(String name, LocalDate d) -> name + " - Later (" + d + ")";
                    };
            System.out.println("    " + timing);
        }

        // 3. Input validation
        System.out.println("\n  Input validation:");

        record UserInput(String field, String value) {}

        UserInput[] inputs = {
            new UserInput("email", ""),
            new UserInput("email", "test@example.com"),
            new UserInput("email", "invalid-email"),
            new UserInput("age", "25"),
            new UserInput("age", "-5"),
            new UserInput("age", "abc")
        };

        for (UserInput input : inputs) {
            String validation =
                    switch (input) {
                        case UserInput(String f, String v) when f.equals("email") && v.isEmpty() -> "Email is required";
                        case UserInput(String f, String v)
                        when f.equals("email") && v.contains("@") -> "Email OK: " + v;
                        case UserInput(String f, String v) when f.equals("email") -> "Invalid email format";

                        case UserInput(String f, String v)
                        when f.equals("age") && v.matches("\\d+") -> {
                            int age = Integer.parseInt(v);
                            yield age >= 0 && age < 150 ? "Age OK: " + age : "Invalid age value";
                        }
                        case UserInput(String f, String v) when f.equals("age") -> "Age must be a number";

                        case UserInput(String f, String v) -> "Unknown field: " + f;
                    };
            System.out.println("    " + input.field() + "=" + input.value() + " -> " + validation);
        }

        System.out.println();
    }
}
