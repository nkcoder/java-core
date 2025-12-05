package org.nkcoder.fp;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

/**
 * Currying and Partial Application in Java.
 *
 * <ul>
 *   <li>Currying: Transform multi-arg function into chain of single-arg functions</li>
 *   <li>Partial Application: Fix some arguments, return new function</li>
 *   <li>Function Factories: Create specialized functions from general ones</li>
 *   <li>Enables better code reuse and composition</li>
 * </ul>
 */
public class CurryingExample {

  static void main(String[] args) {
    whatIsCurrying();
    partialApplication();
    functionFactories();
    realWorldExamples();
  }

  // ============ What is Currying ============

  static void whatIsCurrying() {
    System.out.println("=== What is Currying ===");

    // Traditional: function with 2 arguments
    BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
    System.out.println("add(3, 5) = " + add.apply(3, 5));

    // Curried: chain of single-argument functions
    // f(a, b) becomes f(a)(b)
    Function<Integer, Function<Integer, Integer>> curriedAdd = a -> b -> a + b;
    System.out.println("curriedAdd(3)(5) = " + curriedAdd.apply(3).apply(5));

    // Step by step
    Function<Integer, Integer> add3 = curriedAdd.apply(3);  // Partially apply first arg
    System.out.println("add3(5) = " + add3.apply(5));
    System.out.println("add3(10) = " + add3.apply(10));

    // Curried function with 3 arguments
    Function<Integer, Function<Integer, Function<Integer, Integer>>> curriedSum3 =
        a -> b -> c -> a + b + c;
    System.out.println("curriedSum3(1)(2)(3) = " + curriedSum3.apply(1).apply(2).apply(3));

    // Generic curry helper
    System.out.println("\nUsing curry helper:");
    BiFunction<String, String, String> concat = (a, b) -> a + b;
    var curriedConcat = curry(concat);
    System.out.println("curriedConcat(\"Hello \")(\"World\") = " +
        curriedConcat.apply("Hello ").apply("World"));

    System.out.println("""

        Currying transforms f(a, b) into f(a)(b)
        - Each function takes ONE argument
        - Returns a function for the next argument
        - Named after Haskell Curry (mathematician)
        """);
  }

  // Helper: Convert BiFunction to curried form
  static <A, B, R> Function<A, Function<B, R>> curry(BiFunction<A, B, R> f) {
    return a -> b -> f.apply(a, b);
  }

  // Helper: Convert curried function back to BiFunction
  static <A, B, R> BiFunction<A, B, R> uncurry(Function<A, Function<B, R>> f) {
    return (a, b) -> f.apply(a).apply(b);
  }

  // ============ Partial Application ============

  static void partialApplication() {
    System.out.println("=== Partial Application ===");

    // Original function
    TriFunction<String, String, String, String> formatMessage =
        (level, timestamp, message) -> "[" + level + "] " + timestamp + ": " + message;

    String fullMessage = formatMessage.apply("INFO", "2024-01-15", "Server started");
    System.out.println("Full: " + fullMessage);

    // Partial application: fix the level
    BiFunction<String, String, String> infoLogger = partial1(formatMessage, "INFO");
    BiFunction<String, String, String> errorLogger = partial1(formatMessage, "ERROR");

    System.out.println("Info: " + infoLogger.apply("2024-01-15", "User logged in"));
    System.out.println("Error: " + errorLogger.apply("2024-01-15", "Connection failed"));

    // Partial application: fix level and timestamp
    Function<String, String> todayInfoLogger = partial2(formatMessage, "INFO", "2024-01-15");
    System.out.println("Today Info: " + todayInfoLogger.apply("Processing complete"));

    // Using currying for partial application
    Function<String, Function<String, Function<String, String>>> curriedFormat =
        level -> timestamp -> message -> "[" + level + "] " + timestamp + ": " + message;

    var debugLogger = curriedFormat.apply("DEBUG");
    var debugTodayLogger = debugLogger.apply("2024-01-15");
    System.out.println("Debug Today: " + debugTodayLogger.apply("Cache hit"));

    System.out.println("""

        Partial Application fixes some arguments:
        - f(a, b, c) with a fixed becomes g(b, c)
        - Creates specialized versions of general functions
        - Different from currying (which chains single-arg functions)
        """);
  }

