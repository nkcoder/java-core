package org.nkcoder.concurrency.synchronization;

/**
 * {@code synchronized}: Mutual exclusion - one thread at a time.
 *
 * <ul>
 *   <li>Provides atomicity and visibility</li>
 *   <li>Instance method locks on {@code this}, static method locks on Class</li>
 *   <li>Reentrant: same thread can acquire same lock multiple times</li>
 * </ul>
 */
public class SynchronizedExample {
  public static void main(String[] args) throws InterruptedException {
    CalculateRunnable calculateThread = new CalculateRunnable();

    Thread t1 = new Thread(calculateThread);
    Thread t2 = new Thread(calculateThread);

    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }

  public static class CalculateRunnable implements Runnable {
    private int base = 0;

    public synchronized void increase() {
      base++;
    }

    @Override
    public void run() {
      for (int i = 0; i < 100000; i++) {
        increase();
      }
      System.out.println("base: " + base + ", thread: " + Thread.currentThread().getName());
    }
  }
}
