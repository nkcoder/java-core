package org.nkcoder.j8.concurrency.thread;

import java.util.concurrent.TimeUnit;

/**
 * wait() and notify() Example
 *
 * wait() and notify() are fundamental methods for thread coordination in Java.
 * They allow threads to communicate by waiting for and signaling conditions.
 *
 * Key characteristics:
 * 1. Must be called within synchronized block/method
 * 2. Operate on the object's monitor (intrinsic lock)
 * 3. wait() releases the lock and blocks the thread
 * 4. notify() wakes up one waiting thread (random selection)
 * 5. notifyAll() wakes up all waiting threads
 *
 * How wait() works:
 * - Thread must hold the object's lock
 * - Releases the lock and enters waiting state
 * - Removes thread from runnable state (frees CPU)
 * - Reacquires lock before returning from wait()
 *
 * How notify() works:
 * - Thread must hold the object's lock
 * - Wakes up one waiting thread (scheduler chooses which one)
 * - Notified thread doesn't run immediately - waits for lock
 * - Lock is released when synchronized block exits
 *
 * Important rules:
 * 1. Always call wait()/notify() inside synchronized block on the same object
 * 2. Always use wait() in a loop (check condition before and after)
 *    while (!condition) { obj.wait(); }
 * 3. Prefer notifyAll() over notify() to avoid missed signals
 * 4. wait() can have spurious wakeups (wakes without notify)
 *
 * Common patterns:
 * Producer-Consumer: Producer notifies when item added, Consumer waits for items
 * Condition variables: Wait for a condition to become true
 * Resource allocation: Wait for resource to become available
 *
 * Modern alternatives:
 * - Condition objects with ReentrantLock (more flexible)
 * - CountDownLatch, CyclicBarrier, Semaphore (higher-level)
 * - BlockingQueue (for producer-consumer)
 *
 * This example demonstrates:
 * - Thread t1 waits on object's monitor
 * - Thread t2 notifies t1 to wake up
 * - Important: t1 doesn't resume immediately after notify()
 * - t1 must wait for t2 to exit synchronized block and release lock
 * - Output shows 2-second delay between notify and t1 resuming
 */
public class WaitNotifyExample {

  public static void main(String[] args) {
    // Shared object used for synchronization and wait/notify
    final Object object = new Object();

    Thread t1 = new Thread("t1") {
      @Override
      public void run() {
        synchronized (object) {
          System.out.println(
              Thread.currentThread().getName() + ", start at " + System.currentTimeMillis());
          try {
            // wait() MUST be called inside synchronized block
            // Releases the lock on 'object' and waits
            // Thread goes into waiting state (WAITING)
            object.wait();
          } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
          }
          // After notify() and t2 releases lock, t1 reacquires lock and continues here
          // Note the timestamp - will be ~2 seconds after t2's notify() call
          System.out
              .println(Thread.currentThread().getName() + ", end at " + System.currentTimeMillis());
        }
      }
    };

    Thread t2 = new Thread("t2") {
      @Override
      public void run() {
        synchronized (object) {
          System.out.println(
              Thread.currentThread().getName() + ", start at " + System.currentTimeMillis());

          // notify() MUST be called inside synchronized block
          // Wakes up ONE thread waiting on 'object' (t1 in this case)
          // But t1 won't run yet - must wait for this thread to release lock
          object.notify();

          try {
            // Sleep for 2 seconds WHILE HOLDING THE LOCK
            // t1 is notified but cannot proceed yet (waiting for lock)
            TimeUnit.SECONDS.sleep(2);
          } catch (InterruptedException ex) {
            System.out.println(ex.getStackTrace());
          }

          System.out
              .println(Thread.currentThread().getName() + ", end " + System.currentTimeMillis());
        }
        // Lock is released here when synchronized block exits
        // Now t1 can acquire the lock and continue from wait()
      }
    };

    t1.start();
    t2.start();

    // Expected output timing:
    // 1. t1 starts, calls wait(), releases lock
    // 2. t2 starts, calls notify(), but holds lock for 2 more seconds
    // 3. t2 sleeps 2 seconds (t1 is notified but waiting for lock)
    // 4. t2 exits, releases lock
    // 5. t1 acquires lock and continues (timestamp is ~2 seconds after t2's notify)
  }

}

