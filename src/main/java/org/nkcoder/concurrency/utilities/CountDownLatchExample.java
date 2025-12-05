package org.nkcoder.concurrency.utilities;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch: One-time synchronization barrier that releases waiting threads.
 *
 * <p><strong>Java 25 Note:</strong> Still useful for one-time synchronization events. When
 * structured concurrency (preview) becomes stable, it may offer a cleaner alternative for
 * coordinating subtasks.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Initialized with a count</li>
 *   <li>Threads call await() to wait until count reaches zero</li>
 *   <li>Other threads call countDown() to decrement</li>
 *   <li>Once zero, all waiting threads released (cannot reset)</li>
 * </ul>
 *
 * <p>Interview tip: Know difference between CountDownLatch (one-time) and
 * CyclicBarrier (reusable). Classic use case: wait for N tasks to complete.
 */
public class CountDownLatchExample {

  static void main(String[] args) throws Exception {
    basicUsage();
    startingGun();
    waitForCompletion();
    withTimeout();
    multipleAwaiters();
    bestPractices();
  }

  static void basicUsage() throws Exception {
    System.out.println("=== Basic Usage ===");

    // Count of 3 - need 3 countDown() calls to release
    CountDownLatch latch = new CountDownLatch(3);

    System.out.println("  Initial count: " + latch.getCount());

    // Thread that waits
    Thread waiter = new Thread(() -> {
      try {
        System.out.println("    Waiter: Waiting for latch...");
        latch.await();
        System.out.println("    Waiter: Latch opened! Proceeding...");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
    waiter.start();

    // Count down from other threads
    for (int i = 3; i > 0; i--) {
      Thread.sleep(100);
      latch.countDown();
      System.out.println("  CountDown! Remaining: " + latch.getCount());
    }

    waiter.join();

    // Extra countDown() has no effect
    latch.countDown();
    System.out.println("  Count after extra countDown: " + latch.getCount());

    System.out.println("""

        Key points:
        - await() blocks until count reaches 0
        - countDown() decrements (never blocks)
        - Count cannot go below 0
        - Cannot reset - one-time use only
        """);
  }

  static void startingGun() throws Exception {
    System.out.println("=== Starting Gun Pattern ===");

    // All threads wait for signal to start simultaneously
    CountDownLatch startSignal = new CountDownLatch(1);
    int numRunners = 5;

    System.out.println("  Runners getting ready...");

    try (ExecutorService executor = Executors.newFixedThreadPool(numRunners)) {
      for (int i = 1; i <= numRunners; i++) {
        int runnerId = i;
        executor.submit(() -> {
          try {
            System.out.println("    Runner " + runnerId + " ready");
            startSignal.await(); // Wait for starting gun
            System.out.println("    Runner " + runnerId + " started at " +
                System.currentTimeMillis() % 10000);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
      }

      Thread.sleep(200); // Let all runners get ready

      System.out.println("\n  BANG! (Starting gun fired)");
      startSignal.countDown(); // All runners start at once

      Thread.sleep(100);
    }

    System.out.println();
  }

  static void waitForCompletion() throws Exception {
    System.out.println("=== Wait for All Tasks to Complete ===");

    int numTasks = 5;
    CountDownLatch completionLatch = new CountDownLatch(numTasks);

    System.out.println("  Starting " + numTasks + " tasks...");
    long start = System.currentTimeMillis();

    try (ExecutorService executor = Executors.newFixedThreadPool(numTasks)) {
      for (int i = 1; i <= numTasks; i++) {
        int taskId = i;
        int duration = 100 + (i * 50); // Variable durations
        executor.submit(() -> {
          try {
            Thread.sleep(duration);
            System.out.println("    Task " + taskId + " completed (took " + duration + "ms)");
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } finally {
            completionLatch.countDown(); // ALWAYS count down, even on failure
          }
        });
      }

      // Wait for all tasks
      completionLatch.await();
    }

    long elapsed = System.currentTimeMillis() - start;
    System.out.println("  All tasks completed in " + elapsed + "ms (parallel, not sequential)");

    System.out.println();
  }

  static void withTimeout() throws Exception {
    System.out.println("=== Await with Timeout ===");

    CountDownLatch latch = new CountDownLatch(3);

    // Only 2 countdowns will happen
    Thread.startVirtualThread(() -> {
      try {
        Thread.sleep(50);
        latch.countDown();
        System.out.println("    First countDown");

        Thread.sleep(50);
        latch.countDown();
        System.out.println("    Second countDown");
        // Third never happens!
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    System.out.println("  Waiting with 200ms timeout...");
    boolean completed = latch.await(200, TimeUnit.MILLISECONDS);

    if (completed) {
      System.out.println("  All counted down in time");
    } else {
      System.out.println("  Timeout! Count remaining: " + latch.getCount());
    }

    System.out.println("""

        await(timeout, unit) returns:
        - true: count reached zero
        - false: timeout elapsed before zero
        """);
  }

  static void multipleAwaiters() throws Exception {
    System.out.println("=== Multiple Threads Awaiting ===");

    CountDownLatch latch = new CountDownLatch(1);

    // Multiple threads can await on same latch
    for (int i = 1; i <= 3; i++) {
      int threadId = i;
      Thread.startVirtualThread(() -> {
        try {
          System.out.println("    Thread " + threadId + " waiting...");
          latch.await();
          System.out.println("    Thread " + threadId + " released!");
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    Thread.sleep(100);
    System.out.println("  Opening latch...");
    latch.countDown();

    Thread.sleep(100);

    System.out.println("""

        All waiting threads are released simultaneously
        when count reaches zero (like opening a gate).
        """);
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        Use CountDownLatch for:
        - Wait for N events/tasks to complete
        - Starting gun (all threads start together)
        - Service initialization (wait for dependencies)
        - Test synchronization

        DO:
        - Always countDown() in finally block
        - Use timeout to avoid infinite waits
        - Consider virtual threads for many waiters

        DON'T:
        - Try to reset (use CyclicBarrier instead)
        - Forget countDown on exception paths

        Common pattern:
        ```
        try {
            doWork();
        } finally {
            latch.countDown();  // Always count down!
        }
        ```

        CountDownLatch vs CyclicBarrier:
        +-------------------+-----------------+-----------------+
        | Feature           | CountDownLatch  | CyclicBarrier   |
        +-------------------+-----------------+-----------------+
        | Reusable          | No              | Yes             |
        | Who waits         | Different thread| Same threads    |
        | Who counts        | Worker threads  | Barrier itself  |
        | Action on release | None            | Optional action |
        | Use case          | Wait for events | Phased work     |
        +-------------------+-----------------+-----------------+

        Alternatives:
        - Phaser: Flexible, reusable, dynamic parties
        - CompletableFuture.allOf(): For async workflows
        - Virtual threads: Often simpler than complex sync
        """);
  }
}
