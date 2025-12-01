package org.nkcoder.concurrency.thread;

/**
 * Thread Creation: Two approaches.
 *
 * <ul>
 *   <li>Extend Thread class and override run()</li>
 *   <li>Implement Runnable interface (preferred)</li>
 * </ul>
 *
 * <p>Key: Call {@code start()} not {@code run()} - run() executes in current thread!
 *
 * <p>Java 21+: Prefer virtual threads or ExecutorService for production.
 */
public class ThreadExample {
  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread() {
      @Override
      public void run() {
        System.out.println("Running in thread: " + Thread.currentThread().getName());
      }
    };
    t1.start();

    Thread t2 = new Thread(() -> {
      System.out.println("Running in thread: " + Thread.currentThread().getName());
    });
    t2.start();

    // Java 21+: Thread.ofVirtual().start(() -> ...)

    t1.join();
    t2.join();
    System.out.println("All threads completed");
  }
}
