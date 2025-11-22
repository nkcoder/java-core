package org.nkcoder.j8.concurrency.thread_pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FixedThreadPool Example
 *
 * A FixedThreadPool is an executor with a fixed number of threads that reuse threads
 * from a shared unbounded queue.
 *
 * Key characteristics:
 * 1. Fixed number of threads (specified at creation)
 * 2. Threads are reused for multiple tasks
 * 3. Uses unbounded LinkedBlockingQueue for pending tasks
 * 4. If all threads are busy, new tasks wait in queue
 * 5. Threads remain alive even when idle (until shutdown)
 *
 * Internally equivalent to:
 * new ThreadPoolExecutor(n, n, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>())
 *
 * Advantages:
 * ✓ Limits resource usage (max N threads)
 * ✓ Better than creating new threads for each task (thread reuse)
 * ✓ Simple API for common use case
 * ✓ Automatic thread lifecycle management
 *
 * Disadvantages:
 * ✗ Unbounded queue can grow indefinitely (OutOfMemoryError risk)
 * ✗ Cannot adapt to varying load (fixed size)
 * ✗ Idle threads consume memory even when not needed
 *
 * When to use:
 * ✓ Known, bounded workload
 * ✓ Consistent processing requirements
 * ✓ When you want to limit concurrent task execution
 *
 * Alternatives:
 * - CachedThreadPool: For short-lived tasks, variable load
 * - Custom ThreadPoolExecutor: For fine-grained control (core/max threads, queue size)
 * - ForkJoinPool: For divide-and-conquer parallel tasks
 *
 * Lifecycle:
 * 1. shutdown() - No new tasks accepted, but existing tasks complete
 * 2. shutdownNow() - Attempts to stop all tasks, returns un started tasks
 * 3. awaitTermination() - Waits for shutdown to complete
 *
 * This example demonstrates:
 * - Creating a pool with 5 threads
 * - Submitting 20 tasks (more than pool size)
 * - Tasks are queued and executed as threads become available
 * - try-with-resources ensures proper cleanup (calls shutdown())
 */
public class FixThreadPool {
  public static void main(String[] args) {
    // Create a fixed thread pool with 5 threads
    // try-with-resources automatically calls close() which invokes shutdown()
    try (ExecutorService executorService = Executors.newFixedThreadPool(5)) {

      // Submit 20 tasks to a pool of 5 threads
      // First 5 tasks start immediately, remaining 15 wait in queue
      for (int i = 0; i < 20; i++) {
        executorService.submit(
            () -> {
              try {
                // Each task takes 1 second
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("I'm done.");
            });
      }

      // Initiates orderly shutdown: no new tasks accepted, existing tasks complete
      // Note: try-with-resources will call this automatically, but shown here for clarity
      executorService.shutdown();

      // Total execution time: ~4 seconds (20 tasks / 5 threads * 1 second each)
      // Without thread pool: would need 20 threads or take 20 seconds sequentially
    }
  }
}
