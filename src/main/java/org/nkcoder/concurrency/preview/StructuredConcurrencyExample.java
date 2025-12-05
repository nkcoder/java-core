package org.nkcoder.concurrency.preview;

/**
 * Structured Concurrency (Java 25 Preview): Manage concurrent subtasks as a unit.
 *
 * <p><strong>WARNING: PREVIEW FEATURE</strong>
 *
 * <p>This API is in its 5th preview (since Java 19) and may change or be removed in future
 * releases. Do not use in production code until finalized.
 *
 * <ul>
 *   <li>Parent task waits for all subtasks to complete
 *   <li>Subtask failures propagate to parent
 *   <li>Cancellation cascades to all subtasks
 *   <li>Clear ownership and lifecycle of concurrent tasks
 * </ul>
 *
 * <p>Note: Requires --enable-preview flag.
 *
 * <p>Need to add the following config to build.gradle.kts
 *
 * <pre>{@code
 * // For preview features
 * tasks.withType<JavaCompile> {
 *     options.compilerArgs.addAll(listOf("--enable-preview", "-Xlint:preview"))
 * }
 *
 * tasks.withType<JavaExec> {
 *     jvmArgs("--enable-preview")
 * }
 * }</pre>
 *
 * <p>Intellij Idea: Project Structure -> Project Settings -> Project: language level (25 preview).
 *
 * <p>See: <a href="https://openjdk.org/jeps/505">JEP 505: Structured Concurrency (Fifth
 * Preview)</a>
 */
public class StructuredConcurrencyExample {

