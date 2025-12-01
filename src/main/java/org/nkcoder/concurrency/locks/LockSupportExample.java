package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport: Low-level park/unpark for thread blocking.
 *
 * <ul>
 *   <li>{@code park()} blocks current thread; {@code unpark(thread)} wakes specific thread</li>
 *   <li>Permit-based: {@code unpark()} can be called BEFORE {@code park()}</li>
 *   <li>No synchronization required (unlike wait/notify)</li>
 * </ul>
 */
public class LockSupportExample {

  private static final Object object = new Object();

  public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
      System.out.println("before job done: " + Thread.currentThread().getName());
      synchronized (object) {
        LockSupport.park();
      }
      System.out.println("job done: " + Thread.currentThread().getName());
    }, "t1");

    Thread t2 = new Thread(() -> {
      System.out.println("before job done: " + Thread.currentThread().getName());
      synchronized (object) {
        LockSupport.park();
      }
      System.out.println("job done: " + Thread.currentThread().getName());
    }, "t2");

    t1.start();
    TimeUnit.SECONDS.sleep(3);
    t2.start();

    LockSupport.unpark(t1);
    LockSupport.unpark(t2);

    t1.join();
    t2.join();
  }
}
