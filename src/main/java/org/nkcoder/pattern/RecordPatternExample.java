package org.nkcoder.pattern;

import java.util.List;
import java.util.Map;

/**
 * Record Patterns (Java 21+): Deconstruct records directly in patterns.
 *
 * <p><strong>Java 25 Status:</strong> Finalized in Java 21 and production-ready. Enables powerful destructuring of
 * record components in instanceof and switch.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Deconstruct record components in one step
 *   <li>Nested patterns for deep matching
 *   <li>Works with instanceof and switch
 *   <li>var can infer component types
 * </ul>
 */
public class RecordPatternExample {

    static void main(String[] args) {
        basicDeconstruction();
        nestedPatterns();
        varInPatterns();
        withInstanceof();
        withSwitch();
        realWorldExamples();
    }

    // Sample records
    record Point(int x, int y) {}

    record Line(Point start, Point end) {}

    record Circle(Point center, int radius) {}

    record Rectangle(Point topLeft, int width, int height) {}

    static void basicDeconstruction() {
        System.out.println("=== Basic Record Deconstruction ===");

        Point point = new Point(10, 20);

        // Old way: match then access components
        if (point instanceof Point p) {
            System.out.println("  Old way - x: " + p.x() + ", y: " + p.y());
        }

        // New way: deconstruct directly in pattern
        if (point instanceof Point(int x, int y)) {
            System.out.println("  New way - x: " + x + ", y: " + y);
        }

        // In switch expression
        Object obj = new Point(5, 15);
        String desc =
                switch (obj) {
                    case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
                    default -> "Not a point";
                };
        System.out.println("  Switch: " + desc);

        System.out.println();
    }

    static void nestedPatterns() {
        System.out.println("=== Nested Patterns ===");

        Line line = new Line(new Point(0, 0), new Point(10, 10));

        // Nested deconstruction - extract inner record components
        if (line instanceof Line(Point(int x1, int y1), Point(int x2, int y2))) {
            System.out.println("  Line from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");

            double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            System.out.println("  Length: " + String.format("%.2f", length));
        }

        // Deep nesting in switch
        record Triangle(Point a, Point b, Point c) {}
        Object shape = new Triangle(new Point(0, 0), new Point(4, 0), new Point(2, 3));

        String result =
                switch (shape) {
                    case Triangle(Point(int ax, int ay), Point(int bx, int by), Point(int cx, int cy)) -> {
                        // Can use all extracted values
                        yield "Triangle with vertices (" + ax + "," + ay + "), " + "(" + bx + "," + by + "), (" + cx
                                + "," + cy + ")";
                    }
                    default -> "Unknown shape";
                };

        System.out.println("  " + result);

        System.out.println();
    }

    static void varInPatterns() {
        System.out.println("=== var in Record Patterns ===");

        Point point = new Point(100, 200);

        // var infers the component type
        if (point instanceof Point(var x, var y)) {
            System.out.println("  Using var: x=" + x + ", y=" + y);
            System.out.println("  x type: " + ((Object) x).getClass().getSimpleName());
        }

        // Mix explicit types and var
        Line line = new Line(new Point(1, 2), new Point(3, 4));

        if (line instanceof Line(var start, Point(int endX, int endY))) {
            System.out.println("  Mixed: start=" + start + ", endX=" + endX + ", endY=" + endY);
        }

        // var useful for complex nested types
        record Wrapper(Line content) {}
        Wrapper w = new Wrapper(new Line(new Point(0, 0), new Point(5, 5)));

        if (w instanceof Wrapper(var content)) {
            System.out.println("  Wrapper content: " + content);
        }

        System.out.println();
    }

    static void withInstanceof() {
        System.out.println("=== Record Patterns with instanceof ===");

        Object[] shapes = {
            new Point(5, 10), new Circle(new Point(0, 0), 5), new Rectangle(new Point(0, 0), 10, 20), "not a shape"
        };

        for (Object shape : shapes) {
            if (shape instanceof Point(int x, int y)) {
                System.out.println("  Point: (" + x + ", " + y + ")");
            } else if (shape instanceof Circle(Point(int cx, int cy), int r)) {
                double area = Math.PI * r * r;
                System.out.println("  Circle: center=(" + cx + "," + cy + "), radius=" + r + ", area="
                        + String.format("%.2f", area));
            } else if (shape instanceof Rectangle(Point(int x, int y), int w, int h)) {
                System.out.println("  Rectangle: origin=(" + x + "," + y + "), " + w + "x" + h + ", area=" + (w * h));
            } else {
                System.out.println("  Unknown: " + shape);
            }
        }

        System.out.println();
    }

