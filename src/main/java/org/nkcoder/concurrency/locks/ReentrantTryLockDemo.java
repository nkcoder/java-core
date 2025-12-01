package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code tryLock(timeout)}: Non-blocking lock with timeout.
 *
 * <ul>
 *   <li>{@code tryLock()} returns immediately (true/false)</li>
 *   <li>{@code tryLock(timeout, unit)} waits up to timeout</li>
 *   <li>Only unlock if tryLock() returned true</li>
 * </ul>
 */
public class ReentrantTryLockDemo implements Runnable {
  private static ReentrantLock lock = new ReentrantLock();

  @Override
  public void run() {
    try {
      if (lock.tryLock(3, TimeUnit.SECONDS)) {
        try {
          TimeUnit.SECONDS.sleep(5);
          System.out.println(Thread.currentThread().getId() + ": my job done.");
        } finally {
          lock.unlock();
        }
      } else {
        System.out.println(Thread.currentThread().getId() + ": get lock failed.");
      }
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  public static void main(String[] args) {
    ReentrantTryLockDemo lockDemo = new ReentrantTryLockDemo();
    Thread t1 = new Thread(lockDemo);
    Thread t2 = new Thread(lockDemo);
    t1.start();
    t2.start();
  }
}
