package org.nkcoder.oop;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Sealed types + Records = Algebraic Data Types (ADTs).
 *
 * <ul>
 *   <li>Sealed interface defines the sum type (OR)
 *   <li>Records define product types (AND) with data
 *   <li>Pattern matching enables exhaustive handling
 *   <li>Common in functional programming (Scala, Haskell, Rust)
 * </ul>
 */
public class SealedWithRecordsExample {

  public static void main(String[] args) {
    expressionExample();
    jsonExample();
    optionExample();
    eitherExample();
    statePatternExample();
  }

  // ============ Expression Tree (Classic ADT) ============
  sealed interface Expr permits Literal, Add, Multiply, Negate {}

  record Literal(int value) implements Expr {}

  record Add(Expr left, Expr right) implements Expr {}

  record Multiply(Expr left, Expr right) implements Expr {}

  record Negate(Expr expr) implements Expr {}

  static int evaluate(Expr expr) {
    return switch (expr) {
      case Literal(int value) -> value;
      case Add(Expr left, Expr right) -> evaluate(left) + evaluate(right);
      case Multiply(Expr left, Expr right) -> evaluate(left) * evaluate(right);
      case Negate(Expr e) -> -evaluate(e);
    };
  }

  static String prettyPrint(Expr expr) {
    return switch (expr) {
      case Literal(int value) -> String.valueOf(value);
      case Add(Expr l, Expr r) -> "(" + prettyPrint(l) + " + " + prettyPrint(r) + ")";
      case Multiply(Expr l, Expr r) -> "(" + prettyPrint(l) + " * " + prettyPrint(r) + ")";
      case Negate(Expr e) -> "-" + prettyPrint(e);
    };
  }

  static void expressionExample() {
    System.out.println("=== Expression Tree ADT ===");

    // (3 + 4) * 2
    Expr expr = new Multiply(new Add(new Literal(3), new Literal(4)), new Literal(2));

    System.out.println("Expression: " + prettyPrint(expr));
    System.out.println("Result: " + evaluate(expr));

    // -5 + 10
    Expr expr2 = new Add(new Negate(new Literal(5)), new Literal(10));
    System.out.println("Expression: " + prettyPrint(expr2));
    System.out.println("Result: " + evaluate(expr2));
  }

  // ============ JSON Value (Sum Type) ============
  sealed interface JsonValue
      permits JsonNull, JsonBool, JsonNumber, JsonString, JsonArray, JsonObject {}

  record JsonNull() implements JsonValue {}

  record JsonBool(boolean value) implements JsonValue {}

  record JsonNumber(double value) implements JsonValue {}

  record JsonString(String value) implements JsonValue {}

  record JsonArray(List<JsonValue> elements) implements JsonValue {
    public JsonArray {
      elements = List.copyOf(elements);
    }
  }

  record JsonObject(Map<String, JsonValue> fields) implements JsonValue {
    public JsonObject {
      fields = Map.copyOf(fields);
    }
  }

  static String toJsonString(JsonValue value) {
    return switch (value) {
      case JsonNull() -> "null";
      case JsonBool(boolean b) -> String.valueOf(b);
      case JsonNumber(double n) -> String.valueOf(n);
      case JsonString(String s) -> "\"" + s + "\"";
      case JsonArray(List<JsonValue> elements) ->
          "["
              + elements.stream()
                  .map(SealedWithRecordsExample::toJsonString)
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("")
              + "]";
      case JsonObject(Map<String, JsonValue> fields) ->
          "{"
              + fields.entrySet().stream()
                  .map(e -> "\"" + e.getKey() + "\": " + toJsonString(e.getValue()))
                  .reduce((a, b) -> a + ", " + b)
                  .orElse("")
              + "}";
    };
  }

  static void jsonExample() {
    System.out.println("=== JSON ADT ===");

    JsonValue json =
        new JsonObject(
            Map.of(
                "name", new JsonString("Alice"),
                "age", new JsonNumber(30),
                "active", new JsonBool(true),
                "tags", new JsonArray(List.of(new JsonString("java"), new JsonString("kotlin")))));

    System.out.println("JSON: " + toJsonString(json));

    System.out.println();
  }

  // ============ Option Type (Maybe) ============

  sealed interface Option<T> permits Some, None {
    default boolean isSome() {
      return this instanceof Some;
    }

    default boolean isNone() {
      return this instanceof None;
    }
  }

  record Some<T>(T value) implements Option<T> {}

  record None<T>() implements Option<T> {}

  static <T> T getOrElse(Option<T> opt, T defaultValue) {
    return switch (opt) {
      case Some<T>(T value) -> value;
      case None<T>() -> defaultValue;
    };
  }

