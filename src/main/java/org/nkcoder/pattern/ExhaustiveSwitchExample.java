package org.nkcoder.pattern;

import java.util.List;

/**
 * Exhaustive Switch with Sealed Types (Java 17+/21+): Compiler-enforced completeness.
 *
 * <p><strong>Java 25 Status:</strong> Finalized and production-ready. Combining sealed types
 * with pattern matching enables the compiler to verify all cases are handled.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Sealed types restrict which classes can implement/extend</li>
 *   <li>Compiler knows all possible subtypes</li>
 *   <li>Switch expressions must be exhaustive</li>
 *   <li>No default needed when all cases covered</li>
 * </ul>
 */
public class ExhaustiveSwitchExample {

  static void main(String[] args) {
    enumExhaustiveness();
    sealedClassExhaustiveness();
    sealedInterfaceExhaustiveness();
    nestedSealedTypes();
    algebraicDataTypes();
    benefitsOfExhaustiveness();
  }

  // ===== Enum Exhaustiveness =====

  enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

  static void enumExhaustiveness() {
    System.out.println("=== Enum Exhaustiveness ===");

    Day day = Day.WED;

    // All enum values covered - no default needed
    String type = switch (day) {
      case MON, TUE, WED, THU, FRI -> "Weekday";
      case SAT, SUN -> "Weekend";
    };  // Compiler verifies all values handled

    System.out.println("  " + day + " is a " + type);

    // If you add a new enum value, compiler will error on incomplete switches
    System.out.println("""

        If you add 'HOLIDAY' to Day enum:
        - All switch expressions using Day will fail to compile
        - Forces you to handle the new case
        - No runtime surprises!
        """);
  }

  // ===== Sealed Class Exhaustiveness =====

  static sealed abstract class Shape permits Circle, Rectangle, Triangle {}

  static final class Circle extends Shape {
    private final double radius;
    Circle(double radius) { this.radius = radius; }
    double radius() { return radius; }
  }

  static final class Rectangle extends Shape {
    private final double width, height;
    Rectangle(double width, double height) { this.width = width; this.height = height; }
    double width() { return width; }
    double height() { return height; }
  }

  static final class Triangle extends Shape {
    private final double base, height;
    Triangle(double base, double height) { this.base = base; this.height = height; }
    double base() { return base; }
    double height() { return height; }
  }

  static void sealedClassExhaustiveness() {
    System.out.println("=== Sealed Class Exhaustiveness ===");

    Shape shape = new Circle(5.0);

    // All permitted subclasses covered - no default!
    double area = switch (shape) {
      case Circle c -> Math.PI * c.radius() * c.radius();
      case Rectangle r -> r.width() * r.height();
      case Triangle t -> 0.5 * t.base() * t.height();
    };

    System.out.println("  Shape: " + shape.getClass().getSimpleName());
    System.out.println("  Area: " + String.format("%.2f", area));

    // Test all shapes
    Shape[] shapes = { new Circle(3), new Rectangle(4, 5), new Triangle(6, 4) };
    for (Shape s : shapes) {
      String desc = switch (s) {
        case Circle c -> "Circle r=" + c.radius();
        case Rectangle r -> "Rectangle " + r.width() + "x" + r.height();
        case Triangle t -> "Triangle base=" + t.base() + " h=" + t.height();
      };
      System.out.println("    " + desc);
    }

    System.out.println();
  }

  // ===== Sealed Interface with Records =====

  sealed interface Result<T> permits Success, Failure, Pending {}

  record Success<T>(T value) implements Result<T> {}
  record Failure<T>(String error, Exception cause) implements Result<T> {}
  record Pending<T>() implements Result<T> {}

  static void sealedInterfaceExhaustiveness() {
    System.out.println("=== Sealed Interface with Records ===");

    Result<Integer> result = new Success<>(42);

    // Exhaustive with record patterns
    String message = switch (result) {
      case Success<Integer>(Integer value) -> "Got value: " + value;
      case Failure<Integer>(String error, Exception cause) -> "Error: " + error;
      case Pending<Integer>() -> "Still processing...";
    };

    System.out.println("  Result: " + message);

    // Test all result types
    Result<String>[] results = new Result[] {
        new Success<>("Hello"),
        new Failure<>("Network error", new RuntimeException("Connection refused")),
        new Pending<>()
    };

    for (Result<String> r : results) {
      String status = switch (r) {
        case Success(var v) -> "SUCCESS: " + v;
        case Failure(var err, var cause) -> "FAILED: " + err;
        case Pending() -> "PENDING";
      };
      System.out.println("    " + status);
    }

    System.out.println();
  }

  // ===== Nested Sealed Types =====

  sealed interface Expr permits Literal, BinaryOp, UnaryOp {}

  record Literal(double value) implements Expr {}

