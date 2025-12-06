package org.nkcoder.exceptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Exception Chaining: Preserving exception causes and suppressed exceptions.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, stable. Exception chaining
 * is essential for debugging and understanding failure root causes.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Cause: The exception that triggered this exception</li>
 *   <li>Suppressed: Exceptions that occurred during cleanup</li>
 *   <li>Stack trace preservation</li>
 *   <li>Best practices for exception wrapping</li>
 * </ul>
 */
public class ExceptionChainingExample {

  static void main(String[] args) {
    basicChaining();
    multiLevelChaining();
    suppressedExceptions();
    analyzingStackTraces();
    wrappingPatterns();
    bestPractices();
  }

  // ===== Basic Exception Chaining =====

  static void basicChaining() {
    System.out.println("=== Basic Exception Chaining ===");

    try {
      performOperation();
    } catch (ServiceException e) {
      System.out.println("  Caught: " + e.getMessage());
      System.out.println("  Cause: " + e.getCause());
      System.out.println("  Cause message: " + e.getCause().getMessage());
    }

    System.out.println("""

        Exception chaining:
        - initCause(Throwable) or constructor with cause
        - getCause() retrieves the original exception
        - Preserves full debugging information
        - Shows the "why" behind the exception
        """);
  }

  static void performOperation() throws ServiceException {
    try {
      // Simulate low-level failure
      throw new IOException("Connection reset by peer");
    } catch (IOException e) {
      // Wrap with context, preserve cause
      throw new ServiceException("Failed to fetch data from remote service", e);
    }
  }

  static class ServiceException extends Exception {
    public ServiceException(String message) {
      super(message);
    }

    public ServiceException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  // ===== Multi-Level Chaining =====
  static void multiLevelChaining() {
    System.out.println("=== Multi-Level Chaining ===");

    try {
      processUserRequest();
    } catch (Exception e) {
      System.out.println("  Exception chain:");
      Throwable current = e;
      int level = 0;
      while (current != null) {
        System.out.println("    " + "  ".repeat(level) +
            current.getClass().getSimpleName() + ": " + current.getMessage());
        current = current.getCause();
        level++;
      }
    }

    // Finding root cause
    try {
      processUserRequest();
    } catch (Exception e) {
      Throwable root = getRootCause(e);
      System.out.println("\n  Root cause: " + root.getClass().getSimpleName() +
          ": " + root.getMessage());
    }

    System.out.println();
  }

  static void processUserRequest() {
    try {
      callBusinessLogic();
    } catch (Exception e) {
      throw new RuntimeException("Request processing failed", e);
    }
  }

  static void callBusinessLogic() throws ServiceException {
    try {
      accessDatabase();
    } catch (Exception e) {
      throw new ServiceException("Business logic error", e);
    }
  }

  static void accessDatabase() throws IOException {
    throw new IOException("Database connection timeout");
  }

  static Throwable getRootCause(Throwable throwable) {
    Throwable root = throwable;
    while (root.getCause() != null && root.getCause() != root) {
      root = root.getCause();
    }
    return root;
  }

  // ===== Suppressed Exceptions =====

  static void suppressedExceptions() {
    System.out.println("=== Suppressed Exceptions ===");

    // Suppressed exceptions come from try-with-resources
    try {
      try (FailingResource r1 = new FailingResource("R1");
           FailingResource r2 = new FailingResource("R2")) {
        throw new RuntimeException("Primary failure in try block");
      }
    } catch (Exception e) {
      System.out.println("  Primary: " + e.getMessage());
      System.out.println("  Suppressed count: " + e.getSuppressed().length);
      for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("    Suppressed: " + suppressed.getMessage());
      }
    }

    // Manual suppression
    System.out.println("\n  Manual suppression:");
    Exception primary = new Exception("Main error");
    primary.addSuppressed(new Exception("Cleanup error 1"));
    primary.addSuppressed(new Exception("Cleanup error 2"));

    System.out.println("  Primary: " + primary.getMessage());
    for (Throwable suppressed : primary.getSuppressed()) {
      System.out.println("    Suppressed: " + suppressed.getMessage());
    }

    System.out.println("""

        Suppressed exceptions:
        - Added with addSuppressed(Throwable)
        - Retrieved with getSuppressed()
        - Automatically used by try-with-resources
        - Prevents losing cleanup failures
        """);
  }

  static class FailingResource implements AutoCloseable {
    private final String name;

    public FailingResource(String name) {
      this.name = name;
    }

