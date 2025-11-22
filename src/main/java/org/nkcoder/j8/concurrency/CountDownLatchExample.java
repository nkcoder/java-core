package org.nkcoder.j8.concurrency;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CountDownLatch Example
 *
 * CountDownLatch is a synchronization aid that allows one or more threads to wait
 * until a set of operations being performed in other threads completes.
 *
 * Key characteristics:
 * 1. Initialized with a count (number of events to wait for)
 * 2. countDown() - decrements the count (called by worker threads)
 * 3. await() - blocks until count reaches zero (called by waiting thread)
 * 4. One-time use - count cannot be reset (use CyclicBarrier for reusable alternative)
 * 5. Thread-safe - multiple threads can call countDown() concurrently
 *
 * Common use cases:
 * - Starting multiple threads simultaneously (all threads await on latch with count=1)
 * - Waiting for multiple tasks to complete before proceeding (this example)
 * - Testing concurrent code (ensure all threads are ready before starting)
 *
 * Comparison with other synchronizers:
 * - CyclicBarrier: All threads wait for each other; reusable
 * - CountDownLatch: One/more threads wait for others; single-use
 * - Semaphore: Controls access to a resource pool
 */
public class CountDownLatchExample {

  private void doSomeTask(CountDownLatch countDownLatch) {
    try {
      System.out.println("I'm trying to do some task: " + Thread.currentThread().getName());
      // Simulate work with random delay
      Thread.sleep(new Random().nextInt(1000));

      // Decrement the latch count - signals this task is complete
      // When count reaches 0, waiting threads are released
      countDownLatch.countDown();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("task done by thread: " + Thread.currentThread().getName());
  }

  public static void main(String[] args) {
    CountDownLatchExample countDownLatchExample = new CountDownLatchExample();

    // Initialize latch with count of 10 - main thread will wait for 10 events
    CountDownLatch countDownLatch = new CountDownLatch(10);

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    // Submit 10 tasks - each will call countDown() when finished
    for (int i = 0; i < 10; i++) {
      executorService.submit(() -> countDownLatchExample.doSomeTask(countDownLatch));
    }

    try {
      // Main thread waits here until all 10 tasks call countDown()
      // This blocks until latch count reaches 0
      countDownLatch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // This line executes only after all 10 tasks complete
    System.out.println("main task continue.");
    executorService.shutdown();
  }
}
