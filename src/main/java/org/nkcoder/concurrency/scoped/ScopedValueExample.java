package org.nkcoder.concurrency.scoped;

import java.util.NoSuchElementException;

/**
 * Scoped Values (JEP 506, finalized in Java 25): Modern alternative to ThreadLocal.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Immutable within a scope - safer than ThreadLocal
 *   <li>Automatically inherited by child threads (with StructuredTaskScope)
 *   <li>Efficient with virtual threads - O(1) inheritance (no copying)
 *   <li>Clear lifecycle - value exists only within scope
 * </ul>
 *
 * <p>Mental model: "ScopedValue is like method parameters that don't need explicit passing."
 *
 * <p>Note: StructuredTaskScope examples require --enable-preview.
 */
public class ScopedValueExample {

    // CRITICAL: Must be static final (identity-based lookup)
    // Acts as a "key" in key-value storage - must be shared and stable
    private static final ScopedValue<String> CURRENT_USER = ScopedValue.newInstance();
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();
    private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

    // Use records to group related context values
    record RequestContext(String user, String requestId, String tenantId, String traceId) {}

    static void main(String[] args) throws Exception {
        problemsWithThreadLocal();
        basicBindingAndAccess();
        multipleBindings();
        callWithReturnValue();
        nestedRebinding();
        safeAccessMethods();
        whyStaticFinal();
        contextRecordPattern();
        migrationFromThreadLocal();
        bestPractices();
    }

    // ===== Problems ThreadLocal Solves =====

    static void problemsWithThreadLocal() {
        System.out.println("=== Problems ScopedValue Solves ===");

        System.out.println("""
        Five problems with ThreadLocal that ScopedValue addresses:

        1. UNCONSTRAINED MUTABILITY
           ThreadLocal allows unexpected value changes deep in call stacks.
           ScopedValue is immutable once bound.

        2. MEMORY LEAKS
           Thread pools retain ThreadLocal values unless explicitly cleared.
           ScopedValue automatically cleans up when scope exits.

        3. EXPENSIVE INHERITANCE
           InheritableThreadLocal copies entire maps to child threads - O(n).
           ScopedValue uses reference sharing - O(1).

        4. UNBOUNDED LIFETIME
           No mechanism to constrain ThreadLocal to specific scopes.
           ScopedValue lifetime is exactly the scope duration.

        5. POOR OBSERVABILITY
           Hidden state makes debugging difficult.
           ScopedValue has clear data flow via explicit binding.

        +--------------------+------------------+------------------+
        | Aspect             | ThreadLocal      | ScopedValue      |
        +--------------------+------------------+------------------+
        | Mutability         | Mutable anytime  | Immutable        |
        | Lifetime           | Until removed    | Scope duration   |
        | Inheritance        | O(n) copying     | O(1) reference   |
        | Cleanup            | Manual           | Automatic        |
        | Rebinding          | set() anywhere   | Nested where()   |
        +--------------------+------------------+------------------+
        """);
    }

    // ===== Basic Binding and Access =====

