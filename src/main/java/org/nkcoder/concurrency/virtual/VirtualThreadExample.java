package org.nkcoder.concurrency.virtual;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * Virtual Threads (Java 21+): Lightweight threads for high-throughput I/O.
 *
 * <ul>
 *   <li>Millions of virtual threads can run concurrently
 *   <li>Ideal for I/O-bound tasks (HTTP, database, file I/O)
 *   <li>NOT for CPU-bound tasks (use platform threads/ForkJoinPool)
 *   <li>Managed by JVM, not OS - minimal memory footprint
 * </ul>
 *
 * <p>Java 25: Virtual threads are production-ready and the recommended approach for concurrent I/O
 * operations.
 */
public class VirtualThreadExample {

  static void main(String[] args) throws Exception {
    whatAreVirtualThreads();
    creatingVirtualThreads();
    virtualThreadExecutor();
    scalabilityDemo();
    virtualVsPlatformThreads();
    bestPractices();
  }

  static void whatAreVirtualThreads() {
    System.out.println("=== What Are Virtual Threads? ===");

    System.out.println(
        """
        Virtual threads are lightweight threads managed by the JVM:

        Platform Threads (traditional):
        - 1:1 mapping to OS threads
        - ~1MB stack size each
        - Limited to thousands
        - Expensive context switching

        Virtual Threads (Java 21+):
        - Many-to-few mapping to OS threads (carrier threads)
        - ~1KB initial stack, grows as needed
        - Can have millions running
        - Cheap to create and block

        Use Virtual Threads for:
        - HTTP requests, database queries, file I/O
        - Any blocking operation that waits for external resources

        DON'T use Virtual Threads for:
        - CPU-intensive calculations (use platform threads)
        - Tasks holding locks for long periods
        """);
  }

  static void creatingVirtualThreads() throws Exception {
    System.out.println("=== Creating Virtual Threads ===");

    // Method 1: Thread.startVirtualThread() - fire and forget
    Thread vt1 =
        Thread.startVirtualThread(
            () -> {
              System.out.println("  [1] Running in: " + Thread.currentThread());
            });
    vt1.join();

    // Method 2: Thread.ofVirtual() builder - more control
    Thread vt2 =
        Thread.ofVirtual()
            .name("my-virtual-thread")
            .start(
                () -> {
                  System.out.println("  [2] Named thread: " + Thread.currentThread().getName());
                });
    vt2.join();

    // Method 3: Unstarted thread (start manually)
    Thread vt3 =
        Thread.ofVirtual()
            .name("manual-start")
            .unstarted(
                () -> {
                  System.out.println("  [3] Manually started: " + Thread.currentThread().getName());
                });
    vt3.start();
    vt3.join();

    // Check if thread is virtual
    Thread current = Thread.currentThread();
    System.out.println("\n  Main thread is virtual? " + current.isVirtual()); // false
    System.out.println("  vt1 is virtual? " + vt1.isVirtual());

    System.out.println();
  }

  static void virtualThreadExecutor() throws Exception {
    System.out.println("=== Virtual Thread Executor (Recommended) ===");

    // The recommended way: ExecutorService with virtual threads
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

      // Submit tasks - each gets its own virtual thread
      var future1 =
          executor.submit(
              () -> {
                simulateIO("Task 1", 100);
                return "Result 1";
              });

      var future2 =
          executor.submit(
              () -> {
                simulateIO("Task 2", 150);
                return "Result 2";
              });

      var future3 =
          executor.submit(
              () -> {
                simulateIO("Task 3", 50);
                return "Result 3";
              });

      // Gather results
      System.out.println("  " + future1.get());
      System.out.println("  " + future2.get());
      System.out.println("  " + future3.get());
    }
    // ExecutorService auto-closes with try-with-resources (Java 19+)

