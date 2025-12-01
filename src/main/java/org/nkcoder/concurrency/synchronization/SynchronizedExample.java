package org.nkcoder.concurrency.synchronization;

/**
 * Synchronized Keyword Example
 *
 * The synchronized keyword provides mutual exclusion - ensures only one thread
 * can execute a synchronized block/method at a time for a given object.
 *
 * Key characteristics:
 * 1. Provides atomicity - operations appear indivisible
 * 2. Provides visibility - changes made by one thread are visible to others
 * 3. Prevents race conditions on shared data
 * 4. Uses monitor locks (intrinsic locks) on objects
 * 5. Reentrant - same thread can acquire the same lock multiple times
 *
 * Types of synchronization:
 * 1. Synchronized instance method: locks on 'this' object
 *    - synchronized void method() { }
 * 2. Synchronized static method: locks on Class object
 *    - static synchronized void method() { }
 * 3. Synchronized block on object: locks on specified object
 *    - synchronized(object) { }
 * 4. Synchronized block on class: locks on Class object
 *    - synchronized(MyClass.class) { }
 *
 * How it works:
 * - Every object has an intrinsic lock (monitor)
 * - synchronized acquires the lock before entering, releases after exiting
 * - Other threads trying to acquire same lock must wait
 * - Automatically releases lock even if exception occurs
 *
 * Performance considerations:
 * ✗ Synchronized has overhead - use only when needed
 * ✗ Can cause contention if lock is held too long
 * ✓ Fine-grained locking is better than coarse-grained
 * ✓ Consider alternatives: ReentrantLock, atomic classes, concurrent collections
 *
 * This example demonstrates:
 * - Two threads sharing the same Runnable instance
 * - synchronized method prevents race condition on 'base' variable
 * - Without synchronized, final value would be < 200000 (lost updates)
 * - With synchronized, final value is always exactly 200000
 */
public class SynchronizedExample {
  public static void main(String[] args) throws InterruptedException {

    CalculateRunnable calculateThread = new CalculateRunnable();

    // IMPORTANT: Both threads share the SAME Runnable instance
    // This means they share the same 'base' field
    // Without synchronization, this would cause a race condition
    Thread t1 = new Thread(calculateThread);
    Thread t2 = new Thread(calculateThread);

    t1.start();
    t2.start();
    // Wait for both threads to complete
    t1.join();
    t2.join();

    // Expected output: base should be exactly 200000 (100000 * 2 threads)
  }

  public static class CalculateRunnable implements Runnable {
    private int base = 0;

    // synchronized method - locks on 'this' object
    // Only one thread can execute this method at a time for this instance
    // This prevents race condition: read-modify-write of 'base' is atomic
    public synchronized void increase() {
      base++; // This operation is NOT atomic without synchronization!
      // base++ is actually three operations:
      // 1. Read current value of base
      // 2. Add 1 to the value
      // 3. Write new value back to base
      // Without synchronized, these steps can interleave causing lost updates
    }

    @Override
    public void run() {

      // Each thread increments base 100,000 times
      for (int i = 0; i < 100000; i++) {
        increase(); // synchronized ensures thread-safe increment
      }

      // With synchronization: always prints 200000 (or less if other thread still running)
      // Without synchronization: would print less than 200000 due to lost updates
      System.out.println("base: " + base + ", thread: " + Thread.currentThread().getName());
    }
  }
}