  // Custom TriFunction interface
  @FunctionalInterface
  interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
  }

  // Partial application helpers
  static <A, B, C, R> BiFunction<B, C, R> partial1(TriFunction<A, B, C, R> f, A a) {
    return (b, c) -> f.apply(a, b, c);
  }

  static <A, B, C, R> Function<C, R> partial2(TriFunction<A, B, C, R> f, A a, B b) {
    return c -> f.apply(a, b, c);
  }

  // ============ Function Factories ============

  static void functionFactories() {
    System.out.println("=== Function Factories ===");

    // Multiplier factory
    IntFunction<UnaryOperator<Integer>> multiplier = factor -> x -> x * factor;
    var double_ = multiplier.apply(2);
    var triple = multiplier.apply(3);

    System.out.println("double(5) = " + double_.apply(5));
    System.out.println("triple(5) = " + triple.apply(5));

    // Adder factory
    Function<Integer, UnaryOperator<Integer>> adder = n -> x -> x + n;
    var add10 = adder.apply(10);
    var add100 = adder.apply(100);

    System.out.println("add10(5) = " + add10.apply(5));
    System.out.println("add100(5) = " + add100.apply(5));

    // Comparator factory
    Function<Integer, Function<Integer, Boolean>> greaterThan = threshold -> value -> value > threshold;
    var greaterThan10 = greaterThan.apply(10);
    var greaterThan100 = greaterThan.apply(100);

    System.out.println("greaterThan10(15) = " + greaterThan10.apply(15));
    System.out.println("greaterThan100(15) = " + greaterThan100.apply(15));

    // Prefix/suffix factory
    Function<String, UnaryOperator<String>> prefixer = prefix -> s -> prefix + s;
    Function<String, UnaryOperator<String>> suffixer = suffix -> s -> s + suffix;

    var addHello = prefixer.apply("Hello, ");
    var addBang = suffixer.apply("!");
    var greet = addHello.andThen(addBang);

    System.out.println("greet(\"World\") = " + greet.apply("World"));

    // Compose factories
    var pipeline = double_.andThen(add10).andThen(triple);
    System.out.println("pipeline(5) = (5*2 + 10) * 3 = " + pipeline.apply(5));

    System.out.println("""

        Function Factories create specialized functions:
        - Take configuration, return function
        - Enable code reuse and DRY principle
        - Compose well with andThen/compose
        """);
  }

  // ============ Real World Examples ============

  static void realWorldExamples() {
    System.out.println("=== Real World Examples ===");

    // 1. URL Builder
    System.out.println("-- URL Builder --");
    Function<String, Function<String, Function<String, String>>> urlBuilder =
        protocol -> host -> path -> protocol + "://" + host + path;

    var httpsBuilder = urlBuilder.apply("https");
    var apiBuilder = httpsBuilder.apply("api.example.com");

    System.out.println(apiBuilder.apply("/users"));
    System.out.println(apiBuilder.apply("/products"));

    // 2. Validator Factory
    System.out.println("\n-- Validator Factory --");
    Function<Integer, Function<Integer, Function<String, String>>> rangeValidator =
        min -> max -> value -> {
          try {
            int num = Integer.parseInt(value);
            return (num >= min && num <= max) ? "Valid" : "Out of range [" + min + "-" + max + "]";
          } catch (NumberFormatException e) {
            return "Not a number";
          }
        };

    var ageValidator = rangeValidator.apply(0).apply(150);
    var percentValidator = rangeValidator.apply(0).apply(100);

    System.out.println("Age '25': " + ageValidator.apply("25"));
    System.out.println("Age '200': " + ageValidator.apply("200"));
    System.out.println("Percent '50': " + percentValidator.apply("50"));

    // 3. String Formatter Factory
    System.out.println("\n-- String Formatter Factory --");
    Function<String, Function<String, UnaryOperator<String>>> wrapper =
        prefix -> suffix -> s -> prefix + s + suffix;

    var htmlBold = wrapper.apply("<b>").apply("</b>");
    var htmlItalic = wrapper.apply("<i>").apply("</i>");
    var parentheses = wrapper.apply("(").apply(")");

    System.out.println(htmlBold.apply("important"));
    System.out.println(htmlItalic.apply("emphasized"));
    System.out.println(parentheses.apply("note"));

    // 4. Discount Calculator
    System.out.println("\n-- Discount Calculator --");
    Function<Double, UnaryOperator<Double>> discountBy =
        percent -> price -> price * (1 - percent / 100);

    var tenPercentOff = discountBy.apply(10.0);
    var twentyPercentOff = discountBy.apply(20.0);
    var halfPrice = discountBy.apply(50.0);

    double originalPrice = 100.0;
    System.out.printf("Original: $%.2f%n", originalPrice);
    System.out.printf("10%% off: $%.2f%n", tenPercentOff.apply(originalPrice));
    System.out.printf("20%% off: $%.2f%n", twentyPercentOff.apply(originalPrice));
    System.out.printf("50%% off: $%.2f%n", halfPrice.apply(originalPrice));

    System.out.println("""

        Real-world uses of currying/partial application:
        - Configuration builders (URLs, queries)
        - Validation with configurable rules
        - Text formatting and templating
        - Price calculators with configurable discounts
        - Logger factories with preset levels
        """);
  }
}
