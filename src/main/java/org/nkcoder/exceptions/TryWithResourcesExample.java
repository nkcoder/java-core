package org.nkcoder.exceptions;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Try-With-Resources (Java 7+, enhanced Java 9+): Automatic resource management.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, stable since Java 7.
 * Enhanced in Java 9 to allow effectively final variables.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>AutoCloseable interface for automatic cleanup</li>
 *   <li>Guaranteed close() even if exception thrown</li>
 *   <li>Multiple resources closed in reverse order</li>
 *   <li>Suppressed exceptions preserved</li>
 * </ul>
 */
public class TryWithResourcesExample {

  static void main(String[] args) {
    beforeTryWithResources();
    basicTryWithResources();
    multipleResources();
    java9Enhancement();
    customAutoCloseable();
    suppressedExceptions();
    bestPractices();
  }

  // ===== Before Try-With-Resources (Java 6 style) =====
  static void beforeTryWithResources() {
    System.out.println("=== Before Try-With-Resources (Java 6) ===");

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader("Hello\nWorld"));
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("  Read: " + line);
      }
    } catch (IOException e) {
      System.out.println("  Error: " + e.getMessage());
    } finally {
      // Must manually close - verbose and error-prone
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          // Close exception might hide the original exception!
          System.out.println("  Close error: " + e.getMessage());
        }
      }
    }

    System.out.println("""

        Problems with manual resource management:
        - Verbose boilerplate code
        - Easy to forget close()
        - Close exception can hide original exception
        - Multiple resources = nested try-finally blocks
        """);
  }

  // ===== Basic Try-With-Resources =====
  static void basicTryWithResources() {
    System.out.println("=== Basic Try-With-Resources (Java 7+) ===");

    // Resource declared in try() - automatically closed
    try (BufferedReader reader = new BufferedReader(new StringReader("Hello\nWorld"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println("  Read: " + line);
      }
    } catch (IOException e) {
      System.out.println("  Error: " + e.getMessage());
    }
    // reader.close() called automatically here, even if exception thrown

    System.out.println("""

        Try-with-resources benefits:
        - Automatic close() call
        - Cleaner, less code
        - Exceptions from close() become suppressed
        - Works with any AutoCloseable
        """);
  }

  // ===== Multiple Resources =====
  static void multipleResources() {
    System.out.println("=== Multiple Resources ===");

    // Multiple resources - closed in REVERSE order of declaration
    try (
        StringReader sr = new StringReader("Line1\nLine2\nLine3");
        BufferedReader br = new BufferedReader(sr)
    ) {
      System.out.println("  Reading with multiple resources:");
      String line;
      while ((line = br.readLine()) != null) {
        System.out.println("    " + line);
      }
    } catch (IOException e) {
      System.out.println("  Error: " + e.getMessage());
    }
    // Close order: br.close() first, then sr.close()

    // Demonstrating close order with custom resources
    System.out.println("\n  Close order demonstration:");
    try (
        NamedResource first = new NamedResource("First");
        NamedResource second = new NamedResource("Second");
        NamedResource third = new NamedResource("Third")
    ) {
      System.out.println("    Using resources...");
    }

    System.out.println();
  }

  static class NamedResource implements AutoCloseable {
    private final String name;

    public NamedResource(String name) {
      this.name = name;
      System.out.println("    Opened: " + name);
    }

    @Override
    public void close() {
      System.out.println("    Closed: " + name);
    }
  }

  // ===== Java 9 Enhancement =====

  static void java9Enhancement() {
    System.out.println("=== Java 9 Enhancement ===");

    // Java 7/8: Resource must be declared in try()
    // Java 9+: Can use effectively final variables

    BufferedReader reader = new BufferedReader(new StringReader("Java 9 style"));

    // Java 9+: Use existing effectively final variable
    try (reader) {  // No need to redeclare!
      System.out.println("  Read: " + reader.readLine());
    } catch (IOException e) {
      System.out.println("  Error: " + e.getMessage());
    }

    // Useful when resource comes from a factory or parameter
    processResource(new NamedResource("FromFactory"));

    System.out.println("""

        Java 9 enhancement:
        - Can use effectively final variables in try()
        - Reduces redundant declarations
        - Cleaner code when resources come from elsewhere
        """);
  }

  static void processResource(AutoCloseable resource) {
    try (resource) {  // Use parameter directly (Java 9+)
      System.out.println("  Processing resource");
    } catch (Exception e) {
      System.out.println("  Error: " + e.getMessage());
    }
  }

  // ===== Custom AutoCloseable =====
  static class DatabaseConnection implements AutoCloseable {
    private final String connectionId;
    private boolean closed = false;

    public DatabaseConnection(String id) {
      this.connectionId = id;
      System.out.println("    Connected: " + id);
    }

    public void query(String sql) {
      if (closed) throw new IllegalStateException("Connection closed");
      System.out.println("    Query[" + connectionId + "]: " + sql);
    }

    @Override
    public void close() {
      if (!closed) {
        closed = true;
        System.out.println("    Disconnected: " + connectionId);
      }
    }
  }

  // Closeable vs AutoCloseable
  static class LegacyResource implements Closeable {
    @Override
    public void close() throws IOException {
      // Closeable.close() can only throw IOException
      System.out.println("    Legacy resource closed");
    }
  }

  static void customAutoCloseable() {
    System.out.println("=== Custom AutoCloseable ===");

    try (DatabaseConnection conn = new DatabaseConnection("db-001")) {
      conn.query("SELECT * FROM users");
      conn.query("SELECT * FROM orders");
    }

    // Closeable extends AutoCloseable
    try (LegacyResource legacy = new LegacyResource()) {
      System.out.println("    Using legacy resource");
    } catch (IOException e) {
      System.out.println("    Error: " + e.getMessage());
    }

    System.out.println("""

        AutoCloseable vs Closeable:
        - AutoCloseable: close() throws Exception
        - Closeable: close() throws IOException (more specific)
        - Closeable extends AutoCloseable
        - Use AutoCloseable for new code (more flexible)
        """);
  }

  // ===== Suppressed Exceptions =====

  static class FailingResource implements AutoCloseable {
    private final String name;
    private final boolean failOnUse;
    private final boolean failOnClose;

    public FailingResource(String name, boolean failOnUse, boolean failOnClose) {
      this.name = name;
      this.failOnUse = failOnUse;
      this.failOnClose = failOnClose;
    }

    public void use() {
      if (failOnUse) {
        throw new RuntimeException(name + ": Failed during use");
      }
      System.out.println("    Using " + name);
    }

    @Override
    public void close() {
      if (failOnClose) {
        throw new RuntimeException(name + ": Failed during close");
      }
      System.out.println("    Closed " + name);
    }
  }

  static void suppressedExceptions() {
    System.out.println("=== Suppressed Exceptions ===");

    // Scenario 1: Exception during use, close succeeds
    System.out.println("  Scenario 1: Exception during use");
    try (FailingResource res = new FailingResource("Res1", true, false)) {
      res.use();
    } catch (RuntimeException e) {
      System.out.println("    Caught: " + e.getMessage());
      System.out.println("    Suppressed count: " + e.getSuppressed().length);
    }

    // Scenario 2: Success during use, exception during close
    System.out.println("\n  Scenario 2: Exception during close");
    try (FailingResource res = new FailingResource("Res2", false, true)) {
      res.use();
    } catch (RuntimeException e) {
      System.out.println("    Caught: " + e.getMessage());
    }

    // Scenario 3: Exception during both use AND close
    System.out.println("\n  Scenario 3: Exceptions during both use and close");
    try (FailingResource res = new FailingResource("Res3", true, true)) {
      res.use();
    } catch (RuntimeException e) {
      System.out.println("    Primary exception: " + e.getMessage());
      for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("    Suppressed: " + suppressed.getMessage());
      }
    }

    // Multiple resources with close failures
    System.out.println("\n  Scenario 4: Multiple close failures");
    try (
        FailingResource r1 = new FailingResource("R1", false, true);
        FailingResource r2 = new FailingResource("R2", false, true);
        FailingResource r3 = new FailingResource("R3", true, true)
    ) {
      r1.use();
      r2.use();
      r3.use();  // This throws
    } catch (RuntimeException e) {
      System.out.println("    Primary: " + e.getMessage());
      for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("    Suppressed: " + suppressed.getMessage());
      }
    }

    System.out.println();
  }

  // ===== Best Practices =====
  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        1. Always use try-with-resources for AutoCloseable:
           - Files, streams, connections, locks
           - Don't rely on finalizers

        2. Implement AutoCloseable for your resources:
           - Make close() idempotent (safe to call multiple times)
           - Don't throw exceptions in close() if possible
           - Release all resources in close()

        3. Order matters for multiple resources:
           - Declare in order of dependency
           - Outer resources first, inner resources last
           - They close in reverse order

        4. Handle suppressed exceptions:
           - Check getSuppressed() for complete error info
           - Log all suppressed exceptions
           - Don't ignore close failures

        5. Java 9+ effectively final variables:
           - Use when resource comes from factory/parameter
           - Keeps code cleaner
        """);

    // Example of idempotent close
    System.out.println("  Idempotent close example:");
    DatabaseConnection conn = new DatabaseConnection("idempotent-test");
    conn.close();
    conn.close();  // Safe to call multiple times
    conn.close();

    System.out.println();
  }
}