    System.out.println(
        """

        Why use newVirtualThreadPerTaskExecutor()?
        - Creates a new virtual thread for each task
        - No thread pool sizing needed (auto-scales)
        - Implements AutoCloseable for easy cleanup
        - The standard pattern for virtual thread usage
        """);
  }

  static void scalabilityDemo() throws Exception {
    System.out.println("=== Scalability Demo ===");

    int taskCount = 10_000;

    // Virtual threads - can easily handle 10,000+ concurrent tasks
    System.out.println("  Starting " + taskCount + " virtual threads...");
    Instant start = Instant.now();

    /**
     *
     *
     * <pre>
     * Leaving the try-with-resources block closes the executor. The ExecutorService returned by
     * Executors.newVirtualThreadPerTaskExecutor implements AutoCloseable with a close() that:
     * - Stops accepting new tasks
     * - Waits for all already-submitted tasks to finish
     * - blocks the current thread until termination or interruption
     * </pre>
     */
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      IntStream.range(0, taskCount)
          .forEach(i -> executor.submit(() -> simulateIO("Task #" + i, 100)));
    }

    Duration duration = Duration.between(start, Instant.now());
    System.out.println("  Completed " + taskCount + " tasks in " + duration.toMillis() + "ms");
    System.out.println("  (With platform threads, this would need 10,000 OS threads!)");

    System.out.println();
  }

  static void virtualVsPlatformThreads() throws Exception {
    System.out.println("=== Virtual vs Platform Threads ===");

    int taskCount = 1000;
    int ioDelayMs = 50;

    // Platform threads (limited pool)
    System.out.println("  Platform threads (pool of 100):");
    Instant start1 = Instant.now();
    try (ExecutorService executor = Executors.newFixedThreadPool(100)) {
      IntStream.range(0, taskCount)
          .forEach(i -> executor.submit(() -> simulateIO("Task #" + i, ioDelayMs)));
    }
    System.out.println("    Time: " + Duration.between(start1, Instant.now()).toMillis() + "ms");

    // Virtual threads (unlimited)
    System.out.println("  Virtual threads (unlimited):");
    Instant start2 = Instant.now();
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      IntStream.range(0, taskCount)
          .forEach(i -> executor.submit(() -> simulateIO("Task #" + i, ioDelayMs)));
    }
    System.out.println("    Time: " + Duration.between(start2, Instant.now()).toMillis() + "ms");

    System.out.println(
        """

        Virtual threads excel when:
        - Many concurrent tasks
        - Tasks spend time waiting (I/O)
        - Platform threads would be idle

        Platform threads are better when:
        - CPU-bound computation
        - Need thread affinity
        - Using native code with thread-local state
        """);
  }

  static void bestPractices() {
    System.out.println("=== Best Practices for Virtual Threads ===");

    System.out.println(
        """
        DO:
        - Use for I/O-bound tasks (HTTP, DB, files)
        - Use newVirtualThreadPerTaskExecutor()
        - Write blocking code naturally (no async callbacks)
        - Let virtual threads block - it's cheap!

        DON'T:
        - Use for CPU-intensive work (defeats the purpose)
        - Pool virtual threads (they're cheap, create new ones)
        - Use ThreadLocal carelessly (prefer ScopedValue)
        - Hold locks while doing I/O (pins carrier thread)

        Migration from platform threads:
        1. Replace Executors.newFixedThreadPool(n) with
           Executors.newVirtualThreadPerTaskExecutor()
        2. Replace ThreadLocal with ScopedValue where possible
        3. Review synchronized blocks (avoid blocking I/O inside)
        4. Test thoroughly - behavior is mostly the same

        Performance tips:
        - Virtual threads make blocking cheap, not free
        - Each blocked virtual thread still uses memory
        - Monitor with: -Djdk.virtualThreadScheduler.parallelism=N
        """);
  }

  private static void simulateIO(String task, int delayMs) {
    try {
      System.out.println("    " + task + " starting on " + Thread.currentThread());
      Thread.sleep(delayMs);
      System.out.println("    " + task + " completed");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
