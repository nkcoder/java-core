package org.nkcoder.concurrency.thread;

/**
 * Volatile Keyword Example
 *
 * The volatile keyword guarantees visibility of changes to variables across threads.
 * It's a lightweight synchronization mechanism for simple read/write scenarios.
 *
 * Key characteristics:
 * 1. Guarantees visibility - changes made by one thread are immediately visible to others
 * 2. Prevents instruction reordering around volatile variables
 * 3. Does NOT provide atomicity for compound operations (like i++)
 * 4. Cheaper than synchronization (no lock acquisition)
 * 5. Establishes a happens-before relationship
 *
 * How it works:
 * - Writes to volatile variables are immediately flushed to main memory
 * - Reads of volatile variables always read from main memory (not CPU cache)
 * - Prevents CPU caching issues where threads see stale values
 *
 * When to use volatile:
 * ✓ Simple flags (boolean done, status flags)
 * ✓ When only one thread writes, multiple threads read
 * ✓ When reads/writes don't depend on current value
 * ✗ NOT for compound operations (i++, count = count + 1)
 * ✗ NOT when operations depend on current value (check-then-act)
 *
 * volatile vs synchronized:
 * volatile:
 * - Visibility only, no atomicity
 * - No locking overhead
 * - Cannot use for compound operations
 * - Good for simple flags
 *
 * synchronized:
 * - Both visibility and atomicity
 * - Locking overhead (performance cost)
 * - Can use for compound operations
 * - Provides mutual exclusion
 *
 * The visibility problem this solves:
 * Without volatile, thread t1 might cache initDone=false in CPU cache
 * Even after t2 sets initDone=true, t1 might never see the change (infinite loop!)
 * With volatile, t1 always reads the latest value from main memory
 *
 * This example demonstrates:
 * - Thread t1 waits in a loop for initDone flag
 * - Thread t2 sets the flag to true
 * - volatile ensures t1 sees the change immediately
 * - Without volatile, t1 might loop forever (cached false value)
 */
public class VolatileExample {

  // volatile ensures visibility across threads
  // Without volatile, t1 might never see the change made by t2
  // t1 could cache initDone=false in its CPU cache and loop forever
  private volatile boolean initDone = false;

  public void initWorkDone() {
    // Write to volatile variable - immediately visible to all threads
    this.initDone = true;
    System.out.println("init done.");
  }

  /**
   * Waits for initialization to complete by spinning on volatile flag.
   * Without volatile, this could become an infinite loop due to CPU caching.
   */
  public void startToWork() {
    while (true) {
      // Read volatile variable - always gets latest value from main memory
      // This check will eventually see initDone=true set by another thread
      if (initDone) {
        System.out.println("init done, start to work");
        break;
      }
      // Without volatile, this thread might cache initDone=false
      // and never see the update, causing an infinite loop
    }
  }

  /**
   * Demonstrates volatile ensuring visibility between threads.
   * t1 waits for flag, t2 sets flag, t1 must see the change.
   */
  public static void main(String[] args) throws InterruptedException {
    VolatileExample volatileExample = new VolatileExample();

    // t1 waits for initDone to become true
    Thread t1 = new Thread(volatileExample::startToWork);

    // t2 sets initDone to true
    Thread t2 = new Thread(volatileExample::initWorkDone);

    t1.start();
    t2.start();
    t1.join();
    t2.join();

    // With volatile: t1 sees the change and exits normally
    // Without volatile: t1 might loop forever (visibility issue)
  }
}
