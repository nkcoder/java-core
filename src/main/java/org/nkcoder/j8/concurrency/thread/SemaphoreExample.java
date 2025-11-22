package org.nkcoder.j8.concurrency.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreExample implements Runnable {
  private static final Semaphore SEMAPHORE = new Semaphore(5);

  @Override
  public void run() {
    try {
      SEMAPHORE.acquire();
      TimeUnit.SECONDS.sleep(3);
      System.out.println(Thread.currentThread().getId() + ": I'm done.");
      // System.out.println("-------------------------------------------");
    } catch (InterruptedException exception) {
      exception.printStackTrace();
    } finally {
      SEMAPHORE.release();
    }
  }

  /**
   * main.
   * 
   */
  public static void main(String[] args) {
    SemaphoreExample semaphoreExample = new SemaphoreExample();
    ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(20);

    for (int i = 0; i < 20; i++) {
      newFixedThreadPool.submit(semaphoreExample);
    }
  }


}
