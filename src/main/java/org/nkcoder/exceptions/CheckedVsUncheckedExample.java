package org.nkcoder.exceptions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Checked vs Unchecked Exceptions: When to use which and best practices.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Understanding
 * when to use checked vs unchecked exceptions is essential for good API design.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Checked exceptions: Must be declared or caught</li>
 *   <li>Unchecked exceptions: RuntimeException and Error</li>
 *   <li>Design guidelines for exception choice</li>
 *   <li>Converting between checked and unchecked</li>
 * </ul>
 */
public class CheckedVsUncheckedExample {

  static void main(String[] args) {
    exceptionHierarchy();
    checkedExceptions();
    uncheckedExceptions();
    whenToUseWhich();
    convertingExceptions();
    lambdasAndExceptions();
    modernBestPractices();
  }

  // ===== Exception Hierarchy =====
  static void exceptionHierarchy() {
    System.out.println("=== Exception Hierarchy ===");

    System.out.println("""
        Throwable (root)
        ├── Error (unchecked) - JVM problems, don't catch
        │   ├── OutOfMemoryError
        │   ├── StackOverflowError
        │   └── ...
        └── Exception
            ├── RuntimeException (unchecked) - programming bugs
            │   ├── NullPointerException
            │   ├── IllegalArgumentException
            │   ├── IndexOutOfBoundsException
            │   └── ...
            └── Checked exceptions - recoverable conditions
                ├── IOException
                ├── SQLException
                └── ...

        Rule: RuntimeException and its subclasses + Error = unchecked
              Everything else under Exception = checked
        """);
  }

  // ===== Checked Exceptions =====

  // Method MUST declare checked exceptions
  static String readFile(String path) throws IOException {
    return Files.readString(Path.of(path));
  }

  // Caller MUST handle or propagate
  static void checkedExceptions() {
    System.out.println("=== Checked Exceptions ===");

    // Option 1: Catch and handle
    try {
      String content = readFile("/nonexistent/file.txt");
      System.out.println("Content: " + content);
    } catch (IOException e) {
      System.out.println("  Caught IOException: " + e.getMessage());
    }

    // Option 2: Declare throws (propagate to caller)
    // See readFile() method above

    System.out.println("""

        Checked exceptions:
        - Compiler enforces handling
        - Part of method's contract/signature
        - Caller must catch OR declare throws
        - Examples: IOException, SQLException, ParseException
        """);
  }

  // ===== Unchecked Exceptions =====

  static int divide(int a, int b) {
    if (b == 0) {
      throw new IllegalArgumentException("Divisor cannot be zero");
    }
    return a / b;
  }

  static String getElement(List<String> list, int index) {
    // No need to declare - unchecked
    return list.get(index);  // May throw IndexOutOfBoundsException
  }

  static void uncheckedExceptions() {
    System.out.println("=== Unchecked Exceptions ===");

    // No forced handling - but still catchable
    try {
      int result = divide(10, 0);
      System.out.println("Result: " + result);
    } catch (IllegalArgumentException e) {
      System.out.println("  Caught: " + e.getMessage());
    }

    // Common runtime exceptions
    try {
      String s = null;
      s.length();  // NullPointerException
    } catch (NullPointerException e) {
      System.out.println("  NullPointerException caught");
    }

    try {
      var outOfBound = getElement(List.of("a", "b"), 10);// IndexOutOfBoundsException
      System.out.println("outOfBound: " + outOfBound);
    } catch (IndexOutOfBoundsException e) {
      System.out.println("  IndexOutOfBoundsException caught");
    }

    System.out.println("""

        Unchecked exceptions (RuntimeException):
        - No forced handling
        - Usually indicate programming bugs
        - Should be fixed, not caught
        - Examples: NullPointerException, IllegalArgumentException
        """);
  }

  // ===== When to Use Which =====

  static void whenToUseWhich() {
    System.out.println("=== When to Use Which ===");

    System.out.println("""
        USE CHECKED EXCEPTIONS when:
        1. Caller can reasonably recover
           - File not found → prompt user for different file
           - Network timeout → retry or use cached data

        2. Failure is expected in normal operation
           - Parsing user input that might be invalid
           - External service that might be unavailable

        3. Caller needs to know about possible failure
           - Part of the API contract

        USE UNCHECKED EXCEPTIONS when:
        1. Programming error (bug)
           - Null where not allowed → NullPointerException
           - Invalid argument → IllegalArgumentException
           - Illegal state → IllegalStateException

        2. Caller cannot recover
           - Out of memory
           - Configuration error at startup

        3. Forcing handling would clutter code
           - Every method would need try-catch

        COMMON PATTERNS:
        - Validate inputs: throw IllegalArgumentException (unchecked)
        - Preconditions: throw IllegalStateException (unchecked)
        - External failures: throw checked or wrap in unchecked
        """);
  }