    // Sealed types for withSwitch() - must be at class level
    sealed interface Shape permits PointShape, CircleShape, RectangleShape {}

    record PointShape(int x, int y) implements Shape {}

    record CircleShape(int cx, int cy, int radius) implements Shape {}

    record RectangleShape(int x, int y, int width, int height) implements Shape {}

    static void withSwitch() {
        System.out.println("=== Record Patterns with Switch ===");

        Shape[] shapes = {new PointShape(5, 10), new CircleShape(0, 0, 5), new RectangleShape(10, 20, 30, 40)};

        for (Shape shape : shapes) {
            double area =
                    switch (shape) {
                        case PointShape(int x, int y) -> 0; // Point has no area
                        case CircleShape(int cx, int cy, int r) -> Math.PI * r * r;
                        case RectangleShape(int x, int y, int w, int h) -> w * h;
                    }; // Exhaustive - no default needed!

            System.out.println("  " + shape.getClass().getSimpleName() + " area: " + String.format("%.2f", area));
        }

        System.out.println();
    }

    // Sealed types for realWorldExamples() - must be at class level
    sealed interface Json permits JsonObject, JsonArray, JsonPrimitive {}

    record JsonObject(Map<String, Json> fields) implements Json {}

    record JsonArray(List<Json> elements) implements Json {}

    record JsonPrimitive(Object value) implements Json {}

    record HttpRequest(String method, String path, Map<String, String> params) {}

    sealed interface Result<T> permits Ok, Err {}

    record Ok<T>(T value) implements Result<T> {}

    record Err<T>(String message) implements Result<T> {}

    static void realWorldExamples() {
        System.out.println("=== Real-World Examples ===");

        // 1. JSON-like structure navigation
        Json json = new JsonObject(Map.of(
                "name", new JsonPrimitive("Alice"),
                "age", new JsonPrimitive(30)));

        String result =
                switch (json) {
                    case JsonObject(var fields) -> "Object with " + fields.size() + " fields";
                    case JsonArray(var elements) -> "Array with " + elements.size() + " elements";
                    case JsonPrimitive(var value) -> "Primitive: " + value;
                };
        System.out.println("  JSON: " + result);

        // 2. Expression tree evaluation (using class-level Expr types)
        // (x + 5) * 2
        Expr expr = new BinOp("*", new BinOp("+", new Var("x"), new Const(5)), new Const(2));

        Map<String, Double> env = Map.of("x", 10.0);
        double value = eval(expr, env);
        System.out.println("  Expression (x + 5) * 2 where x=10: " + value);

        // 3. HTTP request routing
        HttpRequest req = new HttpRequest("GET", "/users/123", Map.of());

        String response =
                switch (req) {
                    case HttpRequest(String m, String p, var params)
                    when m.equals("GET") && p.startsWith("/users/") -> "Fetching user: " + p.substring(7);
                    case HttpRequest(String m, String p, var params)
                    when m.equals("POST") && p.equals("/users") -> "Creating user";
                    case HttpRequest(var m, var p, var params) -> "Unknown route: " + m + " " + p;
                };
        System.out.println("  HTTP routing: " + response);

        // 4. Result/Either pattern
        Result<Integer> success = new Ok<>(42);
        Result<Integer> failure = new Err<>("Division by zero");

        for (Result<Integer> r : List.of(success, failure)) {
            String msg =
                    switch (r) {
                        case Ok(Integer v) -> "Success: " + v;
                        case Err(String message) -> "Error: " + message;
                    };
            System.out.println("  Result: " + msg);
        }

        System.out.println();
    }

    // Helper for expression evaluation
    static double eval(Object expr, Map<String, Double> env) {
        return switch (expr) {
            case Const(double v) -> v;
            case Var(String name) -> env.getOrDefault(name, 0.0);
            case BinOp(String op, var left, var right) -> {
                double l = eval(left, env);
                double r = eval(right, env);
                yield switch (op) {
                    case "+" -> l + r;
                    case "-" -> l - r;
                    case "*" -> l * r;
                    case "/" -> l / r;
                    default -> throw new IllegalArgumentException("Unknown op: " + op);
                };
            }
            default -> throw new IllegalArgumentException("Unknown expression");
        };
    }

    // Expression types for eval helper
    sealed interface Expr permits Const, Var, BinOp {}

    record Const(double value) implements Expr {}

    record Var(String name) implements Expr {}

    record BinOp(String op, Expr left, Expr right) implements Expr {}
}
