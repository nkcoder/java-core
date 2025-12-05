package org.nkcoder.concurrency.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore: Controls access to a limited number of resources.
 *
 * <p><strong>Java 25 Note:</strong> Still the right choice for limiting concurrent access to
 * resources (connection pools, rate limiting). No direct modern replacement exists.
 *
 * <p>Key concepts:
 * <ul>
 *   <li>Maintains a set of permits</li>
 *   <li>acquire() takes a permit (blocks if none available)</li>
 *   <li>release() returns a permit</li>
 *   <li>Unlike locks, permits can be released by different thread</li>
 * </ul>
 *
 * <p>Interview tip: Know the difference between Semaphore and Lock.
 * Classic use case: connection pool, rate limiting.
 */
public class SemaphoreExample {

  static void main(String[] args) throws Exception {
    basicUsage();
    resourcePool();
    binaryMutex();
    tryAcquireExample();
    fairSemaphore();
    rateLimiter();
    bestPractices();
  }

  static void basicUsage() throws Exception {
    System.out.println("=== Basic Usage ===");

    // Semaphore with 3 permits
    Semaphore semaphore = new Semaphore(3);

    System.out.println("  Available permits: " + semaphore.availablePermits());

    // Acquire permits
    semaphore.acquire();
    System.out.println("  After 1 acquire: " + semaphore.availablePermits());

    semaphore.acquire(2); // Acquire multiple at once
    System.out.println("  After acquire(2): " + semaphore.availablePermits());

    // Release permits
    semaphore.release();
    System.out.println("  After 1 release: " + semaphore.availablePermits());

    semaphore.release(2);
    System.out.println("  After release(2): " + semaphore.availablePermits());

    // Can release more than acquired (increase permits!)
    semaphore.release();
    System.out.println("  After extra release: " + semaphore.availablePermits());

    System.out.println("""

        Key difference from locks:
        - Semaphore has no ownership (any thread can release)
        - Can have multiple permits (not just 0 or 1)
        - Can increase permits beyond initial count
        """);
  }