  sealed interface BinaryOp extends Expr permits Add, Sub, Mul, Div {}
  record Add(Expr left, Expr right) implements BinaryOp {}
  record Sub(Expr left, Expr right) implements BinaryOp {}
  record Mul(Expr left, Expr right) implements BinaryOp {}
  record Div(Expr left, Expr right) implements BinaryOp {}

  sealed interface UnaryOp extends Expr permits Neg, Abs {}
  record Neg(Expr operand) implements UnaryOp {}
  record Abs(Expr operand) implements UnaryOp {}

  static void nestedSealedTypes() {
    System.out.println("=== Nested Sealed Types ===");

    // Expression: abs(-(5 + 3) * 2)
    Expr expr = new Abs(new Mul(new Neg(new Add(new Literal(5), new Literal(3))), new Literal(2)));

    double result = evaluate(expr);
    System.out.println("  Expression: abs(-(5 + 3) * 2)");
    System.out.println("  Result: " + result);

    // Simpler expression: (10 - 4) / 2
    Expr simple = new Div(new Sub(new Literal(10), new Literal(4)), new Literal(2));
    System.out.println("  (10 - 4) / 2 = " + evaluate(simple));

    System.out.println();
  }

  static double evaluate(Expr expr) {
    return switch (expr) {
      case Literal(double v) -> v;
      case Add(var l, var r) -> evaluate(l) + evaluate(r);
      case Sub(var l, var r) -> evaluate(l) - evaluate(r);
      case Mul(var l, var r) -> evaluate(l) * evaluate(r);
      case Div(var l, var r) -> evaluate(l) / evaluate(r);
      case Neg(var e) -> -evaluate(e);
      case Abs(var e) -> Math.abs(evaluate(e));
    };  // All Expr subtypes covered!
  }

  // ===== Algebraic Data Types =====

  sealed interface Option<T> permits Some, None {}
  record Some<T>(T value) implements Option<T> {}
  record None<T>() implements Option<T> {}

  sealed interface Either<L, R> permits Left, Right {}
  record Left<L, R>(L value) implements Either<L, R> {}
  record Right<L, R>(R value) implements Either<L, R> {}

  static void algebraicDataTypes() {
    System.out.println("=== Algebraic Data Types ===");

    // Option type (like Optional but pattern-matchable)
    Option<String> someValue = new Some<>("Hello");
    Option<String> noValue = new None<>();

    for (Option<String> opt : List.of(someValue, noValue)) {
      String result = switch (opt) {
        case Some(String s) -> "Got: " + s;
        case None() -> "Nothing";
      };
      System.out.println("  Option: " + result);
    }

    // Either type (error or success)
    Either<String, Integer> error = new Left<>("Not a number");
    Either<String, Integer> success = new Right<>(42);

    for (Either<String, Integer> either : List.of(error, success)) {
      String result = switch (either) {
        case Left(String err) -> "Error: " + err;
        case Right(Integer val) -> "Value: " + val;
      };
      System.out.println("  Either: " + result);
    }

    // Combining ADTs
    System.out.println("\n  Combining ADTs (safe division):");
    for (int[] pair : new int[][]{{10, 2}, {10, 0}, {-6, 3}}) {
      Either<String, Integer> divResult = safeDivide(pair[0], pair[1]);
      String output = switch (divResult) {
        case Left(String err) -> pair[0] + "/" + pair[1] + " = Error: " + err;
        case Right(Integer val) -> pair[0] + "/" + pair[1] + " = " + val;
      };
      System.out.println("    " + output);
    }

    System.out.println();
  }

  static Either<String, Integer> safeDivide(int a, int b) {
    if (b == 0) return new Left<>("Division by zero");
    return new Right<>(a / b);
  }

  static void benefitsOfExhaustiveness() {
    System.out.println("=== Benefits of Exhaustive Switches ===");

    System.out.println("""
        Compile-Time Safety:
        - Compiler verifies ALL cases handled
        - No default = intentional coverage
        - Adding new subtype breaks compilation (good!)

        Refactoring Confidence:
        - Rename a case? Compiler catches all usages
        - Remove a subtype? Compiler finds dead code
        - Add a subtype? Compiler shows where to update

        Self-Documenting Code:
        - Switch clearly shows all possibilities
        - No hidden "else" behavior
        - Intent is explicit in the code

        When to use sealed types:
        - Domain models with fixed variants
        - State machines
        - AST/expression trees
        - Result/Option types
        - Event hierarchies

        Pattern:
        ```
        sealed interface X permits A, B, C {}

        // In switch - compiler enforces completeness:
        switch (x) {
            case A a -> handleA(a);
            case B b -> handleB(b);
            case C c -> handleC(c);
            // No default needed!
        }
        ```
        """);
  }
}
