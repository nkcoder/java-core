package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * tryLock() with Timeout Example
 *
 * tryLock() is a non-blocking lock acquisition method that returns immediately
 * or waits for a specified timeout, instead of blocking indefinitely.
 *
 * Key tryLock() variants:
 * 1. tryLock() - Returns immediately, true if acquired, false if not
 * 2. tryLock(timeout, unit) - Waits up to timeout, returns true/false
 * 3. Both are interruptible (throw InterruptedException)
 *
 * How tryLock(timeout) works:
 * - Attempts to acquire lock
 * - If unavailable, waits up to specified timeout
 * - Returns true if lock acquired, false if timeout expires
 * - Can be interrupted during wait (throws InterruptedException)
 *
 * Comparison of lock acquisition methods:
 *
 * lock():
 * - Blocks indefinitely until lock is acquired
 * - No timeout, no way to give up
 * - Not interruptible (ignores interrupts while waiting)
 * - Use when: Must acquire lock no matter what
 *
 * lockInterruptibly():
 * - Blocks indefinitely but can be interrupted
 * - Throws InterruptedException if interrupted
 * - Use when: Need to acquire lock but must be cancellable
 *
 * tryLock():
 * - Returns immediately (non-blocking)
 * - Returns true/false (acquired/not acquired)
 * - Use when: Don't want to wait, try once and give up
 *
 * tryLock(timeout):
 * - Waits up to timeout, then gives up
 * - Returns true/false
 * - Interruptible
 * - Use when: Willing to wait some time, but not forever
 *
 * Use cases for tryLock(timeout):
 * ✓ Avoiding deadlock (timeout and retry with different order)
 * ✓ Implementing timeouts in operations
 * ✓ Responsive UI (don't block user indefinitely)
 * ✓ Resource contention management
 * ✓ Service level agreements (SLA compliance)
 *
 * Best practices:
 * ✓ Always check return value of tryLock()
 * ✓ Only unlock if tryLock() returned true
 * ✓ Use isHeldByCurrentThread() before unlock for safety
 * ✓ Handle false return value appropriately (retry, fail, etc.)
 *
 * This example demonstrates:
 * - First thread acquires lock and holds for 5 seconds
 * - Second thread tries to acquire with 3-second timeout
 * - Second thread times out because first holds lock > 3 seconds
 * - Proper cleanup with isHeldByCurrentThread() check
 */
public class ReentrantTryLockDemo implements Runnable {
  private static ReentrantLock lock = new ReentrantLock();

  @Override
  public void run() {
    try {
      // Try to acquire lock, wait up to 3 seconds
      // Returns true if acquired within 3 seconds, false if timeout
      if (lock.tryLock(3, TimeUnit.SECONDS)) {
        // Successfully acquired lock
        try {
          // Hold lock for 5 seconds (longer than timeout)
          // First thread to acquire will block second thread
          TimeUnit.SECONDS.sleep(5);
          System.out.println(Thread.currentThread().getId() + ": my job done.");
        } finally {
          // Release lock - always in finally block
          lock.unlock();
        }
      } else {
        // Failed to acquire lock within 3 seconds
        // This thread gives up and exits gracefully
        System.out.println(Thread.currentThread().getId() + ": get lock failed.");
      }
    } catch (InterruptedException exception) {
      // Can be interrupted while waiting for lock
      System.out.println(Thread.currentThread().getId() + ": interrupted while waiting");
      exception.printStackTrace();
    } finally {
      // Safety check: only unlock if we hold the lock
      // Important because tryLock() might have returned false
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  /**
   * Demonstrates tryLock() with timeout behavior.
   * First thread acquires lock for 5 seconds.
   * Second thread waits 3 seconds, then times out.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    ReentrantTryLockDemo lockDemo = new ReentrantTryLockDemo();
    Thread t1 = new Thread(lockDemo);
    Thread t2 = new Thread(lockDemo);

    t1.start();
    t2.start();

    // Expected output:
    // - t1 acquires lock, sleeps 5 seconds, completes
    // - t2 tries for 3 seconds, fails because t1 holds lock for 5 seconds
    // - t2 prints "get lock failed" and exits
    // - After 5 seconds, t1 prints "my job done"
  }

}
