package org.nkcoder.fp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Monad-like Patterns in Java.
 *
 * <ul>
 *   <li>Try monad: Wrap exceptions in functional style</li>
 *   <li>Validation: Accumulate multiple errors (vs fail-fast)</li>
 *   <li>Railway-oriented programming: Chain operations that may fail</li>
 *   <li>Real-world pipeline: Parse → Validate → Transform → Save</li>
 * </ul>
 *
 * <p>Note: Java's Optional is a simple monad. These patterns extend the concept.
 */
public class MonadPatternsExample {

  static void main(String[] args) {
    tryMonad();
    validation();
    railwayOriented();
    realWorldPipeline();
  }

  // ============ Try Monad ============

  // Try<T>: Represents either a Success(value) or Failure(exception)
  sealed interface Try<T> permits Success, Failure {

    // Factory methods
    static <T> Try<T> of(ThrowingSupplier<T> supplier) {
      try {
        return new Success<>(supplier.get());
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }

    static <T> Try<T> success(T value) {
      return new Success<>(value);
    }

    static <T> Try<T> failure(Exception e) {
      return new Failure<>(e);
    }

    // Core operations
    <U> Try<U> map(Function<T, U> mapper);

    <U> Try<U> flatMap(Function<T, Try<U>> mapper);

    T getOrElse(T defaultValue);

    boolean isSuccess();
  }

