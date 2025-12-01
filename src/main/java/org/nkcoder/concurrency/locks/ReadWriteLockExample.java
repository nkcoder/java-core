package org.nkcoder.concurrency.locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReadWriteLock: Multiple readers OR one writer at a time.
 *
 * <ul>
 *   <li>Read lock is shared; write lock is exclusive</li>
 *   <li>Good for read-heavy scenarios (caches, configuration)</li>
 * </ul>
 */
public class ReadWriteLockExample {

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private int value = 0;

  public int readValue() {
    try {
      readLock.lock();
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      System.out.println("read value: " + value);
      return value;
    } finally {
      readLock.unlock();
    }
  }

  public void setValue(int newValue) {
    try {
      writeLock.lock();
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException exception) {
        exception.printStackTrace();
      }
      System.out.println("set value to: " + newValue);
      this.value = newValue;
    } finally {
      writeLock.unlock();
    }
  }

  public static void main(String[] args) throws InterruptedException {
    ReadWriteLockExample readWriteLockExample = new ReadWriteLockExample();
    Runnable readRunnable = readWriteLockExample::readValue;
    Runnable writeRunnable = () -> readWriteLockExample.setValue(10);

    ExecutorService executorService = Executors.newFixedThreadPool(20);

    for (int i = 0; i < 18; i++) {
      executorService.submit(readRunnable);
    }
    for (int i = 0; i < 2; i++) {
      executorService.submit(writeRunnable);
    }

    executorService.shutdown();
    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
      executorService.shutdownNow();
    }
  }
}
