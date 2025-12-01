package org.nkcoder.concurrency.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock: Same thread can acquire the lock multiple times.
 *
 * <ul>
 *   <li>Each {@code lock()} increments hold count, each {@code unlock()} decrements</li>
 *   <li>Lock released when hold count reaches 0</li>
 *   <li>Always unlock in finally block</li>
 * </ul>
 */
public class ReentrantLockDemo implements Runnable {

  private static final ReentrantLock LOCK = new ReentrantLock();
  private static int i = 0;

  @Override
  public void run() {
    for (int j = 0; j < 10000000; j++) {
      LOCK.lock();
      LOCK.lock();  // Reentrant: same thread can acquire again
      try {
        i++;
      } finally {
        LOCK.unlock();
        LOCK.unlock();  // Must unlock twice
      }
    }
    System.out.println("i: " + i);
  }

  public static void main(String[] args) throws InterruptedException {
    ReentrantLockDemo lockDemo = new ReentrantLockDemo();
    Thread t1 = new Thread(lockDemo);
    Thread t2 = new Thread(lockDemo);
    t1.start();
    t2.start();
    t1.join();
    t2.join();
  }
}