    @Override
    public void close() throws Exception {
      throw new Exception("Failed to close " + name);
    }
  }

  // ===== Analyzing Stack Traces =====

  static void analyzingStackTraces() {
    System.out.println("=== Analyzing Stack Traces ===");

    try {
      level1();
    } catch (Exception e) {
      // Get stack trace as string
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String stackTrace = sw.toString();

      System.out.println("  Full stack trace:");
      // Print first few lines
      String[] lines = stackTrace.split("\n");
      for (int i = 0; i < Math.min(8, lines.length); i++) {
        System.out.println("    " + lines[i]);
      }
      if (lines.length > 8) {
        System.out.println("    ... (" + (lines.length - 8) + " more lines)");
      }

      // Access stack trace elements programmatically
      System.out.println("\n  Stack trace elements:");
      StackTraceElement[] elements = e.getStackTrace();
      for (int i = 0; i < Math.min(3, elements.length); i++) {
        StackTraceElement el = elements[i];
        System.out.println("    " + el.getClassName() + "." + el.getMethodName() +
            " (line " + el.getLineNumber() + ")");
      }
    }

    System.out.println();
  }

  static void level1() {
    level2();
  }

  static void level2() {
    level3();
  }

  static void level3() {
    throw new RuntimeException("Deep error");
  }

  // ===== Wrapping Patterns =====

  // Pattern 1: Simple wrap with context
  static class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  // Pattern 2: Layer-specific exception
  static class RepositoryException extends RuntimeException {
    private final String entityType;
    private final Object entityId;

    public RepositoryException(String message, String entityType, Object entityId, Throwable cause) {
      super(message, cause);
      this.entityType = entityType;
      this.entityId = entityId;
    }

    public String getEntityType() { return entityType; }
    public Object getEntityId() { return entityId; }

    @Override
    public String getMessage() {
      return super.getMessage() + " [" + entityType + ":" + entityId + "]";
    }
  }

  // Pattern 3: Retry-aware exception
  static class RetryableException extends RuntimeException {
    private final boolean retryable;
    private final int suggestedDelayMs;

    public RetryableException(String message, Throwable cause, boolean retryable, int delayMs) {
      super(message, cause);
      this.retryable = retryable;
      this.suggestedDelayMs = delayMs;
    }

    public boolean isRetryable() { return retryable; }
    public int getSuggestedDelayMs() { return suggestedDelayMs; }
  }

  static void wrappingPatterns() {
    System.out.println("=== Wrapping Patterns ===");

    // Pattern 1: Simple context
    try {
      throw new DataAccessException("Query failed", new IOException("Connection lost"));
    } catch (DataAccessException e) {
      System.out.println("  Simple wrap: " + e.getMessage());
    }

    // Pattern 2: Rich context
    try {
      throw new RepositoryException("Entity not found", "User", 12345L, null);
    } catch (RepositoryException e) {
      System.out.println("  Rich context: " + e.getMessage());
      System.out.println("    Entity: " + e.getEntityType() + ", ID: " + e.getEntityId());
    }

    // Pattern 3: Operational context
    try {
      throw new RetryableException("Service unavailable",
          new IOException("Timeout"), true, 5000);
    } catch (RetryableException e) {
      System.out.println("  Retryable: " + e.getMessage());
      System.out.println("    Can retry: " + e.isRetryable() +
          ", delay: " + e.getSuggestedDelayMs() + "ms");
    }

    System.out.println();
  }

  // ===== Best Practices =====

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        1. ALWAYS preserve the cause:
           Bad:  throw new AppException(e.getMessage());
           Good: throw new AppException("Context", e);

        2. ADD meaningful context:
           Bad:  throw new AppException("Error", e);
           Good: throw new AppException("Failed to process order #123", e);

        3. DON'T lose suppressed exceptions:
           - Let try-with-resources handle them
           - Use addSuppressed() for manual cleanup

        4. USE exception type for categorization:
           - Different types for different failure modes
           - Allows targeted catch blocks

        5. CONSIDER operational information:
           - Is it retryable?
           - What entity was affected?
           - What was the operation?

        6. LOG at appropriate level:
           - Log with full stack trace at origin
           - Don't log again when re-throwing

        7. EXAMINE the full chain when debugging:
           ```
           Throwable t = exception;
           while (t != null) {
               log.error("Caused by: " + t.getMessage());
               t = t.getCause();
           }
           ```

        8. USE fillInStackTrace() carefully:
           - Creates new stack trace
           - Rarely needed, can hide original location
        """);
  }
}
