package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * lockInterruptibly() Example - Interruptible Lock Acquisition
 *
 * This example demonstrates using lockInterruptibly() to make lock acquisition
 * responsive to thread interruption, which can help break deadlocks.
 *
 * Key concepts:
 * 1. lockInterruptibly() - Can be interrupted while waiting for lock
 * 2. lock() - Cannot be interrupted while waiting (ignores interrupts)
 * 3. Interruption throws InterruptedException
 * 4. Useful for cancellation and deadlock recovery
 *
 * Deadlock scenario setup:
 * - Thread t1: acquires lock1, then tries to acquire lock2
 * - Thread t2: acquires lock2, then tries to acquire lock1
 * - Classic circular wait - would deadlock with regular lock()
 *
 * Breaking the deadlock:
 * - Using lockInterruptibly() allows external interruption
 * - When t2 is interrupted, it throws InterruptedException
 * - t2 releases lock2 in finally block and exits
 * - t1 can now acquire lock2 and complete successfully
 *
 * lock() vs lockInterruptibly():
 *
 * lock():
 * - Waits indefinitely for lock
 * - Ignores interrupts while waiting (but remembers interrupt status)
 * - Cannot be cancelled externally
 * - Use when cancellation not needed
 *
 * lockInterruptibly():
 * - Waits for lock but responds to interrupts
 * - Throws InterruptedException if interrupted
 * - Can be cancelled externally
 * - Use when task cancellation is important
 *
 * Common use cases for lockInterruptibly():
 * ✓ Tasks that need to be cancellable
 * ✓ Deadlock detection and recovery
 * ✓ Timeout-based operations (with separate timeout thread)
 * ✓ Graceful shutdown scenarios
 * ✓ Interactive applications where user can cancel operations
 *
 * Error handling pattern:
 * <pre>
 * try {
 *   lock.lockInterruptibly();
 *   try {
 *     // Critical section
 *   } finally {
 *     if (lock.isHeldByCurrentThread()) {
 *       lock.unlock();
 *     }
 *   }
 * } catch (InterruptedException e) {
 *   // Handle interruption (cleanup, exit)
 * }
 * </pre>
 *
 * This example demonstrates:
 * - Creating a potential deadlock (opposite lock order)
 * - Using lockInterruptibly() to make threads interruptible
 * - Breaking deadlock by interrupting one thread
 * - Proper cleanup in finally block (only unlock held locks)
 */
public class ReentrantLockInterruptDemo implements Runnable {

  private static ReentrantLock lock1 = new ReentrantLock();
  private static ReentrantLock lock2 = new ReentrantLock();

  private int value;

  public ReentrantLockInterruptDemo(int value) {
    this.value = value;
  }

  @Override
  public void run() {
    try {
      if (value == 1) {
        // Thread 1: acquire lock1 first, then lock2
        lock1.lockInterruptibly();
        try {
          // Small delay to ensure both threads acquire first lock
          TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        }
        // Try to acquire lock2 - will wait if t2 holds it
        // Can be interrupted while waiting here
        lock2.lockInterruptibly();
        System.out.println(Thread.currentThread().getName() + ": my job done.");
      } else {
        // Thread 2: acquire lock2 first, then lock1 (OPPOSITE ORDER)
        // This creates potential for deadlock
        lock2.lockInterruptibly();
        try {
          // Small delay to ensure both threads acquire first lock
          TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        }
        // Try to acquire lock1 - will wait if t1 holds it
        // Can be interrupted while waiting here (this is where interruption happens)
        lock1.lockInterruptibly();
        System.out.println(Thread.currentThread().getName() + ": my job done.");
      }
    } catch (InterruptedException exception) {
      // Interrupted while waiting for lock
      // This breaks the deadlock by causing this thread to exit
      System.out.println(Thread.currentThread().getName() + ": interrupted while acquiring lock");
      exception.printStackTrace();
    } finally {
      // Clean up: only unlock locks we actually hold
      // IMPORTANT: Check before unlocking to avoid IllegalMonitorStateException
      if (lock1.isHeldByCurrentThread()) {
        lock1.unlock();
      } else if (lock2.isHeldByCurrentThread()) {
        lock2.unlock();
      }

      System.out.println(Thread.currentThread().getName() + ": I'm exit.");
    }
  }

  /**
   * Demonstrates breaking a deadlock using lockInterruptibly() and thread interruption.
   * Creates a circular wait, then interrupts one thread to break the deadlock.
   *
   * @param args command line arguments
   * @throws InterruptedException if main thread is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    // Create two Runnables with different lock orders
    ReentrantLockInterruptDemo lockDemo1 = new ReentrantLockInterruptDemo(1);
    ReentrantLockInterruptDemo lockDemo2 = new ReentrantLockInterruptDemo(2);

    Thread t1 = new Thread(lockDemo1, "t1");
    Thread t2 = new Thread(lockDemo2, "t2");

    // Start both threads - they will create a deadlock
    t1.start();
    t2.start();

    // Wait 1 second for deadlock to occur
    TimeUnit.SECONDS.sleep(1);

    // Interrupt t2 to break the deadlock
    // t2 will throw InterruptedException, release lock2, and exit
    // Then t1 can acquire lock2 and complete successfully
    t2.interrupt();

    // Expected output:
    // - t2 gets interrupted, releases lock2, exits
    // - t1 acquires lock2, completes task, exits
    // - Without interruption, both threads would wait forever (deadlock)
  }

}
