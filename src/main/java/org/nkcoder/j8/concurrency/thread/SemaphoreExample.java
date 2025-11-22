package org.nkcoder.j8.concurrency.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore Example
 *
 * A Semaphore is a synchronization aid that maintains a set of permits.
 * Threads can acquire permits (blocking if none available) and release them.
 *
 * Key characteristics:
 * 1. Initialized with N permits (capacity)
 * 2. acquire() - Acquires a permit, blocks if none available
 * 3. release() - Releases a permit, making it available to others
 * 4. Useful for limiting concurrent access to a resource pool
 * 5. NOT tied to thread ownership (any thread can release any permit)
 *
 * Common methods:
 * - acquire() / acquire(n): Acquire 1 or n permits, block if unavailable
 * - tryAcquire(): Non-blocking attempt to acquire permit
 * - tryAcquire(timeout): Attempt with timeout
 * - release() / release(n): Release 1 or n permits
 * - availablePermits(): Get count of available permits
 *
 * Fair vs Non-fair:
 * - Non-fair (default): Higher throughput, but threads may starve
 * - Fair (new Semaphore(n, true)): FIFO order, no starvation, lower throughput
 *
 * Use cases:
 * - Limiting concurrent access to resources (database connections, file handles)
 * - Implementing resource pools (connection pools, object pools)
 * - Rate limiting (throttling)
 * - Bounded buffers
 *
 * Semaphore vs other synchronizers:
 * - Lock: Exclusive access, 1 permit max, owner-based
 * - Semaphore: Multiple permits, not owner-based, can be used for counting
 * - CountDownLatch: One-time use, counts down to zero
 * - Semaphore: Reusable, permits can go up and down
 *
 * This example demonstrates:
 * - Semaphore with 5 permits limiting concurrent access
 * - 20 threads competing for 5 permits
 * - At most 5 threads execute simultaneously
 * - Output shows batches of ~5 threads completing together
 */
public class SemaphoreExample implements Runnable {
  // Create semaphore with 5 permits - at most 5 threads can hold permits simultaneously
  private static final Semaphore SEMAPHORE = new Semaphore(5);

  @Override
  public void run() {
    try {
      // Acquire a permit - blocks if all 5 permits are taken
      // When a permit becomes available, this thread proceeds
      SEMAPHORE.acquire();

      // Simulate work - sleep for 3 seconds
      // During this time, other threads may be waiting for permits
      TimeUnit.SECONDS.sleep(3);

      System.out.println(Thread.currentThread().getId() + ": I'm done.");

    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      // ALWAYS release in finally block to ensure permit is returned
      // Even if exception occurs, permit is released for others to use
      SEMAPHORE.release();
    }
  }

  /**
   * Demonstrates semaphore limiting concurrency to 5 threads.
   * 20 threads are submitted, but only 5 can run at once.
   * Expected output: Threads complete in batches of ~5, each batch taking ~3 seconds.
   */
  public static void main(String[] args) {
    SemaphoreExample semaphoreExample = new SemaphoreExample();
    // Create thread pool with 20 threads
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);

    // Submit 20 tasks - but semaphore limits concurrent execution to 5
    // This will take ~12 seconds total: 20 tasks / 5 concurrent * 3 seconds each
    for (int i = 0; i < 20; i++) {
      newFixedThreadPool.submit(semaphoreExample);
    }
  }


}