  // ===== Converting Exceptions =====

  // Wrap checked in unchecked
  static String readFileUnchecked(String path) {
    try {
      return Files.readString(Path.of(path));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read: " + path, e);
    }
  }

  // Generic sneaky throw (use with caution!)
  @SuppressWarnings("unchecked")
  static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
    throw (E) e;
  }

  // Better: Custom unchecked wrapper
  static class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
      super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  static void convertingExceptions() {
    System.out.println("=== Converting Exceptions ===");

    // Using UncheckedIOException
    try {
      String content = readFileUnchecked("/no/such/file.txt");
    } catch (UncheckedIOException e) {
      System.out.println("  UncheckedIOException: " + e.getMessage());
      System.out.println("  Original cause: " + e.getCause().getClass().getSimpleName());
    }

    // Custom wrapper
    try {
      throw new ConfigurationException("Invalid config", new IOException("Cannot read"));
    } catch (ConfigurationException e) {
      System.out.println("  ConfigurationException: " + e.getMessage());
    }

    System.out.println("""

        Converting strategies:
        1. UncheckedIOException - standard wrapper for IOException
        2. Custom RuntimeException - domain-specific unchecked
        3. CompletionException - for async operations
        4. Preserve cause - always include original exception
        """);
  }

  // ===== Lambdas and Exceptions =====

  @FunctionalInterface
  interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  static <T> T unchecked(ThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Optional-based approach
  static <T> Optional<T> tryGet(ThrowingSupplier<T> supplier) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  static void lambdasAndExceptions() {
    System.out.println("=== Lambdas and Exceptions ===");

    // Problem: Standard functional interfaces don't allow checked exceptions
    List<String> paths = List.of("file1.txt", "file2.txt");

    // Won't compile - readFile throws IOException:
    // paths.stream().map(CheckedVsUncheckedExample::readFile)...

    // Solution 1: Wrap in try-catch inside lambda
    List<String> contents = paths.stream()
        .map(path -> {
          try {
            return readFile(path);
          } catch (IOException e) {
            return "Error: " + e.getMessage();
          }
        })
        .toList();
    System.out.println("  With try-catch: " + contents);

    // Solution 2: Use wrapper method
    paths.stream()
        .map(path -> unchecked(() -> readFile(path)))
        .forEach(c -> System.out.println("  Unchecked: " + c.substring(0, Math.min(20, c.length()))));

    // Solution 3: Optional for graceful handling
    paths.stream()
        .map(path -> tryGet(() -> readFile(path)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(System.out::println);

    System.out.println("""

        Lambda exception strategies:
        1. Try-catch inside lambda (verbose but explicit)
        2. Wrapper that converts to RuntimeException
        3. Optional-based for graceful failures
        4. Custom functional interface with throws
        """);
  }

  // ===== Modern Best Practices =====

  static void modernBestPractices() {
    System.out.println("=== Modern Best Practices ===");

    System.out.println("""
        1. PREFER UNCHECKED for most cases:
           - Modern Java style trends toward unchecked
           - Cleaner code, especially with lambdas
           - Document exceptions in Javadoc

        2. USE CHECKED sparingly:
           - Only when recovery is truly expected
           - API boundary with external systems
           - Framework-level contracts

        3. NEVER catch and ignore:
           Bad:  catch (Exception e) { }
           Good: catch (Exception e) { log.error("...", e); throw e; }

        4. CATCH specific exceptions:
           Bad:  catch (Exception e) { ... }
           Good: catch (IOException | SQLException e) { ... }

        5. USE finally OR try-with-resources, not both:
           - try-with-resources for cleanup
           - finally for non-resource cleanup

        6. INCLUDE context in messages:
           Bad:  throw new IOException("File not found");
           Good: throw new IOException("Cannot read config: " + path);

        7. PRESERVE exception chains:
           Bad:  throw new AppException(e.getMessage());
           Good: throw new AppException("Operation failed", e);

        8. THROW early, CATCH late:
           - Validate at entry points
           - Handle at appropriate level
        """);
  }
}
