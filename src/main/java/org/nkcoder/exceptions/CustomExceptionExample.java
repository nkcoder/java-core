package org.nkcoder.exceptions;

import java.io.Serial;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom Exceptions: Creating domain-specific exceptions with best practices.
 *
 * <p><strong>Java 25 Status:</strong> Core language feature, unchanged. Well-designed
 * custom exceptions improve error handling and debugging.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>When to create custom exceptions</li>
 *   <li>Checked vs unchecked custom exceptions</li>
 *   <li>Exception with rich context</li>
 *   <li>Exception hierarchies</li>
 * </ul>
 */
public class CustomExceptionExample {

  static void main(String[] args) {
    whenToCreateCustom();
    basicCustomException();
    richContextException();
    exceptionHierarchy();
    validationExceptions();
    builderPatternException();
    bestPractices();
  }

  // ===== When to Create Custom Exceptions =====

  static void whenToCreateCustom() {
    System.out.println("=== When to Create Custom Exceptions ===");

    System.out.println("""
        CREATE custom exception when:
        1. Standard exceptions don't convey enough meaning
           - IllegalArgumentException → ValidationException
           - RuntimeException → OrderProcessingException

        2. You need to add domain-specific context
           - User ID, order number, transaction ID
           - Operation that failed

        3. You want to distinguish exception types for handling
           - Retryable vs non-retryable
           - Client error vs server error

        4. You're building a library/framework
           - Users expect specific exception types
           - Clean API contract

        DON'T create custom exception when:
        - Standard exception conveys the meaning well
        - No additional context needed
        - Not handling differently anyway
        """);
  }

  // ===== Basic Custom Exception =====

  // Unchecked (most common in modern Java)
  static class OrderNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String orderId;

    public OrderNotFoundException(String orderId) {
      super("Order not found: " + orderId);
      this.orderId = orderId;
    }

