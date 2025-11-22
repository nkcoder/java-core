package org.nkcoder.j8.concurrency.thread;

/**
 * Thread Join Example
 *
 * join() is a thread coordination method that allows one thread to wait for another thread
 * to complete before continuing execution.
 *
 * Key characteristics:
 * 1. join() - Blocks the calling thread until the target thread completes
 * 2. join(millis) - Waits at most the specified time in milliseconds
 * 3. join(millis, nanos) - Waits with nanosecond precision
 * 4. Can throw InterruptedException if the waiting thread is interrupted
 *
 * How it works:
 * - When thread A calls threadB.join(), thread A waits until thread B dies
 * - Main thread often uses join() to wait for worker threads to finish
 * - Internally implemented using wait()/notify() mechanism
 *
 * Common patterns:
 * - Wait for computation results from another thread
 * - Ensure tasks complete before proceeding
 * - Coordinate multiple threads (wait for all to finish)
 *
 * yield() comparison (not shown in this example):
 * - yield() is a hint to scheduler to give other threads a chance to run
 * - It's a suggestion, not guaranteed - scheduler may ignore it
 * - Rarely needed in modern applications - use higher-level constructs instead
 *
 * This example demonstrates:
 * - Worker thread calculates a sum
 * - Main thread waits for calculation to complete using join()
 * - Without join(), main thread might print before calculation finishes (race condition)
 * - volatile ensures visibility of 'total' across threads
 */
public class JoinYieldExample {
  // volatile ensures changes to 'total' are visible to all threads
  // Without volatile, main thread might not see the updated value
  private volatile static int total = 0;

  public static void main(String[] args) throws InterruptedException {

    Thread t1 = new Thread() {
      @Override
      public void run() {
        // Calculate sum of 0..99
        for (int i = 0; i < 100; i++) {
          total += i;
        }
      }
    };

    t1.start();

    // Main thread waits here until t1 completes
    // Without this join(), main thread might print total before t1 finishes calculating
    // This would result in printing 0 or a partial sum
    t1.join();

    // This prints after t1 completes - guaranteed to show the final result (4950)
    System.out.println("total: " + total);
  }
}
