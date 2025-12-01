package org.nkcoder.concurrency.thread;

/**
 * {@code join()}: Wait for another thread to complete before continuing.
 *
 * <ul>
 *   <li>{@code join()} blocks until target thread dies</li>
 *   <li>{@code join(millis)} waits with timeout</li>
 *   <li>{@code yield()} hints scheduler to let other threads run (rarely needed)</li>
 * </ul>
 */
public class JoinYieldExample {
  private volatile static int total = 0;

  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread() {
      @Override
      public void run() {
        for (int i = 0; i < 100; i++) {
          total += i;
        }
      }
    };

    t1.start();
    t1.join();
    System.out.println("total: " + total);
  }
}
