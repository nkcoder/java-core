package org.nkcoder.concurrency;

/**
 * Thread Priority Example
 *
 * Thread priority is a hint to the thread scheduler about which threads should get
 * preferential treatment, but it does NOT guarantee execution order.
 *
 * Key characteristics:
 * 1. Priority range: MIN_PRIORITY (1) to MAX_PRIORITY (10), default is NORM_PRIORITY (5)
 * 2. Priority is only a HINT - the scheduler may ignore it
 * 3. Platform-dependent behavior - different results on different operating systems
 * 4. NOT reliable for controlling execution order or timing
 * 5. Higher priority threads get more CPU time, but no guarantees
 *
 * Important notes:
 * - Priority inheritance: Child thread inherits parent's priority by default
 * - OS scheduling: Some OSes map all Java priorities to the same system priority
 * - Modern JVMs: Priority effects are less predictable with modern schedulers
 *
 * Best practices:
 * ✗ Don't rely on priority for correctness (use synchronization instead)
 * ✓ Use priority only for optimization hints (e.g., UI thread vs background tasks)
 * ✓ Always test on target platform - behavior varies significantly
 *
 * This example shows:
 * - How to set thread priorities
 * - That priority doesn't guarantee execution order (both threads compete for lock)
 * - Why you shouldn't depend on priorities for program correctness
 */
public class PriorityExample {

  public static void main(String[] args) throws InterruptedException {

    HighPriorityThread high = new HighPriorityThread();
    high.setPriority(Thread.MAX_PRIORITY); // Priority 10
    LowPriorityThread low = new LowPriorityThread();
    low.setPriority(Thread.MIN_PRIORITY);  // Priority 1

    high.start();
    low.start();

    // Uncomment to wait for threads to complete:
    // high.join();
    // low.join();

    // Main thread exits immediately - daemon threads would terminate here
    System.out.println("main exit.");

  }

  public static class HighPriorityThread extends Thread {

    private int count = 0;

    @Override
    public void run() {
      // Both threads synchronize on the same lock (PriorityExample.class)
      // Only one thread can hold this lock at a time
      // Priority might affect which thread acquires the lock first, but no guarantee
      synchronized (PriorityExample.class) {
        for (int i = 0; i < 10000000; i++) {
          count++;
        }
        System.out.println("high is done, count: " + count);
      }
    }
  }

  public static class LowPriorityThread extends Thread {

    private int count = 0;

    @Override
    public void run() {
      // Competes for the same lock as HighPriorityThread
      // Even with MIN_PRIORITY, it might run first (no guarantees!)
      synchronized (PriorityExample.class) {
        for (int i = 0; i < 10000000; i++) {
          count++;
        }
        System.out.println("low is done, count: " + count);
      }
    }
  }
}
