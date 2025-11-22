package org.nkcoder.j8.concurrency.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport Example
 *
 * LockSupport is a low-level utility class for creating locks and synchronizers.
 * It provides park/unpark methods for thread blocking and unblocking.
 *
 * Key characteristics:
 * 1. park() - Blocks the current thread (similar to wait())
 * 2. unpark(thread) - Unblocks a specific thread (similar to notify())
 * 3. Works with permits - unpark() grants a permit, park() consumes it
 * 4. Permits are binary (0 or 1) - multiple unpark() calls don't stack
 * 5. unpark() can be called BEFORE park() - park() won't block if permit exists
 *
 * Advantages over wait()/notify():
 * - Doesn't require synchronization (no synchronized block needed)
 * - Can target specific threads (unpark(thread) vs notify() which is random)
 * - unpark() can be called before park() (permit-based)
 * - Less prone to spurious wakeups
 * - Doesn't throw InterruptedException (but can be interrupted)
 *
 * LockSupport vs wait()/notify():
 * wait()/notify():
 * - Requires synchronized block
 * - notify() wakes random thread, notifyAll() wakes all
 * - Must be called in correct order (notify before wait won't work)
 *
 * LockSupport:
 * - No synchronization required
 * - unpark() targets specific thread
 * - Order doesn't matter (permit-based)
 *
 * Common use cases:
 * - Building custom synchronizers (used internally by AbstractQueuedSynchronizer)
 * - Fine-grained thread coordination
 * - When you need to unpark before parking
 *
 * This example demonstrates:
 * - park() blocks threads inside synchronized blocks (still works!)
 * - unpark() wakes specific threads by reference
 * - Order independence: unpark() can be called before threads park
 */
public class LockSupportExample {

  private static final Object object = new Object();

  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
      System.out.println("before job done: " + Thread.currentThread().getName());
      synchronized (object) {
        // Park the current thread - blocks here until unpark() is called
        // Note: park() works even inside synchronized blocks (unlike wait())
        LockSupport.park();
      }
      System.out.println("job done: " + Thread.currentThread().getName());
    }, "t1");

    Thread t2 = new Thread(() -> {
      System.out.println("before job done: " + Thread.currentThread().getName());
      synchronized (object) {
        // Park the current thread
        LockSupport.park();
      }
      System.out.println("job done: " + Thread.currentThread().getName());
    }, "t2");

    t1.start();
    // Wait 3 seconds - t1 is parked during this time
    TimeUnit.SECONDS.sleep(3);

    t2.start();

    // Unpark both threads - grants them permits to continue
    // These calls can happen before or after park() - order doesn't matter
    // Each unpark() grants one permit to the specific thread
    LockSupport.unpark(t1);
    LockSupport.unpark(t2);

    // Wait for both threads to complete
    t1.join();
    t2.join();
  }
}
