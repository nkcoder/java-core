package org.nkcoder.j8.concurrency.thread;

/**
 * Daemon Thread Example
 *
 * A daemon thread is a background thread that does not prevent the JVM from exiting
 * when all user (non-daemon) threads have finished.
 *
 * Key characteristics:
 * 1. JVM exits when only daemon threads remain (doesn't wait for them to finish)
 * 2. Daemon threads are typically service threads (GC, finalizers, etc.)
 * 3. Must call setDaemon(true) BEFORE calling start()
 * 4. Child threads inherit daemon status from parent thread
 * 5. Daemon threads are abruptly terminated when JVM exits (no cleanup guaranteed)
 *
 * User thread vs Daemon thread:
 * - User thread: JVM waits for it to complete before exiting
 * - Daemon thread: JVM doesn't wait; terminates immediately when user threads end
 *
 * Common use cases:
 * - Background monitoring or logging
 * - Periodic cleanup tasks
 * - Service threads that support user threads
 *
 * Important warnings:
 * ✗ Don't use daemon threads for I/O operations (might not complete)
 * ✗ Don't use daemon threads for critical tasks requiring cleanup
 * ✓ Good for tasks that can be safely abandoned
 *
 * This example demonstrates:
 * - Daemon thread runs in infinite loop but doesn't prevent JVM exit
 * - Main thread sleeps 500ms, then exits
 * - Daemon thread is terminated abruptly (mid-execution) when main thread ends
 */
public class DaemonThreadExample extends Thread {

  public static void main(String[] args) {

    Thread daemonThread = new DaemonThreadExample();
    // Mark as daemon BEFORE starting
    // If this was false (or not set), JVM would wait for thread to complete
    daemonThread.setDaemon(true);
    daemonThread.start();

    try {
      // Main thread sleeps for 500ms
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Main thread exits here
    // Since daemonThread is a daemon, JVM will exit immediately
    // The daemon thread will be abruptly terminated (even mid-loop)
    // You'll see "I'm alive." printed 2-3 times, then program ends
  }

  @Override
  public void run() {
    // Infinite loop - normally would prevent JVM from exiting
    // But since this is a daemon thread, it won't
    while (true) {
      System.out.println("I'm alive.");
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
