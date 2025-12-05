package org.nkcoder.concurrency.executors;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ExecutorService: Thread pool management for concurrent task execution.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Thread pools avoid the overhead of creating threads for each task</li>
 *   <li>Different executor types for different use cases</li>
 *   <li>Proper shutdown is critical to avoid resource leaks</li>
 *   <li>Future allows retrieving results and handling exceptions</li>
 * </ul>
 *
 * <p>Interview tip: Know when to use each executor type and how to shut down properly.
 */
public class ExecutorServiceExample {

  static void main(String[] args) throws Exception {
    executorTypes();
    executeVsSubmit();
    futureBasics();
    invokeAllAndAny();
    properShutdown();
    threadPoolExecutorConfig();
    bestPractices();
  }

  static void executorTypes() throws Exception {
    System.out.println("=== Executor Types ===");

    // 1. Fixed Thread Pool - fixed number of threads
    // Use for: Known, bounded workload; CPU-bound tasks
    try (ExecutorService fixed = Executors.newFixedThreadPool(3)) {
      System.out.println("  FixedThreadPool(3): Always 3 threads");
      for (int i = 0; i < 5; i++) {
        int taskId = i;
        fixed.submit(() -> {
          System.out.println("    Task " + taskId + " on " + Thread.currentThread().getName());
        });
      }
    }

    Thread.sleep(100);

    // 2. Cached Thread Pool - creates threads as needed, reuses idle ones
    // Use for: Many short-lived tasks; I/O-bound workload
    // Warning: Can create unbounded threads if tasks are slow!
    try (ExecutorService cached = Executors.newCachedThreadPool()) {
      System.out.println("\n  CachedThreadPool: Creates threads as needed");
      for (int i = 0; i < 5; i++) {
        int taskId = i;
        cached.submit(() -> {
          System.out.println("    Task " + taskId + " on " + Thread.currentThread().getName());
        });
      }
    }

    Thread.sleep(100);

    // 3. Single Thread Executor - one thread, tasks execute sequentially
    // Use for: Tasks that must not run concurrently; ordered execution
    try (ExecutorService single = Executors.newSingleThreadExecutor()) {
      System.out.println("\n  SingleThreadExecutor: Sequential execution");
      for (int i = 0; i < 3; i++) {
        int taskId = i;
        single.submit(() -> {
          System.out.println("    Task " + taskId + " (order guaranteed)");
        });
      }
    }

    Thread.sleep(100);

    // 4. Virtual Thread Per Task - Java 21+, best for I/O-bound
    // Use for: High-throughput I/O; blocking operations
    try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
      System.out.println("\n  VirtualThreadPerTaskExecutor: Lightweight threads");
      for (int i = 0; i < 3; i++) {
        int taskId = i;
        virtual.submit(() -> {
          System.out.println("    Task " + taskId + " virtual=" +
              Thread.currentThread().isVirtual());
        });
      }
    }