  @FunctionalInterface
  interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  record Success<T>(T value) implements Try<T> {
    @Override
    public <U> Try<U> map(Function<T, U> mapper) {
      try {
        return new Success<>(mapper.apply(value));
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }

    @Override
    public <U> Try<U> flatMap(Function<T, Try<U>> mapper) {
      try {
        return mapper.apply(value);
      } catch (Exception e) {
        return new Failure<>(e);
      }
    }

    @Override
    public T getOrElse(T defaultValue) {
      return value;
    }

    @Override
    public boolean isSuccess() {
      return true;
    }
  }

  record Failure<T>(Exception exception) implements Try<T> {
    @Override
    public <U> Try<U> map(Function<T, U> mapper) {
      return new Failure<>(exception);
    }

    @Override
    public <U> Try<U> flatMap(Function<T, Try<U>> mapper) {
      return new Failure<>(exception);
    }

    @Override
    public T getOrElse(T defaultValue) {
      return defaultValue;
    }

    @Override
    public boolean isSuccess() {
      return false;
    }
  }

  static void tryMonad() {
    System.out.println("=== Try Monad ===");

    // Wrap potentially failing operations
    Try<Integer> parsed1 = Try.of(() -> Integer.parseInt("42"));
    Try<Integer> parsed2 = Try.of(() -> Integer.parseInt("not a number"));

    System.out.println("Parse '42': " + parsed1);
    System.out.println("Parse 'not a number': " + parsed2);

    // Chain operations safely
    Try<Integer> result = Try.of(() -> Integer.parseInt("10"))
        .map(n -> n * 2)
        .map(n -> n + 5);
    System.out.println("10 * 2 + 5 = " + result);

    // Error propagates through chain
    Try<Integer> errorResult = Try.of(() -> Integer.parseInt("bad"))
        .map(n -> n * 2)  // Skipped
        .map(n -> n + 5); // Skipped
    System.out.println("Error result: " + errorResult);

    // Get with default
    System.out.println("Success getOrElse: " + parsed1.getOrElse(-1));
    System.out.println("Failure getOrElse: " + parsed2.getOrElse(-1));

    // flatMap for nested Try
    Try<Integer> division = Try.of(() -> Integer.parseInt("100"))
        .flatMap(n -> divide(n, 5));
    System.out.println("100 / 5 = " + division);

    Try<Integer> divByZero = Try.of(() -> Integer.parseInt("100"))
        .flatMap(n -> divide(n, 0));
    System.out.println("100 / 0 = " + divByZero);

    System.out.println("""

        Try monad benefits:
        - No try-catch blocks in business logic
        - Errors propagate automatically
        - Composable error handling
        - Explicit about what can fail
        """);
  }

  static Try<Integer> divide(int a, int b) {
    return b == 0
        ? Try.failure(new ArithmeticException("Division by zero"))
        : Try.success(a / b);
  }

  // ============ Validation (Accumulating Errors) ============

  // Validation<E, T>: Either a Valid(value) or Invalid(errors)
  sealed interface Validation<E, T> permits Valid, Invalid {

    static <E, T> Validation<E, T> valid(T value) {
      return new Valid<>(value);
    }

    static <E, T> Validation<E, T> invalid(E error) {
      return new Invalid<>(List.of(error));
    }

    static <E, T> Validation<E, T> invalid(List<E> errors) {
      return new Invalid<>(errors);
    }

    <U> Validation<E, U> map(Function<T, U> mapper);

    // Combine two validations (accumulating errors)
    <U, V> Validation<E, V> combine(Validation<E, U> other, java.util.function.BiFunction<T, U, V> combiner);

    boolean isValid();

    T getValue();

    List<E> getErrors();
  }

  record Valid<E, T>(T value) implements Validation<E, T> {
    @Override
    public <U> Validation<E, U> map(Function<T, U> mapper) {
      return new Valid<>(mapper.apply(value));
    }

    @Override
    public <U, V> Validation<E, V> combine(Validation<E, U> other, java.util.function.BiFunction<T, U, V> combiner) {
      if (other.isValid()) {
        return new Valid<>(combiner.apply(value, other.getValue()));
      }
      return new Invalid<>(other.getErrors());
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public T getValue() {
      return value;
    }

    @Override
    public List<E> getErrors() {
      return List.of();
    }
  }

  record Invalid<E, T>(List<E> errors) implements Validation<E, T> {
    @Override
    public <U> Validation<E, U> map(Function<T, U> mapper) {
      return new Invalid<>(errors);
    }

    @Override
    public <U, V> Validation<E, V> combine(Validation<E, U> other, java.util.function.BiFunction<T, U, V> combiner) {
      if (other.isValid()) {
        return new Invalid<>(errors);
      }
      List<E> allErrors = new ArrayList<>(errors);
      allErrors.addAll(other.getErrors());
      return new Invalid<>(allErrors);
    }

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public T getValue() {
      throw new IllegalStateException("No value in Invalid");
    }

    @Override
    public List<E> getErrors() {
      return errors;
    }
  }

  static void validation() {
    System.out.println("=== Validation (Accumulating Errors) ===");

    // Individual field validations
    record User(String name, String email, int age) {}

    System.out.println("-- Valid User --");
    var validUser = validateUser("Alice", "alice@example.com", 25);
    System.out.println("Result: " + validUser);

    System.out.println("\n-- Invalid User (one error) --");
    var oneError = validateUser("", "alice@example.com", 25);
    System.out.println("Result: " + oneError);

    System.out.println("\n-- Invalid User (multiple errors) --");
    var multipleErrors = validateUser("", "bad-email", -5);
    System.out.println("Result: " + multipleErrors);

    System.out.println("""

        Validation vs Try:
        - Try: Fail-fast, stops at first error
        - Validation: Accumulates ALL errors
        - Use Validation for form validation, input checking
        - Shows all problems at once to the user
        """);
  }

  record UserData(String name, String email, int age) {}

  static Validation<String, UserData> validateUser(String name, String email, int age) {
    Validation<String, String> validName = validateName(name);
    Validation<String, String> validEmail = validateEmail(email);
    Validation<String, Integer> validAge = validateAge(age);

    // Combine all validations
    return validName
        .combine(validEmail, (n, e) -> new Object[] {n, e})
        .combine(validAge, (arr, a) -> new UserData((String) arr[0], (String) arr[1], a));
  }

  static Validation<String, String> validateName(String name) {
    if (name == null || name.isBlank()) {
      return Validation.invalid("Name cannot be empty");
    }
    return Validation.valid(name);
  }

  static Validation<String, String> validateEmail(String email) {
    if (email == null || !email.contains("@")) {
      return Validation.invalid("Invalid email format");
    }
    return Validation.valid(email);
  }

  static Validation<String, Integer> validateAge(int age) {
    if (age < 0 || age > 150) {
      return Validation.invalid("Age must be between 0 and 150");
    }
    return Validation.valid(age);
  }

  // ============ Railway-Oriented Programming ============

  // Result<T, E>: Similar to Either, for chaining fallible operations
  sealed interface Result<T, E> permits Ok, Err {

    static <T, E> Result<T, E> ok(T value) {
      return new Ok<>(value);
    }

    static <T, E> Result<T, E> err(E error) {
      return new Err<>(error);
    }

    <U> Result<U, E> map(Function<T, U> mapper);

    <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper);

    <F> Result<T, F> mapError(Function<E, F> mapper);

    T unwrapOr(T defaultValue);

    boolean isOk();
  }

  record Ok<T, E>(T value) implements Result<T, E> {
    @Override
    public <U> Result<U, E> map(Function<T, U> mapper) {
      return new Ok<>(mapper.apply(value));
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
      return mapper.apply(value);
    }

    @Override
    public <F> Result<T, F> mapError(Function<E, F> mapper) {
      return new Ok<>(value);
    }

    @Override
    public T unwrapOr(T defaultValue) {
      return value;
    }

    @Override
    public boolean isOk() {
      return true;
    }
  }

  record Err<T, E>(E error) implements Result<T, E> {
    @Override
    public <U> Result<U, E> map(Function<T, U> mapper) {
      return new Err<>(error);
    }

    @Override
    public <U> Result<U, E> flatMap(Function<T, Result<U, E>> mapper) {
      return new Err<>(error);
    }

    @Override
    public <F> Result<T, F> mapError(Function<E, F> mapper) {
      return new Err<>(mapper.apply(error));
    }

    @Override
    public T unwrapOr(T defaultValue) {
      return defaultValue;
    }

    @Override
    public boolean isOk() {
      return false;
    }
  }

  static void railwayOriented() {
    System.out.println("=== Railway-Oriented Programming ===");

    // Think of it as two parallel tracks:
    // Success track: ----[op1]----[op2]----[op3]----> Success
    // Failure track: --------------------------------> Failure
    // Any operation can "switch" to the failure track

    System.out.println("-- Success Path --");
    Result<Integer, String> success = parseNumber("42")
        .flatMap(n -> validatePositive(n))
        .map(n -> n * 2);
    System.out.println("Parse '42', validate positive, double: " + success);

    System.out.println("\n-- Failure at Parse --");
    Result<Integer, String> failParse = parseNumber("bad")
        .flatMap(n -> validatePositive(n))
        .map(n -> n * 2);
    System.out.println("Parse 'bad': " + failParse);

    System.out.println("\n-- Failure at Validate --");
    Result<Integer, String> failValidate = parseNumber("-5")
        .flatMap(n -> validatePositive(n))
        .map(n -> n * 2);
    System.out.println("Parse '-5', validate positive: " + failValidate);

    // Transform errors
    System.out.println("\n-- Error Transformation --");
    Result<Integer, Error> typedError = parseNumber("bad")
        .mapError(msg -> new Error("PARSE_ERROR", msg));
    System.out.println("Typed error: " + typedError);

    System.out.println("""

        Railway-Oriented Programming:
        - Operations stay on success track if OK
        - Any error switches to failure track
        - Subsequent operations are skipped on failure track
        - Clean, linear code without nested if/else
        """);
  }

  record Error(String code, String message) {}

  static Result<Integer, String> parseNumber(String s) {
    try {
      return Result.ok(Integer.parseInt(s));
    } catch (NumberFormatException e) {
      return Result.err("Cannot parse '" + s + "' as number");
    }
  }

  static Result<Integer, String> validatePositive(int n) {
    return n > 0
        ? Result.ok(n)
        : Result.err("Number must be positive, got: " + n);
  }

  // ============ Real-World Pipeline ============

  static void realWorldPipeline() {
    System.out.println("=== Real-World Pipeline ===");

    // Pipeline: Parse JSON → Validate → Transform → Save
    record Order(String id, String product, int quantity, double price) {}

    System.out.println("-- Valid Order --");
    String validJson = "{\"id\":\"ORD-001\",\"product\":\"Widget\",\"quantity\":5,\"price\":19.99}";
    var validResult = processOrder(validJson);
    printResult(validResult);

    System.out.println("\n-- Invalid JSON --");
    String invalidJson = "not json at all";
    var invalidJsonResult = processOrder(invalidJson);
    printResult(invalidJsonResult);

    System.out.println("\n-- Invalid Data --");
    String invalidData = "{\"id\":\"\",\"product\":\"Widget\",\"quantity\":-1,\"price\":19.99}";
    var invalidDataResult = processOrder(invalidData);
    printResult(invalidDataResult);

    System.out.println("""

        Real-world pipeline benefits:
        - Each step is a pure function
        - Errors handled uniformly
        - Easy to add/remove/reorder steps
        - Testable in isolation
        """);
  }

  record OrderDto(String id, String product, int quantity, double price) {}
  record ProcessedOrder(String id, String product, int quantity, double total) {}

  static Result<String, String> processOrder(String json) {
    return parseOrderJson(json)
        .flatMap(dto -> validateOrder(dto))
        .map(dto -> new ProcessedOrder(dto.id, dto.product, dto.quantity, dto.quantity * dto.price))
        .flatMap(order -> saveOrder(order));
  }

  static Result<OrderDto, String> parseOrderJson(String json) {
    // Simplified JSON parsing (in real code, use Jackson/Gson)
    if (!json.startsWith("{")) {
      return Result.err("Invalid JSON format");
    }
    // Extract fields (simplified)
    try {
      String id = extractField(json, "id");
      String product = extractField(json, "product");
      int quantity = Integer.parseInt(extractField(json, "quantity"));
      double price = Double.parseDouble(extractField(json, "price"));
      return Result.ok(new OrderDto(id, product, quantity, price));
    } catch (Exception e) {
      return Result.err("JSON parsing failed: " + e.getMessage());
    }
  }

  static String extractField(String json, String field) {
    String pattern = "\"" + field + "\":";
    int start = json.indexOf(pattern) + pattern.length();
    char next = json.charAt(start);
    if (next == '"') {
      int end = json.indexOf('"', start + 1);
      return json.substring(start + 1, end);
    } else {
      int end = start;
      while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
        end++;
      }
      return json.substring(start, end);
    }
  }

  static Result<OrderDto, String> validateOrder(OrderDto dto) {
    if (dto.id == null || dto.id.isBlank()) {
      return Result.err("Order ID cannot be empty");
    }
    if (dto.quantity <= 0) {
      return Result.err("Quantity must be positive");
    }
    if (dto.price <= 0) {
      return Result.err("Price must be positive");
    }
    return Result.ok(dto);
  }

  static Result<String, String> saveOrder(ProcessedOrder order) {
    // Simulate save (could fail in real scenario)
    System.out.println("  Saving order: " + order);
    return Result.ok("Order " + order.id + " saved successfully (total: $" +
        String.format("%.2f", order.total) + ")");
  }

  static void printResult(Result<String, String> result) {
    switch (result) {
      case Ok(String message) -> System.out.println("SUCCESS: " + message);
      case Err(String error) -> System.out.println("ERROR: " + error);
    }
  }
}
