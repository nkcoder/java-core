package org.nkcoder.j8.concurrency.reentrantlock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock Reentrancy Example
 *
 * ReentrantLock is "reentrant" meaning a thread can acquire the same lock multiple times
 * without deadlocking itself. Each lock() must be matched with an unlock().
 *
 * Key characteristics of ReentrantLock:
 * 1. Reentrant - Thread can acquire same lock multiple times (holds count)
 * 2. Explicit locking - lock() and unlock() calls (unlike synchronized)
 * 3. Must unlock in finally block - to ensure release even on exception
 * 4. isHeldByCurrentThread() - Check if current thread holds the lock
 * 5. getHoldCount() - How many times current thread has acquired lock
 *
 * How reentrancy works:
 * - First lock() acquires lock and sets hold count to 1
 * - Second lock() by same thread increments hold count to 2
 * - Must call unlock() twice to fully release (count back to 0)
 * - Different thread cannot acquire until hold count reaches 0
 *
 * Lock hold count:
 * - Tracks how many times current thread has locked
 * - lock() increments count, unlock() decrements count
 * - Lock is fully released when count reaches 0
 * - Prevents deadlock when same thread needs lock multiple times
 *
 * ReentrantLock vs synchronized:
 *
 * ReentrantLock advantages:
 * ✓ tryLock() - Non-blocking lock attempt
 * ✓ tryLock(timeout) - Timed lock attempt
 * ✓ lockInterruptibly() - Can be interrupted while waiting
 * ✓ Fair/non-fair modes
 * ✓ Condition variables (multiple wait sets)
 * ✓ Can check if locked: isLocked(), isHeldByCurrentThread()
 *
 * synchronized advantages:
 * ✓ Simpler syntax (no explicit unlock needed)
 * ✓ Automatic release (even if you forget)
 * ✓ JVM optimizations (biased locking, lock elision)
 * ✓ Works in any context
 *
 * Common patterns:
 * <pre>
 * lock.lock();
 * try {
 *   // Critical section
 * } finally {
 *   lock.unlock();  // ALWAYS in finally!
 * }
 * </pre>
 *
 * Common mistakes:
 * ✗ Forgetting to unlock (causes permanent deadlock)
 * ✗ Not using finally block (lock not released on exception)
 * ✗ Unbalanced lock/unlock counts (more locks than unlocks or vice versa)
 * ✗ Unlocking in wrong order with nested locks
 *
 * This example demonstrates:
 * - Thread acquires same lock twice (nested locking)
 * - Must unlock twice to match two lock() calls
 * - Both threads share same Runnable (same 'i' variable)
 * - Lock ensures thread-safe increment despite reentrancy
 * - Final value should be exactly 20,000,000 (10M per thread)
 */
public class ReentrantLockDemo implements Runnable {

  private static final ReentrantLock LOCK = new ReentrantLock();
  private static int i = 0;

  @Override
  public void run() {
    for (int j = 0; j < 10000000; j++) {
      // First lock() - acquires lock, hold count = 1
      LOCK.lock();
      // Second lock() by SAME thread - reentrant behavior, hold count = 2
      // This would deadlock with a non-reentrant lock!
      // But ReentrantLock allows same thread to acquire multiple times
      LOCK.lock();
      try {
        // Critical section - increment shared variable
        // Only one thread can be here at a time
        i++;
      } finally {
        // Must unlock TWICE to match two lock() calls
        // First unlock() decrements hold count to 1
        LOCK.unlock();
        // Second unlock() decrements hold count to 0 (fully released)
        LOCK.unlock();
        // After both unlocks, other thread can acquire lock
      }
    }
    // Each thread prints when done (might not print 20000000 if other thread still running)
    System.out.println("i: " + i);
  }

  /**
   * Demonstrates ReentrantLock reentrancy with two threads.
   * Shows same thread can acquire lock multiple times without deadlock.
   *
   * @param args command line arguments
   * @throws InterruptedException if join is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    ReentrantLockDemo lockDemo = new ReentrantLockDemo();
    // Both threads share same Runnable instance (shared 'i' variable)
    Thread t1 = new Thread(lockDemo);
    Thread t2 = new Thread(lockDemo);
    t1.start();
    t2.start();
    // Wait for both threads to complete
    t1.join();
    t2.join();

    // Final value should be exactly 20,000,000 (2 threads * 10,000,000 each)
    // Lock prevents race conditions despite nested locking
  }

}