    System.out.println();
  }

  static void executeVsSubmit() {
    System.out.println("=== execute() vs submit() ===");

    try (ExecutorService executor = Executors.newSingleThreadExecutor()) {

      // execute() - fire and forget, no return value
      // Exceptions are handled by UncaughtExceptionHandler
      executor.execute(() -> {
        System.out.println("  execute(): No return value");
      });

      // submit(Runnable) - returns Future<?> (result is null)
      Future<?> future1 = executor.submit(() -> {
        System.out.println("  submit(Runnable): Returns Future<?>");
      });

      // submit(Callable) - returns Future<T> with result
      Future<String> future2 = executor.submit(() -> {
        return "  submit(Callable): Returns Future<T>";
      });

      try {
        future1.get(); // blocks until complete, returns null
        System.out.println(future2.get()); // blocks, returns result
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    System.out.println("""

        Key difference:
        - execute(): void, exceptions go to UncaughtExceptionHandler
        - submit(): Future, exceptions are captured in Future.get()
        """);
  }

  static void futureBasics() throws Exception {
    System.out.println("=== Future Basics ===");

    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

      // Submit a task that takes time
      Future<Integer> future = executor.submit(() -> {
        Thread.sleep(100);
        return 42;
      });

      // Check status (non-blocking)
      System.out.println("  isDone before: " + future.isDone());
      System.out.println("  isCancelled: " + future.isCancelled());

      // Get result (blocking)
      Integer result = future.get();
      System.out.println("  Result: " + result);
      System.out.println("  isDone after: " + future.isDone());

      // Get with timeout
      Future<String> slowTask = executor.submit(() -> {
        Thread.sleep(5000);
        return "slow";
      });

      try {
        slowTask.get(100, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        System.out.println("  Timeout! Cancelling task...");
        slowTask.cancel(true); // interrupt if running
        System.out.println("  Cancelled: " + slowTask.isCancelled());
      }

      // Exception handling
      Future<String> failingTask = executor.submit(() -> {
        throw new RuntimeException("Task failed!");
      });

      try {
        failingTask.get();
      } catch (ExecutionException e) {
        System.out.println("  Exception caught: " + e.getCause().getMessage());
      }
    }

    System.out.println();
  }

  static void invokeAllAndAny() throws Exception {
    System.out.println("=== invokeAll() and invokeAny() ===");

    List<Callable<String>> tasks = List.of(
        () -> { Thread.sleep(100); return "Task A"; },
        () -> { Thread.sleep(50);  return "Task B"; },
        () -> { Thread.sleep(150); return "Task C"; }
    );

    try (ExecutorService executor = Executors.newFixedThreadPool(3)) {

      // invokeAll - wait for ALL tasks to complete
      System.out.println("  invokeAll (waits for all):");
      List<Future<String>> futures = executor.invokeAll(tasks);
      for (Future<String> f : futures) {
        System.out.println("    " + f.get());
      }

      // invokeAny - return FIRST successful result, cancel others
      System.out.println("\n  invokeAny (first wins):");
      String firstResult = executor.invokeAny(tasks);
      System.out.println("    First result: " + firstResult);
    }

    System.out.println();
  }

  static void properShutdown() throws Exception {
    System.out.println("=== Proper Shutdown ===");

    ExecutorService executor = Executors.newFixedThreadPool(2);

    // Submit some tasks
    for (int i = 0; i < 5; i++) {
      int taskId = i;
      executor.submit(() -> {
        try {
          Thread.sleep(100);
          System.out.println("    Task " + taskId + " completed");
        } catch (InterruptedException e) {
          System.out.println("    Task " + taskId + " interrupted");
          Thread.currentThread().interrupt();
        }
      });
    }

    // Proper shutdown pattern (from Java docs)
    System.out.println("  Initiating shutdown...");
    executor.shutdown(); // Stop accepting new tasks

    try {
      // Wait for existing tasks to complete
      if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
        System.out.println("  Tasks didn't finish, forcing shutdown...");
        executor.shutdownNow(); // Cancel running tasks

        // Wait again
        if (!executor.awaitTermination(500, TimeUnit.MILLISECONDS)) {
          System.out.println("  Executor did not terminate!");
        }
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    System.out.println("  isShutdown: " + executor.isShutdown());
    System.out.println("  isTerminated: " + executor.isTerminated());

    // Submitting after shutdown throws RejectedExecutionException
    try {
      executor.submit(() -> System.out.println("This won't run"));
    } catch (RejectedExecutionException e) {
      System.out.println("  Task rejected after shutdown");
    }

    System.out.println("""

        Shutdown steps:
        1. shutdown() - stop accepting new tasks
        2. awaitTermination() - wait for running tasks
        3. shutdownNow() - interrupt if timeout exceeded

        Java 19+: Use try-with-resources (AutoCloseable)
        """);
  }

  static void threadPoolExecutorConfig() {
    System.out.println("=== ThreadPoolExecutor Configuration ===");

    // Direct ThreadPoolExecutor for fine-grained control
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    System.out.println("  Core pool size: " + executor.getCorePoolSize());
    System.out.println("  Max pool size: " + executor.getMaximumPoolSize());
    System.out.println("  Active count: " + executor.getActiveCount());
    System.out.println("  Pool size: " + executor.getPoolSize());
    System.out.println("  Queue size: " + executor.getQueue().size());

    // Submit tasks and check again
    for (int i = 0; i < 10; i++) {
      executor.submit(() -> {
        try { Thread.sleep(50); } catch (InterruptedException e) {}
      });
    }

    System.out.println("\n  After submitting 10 tasks:");
    System.out.println("  Active count: " + executor.getActiveCount());
    System.out.println("  Queue size: " + executor.getQueue().size());
    System.out.println("  Completed tasks: " + executor.getCompletedTaskCount());

    executor.shutdown();

    System.out.println("""

        ThreadPoolExecutor parameters:
        - corePoolSize: Threads kept alive even when idle
        - maxPoolSize: Maximum threads allowed
        - keepAliveTime: How long excess threads wait before terminating
        - workQueue: Queue for tasks when all threads busy
        - threadFactory: How to create threads
        - rejectedHandler: What to do when queue is full
        """);
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        Choosing an Executor:
        +-------------------------+----------------------------------+
        | Use Case                | Executor                         |
        +-------------------------+----------------------------------+
        | CPU-bound, fixed work   | newFixedThreadPool(nCPUs)        |
        | I/O-bound, many tasks   | newVirtualThreadPerTaskExecutor()|
        | Sequential execution    | newSingleThreadExecutor()        |
        | Scheduled/periodic      | newScheduledThreadPool()         |
        | Short-lived tasks       | newCachedThreadPool() (careful!) |
        +-------------------------+----------------------------------+

        DO:
        - Always shut down executors properly
        - Use try-with-resources (Java 19+)
        - Size fixed pools based on task type:
          - CPU-bound: Runtime.getRuntime().availableProcessors()
          - I/O-bound: Higher, depends on blocking ratio
        - Handle exceptions from Future.get()
        - Use timeouts to avoid infinite waits

        DON'T:
        - Create executor per task (defeats pooling purpose)
        - Use CachedThreadPool for slow/blocking tasks
        - Ignore InterruptedException (restore interrupt status)
        - Forget to shut down (resource leak)

        Java 21+ recommendation:
        - Use virtual threads for I/O-bound work
        - Use platform thread pools for CPU-bound work
        """);
  }
}
