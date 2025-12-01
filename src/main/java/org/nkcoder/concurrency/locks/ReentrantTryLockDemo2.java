package org.nkcoder.concurrency.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Deadlock Avoidance: Use {@code tryLock()} to back off instead of waiting.
 *
 * <ul>
 *   <li>If can't acquire all locks, release and retry</li>
 *   <li>Breaks "hold and wait" condition required for deadlock</li>
 *   <li>Different sleep times reduce live lock risk</li>
 * </ul>
 */
public class ReentrantTryLockDemo2 {

  private static ReentrantLock lockOne = new ReentrantLock();
  private static ReentrantLock lockTwo = new ReentrantLock();

  public static void main(String[] args) {
    Thread t1 = new Thread(new TryLockRunnable(0), "t1");
    Thread t2 = new Thread(new TryLockRunnable(1), "t2");
    t1.start();
    t2.start();
  }

  static class TryLockRunnable implements Runnable {
    private int order;

    public TryLockRunnable(int order) {
      this.order = order;
    }

    @Override
    public void run() {
      if (order == 0) {
        while (true) {
          if (lockOne.tryLock()) {
            try {
              try {
                Thread.sleep(50);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              if (lockTwo.tryLock()) {
                try {
                  System.out.println("My job is done, name: " + Thread.currentThread().getName());
                  return;
                } finally {
                  lockTwo.unlock();
                }
              }
            } finally {
              lockOne.unlock();
            }
          }
        }
      } else {
        while (true) {
          if (lockTwo.tryLock()) {
            try {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              if (lockOne.tryLock()) {
                try {
                  System.out.println("My job is done, name: " + Thread.currentThread().getName());
                  return;
                } finally {
                  lockOne.unlock();
                }
              }
            } finally {
              lockTwo.unlock();
            }
          }
        }
      }
    }
  }
}
