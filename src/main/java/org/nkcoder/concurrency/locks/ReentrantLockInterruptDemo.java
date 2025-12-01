package org.nkcoder.concurrency.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@code lockInterruptibly()}: Can be interrupted while waiting for lock.
 *
 * <ul>
 *   <li>{@code lock()} ignores interrupts; {@code lockInterruptibly()} throws InterruptedException</li>
 *   <li>Useful for deadlock recovery: interrupt one thread to break deadlock</li>
 * </ul>
 */
public class ReentrantLockInterruptDemo implements Runnable {

  private static ReentrantLock lock1 = new ReentrantLock();
  private static ReentrantLock lock2 = new ReentrantLock();
  private int value;

  public ReentrantLockInterruptDemo(int value) {
    this.value = value;
  }

  @Override
  public void run() {
    try {
      if (value == 1) {
        lock1.lockInterruptibly();
        try {
          TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        }
        lock2.lockInterruptibly();
        System.out.println(Thread.currentThread().getName() + ": my job done.");
      } else {
        lock2.lockInterruptibly();
        try {
          TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException exception) {
          exception.printStackTrace();
        }
        lock1.lockInterruptibly();
        System.out.println(Thread.currentThread().getName() + ": my job done.");
      }
    } catch (InterruptedException exception) {
      System.out.println(Thread.currentThread().getName() + ": interrupted while acquiring lock");
      exception.printStackTrace();
    } finally {
      if (lock1.isHeldByCurrentThread()) {
        lock1.unlock();
      } else if (lock2.isHeldByCurrentThread()) {
        lock2.unlock();
      }
      System.out.println(Thread.currentThread().getName() + ": I'm exit.");
    }
  }

  public static void main(String[] args) throws InterruptedException {
    ReentrantLockInterruptDemo lockDemo1 = new ReentrantLockInterruptDemo(1);
    ReentrantLockInterruptDemo lockDemo2 = new ReentrantLockInterruptDemo(2);

    Thread t1 = new Thread(lockDemo1, "t1");
    Thread t2 = new Thread(lockDemo2, "t2");

    t1.start();
    t2.start();

    TimeUnit.SECONDS.sleep(1);
    t2.interrupt();  // Break the deadlock
  }
}
