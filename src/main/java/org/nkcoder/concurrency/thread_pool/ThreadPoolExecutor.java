package org.nkcoder.concurrency.thread_pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Custom ThreadPoolExecutor Configuration Example
 *
 * ThreadPoolExecutor is the foundation class for Executors utility methods.
 * Direct instantiation allows fine-grained control over thread pool behavior.
 *
 * Constructor parameters explained:
 * 1. corePoolSize: Minimum threads to keep alive (even if idle)
 * 2. maximumPoolSize: Maximum threads allowed
 * 3. keepAliveTime: How long excess idle threads wait before terminating
 * 4. unit: Time unit for keepAliveTime
 * 5. workQueue: Queue for holding tasks before execution
 * 6. threadFactory: Factory for creating new threads (optional customization)
 * 7. handler: Policy for handling rejected tasks when queue is full
 *
 * Task submission and execution flow:
 * 1. If running threads < corePoolSize: Create new thread
 * 2. If running threads >= corePoolSize: Queue the task
 * 3. If queue is full and threads < maximumPoolSize: Create new thread
 * 4. If queue is full and threads >= maximumPoolSize: Reject (call handler)
 *
 * Work queue types:
 * - ArrayBlockingQueue: Bounded queue (prevents resource exhaustion)
 * - LinkedBlockingQueue: Optionally bounded (unbounded = no max threads)
 * - SynchronousQueue: Direct handoff (no queuing, immediate execution)
 * - PriorityBlockingQueue: Priority-based task execution
 *
 * Rejection policies (when queue full + max threads reached):
 * - AbortPolicy (default): Throws RejectedExecutionException
 * - CallerRunsPolicy: Runs task in caller's thread (throttling mechanism)
 * - DiscardPolicy: Silently drops task
 * - DiscardOldestPolicy: Drops oldest queued task, retries submission
 *
 * Thread factory use cases:
 * - Custom thread naming (debugging)
 * - Setting daemon status
 * - Setting thread priority
 * - Custom exception handlers (setUncaughtExceptionHandler)
 * - Security contexts (SecurityManager)
 *
 * Common pitfalls:
 * 1. Unbounded queue with fixed core size = maximumPoolSize never used
 * 2. Thread.sleep() for waiting completion (use shutdown + awaitTermination)
 * 3. Forgetting to shutdown (threads keep JVM alive)
 * 4. Silent task failures (check Future results!)
 *
 * Java 21 modern alternatives:
 * - Virtual threads: Executors.newVirtualThreadPerTaskExecutor()
 *   (No complex sizing needed, scales to millions of threads)
 * - Structured Concurrency: StructuredTaskScope (preview)
 *   (Automatic cleanup, better error handling)
 *
 * This example demonstrates:
 * - Custom thread factory for thread creation logging
 * - Custom rejection handler for discarded tasks
 * - Bounded queue causing rejections (30 tasks, 5 threads, 10 queue slots)
 * - Proper shutdown pattern
 */
public class ThreadPoolExecutor {

  public static void main(String[] args) throws InterruptedException {
    // Custom ThreadPoolExecutor with explicit configuration
    // corePoolSize=5, maxPoolSize=5, queue capacity=10
    // Total capacity: 5 executing + 10 queued = 15 tasks
    // Submitting 30 tasks will cause 15 rejections
    java.util.concurrent.ThreadPoolExecutor threadPoolExecutor =
        new java.util.concurrent.ThreadPoolExecutor(
            5,  // Core pool size (minimum threads)
            5,  // Maximum pool size (max threads, same as core here)
            0,  // Keep alive time (0 = excess threads die immediately)
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10),  // Bounded queue (10 slots)

            // Custom ThreadFactory: logs thread creation
            (r) -> {
              Thread thread = new Thread(r);
              System.out.println("created thread: " + thread);
              return thread;
            },

            // Custom RejectedExecutionHandler: logs rejected tasks
            // When queue is full (10) and all threads busy (5), reject new tasks
            ((r, executor) -> System.out.println("task is discarded: " + r.toString()))
        );

    // Submit 30 tasks (more than capacity of 15)
    // Expected: 15 tasks execute/queue, 15 tasks rejected
    for (int i = 0; i < 30; i++) {
      threadPoolExecutor.submit(() -> {
        System.out.println("I'm running in thread: " + Thread.currentThread().getName());
        try {
          Thread.sleep(500);  // Simulate work
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();  // Restore interrupt status
          System.err.println("Task interrupted: " + e.getMessage());
        }
      });
    }

    // Proper shutdown pattern
    threadPoolExecutor.shutdown();

    // Wait up to 20 seconds for all tasks to complete
    if (!threadPoolExecutor.awaitTermination(20, TimeUnit.SECONDS)) {
      System.err.println("Tasks did not complete in time, forcing shutdown");
      threadPoolExecutor.shutdownNow();
    }

    System.out.println("All tasks completed or timed out");
  }
}

/*
 * Java 21 Alternative (Virtual Threads):
 *
 * try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
 *     for (int i = 0; i < 30; i++) {
 *         executor.submit(() -> {
 *             System.out.println("Running in virtual thread: " + Thread.currentThread());
 *             Thread.sleep(500);
 *         });
 *     }
 * } // Auto shutdown
 *
 * Benefits: No queue sizing, no thread pool sizing, scales automatically
 */
