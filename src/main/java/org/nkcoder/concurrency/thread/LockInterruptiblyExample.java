package org.nkcoder.concurrency.thread;

import java.util.concurrent.locks.ReentrantLock;

/**
 * lockInterruptibly() Example - Breaking Deadlock with Interruption
 *
 * lockInterruptibly() is a ReentrantLock method that attempts to acquire a lock
 * but can be interrupted while waiting, unlike the regular lock() method.
 *
 * Key differences:
 * 1. lock() - Acquires lock, ignores interrupts while waiting (not interruptible)
 * 2. lockInterruptibly() - Acquires lock, throws InterruptedException if interrupted while waiting
 * 3. tryLock() - Attempts to acquire without waiting (returns immediately)
 *
 * Deadlock scenario in this example:
 * - Thread 1: acquires lockOne, tries to acquire lockTwo
 * - Thread 2: acquires lockTwo, tries to acquire lockOne
 * - Classic deadlock: each thread holds one lock and waits for the other
 *
 * Breaking the deadlock:
 * - Using lockInterruptibly() allows us to interrupt the waiting thread
 * - When interrupted, thread releases its held lock and exits
 * - This breaks the circular wait condition
 *
 * Lock acquisition methods comparison:
 * - lock(): Uninterruptible, waits forever
 * - lockInterruptibly(): Can be interrupted while waiting
 * - tryLock(): Non-blocking, returns immediately
 * - tryLock(timeout): Waits for specified time, then gives up
 *
 * Best practices:
 * ✓ Use lockInterruptibly() when you need cancellation capability
 * ✓ Always use try-finally to ensure locks are released
 * ✓ Check isHeldByCurrentThread() before unlocking
 * ✗ Don't unlock in catch block unless you acquired the lock
 *
 * This example demonstrates:
 * - Creating a potential deadlock situation
 * - Using lockInterruptibly() to make threads responsive to interruption
 * - Breaking deadlock by interrupting one thread
 * - Proper lock cleanup in finally block
 */
public class LockInterruptiblyExample {

  private static final ReentrantLock lockOne = new ReentrantLock();
  private static final ReentrantLock lockTwo = new ReentrantLock();

  public static void main(String[] args) throws InterruptedException {
    LockThread threadOne = new LockThread(0, "t1");
    LockThread threadTwo = new LockThread(1, "t2");

    threadOne.start();
    threadTwo.start();

    // Allow threads to create deadlock
    Thread.sleep(500);

    // Interrupt threadTwo to break the deadlock
    // Because we use lockInterruptibly(), threadTwo will throw InterruptedException
    // and release lockTwo, allowing threadOne to proceed
    threadTwo.interrupt();
  }

  static class LockThread extends Thread {
    private int order;
    private String name;

    public LockThread(int order, String name) {
      this.order = order;
      this.name = name;
    }

    @Override
    public void run() {
      try {
        if (order == 0) {
          // Thread 1: acquire lockOne first, then lockTwo
          lockOne.lockInterruptibly();
          Thread.sleep(300); // Give thread 2 time to acquire lockTwo (creates deadlock)
          lockTwo.lockInterruptibly(); // Will wait here if thread 2 holds lockTwo
        } else {
          // Thread 2: acquire lockTwo first, then lockOne (opposite order - deadlock!)
          lockTwo.lockInterruptibly();
          Thread.sleep(300); // Give thread 1 time to acquire lockOne (creates deadlock)
          lockOne.lockInterruptibly(); // Will wait here, but can be interrupted!
        }
      } catch (InterruptedException e) {
        // When interrupted, thread releases held locks and exits
        System.out.println("Thread " + name + " was interrupted");
        e.printStackTrace();
      } finally {
        // Always check before unlocking - only unlock locks we actually hold
        // This prevents IllegalMonitorStateException
        if (lockOne.isHeldByCurrentThread()) {
          lockOne.unlock();
        }
        if (lockTwo.isHeldByCurrentThread()) {
          lockTwo.unlock();
        }
      }

      System.out.println("Thread is exited: " + name);
    }
  }
}
