package org.nkcoder.concurrency.reentrantlock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Fair vs Non-Fair ReentrantLock Example
 *
 * ReentrantLock can be created in two modes: fair or non-fair.
 * This significantly affects lock acquisition order and performance.
 *
 * Fair Lock (new ReentrantLock(true)):
 * - Threads acquire lock in FIFO order (first-come-first-served)
 * - The longest-waiting thread gets the lock next
 * - Prevents thread starvation (every thread eventually gets the lock)
 * - Lower throughput due to thread scheduling overhead
 * - More predictable behavior
 *
 * Non-Fair Lock (new ReentrantLock() or new ReentrantLock(false)):
 * - No guaranteed order - any waiting thread might acquire lock
 * - Thread that just released lock might immediately reacquire it
 * - Higher throughput (less context switching)
 * - Possible thread starvation (some threads might wait very long)
 * - Default mode for better performance
 *
 * Performance comparison:
 * Fair lock: 10-100x slower than non-fair in high contention scenarios
 * Non-fair lock: Better CPU utilization, fewer context switches
 *
 * When to use each:
 * Fair lock:
 * ✓ When fairness is critical (no starvation allowed)
 * ✓ Predictable response times required
 * ✓ Low contention scenarios
 * ✓ When order of operations matters
 *
 * Non-fair lock (default):
 * ✓ When throughput is more important than fairness
 * ✓ High contention scenarios
 * ✓ Most applications (default choice)
 *
 * How fairness works:
 * - Maintains an internal queue of waiting threads
 * - On unlock(), wakes the head of the queue
 * - New lock attempts go to end of queue
 * - Overhead: queue management + thread scheduling
 *
 * This example demonstrates:
 * - Fair lock ensures threads take turns in order
 * - Run with fair=true: Output shows t1, t2, t1, t2, t1, t2... (alternating)
 * - Run with fair=false: Output is more random, one thread might dominate
 * - Observe the difference in fairness vs throughput
 */
public class ReentrantFairLockDemo implements Runnable {
  // Fair lock: true means threads acquire lock in FIFO order
  // Change to false to see non-fair behavior (random order, possible starvation)
  private static ReentrantLock fairLock = new ReentrantLock(true);

  @Override
  public void run() {
    // Infinite loop to demonstrate lock acquisition pattern
    while (true) {
      try {
        // Acquire lock - with fair=true, threads will alternate in FIFO order
        // With fair=false, same thread might acquire lock multiple times in a row
        fairLock.lock();
        System.out.println(Thread.currentThread().getName() + ": get lock");
      } finally {
        // Always check before unlocking to avoid IllegalMonitorStateException
        if (fairLock.isHeldByCurrentThread()) {
          fairLock.unlock();
        }
      }
    }
  }

  /**
   * Demonstrates fair vs non-fair lock behavior.
   * With fair lock, threads will alternate acquiring the lock.
   * Try changing fairLock to non-fair (false) and observe the difference.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    ReentrantFairLockDemo fairLockDemo = new ReentrantFairLockDemo();
    Thread t1 = new Thread(fairLockDemo, "t1");
    Thread t2 = new Thread(fairLockDemo, "t2");
    t1.start();
    t2.start();

    // Expected output with fair=true: t1 and t2 alternate fairly
    // Expected output with fair=false: random order, possible t1 t1 t1 t2 t1 t1...
  }

}
