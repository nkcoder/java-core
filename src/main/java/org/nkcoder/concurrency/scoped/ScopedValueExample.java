package org.nkcoder.concurrency.scoped;

/**
 * Scoped Values (Java 25): Modern alternative to ThreadLocal for virtual threads.
 *
 * <ul>
 *   <li>Immutable within a scope - safer than ThreadLocal
 *   <li>Automatically inherited by child threads
 *   <li>Efficient with virtual threads (no copying)
 *   <li>Clear lifecycle - value exists only within scope
 * </ul>
 *
 * <p>ScopedValue is finalized in JDK 25 (JEP 506).
 *
 * <p>Note: StructuredTaskScope examples require --enable-preview.
 */
public class ScopedValueExample {

  // Declare scoped values as static final
  private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
  private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
  private static final ScopedValue<Context> CONTEXT = ScopedValue.newInstance();

  record Context(String user, String requestId, String tenantId) {}

  static void main(String[] args) throws Exception {
    whatAreScopedValues();
    basicUsage();
    nestedScopes();
    multipleBindings();
    scopedValueVsThreadLocal();
    realWorldExample();
    bestPractices();
  }

  static void whatAreScopedValues() {
    System.out.println("=== What Are Scoped Values? ===");

    System.out.println(
        """
        Scoped Values are a modern replacement for ThreadLocal:

        ThreadLocal problems with virtual threads:
        - Mutable: can be changed anytime
        - Inherited by copying (expensive for millions of threads)
        - Unbounded lifetime (memory leaks)
        - Hard to track where values come from

        ScopedValue benefits:
        - Immutable within a scope (set once, read many)
        - Inherited by reference (efficient)
        - Bounded lifetime (scope-based)
        - Clear data flow (explicit binding)

        Use cases:
        - Request context (user, tenant, trace ID)
        - Transaction context
        - Security credentials
        - Logging context
        """);
  }

  static void basicUsage() {
    System.out.println("=== Basic Usage ===");

    // Value is unbound outside of where()
    System.out.println("  Before binding: isBound = " + CURRENT_USER.isBound());

    // Bind a value and run code with it
    ScopedValue.where(CURRENT_USER, "alice")
        .run(
            () -> {
              System.out.println("  Inside scope: " + CURRENT_USER.get());
              System.out.println("  isBound = " + CURRENT_USER.isBound());

              // Can call methods that access the scoped value
              doSomethingWithUser();
            });

    System.out.println("  After scope: isBound = " + CURRENT_USER.isBound());

    // Getting value when unbound throws exception
    try {
      CURRENT_USER.get();
    } catch (Exception e) {
      System.out.println("  Accessing unbound throws: " + e.getClass().getSimpleName());
    }

    // Safe access with orElse
    String user = CURRENT_USER.orElse("anonymous");
    System.out.println("  orElse when unbound: " + user);

    System.out.println();
  }

  private static void doSomethingWithUser() {
    // Access scoped value without passing as parameter
    System.out.println("    doSomethingWithUser() sees: " + CURRENT_USER.get());
  }

  static void nestedScopes() {
    System.out.println("=== Nested Scopes (Rebinding) ===");

    ScopedValue.where(CURRENT_USER, "alice")
        .run(
            () -> {
              System.out.println("  Outer scope: " + CURRENT_USER.get());

              // Can rebind in nested scope (shadowing)
              ScopedValue.where(CURRENT_USER, "bob")
                  .run(
                      () -> {
                        System.out.println("    Inner scope: " + CURRENT_USER.get());
                      });

              // Back to original value
              System.out.println("  Back to outer: " + CURRENT_USER.get());
            });

    System.out.println();
  }

  static void multipleBindings() {
    System.out.println("=== Multiple Bindings ===");

    // Bind multiple scoped values at once
    ScopedValue.where(CURRENT_USER, "alice")
        .where(REQUEST_ID, "req-12345")
        .run(
            () -> {
              System.out.println("  User: " + CURRENT_USER.get());
              System.out.println("  Request: " + REQUEST_ID.get());
            });

    // Using a context record for related values
    var context = new Context("bob", "req-67890", "tenant-xyz");
    ScopedValue.where(CONTEXT, context)
        .run(
            () -> {
              Context ctx = CONTEXT.get();
              System.out.println("  Context user: " + ctx.user());
              System.out.println("  Context request: " + ctx.requestId());
              System.out.println("  Context tenant: " + ctx.tenantId());
            });

    System.out.println();
  }

  static void scopedValueVsThreadLocal() throws Exception {
    System.out.println("=== ScopedValue vs ThreadLocal ===");

    // ThreadLocal way (old, not recommended with virtual threads)
    ThreadLocal<String> threadLocalUser = new ThreadLocal<>();
    threadLocalUser.set("alice");
    System.out.println("  ThreadLocal: " + threadLocalUser.get());
    threadLocalUser.remove(); // Must manually clean up!

    // ScopedValue way (recommended)
    ScopedValue.where(CURRENT_USER, "alice")
        .run(
            () -> {
              System.out.println("  ScopedValue: " + CURRENT_USER.get());
              // Automatic cleanup when scope exits
            });

    System.out.println(
        """

        Key differences:
        +--------------------+------------------+------------------+
        | Feature            | ThreadLocal      | ScopedValue      |
        +--------------------+------------------+------------------+
        | Mutability         | Mutable          | Immutable        |
        | Inheritance        | Copies value     | Shares reference |
        | Cleanup            | Manual remove()  | Automatic        |
        | Rebinding          | set() anytime    | Only via where() |
        | Virtual threads    | Expensive        | Efficient        |
        +--------------------+------------------+------------------+

        When to use ThreadLocal still:
        - Legacy code that can't be migrated
        - Frameworks that require it
        - Truly thread-local state (not inherited)
        """);
  }

  static void realWorldExample() throws Exception {
    System.out.println("=== Real-World Example: Request Context ===");

    // Simulate handling an HTTP request
    record Request(String userId, String tenantId, String traceId) {}

    var request = new Request("user-123", "tenant-abc", "trace-xyz");

    // Bind context for the entire request handling
    ScopedValue.where(CONTEXT, new Context(request.userId(), request.traceId(), request.tenantId()))
        .run(
            () -> {
              System.out.println("  Handling request for: " + CONTEXT.get().user());
              handleRequest();
            });

    System.out.println();
  }

  private static void handleRequest() {
    // Service layer - context is available without passing
    System.out.println("    [Service] Processing for tenant: " + CONTEXT.get().tenantId());
    validatePermissions();
    saveToDatabase();
  }

  private static void validatePermissions() {
    // Security check - context is available
    System.out.println("    [Security] Checking user: " + CONTEXT.get().user());
  }

  private static void saveToDatabase() {
    // Data layer - can log with trace ID
    System.out.println("    [DB] Saving with trace: " + CONTEXT.get().requestId());
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println(
        """
        DO:
        - Declare ScopedValue as static final
        - Use immutable objects as values
        - Use records for complex context
        - Bind early, use throughout request
        - Use where().run() or where().call()

        DON'T:
        - Store ScopedValue instances as locals
        - Use mutable objects (defeats immutability)
        - Access before binding (throws exception)
        - Forget to handle unbound case with orElse()

        Migration from ThreadLocal:
        1. Replace ThreadLocal<T> with ScopedValue<T>
        2. Replace set()/get()/remove() with where().run()
        3. Move binding to entry points (controllers, handlers)
        4. Remove manual cleanup code

        Performance tips:
        - Reading is very fast (nearly free)
        - Binding creates a new scope object (small cost)
        - Inheritance is O(1) (reference sharing)
        """);
  }
}
