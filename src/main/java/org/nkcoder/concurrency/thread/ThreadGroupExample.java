package org.nkcoder.concurrency.thread;

/**
 * ThreadGroup: Organize threads into hierarchical groups for bulk operations.
 *
 * <ul>
 *   <li>{@code activeCount()}, {@code enumerate()}, {@code list()}, {@code interrupt()}</li>
 *   <li>Useful for debugging and bulk operations</li>
 * </ul>
 *
 * <p>Consider ExecutorService for modern task management.
 */
public class ThreadGroupExample implements Runnable {
  private static volatile boolean done = false;

  public static void main(String[] args) throws InterruptedException {
    ThreadGroup threadGroup = new ThreadGroup("worker-group");

    Thread t1 = new Thread(threadGroup, new ThreadGroupExample(), "t1");
    Thread t2 = new Thread(threadGroup, new ThreadGroupExample(), "t2");

    t1.start();
    t2.start();

    threadGroup.list();  // Debug: print group structure
    System.out.println("activeCount: " + threadGroup.activeCount());

    Thread.sleep(200);
    done = true;
  }

  @Override
  public void run() {
    while (!done) {
      String name = Thread.currentThread().getThreadGroup().getName() + ": "
          + Thread.currentThread().getName();
      System.out.println("I'm: " + name);
    }
  }
}
