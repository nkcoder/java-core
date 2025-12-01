package org.nkcoder.concurrency.reentrantlock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock Condition Example
 *
 * Condition is the ReentrantLock equivalent of wait()/notify() for Object monitors.
 * It provides more flexible thread coordination than traditional wait/notify.
 *
 * Key concepts:
 * 1. Condition is created from a Lock: lock.newCondition()
 * 2. Multiple Conditions can be created from same lock (unlike wait/notify)
 * 3. await() - Like wait(), releases lock and waits
 * 4. signal() - Like notify(), wakes one waiting thread
 * 5. signalAll() - Like notifyAll(), wakes all waiting threads
 *
 * How it works:
 * - await() must be called while holding the lock
 * - await() releases the lock and blocks the thread
 * - signal()/signalAll() must also be called while holding the lock
 * - Signaled thread reacquires lock before returning from await()
 *
 * Condition vs wait()/notify():
 *
 * Condition advantages:
 * ✓ Multiple conditions per lock (e.g., notEmpty, notFull for bounded buffer)
 * ✓ Can choose which condition to signal (more precise control)
 * ✓ More methods: awaitUninterruptibly(), await(timeout), awaitNanos()
 * ✓ Better API design (explicit Lock object)
 * ✓ Can use with any Lock implementation
 *
 * wait()/notify():
 * - Only one wait set per object
 * - notifyAll() wakes all threads (less efficient)
 * - Tied to synchronized blocks
 * - Part of Object class (every object has it)
 *
 * Common Condition methods:
 * - await(): Wait indefinitely
 * - await(time, unit): Wait with timeout
 * - awaitUninterruptibly(): Cannot be interrupted
 * - signal(): Wake one waiting thread
 * - signalAll(): Wake all waiting threads
 *
 * Typical usage pattern:
 * <pre>
 * lock.lock();
 * try {
 *   while (!condition) {
 *     conditionObj.await();
 *   }
 *   // ... critical section
 * } finally {
 *   lock.unlock();
 * }
 * </pre>
 *
 * Use cases:
 * - Producer-Consumer with multiple conditions (notEmpty, notFull)
 * - Complex state machines with multiple wait conditions
 * - When you need more than one condition variable
 * - Better alternative to wait/notify for new code
 *
 * This example demonstrates:
 * - Thread waits on condition using await()
 * - Main thread signals after 3 seconds using signalAll()
 * - Proper lock management with try-finally
 * - Basic condition variable coordination
 */
public class ReentrantLockConditionDemo implements Runnable {

  private static ReentrantLock lock = new ReentrantLock();
  // Create condition from lock - multiple conditions can be created from same lock
  private static Condition condition = lock.newCondition();

  @Override
  public void run() {
    try {
      // Must acquire lock before calling await()
      lock.lock();

      // await() releases lock and waits for signal
      // Similar to Object.wait() but more flexible
      // Thread enters WAITING state here
      condition.await();

      // After signal and reacquiring lock, thread continues here
      System.out.println(Thread.currentThread().getId() + ": I'm done.");
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      // Always unlock in finally block
      lock.unlock();
    }
  }

  /**
   * Demonstrates Condition for thread coordination.
   * Thread waits on condition, main thread signals after delay.
   *
   * @param args command line arguments
   * @throws InterruptedException if sleep is interrupted
   */
  public static void main(String[] args) throws InterruptedException {
    ReentrantLockConditionDemo conditionDemo = new ReentrantLockConditionDemo();
    Thread t1 = new Thread(conditionDemo);

    // Start thread - it will acquire lock and call await()
    t1.start();

    // Main thread sleeps 3 seconds while t1 waits
    TimeUnit.SECONDS.sleep(3);

    try {
      // Must acquire same lock before signaling
      lock.lock();

      // Signal all waiting threads on this condition
      // If multiple threads were waiting, signalAll() wakes all of them
      // signal() would wake only one thread (arbitrary choice)
      condition.signalAll();

      // Note: t1 won't resume until we exit this block and release lock
    } finally {
      lock.unlock();
    }

    // After lock is released, t1 can reacquire it and continue from await()
  }
}
