package org.nkcoder.concurrency.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code lockInterruptibly()}: Acquires lock but can be interrupted while waiting.
 *
 * <ul>
 *   <li>Unlike {@code lock()} which ignores interrupts</li>
 *   <li>Useful for breaking deadlock by interrupting one thread</li>
 * </ul>
 */
public class LockInterruptiblyExample {

  private static final ReentrantLock lockOne = new ReentrantLock();
  private static final ReentrantLock lockTwo = new ReentrantLock();

  public static void main(String[] args) throws InterruptedException {
    LockThread threadOne = new LockThread(0, "t1");
    LockThread threadTwo = new LockThread(1, "t2");

    threadOne.start();
    threadTwo.start();

    Thread.sleep(500);
    threadTwo.interrupt();  // Break the deadlock
  }

  static class LockThread extends Thread {
    private int order;
    private String name;

    public LockThread(int order, String name) {
      this.order = order;
      this.name = name;
    }

    @Override
    public void run() {
      try {
        if (order == 0) {
          lockOne.lockInterruptibly();
          Thread.sleep(300);
          lockTwo.lockInterruptibly();
        } else {
          lockTwo.lockInterruptibly();
          Thread.sleep(300);
          lockOne.lockInterruptibly();
        }
      } catch (InterruptedException e) {
        System.out.println("Thread " + name + " was interrupted");
        e.printStackTrace();
      } finally {
        if (lockOne.isHeldByCurrentThread()) {
          lockOne.unlock();
        }
        if (lockTwo.isHeldByCurrentThread()) {
          lockTwo.unlock();
        }
      }
      System.out.println("Thread is exited: " + name);
    }
  }
}
