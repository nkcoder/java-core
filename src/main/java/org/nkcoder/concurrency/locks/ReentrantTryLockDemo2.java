package org.nkcoder.concurrency.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Deadlock Avoidance with tryLock() Example
 *
 * This example demonstrates using tryLock() to avoid deadlock through the
 * "try and back off" strategy instead of waiting indefinitely for locks.
 *
 * Deadlock prevention strategy:
 * 1. Try to acquire first lock (non-blocking tryLock)
 * 2. If successful, try to acquire second lock (non-blocking)
 * 3. If second lock unavailable, release first lock and retry
 * 4. This breaks the "hold and wait" condition required for deadlock
 *
 * Why this avoids deadlock:
 * - Traditional lock() creates deadlock when threads hold locks and wait
 * - tryLock() doesn't wait - it either succeeds or returns false immediately
 * - If can't acquire all locks, thread releases what it has and retries
 * - This allows other threads to make progress
 *
 * Deadlock conditions (all 4 required for deadlock):
 * 1. Mutual exclusion - locks are exclusive ✓
 * 2. Hold and wait - threads hold locks and wait for more ✗ (broken by tryLock)
 * 3. No preemption - can't forcefully take locks ✓
 * 4. Circular wait - circular dependency in lock order ✓
 *
 * By breaking condition #2 (hold and wait) with tryLock(), deadlock is impossible.
 *
 * Comparison with other deadlock prevention strategies:
 *
 * Lock ordering:
 * - Always acquire locks in same order (e.g., always lock1 then lock2)
 * - Prevents circular wait
 * - Requires global knowledge of all locks
 *
 * Lock timeout (tryLock with timeout):
 * - Wait limited time, then give up
 * - Eventually breaks deadlock but slower
 * - Simpler than tryLock without timeout
 *
 * tryLock() without timeout (this example):
 * - Immediate failure if can't acquire
 * - Fast recovery from contention
 * - May use more CPU (busy retrying)
 * - Best responsiveness
 *
 * Live lock risk:
 * - Both threads might repeatedly acquire their first lock, fail second, retry
 * - Different sleep times (50ms vs 100ms) reduce this risk
 * - Randomized backoff would be even better
 *
 * Trade-offs:
 * ✓ Never deadlocks (releases locks if can't get all)
 * ✓ Fast response to contention
 * ✓ No need to know global lock ordering
 * ✗ More CPU usage (retry loop)
 * ✗ Possible live lock (less likely with different timings)
 * ✗ More complex code than simple lock()
 *
 * This example demonstrates:
 * - Two threads trying to acquire two locks in opposite order
 * - Using tryLock() to avoid deadlock (back off if can't get both)
 * - Different sleep times to reduce live lock probability
 * - Threads eventually succeed and print completion message
 */
public class ReentrantTryLockDemo2 {

  private static ReentrantLock lockOne = new ReentrantLock();
  private static ReentrantLock lockTwo = new ReentrantLock();

  public static void main(String[] args) {
    // Two threads with opposite lock orders
    // Without tryLock(), this would deadlock
    Thread t1 = new Thread(new TryLockRunnable(0), "t1");
    Thread t2 = new Thread(new TryLockRunnable(1), "t2");

    t1.start();
    t2.start();

    // Both threads will eventually succeed and print "My job is done"
    // No deadlock occurs because threads back off if they can't acquire both locks
  }

  static class TryLockRunnable implements Runnable {

    private int order;

    public TryLockRunnable(int order) {
      this.order = order;
    }

    @Override
    public void run() {
      if (order == 0) {
        // Thread 1: tries to acquire lockOne first, then lockTwo
        while (true) {
          // Try to acquire first lock (non-blocking)
          if (lockOne.tryLock()) {
            try {
              try {
                // Simulate some work with first lock
                // Different sleep time (50ms) reduces live lock risk
                Thread.sleep(50);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }

              // Try to acquire second lock (non-blocking)
              if (lockTwo.tryLock()) {
                try {
                  // Success! Both locks acquired
                  System.out.println("My job is done, name: " + Thread.currentThread().getName());
                  return; // Exit successfully (implicit unlocks in finally blocks)
                } finally {
                  lockTwo.unlock();
                }
              }
              // If couldn't get lockTwo, release lockOne and retry
              // This is the key: don't wait holding lockOne
            } finally {
              lockOne.unlock();
            }
          }
          // If couldn't get lockOne or lockTwo, loop and retry
          // No locks held during retry - allows other thread to progress
        }
      } else {
        // Thread 2: tries to acquire lockTwo first, then lockOne (OPPOSITE ORDER)
        while (true) {
          // Try to acquire first lock (non-blocking)
          if (lockTwo.tryLock()) {
            try {
              try {
                // Simulate some work with first lock
                // Different sleep time (100ms) reduces live lock risk
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }

              // Try to acquire second lock (non-blocking)
              if (lockOne.tryLock()) {
                try {
                  // Success! Both locks acquired
                  System.out.println("My job is done, name: " + Thread.currentThread().getName());
                  return; // Exit successfully (implicit unlocks in finally blocks)
                } finally {
                  lockOne.unlock();
                }
              }
              // If couldn't get lockOne, release lockTwo and retry
              // This is the key: don't wait holding lockTwo
            } finally {
              lockTwo.unlock();
            }
          }
          // If couldn't get lockTwo or lockOne, loop and retry
          // No locks held during retry - allows other thread to progress
        }
      }
    }
  }
}
