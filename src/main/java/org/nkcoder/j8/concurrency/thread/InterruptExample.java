package org.nkcoder.j8.concurrency.thread;

/**
 * Thread Interrupt Example
 *
 * Thread interruption is a cooperative mechanism to request a thread to stop what it's doing.
 * It's NOT a forceful thread termination - the interrupted thread must check and handle it.
 *
 * Key concepts:
 * 1. interrupt() - Sets the interrupt flag for a thread (doesn't stop the thread)
 * 2. isInterrupted() - Checks if thread's interrupt flag is set (doesn't clear flag)
 * 3. interrupted() - Static method that checks AND clears the interrupt flag
 *
 * Special behavior with blocking methods:
 * - If thread is blocked in sleep()/wait()/join(), these methods throw InterruptedException
 * - When InterruptedException is thrown, the interrupt flag is AUTOMATICALLY CLEARED
 * - Common pattern: catch InterruptedException, then call interrupt() again to restore flag
 *
 * Interrupt status management:
 * - Normal code: Check isInterrupted() periodically and exit gracefully
 * - Blocking calls: Catch InterruptedException, cleanup, and restore interrupt status
 *
 * Best practices:
 * ✓ Always check interrupt status in long-running loops
 * ✓ Restore interrupt status after catching InterruptedException: Thread.currentThread().interrupt()
 * ✓ Cleanup resources before exiting when interrupted
 * ✗ Don't swallow InterruptedException without restoring interrupt status
 * ✗ Don't use Thread.stop() - it's deprecated and dangerous
 *
 * This example demonstrates:
 * 1. Thread checks interrupt status in loop (isInterrupted())
 * 2. InterruptedException during sleep() clears the interrupt flag
 * 3. Re-interrupting after catching exception to maintain interrupt status
 * 4. Proper cleanup and exit when interrupted
 */
public class InterruptExample {
  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(new MyRunnable());
    t1.start();
    // Main thread waits 3 seconds
    Thread.sleep(3000);
    // Request t1 to interrupt - this sets t1's interrupt flag
    // t1 must cooperatively check and handle this interruption
    t1.interrupt();
  }

  public static class MyRunnable implements Runnable {

    @Override
    public void run() {
      while (true) {
        // Check interrupt status periodically
        // when current thread is interrupted, stop
        if (Thread.currentThread().isInterrupted()) {
          System.out.println("I'm interrupted, exit.");
          break;
        }

        try {
          Thread.sleep(2000);
        } catch (InterruptedException ex) {
          // IMPORTANT: When sleep()/wait()/join() throws InterruptedException,
          // the interrupt status is AUTOMATICALLY CLEARED
          System.out.println("I'm interrupted when sleeping.");

          // Restore interrupt status so the loop check can detect it
          // This is the standard pattern for handling InterruptedException
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
