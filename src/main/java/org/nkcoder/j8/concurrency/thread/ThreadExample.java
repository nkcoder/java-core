package org.nkcoder.j8.concurrency.thread;

/**
 * Basic Thread Creation Example
 *
 * Demonstrates the fundamental ways to create and start threads in Java.
 *
 * Two main approaches:
 * 1. Extend Thread class and override run()
 * 2. Implement Runnable interface and pass to Thread constructor
 *
 * Thread lifecycle states:
 * - NEW: Thread created but not started
 * - RUNNABLE: Executing or ready to execute
 * - BLOCKED: Waiting for monitor lock
 * - WAITING: Waiting indefinitely for another thread
 * - TIMED_WAITING: Waiting for specified time
 * - TERMINATED: Execution completed
 *
 * Key methods:
 * - start(): Begins thread execution (calls run() in new thread)
 * - run(): Contains the code to execute (don't call directly!)
 * - join(): Wait for thread to complete
 * - interrupt(): Request thread interruption
 *
 * Modern alternatives (Java 21):
 * ✓ Virtual threads: Thread.ofVirtual().start(task)
 * ✓ ExecutorService: Better thread pool management
 * ✓ CompletableFuture: Async programming
 * ✓ Structured Concurrency: Parent-child task relationships
 *
 * This example shows the simplest thread creation pattern.
 * For production code, prefer ExecutorService or virtual threads.
 */
public class ThreadExample {
  public static void main(String[] args) throws InterruptedException {
    // Method 1: Anonymous Thread subclass (shown here)
    Thread t1 = new Thread() {
      @Override
      public void run() {
        // This code runs in a new thread
        System.out.println("Running in a new thread: " + Thread.currentThread().getName());
      }
    };

    // Start the thread - JVM calls run() in a new thread
    // NEVER call run() directly - it would execute in current thread!
    t1.start();

    // Method 2: Using Runnable (modern preferred approach)
    Thread t2 = new Thread(() -> {
      System.out.println("Running in another thread: " + Thread.currentThread().getName());
    });
    t2.start();

    // Java 21: Virtual threads (lightweight, millions possible)
    // Uncomment for Java 21+:
    // Thread.ofVirtual().start(() ->
    //     System.out.println("Running in virtual thread: " + Thread.currentThread())
    // );

    // Wait for threads to complete (optional)
    t1.join();
    t2.join();

    System.out.println("All threads completed");
  }
}