    public String getOrderId() {
      return orderId;
    }
  }

  // Checked (when recovery is expected)
  static class PaymentDeclinedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String reason;

    public PaymentDeclinedException(String reason) {
      super("Payment declined: " + reason);
      this.reason = reason;
    }

    public PaymentDeclinedException(String reason, Throwable cause) {
      super("Payment declined: " + reason, cause);
      this.reason = reason;
    }

    public String getReason() {
      return reason;
    }
  }

  static void basicCustomException() {
    System.out.println("=== Basic Custom Exception ===");

    // Unchecked - no forced handling
    try {
      findOrder("ORD-999");
    } catch (OrderNotFoundException e) {
      System.out.println("  Caught: " + e.getMessage());
      System.out.println("  Order ID: " + e.getOrderId());
    }

    // Checked - must handle
    try {
      processPayment(0);
    } catch (PaymentDeclinedException e) {
      System.out.println("  Payment failed: " + e.getReason());
    }

    System.out.println("""

        Custom exception essentials:
        - Extend RuntimeException (unchecked) or Exception (checked)
        - Include serialVersionUID for Serializable
        - Provide constructors for message and cause
        - Add domain-specific fields with getters
        """);
  }

  static void findOrder(String orderId) {
    // Simulate not found
    throw new OrderNotFoundException(orderId);
  }

  static void processPayment(double amount) throws PaymentDeclinedException {
    if (amount <= 0) {
      throw new PaymentDeclinedException("Invalid amount");
    }
  }

  // ===== Rich Context Exception =====

  static class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String errorCode;
    private final Instant timestamp;
    private final Map<String, Object> context;

    public ApiException(int statusCode, String errorCode, String message) {
      this(statusCode, errorCode, message, null, Map.of());
    }

    public ApiException(int statusCode, String errorCode, String message,
                        Throwable cause, Map<String, Object> context) {
      super(message, cause);
      this.statusCode = statusCode;
      this.errorCode = errorCode;
      this.timestamp = Instant.now();
      this.context = new HashMap<>(context);
    }

    public int getStatusCode() { return statusCode; }
    public String getErrorCode() { return errorCode; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getContext() { return Collections.unmodifiableMap(context); }

    @Override
    public String toString() {
      return String.format("ApiException[%d %s] %s (context=%s)",
          statusCode, errorCode, getMessage(), context);
    }
  }

  static void richContextException() {
    System.out.println("=== Rich Context Exception ===");

    try {
      throw new ApiException(404, "USER_NOT_FOUND", "User does not exist",
          null, Map.of("userId", 12345L, "requestedBy", "admin"));
    } catch (ApiException e) {
      System.out.println("  Status: " + e.getStatusCode());
      System.out.println("  Error code: " + e.getErrorCode());
      System.out.println("  Message: " + e.getMessage());
      System.out.println("  Timestamp: " + e.getTimestamp());
      System.out.println("  Context: " + e.getContext());
    }

    System.out.println();
  }

  // ===== Exception Hierarchy =====

  // Base exception for the application
  static class ApplicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ApplicationException(String message) {
      super(message);
    }

    public ApplicationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  // Service layer exceptions
  static class ServiceException extends ApplicationException {
    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceException(String message) {
      super(message);
    }

    public ServiceException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  // Data access exceptions
  static class DataAccessException extends ApplicationException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  // Specific service exceptions
  static class UserServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 1L;

    public UserServiceException(String message) {
      super(message);
    }
  }

  static class OrderServiceException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String orderId;

    public OrderServiceException(String message, String orderId) {
      super(message);
      this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
  }

  static void exceptionHierarchy() {
    System.out.println("=== Exception Hierarchy ===");

    System.out.println("""
        ApplicationException (base)
        ├── ServiceException
        │   ├── UserServiceException
        │   └── OrderServiceException
        └── DataAccessException
        """);

    // Can catch at different levels
    try {
      throw new OrderServiceException("Order validation failed", "ORD-123");
    } catch (OrderServiceException e) {
      System.out.println("  Specific catch: " + e.getClass().getSimpleName());
      System.out.println("    Order ID: " + e.getOrderId());
    }

    try {
      throw new UserServiceException("User not found");
    } catch (ServiceException e) {
      // Catches UserServiceException and OrderServiceException
      System.out.println("  Service-level catch: " + e.getClass().getSimpleName());
    }

    try {
      throw new DataAccessException("Connection failed", new RuntimeException("Timeout"));
    } catch (ApplicationException e) {
      // Catches all application exceptions
      System.out.println("  Application-level catch: " + e.getClass().getSimpleName());
    }

    System.out.println();
  }

  // ===== Validation Exceptions =====

  static class ValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> fieldErrors;

    public ValidationException(String message, Map<String, String> fieldErrors) {
      super(message);
      this.fieldErrors = new HashMap<>(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
      return Collections.unmodifiableMap(fieldErrors);
    }

    public boolean hasFieldError(String field) {
      return fieldErrors.containsKey(field);
    }

    public String getFieldError(String field) {
      return fieldErrors.get(field);
    }
  }

  static void validationExceptions() {
    System.out.println("=== Validation Exceptions ===");

    Map<String, String> errors = new HashMap<>();
    errors.put("email", "Invalid email format");
    errors.put("age", "Must be 18 or older");
    errors.put("password", "Must be at least 8 characters");

    try {
      throw new ValidationException("User validation failed", errors);
    } catch (ValidationException e) {
      System.out.println("  Validation failed:");
      for (Map.Entry<String, String> error : e.getFieldErrors().entrySet()) {
        System.out.println("    " + error.getKey() + ": " + error.getValue());
      }
    }

    System.out.println();
  }

  // ===== Builder Pattern Exception =====
  static class DetailedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String operation;
    private final Map<String, Object> details;
    private final boolean retryable;

    private DetailedException(Builder builder) {
      super(builder.message, builder.cause);
      this.errorCode = builder.errorCode;
      this.operation = builder.operation;
      this.details = Collections.unmodifiableMap(new HashMap<>(builder.details));
      this.retryable = builder.retryable;
    }

    public String getErrorCode() { return errorCode; }
    public String getOperation() { return operation; }
    public Map<String, Object> getDetails() { return details; }
    public boolean isRetryable() { return retryable; }

    public static Builder builder(String message) {
      return new Builder(message);
    }

    public static class Builder {
      private final String message;
      private String errorCode = "UNKNOWN";
      private String operation;
      private Throwable cause;
      private Map<String, Object> details = new HashMap<>();
      private boolean retryable = false;

      private Builder(String message) {
        this.message = message;
      }

      public Builder errorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
      }

      public Builder operation(String operation) {
        this.operation = operation;
        return this;
      }

      public Builder cause(Throwable cause) {
        this.cause = cause;
        return this;
      }

      public Builder detail(String key, Object value) {
        this.details.put(key, value);
        return this;
      }

      public Builder retryable(boolean retryable) {
        this.retryable = retryable;
        return this;
      }

      public DetailedException build() {
        return new DetailedException(this);
      }
    }
  }

  static void builderPatternException() {
    System.out.println("=== Builder Pattern Exception ===");

    DetailedException ex = DetailedException.builder("Order processing failed")
        .errorCode("ORDER_ERR_001")
        .operation("submitOrder")
        .detail("orderId", "ORD-12345")
        .detail("userId", 42L)
        .detail("amount", 99.99)
        .retryable(true)
        .cause(new RuntimeException("Payment gateway timeout"))
        .build();

    try {
      throw ex;
    } catch (DetailedException e) {
      System.out.println("  Error: " + e.getMessage());
      System.out.println("  Code: " + e.getErrorCode());
      System.out.println("  Operation: " + e.getOperation());
      System.out.println("  Retryable: " + e.isRetryable());
      System.out.println("  Details: " + e.getDetails());
      System.out.println("  Cause: " + e.getCause().getMessage());
    }

    System.out.println();
  }

  // ===== Best Practices =====

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        1. NAMING:
           - End with "Exception"
           - Be specific: OrderNotFoundException > NotFoundException
           - Use domain terminology

        2. SERIALIZATION:
           - Include serialVersionUID
           - Make fields serializable or transient

        3. CONSTRUCTORS - provide these:
           - (String message)
           - (String message, Throwable cause)
           - Domain-specific constructors

        4. FIELDS:
           - Keep relevant context
           - Make immutable
           - Provide getters

        5. toString():
           - Override for debugging
           - Include key fields

        6. HIERARCHY:
           - Don't make too deep (2-3 levels max)
           - Group by domain or layer
           - Base exception for application

        7. DOCUMENTATION:
           - Javadoc when it's thrown
           - What caller should do

        8. TESTING:
           - Unit test exception creation
           - Verify message format
           - Check serialization if used
        """);
  }
}
