package org.nkcoder.concurrency.executors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CompletableFuture: Composable asynchronous programming in Java.
 *
 * <p>Key concepts:
 *
 * <ul>
 *   <li>Non-blocking async operations with callbacks
 *   <li>Chainable transformations (like Stream for async)
 *   <li>Combine multiple futures
 *   <li>Flexible exception handling
 * </ul>
 *
 * <p>Interview tip: Know the difference between thenApply vs thenCompose, and handle vs exceptionally.
 */
public class CompletableFutureExample {

    static void main(String[] args) throws Exception {
        creatingFutures();
        chainingOperations();
        thenApplyVsThenCompose();
        combiningFutures();
        exceptionHandling();
        asyncVsSync();
        realWorldExample();
        bestPractices();
    }

    static void creatingFutures() throws Exception {
        System.out.println("=== Creating CompletableFutures ===");

        // 1. supplyAsync - async task with return value
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Result from supplyAsync";
        });

        // 2. runAsync - async task without return value
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            sleep(50);
            System.out.println("  runAsync completed");
        });

        // 3. completedFuture - already completed with value
        CompletableFuture<String> future3 = CompletableFuture.completedFuture("Immediate");

        // 4. Manual completion
        CompletableFuture<String> future4 = new CompletableFuture<>();
        // Complete from another thread or later
        future4.complete("Manually completed");

        // 5. With custom executor
        try (ExecutorService customExecutor = Executors.newFixedThreadPool(2)) {
            CompletableFuture<String> future5 = CompletableFuture.supplyAsync(() -> "Custom executor", customExecutor);
            System.out.println("  Custom executor: " + future5.get());
        }

        // Get results
        System.out.println("  supplyAsync: " + future1.get());
        future2.get(); // wait for completion
        System.out.println("  completedFuture: " + future3.get());
        System.out.println("  manual: " + future4.get());

        System.out.println();
    }

    static void chainingOperations() throws Exception {
        System.out.println("=== Chaining Operations ===");

        // thenApply - transform result (like map)
        CompletableFuture<Integer> lengthFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApply(s -> s + " World") // String -> String
                .thenApply(String::length); // String -> Integer

        System.out.println("  thenApply chain: " + lengthFuture.get());

        // thenAccept - consume result (no return)
        CompletableFuture.supplyAsync(() -> "Consumed")
                .thenAccept(s -> System.out.println("  thenAccept: " + s))
                .get();

        // thenRun - run action after completion (ignores result)
        CompletableFuture.supplyAsync(() -> "Ignored")
                .thenRun(() -> System.out.println("  thenRun: Runs after, ignores result"))
                .get();

        System.out.println();
    }

    static void thenApplyVsThenCompose() throws Exception {
        System.out.println("=== thenApply vs thenCompose ===");

        // Simulating async service calls
        java.util.function.Function<String, CompletableFuture<String>> asyncService =
                input -> CompletableFuture.supplyAsync(() -> {
                    sleep(50);
                    return "Processed: " + input;
                });

        // thenApply with async function -> nested CompletableFuture (bad)
        CompletableFuture<CompletableFuture<String>> nested =
                CompletableFuture.supplyAsync(() -> "data").thenApply(asyncService); // Returns CF<CF<String>>!

        // thenCompose flattens (like flatMap)
        CompletableFuture<String> flat =
                CompletableFuture.supplyAsync(() -> "data").thenCompose(asyncService); // Returns CF<String>

        System.out.println("  thenApply (nested): " + nested.get().get());
        System.out.println("  thenCompose (flat): " + flat.get());

        System.out.println("""

        Rule of thumb:
        - thenApply: sync function (T -> U)
        - thenCompose: async function (T -> CompletableFuture<U>)

        Like Stream:
        - thenApply ~ map
        - thenCompose ~ flatMap
        """);
    }

    static void combiningFutures() throws Exception {
        System.out.println("=== Combining Futures ===");

        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Hello";
        });

        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(80);
            return "World";
        });

        // thenCombine - combine two futures when both complete
        CompletableFuture<String> combined = future1.thenCombine(future2, (s1, s2) -> s1 + " " + s2);
        System.out.println("  thenCombine: " + combined.get());

        // allOf - wait for ALL futures (returns Void)
        CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> {
            sleep(50);
            return "A";
        });
        CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> {
            sleep(30);
            return "B";
        });
        CompletableFuture<String> c = CompletableFuture.supplyAsync(() -> {
            sleep(40);
            return "C";
        });

        CompletableFuture<Void> allDone = CompletableFuture.allOf(a, b, c);
        allDone.get(); // wait for all

        // Now safe to get results
        System.out.println("  allOf results: " + a.get() + ", " + b.get() + ", " + c.get());

        // anyOf - complete when ANY future completes
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return "Slow";
        });
        CompletableFuture<String> fast = CompletableFuture.supplyAsync(() -> {
            sleep(20);
            return "Fast";
        });

        CompletableFuture<Object> anyDone = CompletableFuture.anyOf(slow, fast);
        System.out.println("  anyOf (first): " + anyDone.get());

        System.out.println();
    }

    static void exceptionHandling() throws Exception {
        System.out.println("=== Exception Handling ===");

        // exceptionally - recover from exception
        CompletableFuture<String> recovered = CompletableFuture.supplyAsync(() -> {
                    if (true) throw new RuntimeException("Oops!");
                    return "OK";
                })
                .exceptionally(ex -> "Recovered from: " + ex.getMessage());

        System.out.println("  exceptionally: " + recovered.get());

        // handle - process result OR exception
        CompletableFuture<String> handled = CompletableFuture.supplyAsync(() -> {
                    if (Math.random() > 0.5) throw new RuntimeException("Random fail");
                    return "Success";
                })
                .handle((result, ex) -> {
                    if (ex != null) return "Handled error: " + ex.getMessage();
                    return "Result: " + result;
                });

        System.out.println("  handle: " + handled.get());

        // whenComplete - side effect, doesn't transform
        CompletableFuture<String> logged = CompletableFuture.supplyAsync(() -> "Data")
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.out.println("  whenComplete: Error - " + ex.getMessage());
                    } else {
                        System.out.println("  whenComplete: Success - " + result);
                    }
                });
        logged.get();

        System.out.println("""

        Exception handling methods:
        +----------------+-------------+------------------+
        | Method         | Can recover | Transforms value |
        +----------------+-------------+------------------+
        | exceptionally  | Yes         | Only on error    |
        | handle         | Yes         | Always           |
        | whenComplete   | No          | No (side effect) |
        +----------------+-------------+------------------+
        """);
    }

    static void asyncVsSync() throws Exception {
        System.out.println("=== Async vs Sync Methods ===");

        // Most methods have 3 variants:
        // - thenApply: runs in same thread as previous stage
        // - thenApplyAsync: runs in ForkJoinPool.commonPool()
        // - thenApplyAsync(fn, executor): runs in custom executor

        CompletableFuture.supplyAsync(() -> {
                    System.out.println("  Supply: " + Thread.currentThread().getName());
                    return "data";
                })
                .thenApply(s -> {
                    System.out.println("  thenApply: " + Thread.currentThread().getName());
                    return s.toUpperCase();
                })
                .thenApplyAsync(s -> {
                    System.out.println(
                            "  thenApplyAsync: " + Thread.currentThread().getName());
                    return s + "!";
                })
                .get();

        System.out.println("""

        Thread behavior:
        - thenApply: May run in completing thread OR calling thread
        - thenApplyAsync: Always runs in ForkJoinPool (or custom executor)

        Use Async variants when:
        - Operation is slow/blocking
        - Need specific executor
        - Want guaranteed async execution
        """);
    }

    static void realWorldExample() throws Exception {
        System.out.println("=== Real-World Example: API Aggregation ===");

        record User(String name, String email) {}
        record Order(int count, double total) {}
        record Dashboard(User user, Order order, String recommendation) {}

        // Simulate async service calls
        CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(() -> {
            sleep(80);
            return new User("Alice", "alice@example.com");
        });

        CompletableFuture<Order> orderFuture = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return new Order(5, 299.99);
        });

        CompletableFuture<String> recsFuture = CompletableFuture.supplyAsync(() -> {
            sleep(60);
            return "Widget Pro";
        });

        // Combine all results
        long start = System.currentTimeMillis();

        CompletableFuture<Dashboard> dashboardFuture = userFuture
                .thenCombine(orderFuture, (user, order) -> new Object[] {user, order})
                .thenCombine(recsFuture, (arr, rec) -> new Dashboard((User) arr[0], (Order) arr[1], rec));

        Dashboard dashboard = dashboardFuture.get();
        long elapsed = System.currentTimeMillis() - start;

        System.out.println("  Loaded in " + elapsed + "ms (parallel, not 240ms sequential)");
        System.out.println("  User: " + dashboard.user().name());
        System.out.println("  Orders: " + dashboard.order().count());
        System.out.println("  Recommendation: " + dashboard.recommendation());

        System.out.println();
    }

    static void bestPractices() {
        System.out.println("=== Best Practices ===");

        System.out.println("""
        DO:
        - Use supplyAsync/runAsync to start async chains
        - Use thenCompose for async-returning functions (avoid nesting)
        - Handle exceptions with handle() or exceptionally()
        - Use custom executor for blocking operations
        - Use allOf/anyOf for multiple independent futures

        DON'T:
        - Call get() too early (blocks, defeats async purpose)
        - Ignore exceptions (use handle/exceptionally)
        - Use common pool for blocking I/O (can starve other tasks)
        - Create deeply nested callbacks (hard to read)

        Timeouts (Java 9+):
        - orTimeout(duration): Fails with TimeoutException
        - completeOnTimeout(value, duration): Completes with default

        Java 21+ alternative:
        - For simple parallel I/O, virtual threads may be cleaner
        - CompletableFuture still good for complex async workflows

        Common patterns:
        - API aggregation: allOf + individual gets
        - Fallback: exceptionally or handle
        - Racing: anyOf for first response
        - Pipeline: chain of thenApply/thenCompose
        """);
    }

    private static void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
