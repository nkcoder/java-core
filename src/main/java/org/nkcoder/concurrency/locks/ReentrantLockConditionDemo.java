package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Condition: The ReentrantLock equivalent of {@code wait()}/{@code notify()}.
 *
 * <ul>
 *   <li>{@code lock.newCondition()} creates a Condition</li>
 *   <li>{@code await()} releases lock and waits; {@code signal()}/{@code signalAll()} wakes threads</li>
 *   <li>Multiple Conditions per lock (unlike wait/notify with one wait set)</li>
 * </ul>
 */
public class ReentrantLockConditionDemo implements Runnable {

  private static ReentrantLock lock = new ReentrantLock();
  private static Condition condition = lock.newCondition();

  @Override
  public void run() {
    try {
      lock.lock();
      condition.await();
      System.out.println(Thread.currentThread().getId() + ": I'm done.");
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    ReentrantLockConditionDemo conditionDemo = new ReentrantLockConditionDemo();
    Thread t1 = new Thread(conditionDemo);
    t1.start();

    TimeUnit.SECONDS.sleep(3);

    try {
      lock.lock();
      condition.signalAll();
    } finally {
      lock.unlock();
    }
  }
}