    static void basicBindingAndAccess() {
        System.out.println("=== Basic Binding and Access ===");

        // Value is unbound outside of where()
        System.out.println("  Before binding: isBound = " + CURRENT_USER.isBound());

        // Bind a value and run code with it
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Inside scope: " + CURRENT_USER.get());
            System.out.println("  isBound = " + CURRENT_USER.isBound());

            // Can call methods that access the scoped value
            // No need to pass as parameter!
            processRequest();
        });

        // Automatic cleanup after scope exits
        System.out.println("  After scope: isBound = " + CURRENT_USER.isBound());

        System.out.println();
    }

    private static void processRequest() {
        // Access scoped value without it being passed as parameter
        System.out.println("    processRequest() sees user: " + CURRENT_USER.get());
        validatePermissions();
        saveToDatabase();
    }

    private static void validatePermissions() {
        System.out.println("    validatePermissions() sees user: " + CURRENT_USER.get());
    }

    private static void saveToDatabase() {
        System.out.println("    saveToDatabase() sees user: " + CURRENT_USER.get());
    }

    // ===== Multiple Bindings =====

    static void multipleBindings() {
        System.out.println("=== Multiple Bindings ===");

        // Chain multiple bindings with .where()
        ScopedValue.where(CURRENT_USER, "alice")
                .where(REQUEST_ID, "req-12345")
                .where(TENANT_ID, "tenant-xyz")
                .run(() -> {
                    System.out.println("  User: " + CURRENT_USER.get());
                    System.out.println("  Request: " + REQUEST_ID.get());
                    System.out.println("  Tenant: " + TENANT_ID.get());
                });

        System.out.println();
    }

    // ===== call() for Return Values =====

    static void callWithReturnValue() throws Exception {
        System.out.println("=== call() for Return Values ===");

        // Use call() when you need a return value
        String result = ScopedValue.where(CURRENT_USER, "bob").call(() -> {
            return "Processed request for: " + CURRENT_USER.get();
        });

        System.out.println("  Result: " + result);

        // call() throws checked exceptions
        Integer computed = ScopedValue.where(REQUEST_ID, "calc-001").call(() -> {
            return computeExpensiveValue();
        });

        System.out.println("  Computed: " + computed);

        System.out.println("""

        run() vs call():
        - run(): Takes Runnable, returns void
        - call(): Takes Callable, returns value, throws Exception
        """);
    }

    private static Integer computeExpensiveValue() {
        return 42; // Placeholder for expensive computation
    }

    // ===== Nested Rebinding =====

    static void nestedRebinding() {
        System.out.println("=== Nested Rebinding ===");

        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("  Outer scope: " + CURRENT_USER.get());

            // Rebind in nested scope (shadowing)
            ScopedValue.where(CURRENT_USER, "bob").run(() -> {
                System.out.println("    Inner scope: " + CURRENT_USER.get());

                // Can nest further
                ScopedValue.where(CURRENT_USER, "charlie").run(() -> {
                    System.out.println("      Deepest scope: " + CURRENT_USER.get());
                });

                System.out.println("    Back to inner: " + CURRENT_USER.get());
            });

            // Back to original value automatically
            System.out.println("  Back to outer: " + CURRENT_USER.get());
        });

        System.out.println("""

        Rebinding notes:
        - Inner binding shadows outer binding
        - Original value restored when inner scope exits
        - Typical scope depth < 5 in real applications
        - JVM heavily optimizes scope lookup
        """);
    }

    // ===== Safe Access Methods =====

    static void safeAccessMethods() {
        System.out.println("=== Safe Access Methods ===");

        // get() throws if unbound
        System.out.println("  Attempting get() when unbound:");
        try {
            CURRENT_USER.get();
        } catch (NoSuchElementException e) {
            System.out.println("    Throws: " + e.getClass().getSimpleName());
        }

        // isBound() - check before accessing
        System.out.println("\n  Using isBound() check:");
        if (CURRENT_USER.isBound()) {
            System.out.println("    User: " + CURRENT_USER.get());
        } else {
            System.out.println("    User not bound, using default");
        }

        // orElse() - provide default
        System.out.println("\n  Using orElse():");
        String user = CURRENT_USER.orElse("anonymous");
        System.out.println("    User: " + user);

        // orElseThrow() - fail fast with custom exception
        System.out.println("\n  Using orElseThrow():");
        try {
            CURRENT_USER.orElseThrow(() -> new IllegalStateException("User context required"));
        } catch (IllegalStateException e) {
            System.out.println("    Custom exception: " + e.getMessage());
        }

        System.out.println("""

        Recommendation:
        - Use orElse() for sensible defaults
        - Use orElseThrow() to fail fast with clear messages
        - Avoid isBound() + get() when alternatives exist
        """);
    }

    // ===== Why Static Final? =====

    static void whyStaticFinal() {
        System.out.println("=== Why Static Final is Required ===");

        System.out.println("""
        ScopedValue uses IDENTITY-BASED lookup (reference equality, not equals()).
        The same instance must exist for both binding and reading.

        CORRECT - Static final field:
        ```java
        public class RequestContext {
            public static final ScopedValue<User> USER =
                ScopedValue.newInstance();
        }
        ```

        WRONG - Instance field (different instance per object!):
        ```java
        public class Handler {
            private final ScopedValue<User> user =
                ScopedValue.newInstance();
        }
        ```

        WRONG - Local variable (new instance each call!):
        ```java
        public void handle(Request request) {
            ScopedValue<User> user = ScopedValue.newInstance();
        }
        ```

        Mental model: "ScopedValue acts as a KEY in key-value storage.
        The key must be shared, stable, and globally accessible."
        """);
    }

    // ===== Context Record Pattern =====

    static void contextRecordPattern() {
        System.out.println("=== Context Record Pattern ===");

        // Instead of many individual ScopedValues, use a record
        var context = new RequestContext("alice", "req-123", "tenant-xyz", "trace-abc");

        ScopedValue.where(CONTEXT, context).run(() -> {
            RequestContext ctx = CONTEXT.get();
            System.out.println("  User: " + ctx.user());
            System.out.println("  Request ID: " + ctx.requestId());
            System.out.println("  Tenant: " + ctx.tenantId());
            System.out.println("  Trace ID: " + ctx.traceId());

            // Demonstrate access in nested methods
            handleBusinessLogic();
        });

        System.out.println("""

        Benefits of record pattern:
        - Single ScopedValue for related data
        - Immutable by design (records are immutable)
        - Type-safe access to all context fields
        - Cleaner than many individual ScopedValues
        """);
    }

    private static void handleBusinessLogic() {
        RequestContext ctx = CONTEXT.get();
        System.out.println("    [Business] Processing for tenant: " + ctx.tenantId());
        System.out.println("    [Business] Trace: " + ctx.traceId());
    }

    // ===== Migration from ThreadLocal =====

    static void migrationFromThreadLocal() {
        System.out.println("=== Migration from ThreadLocal ===");

        // OLD: ThreadLocal approach (error-prone)
        System.out.println("  ThreadLocal approach:");
        ThreadLocal<String> threadLocalUser = new ThreadLocal<>();
        try {
            threadLocalUser.set("alice");
            System.out.println("    User: " + threadLocalUser.get());
            // Must manually clean up!
        } finally {
            threadLocalUser.remove(); // Easy to forget!
        }

        // NEW: ScopedValue approach (safe)
        System.out.println("\n  ScopedValue approach:");
        ScopedValue.where(CURRENT_USER, "alice").run(() -> {
            System.out.println("    User: " + CURRENT_USER.get());
            // Automatic cleanup when scope exits!
        });

        System.out.println("""

        Migration steps:
        1. Replace ThreadLocal<T> with static final ScopedValue<T>
        2. Replace set()/get()/remove() with where().run() or where().call()
        3. Move binding to entry points (controllers, handlers)
        4. Remove try-finally cleanup code
        5. Use StructuredTaskScope for child thread inheritance

        When to keep ThreadLocal:
        - Database connections (when pool manages lifecycle)
        - Third-party library compatibility
        - Situations truly requiring mutability
        """);
    }

    // ===== Best Practices =====

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        DO:
        ✓ Declare ScopedValue as static final
        ✓ Use immutable objects as values (records are ideal)
        ✓ Bind at entry points (request handlers, event listeners)
        ✓ Use where().run() or where().call()
        ✓ Provide sensible defaults with orElse()
        ✓ Group related values into records
        ✓ Use StructuredTaskScope for virtual thread inheritance

        DON'T:
        ✗ Store ScopedValue instances as local variables or instance fields
        ✗ Use mutable objects (defeats immutability guarantees)
        ✗ Access before binding without checking (throws exception)
        ✗ Use raw Thread.startVirtualThread() (won't inherit values!)
        ✗ Capture ScopedValue reference in fields (always use get())

        Performance notes:
        - Reading is very fast (heavily optimized by JVM)
        - Binding creates scope object (small cost)
        - Inheritance is O(1) - reference sharing, not copying
        - Typical scope depth < 5, lookup is efficient

        Decision matrix:
        Use ScopedValue for:
        - Immutable request context
        - Security principals
        - Virtual thread workloads
        - Clear scope boundaries

        Use ThreadLocal for:
        - Database connections (pool-managed)
        - Third-party library requirements
        - Truly mutable per-thread state
        """);
    }
}
