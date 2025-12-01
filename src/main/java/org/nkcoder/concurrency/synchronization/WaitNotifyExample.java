package org.nkcoder.concurrency.synchronization;

import java.util.concurrent.TimeUnit;

/**
 * {@code wait()}/{@code notify()}: Thread coordination on object's monitor.
 *
 * <ul>
 *   <li>Must be called inside synchronized block on same object</li>
 *   <li>{@code wait()} releases lock and blocks; {@code notify()} wakes one waiting thread</li>
 *   <li>Always use in a loop: {@code while (!condition) { obj.wait(); }}</li>
 *   <li>Notified thread runs only after notifier releases lock</li>
 * </ul>
 */
public class WaitNotifyExample {

  public static void main(String[] args) {
    final Object object = new Object();

    Thread t1 = new Thread("t1") {
      @Override
      public void run() {
        synchronized (object) {
          System.out.println(Thread.currentThread().getName() + ", start at " + System.currentTimeMillis());
          try {
            object.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println(Thread.currentThread().getName() + ", end at " + System.currentTimeMillis());
        }
      }
    };

    Thread t2 = new Thread("t2") {
      @Override
      public void run() {
        synchronized (object) {
          System.out.println(Thread.currentThread().getName() + ", start at " + System.currentTimeMillis());
          object.notify();
          try {
            TimeUnit.SECONDS.sleep(2);
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
          System.out.println(Thread.currentThread().getName() + ", end " + System.currentTimeMillis());
        }
      }
    };

    t1.start();
    t2.start();
  }
}
