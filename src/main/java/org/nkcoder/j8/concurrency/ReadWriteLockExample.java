package org.nkcoder.j8.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockExample {

  private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private final Lock readLock = readWriteLock.readLock();
  private final Lock writeLock = readWriteLock.writeLock();
  private int value = 0;

  /**
   * read.
   * 
   * @return value
   */
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

  /**
   * write.
   * 
   * @param newValue newValue
   */
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

  /**
   * main.
   * 
   * @param args args
   * @throws InterruptedException exception
   */
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