  static <T, U> Option<U> map(Option<T> opt, Function<T, U> f) {
    return switch (opt) {
      case Some<T>(T value) -> new Some<>(f.apply(value));
      case None<T>() -> new None<>();
    };
  }

  static void optionExample() {
    System.out.println("=== Option Type ===");

    Option<String> some = new Some<>("Hello");
    Option<String> none = new None<>();

    System.out.println("some value: " + getOrElse(some, "default"));
    System.out.println("none value: " + getOrElse(none, "default"));

    System.out.println("some is Some?: " + some.isSome());
    System.out.println("none is None?: " + none.isNone());

    Option<Integer> length = map(some, String::length);
    System.out.println("mapped length: " + getOrElse(length, 0));

    // Pattern matching
    String result =
        switch (some) {
          case Some<String>(String s) -> "Got: " + s;
          case None<String>() -> "Nothing";
        };
    System.out.println("Pattern match: " + result);

    System.out.println();
  }

  // ============ Either Type (Result) ============
  sealed interface Either<L, R> permits Left, Right {
    default boolean isLeft() {
      return this instanceof Left;
    }

    default boolean isRight() {
      return this instanceof Right;
    }
  }

  record Left<L, R>(L value) implements Either<L, R> {}

  record Right<L, R>(R value) implements Either<L, R> {}

  static Either<String, Integer> divide(int a, int b) {
    if (b == 0) {
      return new Left<>("Division by zero");
    }
    return new Right<>(a / b);
  }

  static void eitherExample() {
    System.out.println("=== Either Type ===");

    Either<String, Integer> success = divide(10, 2);
    Either<String, Integer> failure = divide(10, 0);

    System.out.println("10/2 = " + formatResult(success));
    System.out.println("10/0 = " + formatResult(failure));

    System.out.println("Success is right? " + success.isRight());
    System.out.println("Failure is left? " + failure.isLeft());

    // Chaining operations
    var result = divide(100, 5);
    String message =
        switch (result) {
          case Right<String, Integer>(Integer value) -> "Result: " + value;
          case Left<String, Integer>(String error) -> "Error: " + error;
        };
    System.out.println(message);

    System.out.println();
  }

  static String formatResult(Either<String, Integer> result) {
    return switch (result) {
      case Right<String, Integer>(Integer value) -> String.valueOf(value);
      case Left<String, Integer>(String error) -> "ERROR: " + error;
    };
  }

  // ============ State Pattern with ADT ============
  sealed interface OrderState permits Pending, Confirmed, Shipped, Delivered, Cancelled {}

  record Pending(String orderId) implements OrderState {}

  record Confirmed(String orderId, Instant confirmedAt) implements OrderState {}

  record Shipped(String orderId, String trackingNumber) implements OrderState {}

  record Delivered(String orderId, Instant deliveredAt) implements OrderState {}

  record Cancelled(String orderId, String reason) implements OrderState {}

  static String describeState(OrderState state) {
    return switch (state) {
      case Pending(String id) -> "Order " + id + " is pending confirmation";
      case Confirmed(String id, Instant at) -> "Order " + id + " confirmed at " + at;
      case Shipped(String id, String tracking) -> "Order " + id + " shipped, tracking: " + tracking;
      case Delivered(String id, Instant at) -> "Order " + id + " delivered at " + at;
      case Cancelled(String id, String reason) -> "Order " + id + " cancelled: " + reason;
    };
  }

  static OrderState transition(OrderState current, String action) {
    return switch (current) {
      case Pending(String id) ->
          switch (action) {
            case "confirm" -> new Confirmed(id, Instant.now());
            case "cancel" -> new Cancelled(id, "Customer request");
            default -> current;
          };
      case Confirmed(String id, Instant _)  ->
          switch (action) {
            case "ship" -> new Shipped(id, "TRACK-" + id);
            case "cancel" -> new Cancelled(id, "Cancelled after confirmation");
            default -> current;
          };
      case Shipped(String id, String _) ->
          switch (action) {
            case "deliver" -> new Delivered(id, java.time.Instant.now());
            default -> current;
          };
      case Delivered _, Cancelled _ -> current; // Terminal states
    };
  }

  static void statePatternExample() {
    System.out.println("=== State Pattern with ADT ===");

    OrderState order = new Pending("ORD-123");
    System.out.println(describeState(order));

    order = transition(order, "confirm");
    System.out.println(describeState(order));

    order = transition(order, "ship");
    System.out.println(describeState(order));

    order = transition(order, "deliver");
    System.out.println(describeState(order));

    System.out.println(
        """

          Benefits of ADT pattern:
          - Type-safe state transitions
          - Each state carries its own data
          - Exhaustive pattern matching
          - Immutable state objects
          - Clear domain modeling
          """);
  }
}
