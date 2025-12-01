package org.nkcoder.concurrency.thread;

/**
 * Thread Priority: A hint to the scheduler, NOT a guarantee.
 *
 * <ul>
 *   <li>Range: MIN_PRIORITY (1) to MAX_PRIORITY (10), default NORM_PRIORITY (5)</li>
 *   <li>Platform-dependent: OS may ignore or map all priorities the same</li>
 * </ul>
 *
 * <p>Don't rely on priority for correctness - use synchronization instead.
 */
public class PriorityExample {

  public static void main(String[] args) throws InterruptedException {
    HighPriorityThread high = new HighPriorityThread();
    high.setPriority(Thread.MAX_PRIORITY);
    LowPriorityThread low = new LowPriorityThread();
    low.setPriority(Thread.MIN_PRIORITY);

    high.start();
    low.start();

    System.out.println("main exit.");
  }

  public static class HighPriorityThread extends Thread {
    private int count = 0;

    @Override
    public void run() {
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
      synchronized (PriorityExample.class) {
        for (int i = 0; i < 10000000; i++) {
          count++;
        }
        System.out.println("low is done, count: " + count);
      }
    }
  }
}