  /*
  static void main(String[] args) throws Exception {
    whatIsStructuredConcurrency();
    basicUsage();
    allSuccessOrThrow();
    anySuccessfulResult();
    withTimeout();
    realWorldExample();
    bestPractices();
  }

  static void whatIsStructuredConcurrency() {
    System.out.println("=== What Is Structured Concurrency? ===");

    System.out.println("""
        Structured Concurrency treats concurrent tasks like structured code blocks:

        Problems with unstructured concurrency:
        - Tasks outlive their parent (thread leaks)
        - Errors lost or hard to propagate
        - Cancellation is manual and error-prone
        - Hard to reason about task lifetimes

        Structured Concurrency guarantees:
        - All subtasks complete before scope exits
        - Errors propagate to parent automatically
        - Cancellation cascades to all subtasks
        - Clear parent-child relationship

        Java 25 API (JEP 505):
        - StructuredTaskScope.open() - creates a scope
        - scope.fork() - starts subtask in virtual thread
        - scope.join() - waits for subtasks
        - Joiner policies control completion behavior
        """);
  }

  static void basicUsage() throws Exception {
    System.out.println("=== Basic Usage ===");

    // Open a scope - creates a container for subtasks
    try (var scope = StructuredTaskScope.open()) {

      // Fork subtasks - each runs in a virtual thread
      Subtask<String> task1 = scope.fork(() -> {
        Thread.sleep(100);
        return "Result from task 1";
      });

      Subtask<String> task2 = scope.fork(() -> {
        Thread.sleep(150);
        return "Result from task 2";
      });

      Subtask<String> task3 = scope.fork(() -> {
        Thread.sleep(50);
        return "Result from task 3";
      });

      // Wait for all subtasks to complete
      scope.join();

      // All tasks guaranteed to be done here
      System.out.println("  " + task1.get());
      System.out.println("  " + task2.get());
      System.out.println("  " + task3.get());
    }
    // Scope closes - all subtasks definitely finished

    System.out.println();
  }

  static void allSuccessOrThrow() throws Exception {
    System.out.println("=== All Success Or Throw (Default) ===");

    // Default behavior: wait for all, throw if any fails
    System.out.println("  Successful case:");
    try (var scope = StructuredTaskScope.open()) {
      Subtask<String> user = scope.fork(() -> fetchUser("user-123"));
      Subtask<String> order = scope.fork(() -> fetchOrder("order-456"));

      scope.join(); // Waits for all, throws FailedException if any fails

      System.out.println("    User: " + user.get());
      System.out.println("    Order: " + order.get());
    }

    // With a failing task
    System.out.println("\n  With a failing task:");
    try (var scope = StructuredTaskScope.open()) {
      Subtask<String> goodTask = scope.fork(() -> {
        Thread.sleep(200); // Slow task
        System.out.println("    Good task completed (may not see this)");
        return "Good";
      });

      Subtask<String> badTask = scope.fork(() -> {
        Thread.sleep(50);
        throw new RuntimeException("Something went wrong!");
      });

      scope.join();

    } catch (StructuredTaskScope.FailedException e) {
      System.out.println("    Caught FailedException: " + e.getCause().getMessage());
    }

    System.out.println();
  }

  static void anySuccessfulResult() throws Exception {
    System.out.println("=== Any Successful Result (First Wins) ===");

    // Use Joiner to get first successful result
    try (var scope = StructuredTaskScope.open(Joiner.<String>anySuccessfulResultOrThrow())) {

      // Race multiple sources - first to respond wins
      scope.fork(() -> fetchFromServer("server-1", 300));
      scope.fork(() -> fetchFromServer("server-2", 100)); // Fastest
      scope.fork(() -> fetchFromServer("server-3", 200));

      // join() returns the first successful result
      String result = scope.join();
      System.out.println("  First result: " + result);
      System.out.println("  (Other servers' requests were cancelled)");
    }

    System.out.println();
  }

  static void withTimeout() throws Exception {
    System.out.println("=== With Timeout ===");

    Duration timeout = Duration.ofMillis(100);

    try (var scope = StructuredTaskScope.open(
        Joiner.awaitAllSuccessfulOrThrow(),
        cf -> cf.withTimeout(timeout))) {

      scope.fork(() -> {
        Thread.sleep(50); // Fast enough
        return "Fast task";
      });

      scope.fork(() -> {
        Thread.sleep(500); // Too slow - will be interrupted
        return "Slow task";
      });

      scope.join();

    } catch (StructuredTaskScope.FailedException e) {
      System.out.println("  Timeout or failure: " + e.getCause().getClass().getSimpleName());
    }

    System.out.println();
  }

  static void realWorldExample() throws Exception {
    System.out.println("=== Real-World Example: Aggregate API Response ===");

    record UserProfile(String name, String email) {}
    record OrderHistory(int count, double total) {}
    record Recommendations(String[] items) {}
    record DashboardData(UserProfile user, OrderHistory orders, Recommendations recs) {}

    // Fetch dashboard data from multiple services concurrently
    Supplier<DashboardData> fetchDashboard = () -> {
      try (var scope = StructuredTaskScope.open()) {

        // Fork requests to different services
        Subtask<UserProfile> userTask = scope.fork(() -> {
          Thread.sleep(80); // Simulate API call
          return new UserProfile("Alice", "alice@example.com");
        });

        Subtask<OrderHistory> ordersTask = scope.fork(() -> {
          Thread.sleep(120);
          return new OrderHistory(42, 1234.56);
        });

        Subtask<Recommendations> recsTask = scope.fork(() -> {
          Thread.sleep(60);
          return new Recommendations(new String[]{"Widget", "Gadget"});
        });

        scope.join();

        // Aggregate results
        return new DashboardData(
            userTask.get(),
            ordersTask.get(),
            recsTask.get()
        );

      } catch (Exception e) {
        throw new RuntimeException("Failed to load dashboard", e);
      }
    };

    Instant start = Instant.now();
    DashboardData dashboard = fetchDashboard.get();
    Duration elapsed = Duration.between(start, Instant.now());

    System.out.println("  Dashboard loaded in " + elapsed.toMillis() + "ms:");
    System.out.println("    User: " + dashboard.user().name());
    System.out.println("    Orders: " + dashboard.orders().count() + " totaling $" +
        String.format("%.2f", dashboard.orders().total()));
    System.out.println("    Recommendations: " + String.join(", ", dashboard.recs().items()));
    System.out.println("  (Sequential would take ~260ms, parallel took ~120ms)");

    System.out.println();
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        Java 25 API:
        - StructuredTaskScope.open() - default, all must succeed
        - StructuredTaskScope.open(Joiner.anySuccessfulResultOrThrow()) - first wins
        - StructuredTaskScope.open(joiner, config) - with timeout/factory

        DO:
        - Use try-with-resources (scope auto-closes)
        - Call join() before accessing results
        - Use appropriate Joiner for your use case
        - Keep scopes short-lived

        DON'T:
        - Store Subtask references outside the scope
        - Call get() before join()
        - Create very long-lived scopes
        - Fork after calling join()

        Joiner policies:
        - awaitAllSuccessfulOrThrow() - all must succeed (default)
        - anySuccessfulResultOrThrow() - race, first result wins
        - allSuccessfulOrThrow() - collect all results
        - Custom Joiners for advanced scenarios

        Error handling:
        - FailedException wraps the first subtask failure
        - Check Subtask.state() for individual status
        - Use exception() method on failed subtasks
        """);
  }

  // Helper methods simulating I/O operations

  private static String fetchUser(String userId) throws InterruptedException {
    Thread.sleep(100);
    return "User[" + userId + "]";
  }

  private static String fetchOrder(String orderId) throws InterruptedException {
    Thread.sleep(80);
    return "Order[" + orderId + "]";
  }

  private static String fetchFromServer(String server, int latencyMs) throws InterruptedException {
    Thread.sleep(latencyMs);
    return "Response from " + server;
  }

   */
}