  static void resourcePool() throws Exception {
    System.out.println("=== Resource Pool Example ===");

    class ConnectionPool {
      private final Semaphore semaphore;
      private final String[] connections;
      private final boolean[] used;

      ConnectionPool(int size) {
        semaphore = new Semaphore(size);
        connections = new String[size];
        used = new boolean[size];
        for (int i = 0; i < size; i++) {
          connections[i] = "Connection-" + i;
        }
      }

      String acquire() throws InterruptedException {
        semaphore.acquire(); // Wait for available connection
        return getAvailableConnection();
      }

      void release(String connection) {
        returnConnection(connection);
        semaphore.release(); // Signal connection available
      }

      private synchronized String getAvailableConnection() {
        for (int i = 0; i < connections.length; i++) {
          if (!used[i]) {
            used[i] = true;
            return connections[i];
          }
        }
        throw new IllegalStateException("No connection available");
      }

      private synchronized void returnConnection(String connection) {
        for (int i = 0; i < connections.length; i++) {
          if (connections[i].equals(connection)) {
            used[i] = false;
            return;
          }
        }
      }
    }

    ConnectionPool pool = new ConnectionPool(2); // Only 2 connections

    try (ExecutorService executor = Executors.newFixedThreadPool(5)) {
      for (int i = 1; i <= 5; i++) {
        int taskId = i;
        executor.submit(() -> {
          try {
            System.out.println("    Task " + taskId + " requesting connection...");
            String conn = pool.acquire();
            System.out.println("    Task " + taskId + " got " + conn);
            Thread.sleep(100); // Use connection
            pool.release(conn);
            System.out.println("    Task " + taskId + " released " + conn);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
      }
    }

    System.out.println();
  }

  static void binaryMutex() throws Exception {
    System.out.println("=== Binary Semaphore (Mutex) ===");

    // Semaphore with 1 permit acts like a mutex
    Semaphore mutex = new Semaphore(1);

    class SharedCounter {
      private int count = 0;

      void increment() throws InterruptedException {
        mutex.acquire();
        try {
          count++;
        } finally {
          mutex.release();
        }
      }

      int getCount() {
        return count;
      }
    }

    SharedCounter counter = new SharedCounter();

    try (ExecutorService executor = Executors.newFixedThreadPool(4)) {
      for (int i = 0; i < 1000; i++) {
        executor.submit(() -> {
          try {
            counter.increment();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        });
      }
    }

    System.out.println("  Counter: " + counter.getCount());

    System.out.println("""

        Binary semaphore vs Lock:
        +------------------+------------------+------------------+
        | Feature          | Semaphore(1)     | Lock             |
        +------------------+------------------+------------------+
        | Ownership        | No               | Yes              |
        | Release by other | Yes              | No (exception)   |
        | Reentrant        | No               | Yes (Reentrant)  |
        | Condition        | No               | Yes              |
        +------------------+------------------+------------------+

        Use Lock when you need ownership semantics.
        Use Semaphore when different thread may release.
        """);
  }

  static void tryAcquireExample() throws Exception {
    System.out.println("=== tryAcquire (Non-blocking) ===");

    Semaphore semaphore = new Semaphore(2);
    semaphore.acquire(2); // Take all permits

    // tryAcquire - returns immediately
    boolean acquired = semaphore.tryAcquire();
    System.out.println("  tryAcquire(): " + acquired);

    // tryAcquire with timeout
    System.out.println("  tryAcquire(100ms)...");
    long start = System.currentTimeMillis();
    acquired = semaphore.tryAcquire(100, TimeUnit.MILLISECONDS);
    long elapsed = System.currentTimeMillis() - start;
    System.out.println("  Result: " + acquired + " (waited " + elapsed + "ms)");

    // Release in background, then try again
    Thread.startVirtualThread(() -> {
      try {
        Thread.sleep(50);
        semaphore.release();
        System.out.println("  Released permit from another thread");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    acquired = semaphore.tryAcquire(200, TimeUnit.MILLISECONDS);
    System.out.println("  tryAcquire after release: " + acquired);

    semaphore.release(); // Clean up

    System.out.println();
  }

  static void fairSemaphore() throws Exception {
    System.out.println("=== Fair Semaphore ===");

    // Non-fair (default): threads may acquire out of order
    Semaphore nonFair = new Semaphore(1, false);

    // Fair: FIFO order guaranteed
    Semaphore fair = new Semaphore(1, true);

    System.out.println("  Non-fair semaphore isFair: " + nonFair.isFair());
    System.out.println("  Fair semaphore isFair: " + fair.isFair());

    System.out.println("""

        Non-fair (default):
        - Threads may "barge" ahead of waiting threads
        - Better throughput
        - Possible starvation

        Fair:
        - FIFO order
        - No starvation
        - Lower throughput

        Query methods:
        - hasQueuedThreads(): Are threads waiting?
        - getQueueLength(): How many waiting?
        - availablePermits(): How many permits available?
        """);
  }

  static void rateLimiter() throws Exception {
    System.out.println("=== Rate Limiter Pattern ===");

    class SimpleRateLimiter {
      private final Semaphore semaphore;
      private final int maxPermits;
      private final long refillIntervalMs;

      SimpleRateLimiter(int permitsPerSecond) {
        this.maxPermits = permitsPerSecond;
        this.semaphore = new Semaphore(permitsPerSecond);
        this.refillIntervalMs = 1000;

        // Background thread to refill permits
        Thread refiller = new Thread(() -> {
          while (!Thread.currentThread().isInterrupted()) {
            try {
              Thread.sleep(refillIntervalMs);
              int permitsToAdd = maxPermits - semaphore.availablePermits();
              if (permitsToAdd > 0) {
                semaphore.release(permitsToAdd);
              }
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
        });
        refiller.setDaemon(true);
        refiller.start();
      }

      boolean tryAcquire() {
        return semaphore.tryAcquire();
      }

      void acquire() throws InterruptedException {
        semaphore.acquire();
      }
    }

    SimpleRateLimiter limiter = new SimpleRateLimiter(5); // 5 requests per second

    System.out.println("  Rate limiter: 5 permits/second");
    System.out.println("  Trying 10 requests quickly:");

    for (int i = 1; i <= 10; i++) {
      if (limiter.tryAcquire()) {
        System.out.println("    Request " + i + ": ALLOWED");
      } else {
        System.out.println("    Request " + i + ": RATE LIMITED");
      }
    }

    System.out.println("\n  Waiting 1 second for refill...");
    Thread.sleep(1100);

    System.out.println("  Trying 3 more requests:");
    for (int i = 1; i <= 3; i++) {
      if (limiter.tryAcquire()) {
        System.out.println("    Request " + i + ": ALLOWED");
      }
    }

    System.out.println();
  }

  static void bestPractices() {
    System.out.println("=== Best Practices ===");

    System.out.println("""
        Use Semaphore for:
        - Resource pools (connections, threads)
        - Rate limiting
        - Bounding concurrent access
        - Producer-consumer (bounded buffer)

        DO:
        - Always release in finally block
        - Consider fair mode if starvation is an issue
        - Use tryAcquire for non-blocking attempts

        DON'T:
        - Assume ownership (different thread can release)
        - Forget to release (resource leak)
        - Use when Lock semantics needed

        Common patterns:
        ```
        // Resource pool
        semaphore.acquire();
        try {
            useResource();
        } finally {
            semaphore.release();
        }

        // Non-blocking
        if (semaphore.tryAcquire()) {
            try {
                useResource();
            } finally {
                semaphore.release();
            }
        } else {
            handleResourceUnavailable();
        }
        ```

        Semaphore variants:
        - Semaphore(n): n permits
        - Semaphore(n, fair): n permits with FIFO
        - Semaphore(0): Start with none, release adds permits
        """);
  }
}
